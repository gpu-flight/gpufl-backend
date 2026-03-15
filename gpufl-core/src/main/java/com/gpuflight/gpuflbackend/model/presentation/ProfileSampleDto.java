package com.gpuflight.gpuflbackend.model.presentation;

import java.time.Instant;

public record ProfileSampleDto(
        String id,
        String sessionId,
        String scopeName,
        String sampleKind,
        String functionName,
        String pcOffset,
        String sourceFile,
        Integer sourceLine,
        long instExecuted,
        long threadInstExecuted,
        Integer stallReason,
        String reasonName,
        long sampleCount,
        int occurrenceCount,
        Instant createdAt
) {}
