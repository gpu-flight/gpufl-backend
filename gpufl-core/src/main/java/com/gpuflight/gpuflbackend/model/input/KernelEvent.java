package com.gpuflight.gpuflbackend.model.input;

import java.math.BigDecimal;

public record KernelEvent(
        int pid,
        String app,
        String sessionId,
        String name,
        String platform,
        int deviceId,
        long startNs,
        long endNs,
        long apiStartNs,
        long apiExitNs,
        long streamId,
        boolean hasDetails,
        String grid,
        String block,
        long dynSharedBytes,
        int numRegs,
        long staticSharedBytes,
        long localBytes,
        long constBytes,
        BigDecimal occupancy,
        BigDecimal regOccupancy,
        BigDecimal smemOccupancy,
        BigDecimal warpOccupancy,
        BigDecimal blockOccupancy,
        String limitingResource,
        long maxActiveBlocks,
        long corrId,
        String stackTrace,
        String userScope,
        int scopeDepth,
        // Phase 1a CUPTI fields
        long localMemTotalBytes,
        int cacheConfigRequested,
        int cacheConfigExecuted,
        long sharedMemExecutedBytes
) {}
