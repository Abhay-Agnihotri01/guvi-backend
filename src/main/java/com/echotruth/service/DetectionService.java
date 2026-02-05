package com.echotruth.service;

import com.echotruth.dto.DetectionResponse;
import com.echotruth.model.Detection;
import com.echotruth.model.User;
import com.echotruth.repository.DetectionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DetectionService {
    
    private static final Logger logger = LoggerFactory.getLogger(DetectionService.class);
    
    @Autowired
    private DetectionRepository detectionRepository;
    
    @Autowired
    private AiServiceClient aiServiceClient;
    
    @Autowired
    private AuthService authService;
    
    public DetectionResponse analyzeVoice(MultipartFile audioFile, String languageHint) {
        try {
            User currentUser = authService.getCurrentUser();
            logger.info("Starting voice analysis for user: {}, file: {}", 
                       currentUser.getUsername(), audioFile.getOriginalFilename());
            
            long startTime = System.currentTimeMillis();
            
            // Call AI service
            Map<String, Object> aiResponse = aiServiceClient.analyzeAudio(audioFile, languageHint);
            
            return processAiResponse(currentUser, aiResponse, startTime);
            
        } catch (Exception e) {
            logger.error("Failed to analyze voice: {}", e.getMessage(), e);
            throw new RuntimeException("Voice analysis failed: " + e.getMessage(), e);
        }
    }

    public DetectionResponse analyzeVoiceFromUrl(String audioUrl, String languageHint) {
        try {
            User currentUser = authService.getCurrentUser();
            logger.info("Starting voice analysis from URL for user: {}, url: {}", 
                       currentUser.getUsername(), audioUrl);
            
            long startTime = System.currentTimeMillis();
            
            // Handle Google Drive links
            String downloadUrl = audioUrl;
            if (audioUrl.contains("drive.google.com")) {
                String fileId = extractDriveFileId(audioUrl);
                if (fileId != null) {
                    downloadUrl = "https://drive.google.com/uc?id=" + fileId + "&export=download";
                    logger.info("Converted Drive URL to download URL: {}", downloadUrl);
                }
            }
            
            // Download file
            java.nio.file.Path tempFile = java.nio.file.Files.createTempFile("download_", ".tmp");
            try (java.io.InputStream in = new java.net.URL(downloadUrl).openStream()) {
                java.nio.file.Files.copy(in, tempFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
            
            // Call AI service with path
            Map<String, Object> aiResponse = aiServiceClient.analyzeAudio(tempFile, "downloaded_" + System.currentTimeMillis() + ".audio", languageHint);
            
            // Cleanup temp file
            try {
                java.nio.file.Files.deleteIfExists(tempFile);
            } catch (Exception e) {
                logger.warn("Failed to delete temp file: {}", tempFile, e);
            }
            
            return processAiResponse(currentUser, aiResponse, startTime);
            
        } catch (Exception e) {
            logger.error("Failed to analyze voice from URL: {}", e.getMessage(), e);
            throw new RuntimeException("Voice analysis failed: " + e.getMessage(), e);
        }
    }

    public DetectionResponse analyzeVoiceInternal(java.nio.file.Path audioPath, String originalFilename, String languageHint) {
        try {
            // Try to get current user, but don't fail if not present (API client case)
            User currentUser = null;
            try {
                currentUser = authService.getCurrentUser();
            } catch (Exception e) {
                logger.debug("No current user found (likely API client request)");
            }
            
            logger.info("Starting internal voice analysis for user: {}, file: {}", 
                       currentUser != null ? currentUser.getUsername() : "API_CLIENT", originalFilename);
            
            long startTime = System.currentTimeMillis();
            
            // Call AI service
            Map<String, Object> aiResponse = aiServiceClient.analyzeAudio(audioPath, originalFilename, languageHint);
            
            long processingTime = System.currentTimeMillis() - startTime;
            
            // Create detection record
            Detection detection = new Detection();
            detection.setUser(currentUser); // can be null
            detection.setAudioFileName((String) aiResponse.get("audio_file_name"));
            detection.setAudioFileUrl((String) aiResponse.get("audio_file_path"));
            detection.setClassification(
                "AI_GENERATED".equals(aiResponse.get("classification")) 
                    ? Detection.Classification.AI_GENERATED 
                    : Detection.Classification.HUMAN
            );
            detection.setConfidence(((Number) aiResponse.get("confidence")).doubleValue());
            detection.setLanguage((String) aiResponse.get("language"));
            detection.setExplanation((Map<String, Object>) aiResponse.get("explanation"));
            detection.setProcessingTimeMs(processingTime);
            
            if (currentUser != null) {
                detection = detectionRepository.save(detection);
            } else {
                // For API clients without user, maybe we don't save to DB or save with null user if DB allows?
                // If DB requires user, we skip saving or need a dummy user.
                // Assuming DB requires User (usually OneToMany).
                // Let's just return response without saving to history for now to avoid DB constraint issues.
                detection.setId("temp-" + java.util.UUID.randomUUID());
                detection.setCreatedAt(LocalDateTime.now());
            }
            
            logger.info("Internal voice analysis completed successfully - Classification: {}, Confidence: {}", 
                       detection.getClassification(), detection.getConfidence());
            
            return mapToResponse(detection);
            
        } catch (Exception e) {
            logger.error("Failed to analyze voice internally: {}", e.getMessage(), e);
            throw new RuntimeException("Voice analysis failed: " + e.getMessage(), e);
        }
    }

    private String extractDriveFileId(String url) {
        // Simple regex to extract ID from typical Drive URLs
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("/d/([a-zA-Z0-9_-]+)");
        java.util.regex.Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private DetectionResponse processAiResponse(User currentUser, Map<String, Object> aiResponse, long startTime) {
        long processingTime = System.currentTimeMillis() - startTime;
        
        // Create detection record
        Detection detection = new Detection();
        detection.setUser(currentUser);
        detection.setAudioFileName((String) aiResponse.get("audio_file_name"));
        detection.setAudioFileUrl((String) aiResponse.get("audio_file_path")); // Or the original URL if we wanted to store that
        detection.setClassification(
            "AI_GENERATED".equals(aiResponse.get("classification")) 
                ? Detection.Classification.AI_GENERATED 
                : Detection.Classification.HUMAN
        );
        detection.setConfidence(((Number) aiResponse.get("confidence")).doubleValue());
        detection.setLanguage((String) aiResponse.get("language"));
        detection.setExplanation((Map<String, Object>) aiResponse.get("explanation"));
        detection.setProcessingTimeMs(processingTime);
        
        detection = detectionRepository.save(detection);
        
        logger.info("Voice analysis completed successfully - ID: {}, Classification: {}, Confidence: {}", 
                   detection.getId(), detection.getClassification(), detection.getConfidence());
        
        return mapToResponse(detection);
    }
    
    public DetectionResponse getDetection(String id) {
        Detection detection = detectionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Detection not found"));
        
        // Verify user owns this detection
        User currentUser = authService.getCurrentUser();
        if (!detection.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Unauthorized access to detection");
        }
        
        return mapToResponse(detection);
    }
    
    public Page<DetectionResponse> getHistory(Pageable pageable) {
        User currentUser = authService.getCurrentUser();
        Page<Detection> detections = detectionRepository.findByUserOrderByCreatedAtDesc(currentUser, pageable);
        return detections.map(this::mapToResponse);
    }
    
    public void deleteDetection(String id) {
        Detection detection = detectionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Detection not found"));
        
        User currentUser = authService.getCurrentUser();
        if (!detection.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Unauthorized access to detection");
        }
        
        detectionRepository.delete(detection);
    }
    
    private DetectionResponse mapToResponse(Detection detection) {
        DetectionResponse response = new DetectionResponse();
        response.setId(detection.getId());
        response.setClassification(detection.getClassification().name());
        response.setConfidence(detection.getConfidence());
        response.setLanguage(detection.getLanguage());
        response.setProcessingTimeMs(detection.getProcessingTimeMs());
        response.setCreatedAt(detection.getCreatedAt());
        response.setAudioFileName(detection.getAudioFileName());
        
        // Map explanation
        Map<String, Object> explanation = detection.getExplanation();
        if (explanation != null) {
            DetectionResponse.ExplanationDto explanationDto = new DetectionResponse.ExplanationDto();
            explanationDto.setReasoning((List<String>) explanation.get("reasoning"));
            explanationDto.setModelScores((Map<String, Double>) explanation.get("model_scores"));
            explanationDto.setPitchAnomaly((Boolean) explanation.get("pitch_anomaly"));
            explanationDto.setSpectralArtifacts((Boolean) explanation.get("spectral_artifacts"));
            explanationDto.setEnsembleAgreement((String) explanation.get("ensemble_agreement"));
            response.setExplanation(explanationDto);
        }
        
        return response;
    }
}
