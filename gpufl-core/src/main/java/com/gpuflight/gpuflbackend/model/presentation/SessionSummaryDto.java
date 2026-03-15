package com.gpuflight.gpuflbackend.model.presentation;

import java.time.Instant;
import java.util.List;

public record SessionSummaryDto(
        String sessionId,
        String appName,
        Instant startTime,
        Instant endTime,
        List<CudaStaticDeviceDto> gpus
) {}
