package com.gpuflight.gpuflbackend.model.presentation;

import java.math.BigDecimal;
import java.time.Instant;

public record KernelEventDto(
        String id,
        Instant time,
        long startNs,
        long endNs,
        long durationNs,
        String sessionId,
        int deviceId,
        int pid,
        String app,
        String platform,
        String name,
        long corrId,
        String cudaError,
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
        String extraParams,
        Instant createdAt,
        Instant updatedAt
) {
}
