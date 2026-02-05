package com.echotruth.controller;

import com.echotruth.service.AiServiceClient;
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
@RequestMapping("/api/v1/health")
public class HealthController {
    
    private static final Logger logger = LoggerFactory.getLogger(HealthController.class);
    
    @Autowired
    private AiServiceClient aiServiceClient;
    
    @GetMapping
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        
        try {
            // Check AI service health
            boolean aiServiceHealthy = aiServiceClient.checkHealth();
            
            health.put("status", aiServiceHealthy ? "healthy" : "degraded");
            health.put("backend", "healthy");
            health.put("ai_service", aiServiceHealthy ? "healthy" : "unhealthy");
            health.put("timestamp", System.currentTimeMillis());
            
            logger.info("Health check completed - AI Service: {}", aiServiceHealthy ? "healthy" : "unhealthy");
            
            return ResponseEntity.ok(health);
            
        } catch (Exception e) {
            logger.error("Health check failed: {}", e.getMessage(), e);
            
            health.put("status", "unhealthy");
            health.put("backend", "healthy");
            health.put("ai_service", "error");
            health.put("error", e.getMessage());
            health.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.status(503).body(health);
        }
    }
}