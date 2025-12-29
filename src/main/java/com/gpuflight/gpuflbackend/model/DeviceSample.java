package com.gpuflight.gpuflbackend.model;

public record DeviceSample(
        int id,
        String name,
        String uuid,
        String vendor,
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
        long pcieTxBw
) {
}
