package com.gpuflight.gpuflbackend.model.presentation;

import java.time.Instant;
import java.util.List;

public record SystemEventDto(
        String sessionId,
        Integer pid,
        String app,
        String name,
        String eventType,
        Long tsNs,
        Long rangeStart,
        Long rangeEnd,
        List<HostMetricsDto> hostMetrics,
        List<DeviceMetricsDto> deviceMetrics,
        Instant createdAt,
        Instant updatedAt
) {}
