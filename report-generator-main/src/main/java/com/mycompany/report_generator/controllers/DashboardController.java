package com.mycompany.report_generator.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class DashboardController {

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard(Authentication authentication) {
        // Extragem username-ul (doctorCode) din token
        String doctorCode = authentication.getName();

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Welcome to dashboard!");
        response.put("doctorCode", doctorCode);
        response.put("status", "success");

        return ResponseEntity.ok(response);
    }
}