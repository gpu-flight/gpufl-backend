package com.gpuflight.gpuflbackend.service;

import com.gpuflight.gpuflbackend.model.EventWrapper;
import com.gpuflight.gpuflbackend.model.MetricType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventProcessingService {
    private final InitEventService initEventService;
    private final KernelEventService kernelEventService;
    private final ScopeEventService scopeEventService;
    private final SystemEventService systemEventService;

    @Transactional
    public void processEvent(MetricType type, EventWrapper eventWrapper) {
        switch (type) {
            case init -> initEventService.addInitEvent(eventWrapper);
            case kernel_event -> kernelEventService.addKernelEvent(eventWrapper);
            case scope_begin -> scopeEventService.addScopeEventBegin(eventWrapper);
            case scope_end -> scopeEventService.addScopeEventEnd(eventWrapper);
            case shutdown -> initEventService.shutdownEvent(eventWrapper);
            case system_start, system_stop, system_sample -> systemEventService.addSystemEvent(type, eventWrapper);
            default -> log.warn("Unhandled event type: {}", type);
        }
    }
}
