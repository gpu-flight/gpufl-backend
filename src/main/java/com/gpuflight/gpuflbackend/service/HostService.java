package com.gpuflight.gpuflbackend.service;

import com.gpuflight.gpuflbackend.model.presentation.HostSummaryDto;

import java.util.List;

public interface HostService {
    List<HostSummaryDto> getHostSummaries();
}
