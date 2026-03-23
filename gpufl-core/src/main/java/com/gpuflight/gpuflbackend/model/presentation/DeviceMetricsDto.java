package com.gpuflight.gpuflbackend.model.presentation;

import java.time.Instant;

public record DeviceMetricsDto(
        String id,
        Integer deviceId,
        String sessionId,
        Instant time,
        Long tsNs,
        Integer gpuUtil,
        Integer memUtil,
        Integer tempC,
        Integer powerMw,
        Long usedMib,
        Instant createdAt,
        Instant updatedAt
) {
}
