package com.echotruth.controller;

import com.echotruth.dto.DetectionResponse;
import com.echotruth.service.DetectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/history")
@Tag(name = "Detection History", description = "Voice detection history management")
public class HistoryController {
    
    @Autowired
    private DetectionService detectionService;
    
    @GetMapping
    @Operation(summary = "Get detection history")
    public ResponseEntity<Page<DetectionResponse>> getHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<DetectionResponse> history = detectionService.getHistory(PageRequest.of(page, size));
        return ResponseEntity.ok(history);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get specific detection from history")
    public ResponseEntity<DetectionResponse> getHistoryItem(@PathVariable String id) {
        return ResponseEntity.ok(detectionService.getDetection(id));
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete detection from history")
    public ResponseEntity<Void> deleteDetection(@PathVariable String id) {
        detectionService.deleteDetection(id);
        return ResponseEntity.noContent().build();
    }
}
