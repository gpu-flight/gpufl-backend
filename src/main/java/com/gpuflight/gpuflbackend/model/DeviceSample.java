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
        Long pcieTxBw
) {
}
