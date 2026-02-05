package com.echotruth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetectionResponse {
    private String id;
    private String classification;
    private Double confidence;
    private String language;
    private ExplanationDto explanation;
    private Long processingTimeMs;
    private LocalDateTime createdAt;
    private String audioFileName;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExplanationDto {
        private List<String> reasoning;
        private Map<String, Double> modelScores;
        private Boolean pitchAnomaly;
        private Boolean spectralArtifacts;
        private String ensembleAgreement;
    }
}
