package com.gpuflight.gpuflbackend.model;

import java.util.List;

public record SystemSampleEvent(
        int pid,
        String app,
        String sessionId,
        String name,
        long tsNs,
        HostSample host,
        List<DeviceSample> devices
){}
