package com.gpuflight.gpuflbackend.model.presentation;

import java.time.Instant;

public record HostMetricsDto(
        String id,
        String sessionId,
        Instant time,
        String hostname,
        String ipAddr,
        Long tsNs,
        Double cpuPct,
        Long ramUsedMib,
        Long ramTotalMib,
        Instant createdAt,
        Instant updatedAt
) {
}
