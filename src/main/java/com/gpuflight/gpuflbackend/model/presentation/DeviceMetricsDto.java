package com.gpuflight.gpuflbackend.model.presentation;

import java.time.Instant;

public record DeviceMetricsDto(
        String id,
        Integer deviceId,
        String sessionId,
        Instant time,
        Long tsNs,
        String name,
        String uuid,
        String vendor,
        String eventType,
        Integer pciBus,
        Long usedMib,
        Long freeMib,
        Long totalMib,
        Integer utilGpu,
        Integer utilMem,
        Integer tempC,
        Integer powerMw,
        Integer clkGfx,
        Integer clkSm,
        Integer clkMem,
        Integer throttlePwr,
        Integer throttleTherm,
        Long pcieRxBw,
        Long pcieTxBw,
        Instant createdAt,
        Instant updatedAt
) {
}
