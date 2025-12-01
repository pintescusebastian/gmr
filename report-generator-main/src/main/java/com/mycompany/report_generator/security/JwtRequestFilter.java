package com.mycompany.report_generator.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    private final DoctorDetailsService doctorDetailsService;
    private final JwtUtil jwtUtil;

    public JwtRequestFilter(
        DoctorDetailsService doctorDetailsService,
        JwtUtil jwtUtil
    ) {
        this.doctorDetailsService = doctorDetailsService;
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain chain
    ) throws ServletException, IOException {
        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;

        if (
            authorizationHeader != null &&
            authorizationHeader.startsWith("Bearer ")
        ) {
            jwt = authorizationHeader.substring(7);
            try {
                username = jwtUtil.extractUsername(jwt);
            } catch (Exception e) {
                // Eșec la parsare sau token expirat
                System.err.println(
                    "JWT Invalid sau expirat: " + e.getMessage()
                );
            }
        }

        if (
            username != null &&
            SecurityContextHolder.getContext().getAuthentication() == null
        ) {
            UserDetails userDetails =
                this.doctorDetailsService.loadUserByUsername(username);

            if (jwtUtil.validateToken(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                    new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                    );
                usernamePasswordAuthenticationToken.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
                );
                SecurityContextHolder.getContext().setAuthentication(
                    usernamePasswordAuthenticationToken
                );
            }
        }
        chain.doFilter(request, response);
    }
}
