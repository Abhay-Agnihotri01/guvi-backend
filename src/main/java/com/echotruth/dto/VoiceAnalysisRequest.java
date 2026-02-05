package com.echotruth.dto;

import lombok.Data;

@Data
public class VoiceAnalysisRequest {
    private String audio; // Base64 encoded audio
    private String language; // Optional language hint
}
