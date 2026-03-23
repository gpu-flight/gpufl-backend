package com.gpuflight.gpuflbackend.service;

import com.gpuflight.gpuflbackend.model.SessionInsightsDto;

public interface InsightService {
    SessionInsightsDto getInsights(String sessionId);
}
