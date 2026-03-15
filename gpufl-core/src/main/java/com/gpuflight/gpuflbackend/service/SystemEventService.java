package com.gpuflight.gpuflbackend.service;

import com.gpuflight.gpuflbackend.model.EventWrapper;
import com.gpuflight.gpuflbackend.model.MetricType;
import com.gpuflight.gpuflbackend.model.presentation.SystemEventDto;

import java.time.Instant;
import java.util.List;

public interface SystemEventService {
    void addSystemEvent(MetricType type, EventWrapper eventWrapper);
    List<SystemEventDto> getSystemEvents(String sessionId, Instant dateFrom, Instant dateTo);
}
