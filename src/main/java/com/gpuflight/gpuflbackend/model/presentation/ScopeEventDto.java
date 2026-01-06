package com.gpuflight.gpuflbackend.model.presentation;

import java.time.Instant;

public record ScopeEventDto(
        String id,
        Instant time,
        Long tsNs,
        String sessionId,
        String name,
        String tag,
        String userScope,
        int scopeDepth,
        Instant createdAt,
        Instant updatedAt
) {}
