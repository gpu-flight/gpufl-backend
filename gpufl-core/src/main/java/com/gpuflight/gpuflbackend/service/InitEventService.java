package com.gpuflight.gpuflbackend.service;

import com.gpuflight.gpuflbackend.model.EventWrapper;
import com.gpuflight.gpuflbackend.model.presentation.InitEventDto;

import java.time.Instant;
import java.util.List;

public interface InitEventService {
    void addInitEvent(EventWrapper eventWrapper);
    List<InitEventDto> getInitEvents(Instant dateFrom, Instant dateTo);
    void shutdownEvent(EventWrapper eventWrapper);
}
