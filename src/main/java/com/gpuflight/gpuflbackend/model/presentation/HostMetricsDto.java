package com.gpuflight.gpuflbackend.model.presentation;

import java.time.Instant;

public record HostMetricsDto(
        String id,
        String sessionId,
        Instant time,
        String hostname,
        String ipAddr,
        long tsNs,
        double cpuPct,
        long ramUsedMib,
        long ramTotalMib,
        Instant createdAt,
        Instant updatedAt
) {
}
