package com.gpuflight.gpuflbackend.model.presentation;

import java.time.Instant;
import java.util.List;

public record InitEventDto(
        String id,
        int pid,
        String app,
        String sessionId,
        long tsNs,
        int systemRateMs,
        List<HostMetricsDto> hostMetrics,
        List<CudaStaticDeviceDto> cudaStaticDevices,
        Instant createdAt,
        Instant updatedAt
) {}
