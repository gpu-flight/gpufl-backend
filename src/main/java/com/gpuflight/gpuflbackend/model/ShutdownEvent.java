package com.gpuflight.gpuflbackend.model;

public record ShutdownEvent(
        MetricType type,
        int pid,
        String app,
        String sessionId,
        long tsNs
) implements MetricEvent {}
