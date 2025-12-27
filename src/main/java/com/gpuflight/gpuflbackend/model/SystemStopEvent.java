package com.gpuflight.gpuflbackend.model;

import java.util.List;

public record SystemStopEvent(
        MetricType type,
        int pid,
        String app,
        String sessionId,
        String name,
        String tag,
        long tsNs,
        HostSample host,
        List<DeviceSample> devices
) implements MetricEvent {}
