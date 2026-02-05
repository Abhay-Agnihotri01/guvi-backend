package com.echotruth.controller;

import com.echotruth.model.User;
import com.echotruth.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/test")
public class TestController {
    
    private static final Logger logger = LoggerFactory.getLogger(TestController.class);
    
    @Autowired
    private AuthService authService;
    
    @GetMapping("/auth")
    public ResponseEntity<Map<String, Object>> testAuth() {
        try {
            User currentUser = authService.getCurrentUser();
            
            Map<String, Object> response = new HashMap<>();
            response.put("authenticated", true);
            response.put("username", currentUser.getUsername());
            response.put("email", currentUser.getEmail());
            response.put("timestamp", System.currentTimeMillis());
            
            logger.info("Auth test successful for user: {}", currentUser.getUsername());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Auth test failed: {}", e.getMessage());
            
            Map<String, Object> response = new HashMap<>();
            response.put("authenticated", false);
            response.put("error", e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.status(401).body(response);
        }
    }
    
    @GetMapping("/ping")
    public ResponseEntity<Map<String, Object>> ping() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "pong");
        response.put("timestamp", System.currentTimeMillis());
        response.put("service", "echotruth-backend");
        
        return ResponseEntity.ok(response);
    }
}