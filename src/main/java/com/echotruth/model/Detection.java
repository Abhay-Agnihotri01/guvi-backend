package com.echotruth.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "detections")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Detection {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "audio_file_url")
    private String audioFileUrl;
    
    @Column(name = "audio_file_name")
    private String audioFileName;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Classification classification;
    
    @Column(nullable = false)
    private Double confidence;
    
    @Column(nullable = false)
    private String language;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "explanation", columnDefinition = "jsonb")
    private Map<String, Object> explanation;
    
    @Column(name = "processing_time_ms")
    private Long processingTimeMs;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    public enum Classification {
        AI_GENERATED,
        HUMAN
    }
}
