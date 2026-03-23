package com.gpuflight.gpuflbackend.model.presentation;

import java.time.Instant;

public record ProfileSampleDto(
        String id,
        String sessionId,
        String scopeName,
        Integer deviceId,
        String sampleKind,
        String functionName,
        Integer pcOffset,
        String metricName,
        long metricValue,
        Integer stallReason,
        int occurrenceCount,
        Instant createdAt
) {}
