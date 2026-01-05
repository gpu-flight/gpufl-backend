package com.gpuflight.gpuflbackend.model.presentation;

import java.time.Instant;
import java.util.List;

public record InitEventDto(
        int pid,
        String app,
        String sessionId,
        String logPath,
        Instant time,
        long tsNs,
        int systemRateMs,
        List<HostMetricsDto> hostMetrics,
        List<CudaStaticDeviceDto> cudaStaticDevices,
        Instant createdAt,
        Instant updatedAt
) {}
