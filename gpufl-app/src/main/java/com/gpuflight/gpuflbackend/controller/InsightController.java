package com.gpuflight.gpuflbackend.controller;

import com.gpuflight.gpuflbackend.model.SessionInsightsDto;
import com.gpuflight.gpuflbackend.service.InsightService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/insights")
@RequiredArgsConstructor
public class InsightController {

    private final InsightService insightService;

    @GetMapping("/{sessionId}")
    public ResponseEntity<SessionInsightsDto> getInsights(@PathVariable String sessionId) {
        return ResponseEntity.ok(insightService.getInsights(sessionId));
    }
}
