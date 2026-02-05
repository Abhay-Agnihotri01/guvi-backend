package com.echotruth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiKeyResponse {
    private String id;
    private String name;
    private String keyPrefix;
    private String fullKey; // Only included when creating/regenerating
    private Integer rateLimit;
    private Boolean isActive;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private LocalDateTime lastUsedAt;
}
