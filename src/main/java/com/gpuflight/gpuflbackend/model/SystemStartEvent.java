package com.gpuflight.gpuflbackend.model;

import java.util.List;

public record SystemStartEvent(
        MetricType type,
        int pid,
        String app,
        String name,
        String sessionId,
        long tsNs,
        HostSample host,
        List<DeviceSample> devices
) implements MetricEvent {}
