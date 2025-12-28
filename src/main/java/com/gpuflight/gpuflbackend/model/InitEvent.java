package com.gpuflight.gpuflbackend.model;

import java.util.List;

public record InitEvent(
        MetricType type,
        int pid,
        String app,
        String sessionId,
        long tsNs,
        int systemRateMs,
        HostSample host,
        List<DeviceSample> devices,
        List<CudaStaticDevice> cudaStaticDevices
) implements MetricEvent {}
