package com.gpuflight.gpuflbackend.model.input;

import com.gpuflight.gpuflbackend.model.MetricType;

public record KernelEndEvent(
        int pid,
        String app,
        String sessionId,
        String deviceId,
        String name,
        long tsNs,
        String cudaError,
        long corrId,
        String stackTrace,
        String userScope,
        int scopeDepth
){}
