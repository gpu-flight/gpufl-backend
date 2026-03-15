package com.gpuflight.gpuflbackend.model;

import java.util.List;

public record InitEvent(
        int pid,
        String app,
        String sessionId,
        String logPath,
        long tsNs,
        int systemRateMs,
        HostSample host,
        List<DeviceSample> devices,
        List<CudaStaticDevice> cudaStaticDevices
){}
