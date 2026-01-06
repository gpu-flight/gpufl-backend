package com.gpuflight.gpuflbackend.model.input;

import com.gpuflight.gpuflbackend.model.MetricType;

import java.math.BigDecimal;

public record KernelBeginEvent(
        int pid,
        String app,
        String sessionId,
        String name,
        String platform,
        int deviceId,
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
        long corrId,
        String cudaError,
        String stackTrace,
        String userScope,
        int scopeDepth
){}
