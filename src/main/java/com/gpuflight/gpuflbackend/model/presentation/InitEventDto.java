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
        Long shutdownTsNs,
        int systemRateMs,
        List<HostMetricsDto> hostMetrics,
        List<CudaStaticDeviceDto> cudaStaticDevices,
        List<ScopeEventDto> scopes,
        List<KernelEventDto> kernels,
        Instant createdAt,
        Instant updatedAt
) {}
