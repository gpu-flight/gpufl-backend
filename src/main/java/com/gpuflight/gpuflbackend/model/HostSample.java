package com.gpuflight.gpuflbackend.model;

import java.math.BigDecimal;

public record HostSample(
        Double cpuPct,
        Long ramUsedMib,
        Long ramTotalMib
) {
}
