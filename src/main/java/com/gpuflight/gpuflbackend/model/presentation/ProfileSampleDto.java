package com.gpuflight.gpuflbackend.model.presentation;

import java.time.Instant;

public record ProfileSampleDto(
        String id,
        String sessionId,
        long tsNs,
        int deviceId,
        long corrId,
        String sampleKind,
        String metricName,
        Long metricValue,
        String pcOffset,
        String functionName,
        String sourceFile,
        Integer sourceLine,
        Integer sampleCount,
        Integer stallReason,
        String reasonName,
        Instant createdAt
) {}
