package com.mycompany.report_generator.controllers;

import com.mycompany.report_generator.dto.AuthResponseDTO;
import com.mycompany.report_generator.dto.LoginRequestDTO;
import com.mycompany.report_generator.security.DoctorDetailsService;
import com.mycompany.report_generator.security.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final DoctorDetailsService doctorDetailsService;
    private final JwtUtil jwtUtil;

    public AuthController(
            AuthenticationManager authenticationManager,
            DoctorDetailsService doctorDetailsService,
            JwtUtil jwtUtil
    ) {
        this.authenticationManager = authenticationManager;
        this.doctorDetailsService = doctorDetailsService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> createAuthenticationToken(
            @RequestBody LoginRequestDTO loginRequest,
            HttpServletResponse response // ← ADAUGĂ PARAMETRU
    ) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getDoctorCode(),
                            loginRequest.getPassword()
                    )
            );
        } catch (BadCredentialsException e) {
            return ResponseEntity.ok(
                    new AuthResponseDTO(
                            null,
                            "Autentificare eșuată: Cod doctor sau parolă incorectă."
                    )
            );
        }

        final UserDetails userDetails = doctorDetailsService.loadUserByUsername(
                loginRequest.getDoctorCode()
        );

        final String jwt = jwtUtil.generateToken(userDetails);

        // COOKIE
        Cookie jwtCookie = new Cookie("JWT_TOKEN", jwt);
        jwtCookie.setHttpOnly(true); // Protecție XSS
        jwtCookie.setSecure(false); // true pentru HTTPS în producție
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(10 * 60 * 60); // 10 ore
        response.addCookie(jwtCookie);

        String successMessage =
                "Autentificare reușită pentru doctorul: " +
                        loginRequest.getDoctorCode();
        return ResponseEntity.ok(new AuthResponseDTO(jwt, successMessage));
    }
}