package com.gpuflight.gpuflbackend.model;

public record KernelEndEvent(
        MetricType type,
        int pid,
        String app,
        String sessionId,
        String deviceId,
        String name,
        long tsNs,
        String cudaError,
        int corrId
) implements MetricEvent {}
