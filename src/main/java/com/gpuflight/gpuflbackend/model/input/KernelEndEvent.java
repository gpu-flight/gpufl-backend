package com.gpuflight.gpuflbackend.model.input;

import com.gpuflight.gpuflbackend.model.MetricEvent;
import com.gpuflight.gpuflbackend.model.MetricType;

public record KernelEndEvent(
        MetricType type,
        int pid,
        String app,
        String sessionId,
        String deviceId,
        String name,
        long tsNs,
        String cudaError,
        long corrId
) implements MetricEvent {}
