package com.mycompany.report_generator.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final DoctorDetailsService doctorDetailsService;
    private final JwtRequestFilter jwtRequestFilter;

    public SecurityConfig(
        DoctorDetailsService doctorDetailsService,
        JwtRequestFilter jwtRequestFilter
    ) {
        this.doctorDetailsService = doctorDetailsService;
        this.jwtRequestFilter = jwtRequestFilter;
    }

    // 1. Definim PasswordEncoder pentru a accepta {noop}
    @Bean
    public PasswordEncoder passwordEncoder() {
        // DelegatingPasswordEncoder acceptă {noop}, asigurând compatibilitatea cu DoctorDetailsService
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    // 2. AuthenticationManager este necesar pentru a procesa cererea de login
    @Bean
    public AuthenticationManager authenticationManager(
        AuthenticationConfiguration authenticationConfiguration
    ) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    // 3. Configurația pentru a lega DoctorDetailsService și PasswordEncoder
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider =
            new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(doctorDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    // 4. Configurația Filter Chain (reguli de autorizare)
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authz ->
                        authz
                                .requestMatchers("/api/auth/login").permitAll()
                                .requestMatchers(
                                        "/",
                                        "/index.html",
                                        "/login.html",
                                        "/css/**",
                                        "/js/**",
                                        "/images/**"
                                ).permitAll()
                                // ← Paginile protejate
                                .requestMatchers("/dashboard.html", "/new-report.html").authenticated()
                                .requestMatchers("/api/**").authenticated()
                                .anyRequest().authenticated()
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .addFilterBefore(
                        jwtRequestFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(java.util.Arrays.asList("*")); // Permite toate originile
        configuration.setAllowedMethods(java.util.Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(java.util.Arrays.asList("*"));
        configuration.setAllowCredentials(false); // Important: false când folosești "*" la origins

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
