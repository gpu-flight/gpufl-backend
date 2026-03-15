package com.gpuflight.gpuflbackend.model.presentation;

import java.util.List;

public record HostSummaryDto(
        String hostname,
        String ipAddr,
        List<SessionSummaryDto> sessions
) {}
