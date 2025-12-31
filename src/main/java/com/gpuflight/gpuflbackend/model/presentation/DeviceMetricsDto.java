package com.gpuflight.gpuflbackend.model.presentation;

import java.time.Instant;

public record DeviceMetricsDto(
        String id,
        int deviceId,
        String sessionId,
        Instant time,
        long tsNs,
        String name,
        String uuid,
        String vendor,
        String eventType,
        int pciBus,
        long usedMib,
        long freeMib,
        long totalMib,
        int utilGpu,
        int utilMem,
        int tempC,
        int powerMw,
        int clkGfx,
        int clkSm,
        int clkMem,
        int throttlePwr,
        int throttleTherm,
        long pcieRxBw,
        long pcieTxBw,
        Instant createAt,
        Instant updatedAt
) {
}
