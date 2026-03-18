package com.gpuflight.gpuflbackend.model;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class SessionInsightsDto {
    String sessionId;
    List<InsightDto> insights;
}
