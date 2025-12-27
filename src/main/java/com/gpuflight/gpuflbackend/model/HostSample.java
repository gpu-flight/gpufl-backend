package com.gpuflight.gpuflbackend.model;

import java.math.BigDecimal;

public record HostSample(
        double cpuPct,
        long ramUsedMib,
        long ramTotalMib
) {
}
