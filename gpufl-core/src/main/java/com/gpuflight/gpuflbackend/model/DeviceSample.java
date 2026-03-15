package com.gpuflight.gpuflbackend.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DeviceSample(
        @JsonProperty("id")
        Integer deviceId,
        String name,
        String uuid,
        String vendor,
        Integer pciBus,
        Long usedMib,
        Long freeMib,
        Long totalMib,
        Integer utilGpuPct,
        Integer utilMemPct,
        Integer tempC,
        Integer powerMw,
        Integer clkGfxMhz,
        Integer clkSmMhz,
        Integer clkMemMhz,
        Integer throttlePwr,
        Integer throttleTherm,
        Long pcieRxBwBps,
        Long pcieTxBwBps
) {
}
