package com.gpuflight.gpuflbackend.model;

import java.math.BigDecimal;

public record KernelBeginEvent(
        MetricType type,
        int pid,
        String app,
        String sessionId,
        String name,
        String platform,
        String deviceId,
        long tsNs,
        long durationNs,
        boolean hasDetails,

        String grid,
        String block,
        long dynSharedBytes,
        int numRegs,
        long staticSharedBytes,
        long localBytes,
        long constBytes,
        BigDecimal occupancy,
        long maxActiveBlocks,
        int corrId,
        String cudaError
) implements MetricEvent {}
