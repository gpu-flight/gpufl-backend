package com.gpuflight.gpuflbackend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gpuflight.gpuflbackend.dao.*;
import com.gpuflight.gpuflbackend.entity.*;
import com.gpuflight.gpuflbackend.exception.EventProcessingException;
import com.gpuflight.gpuflbackend.model.*;
import com.gpuflight.gpuflbackend.model.input.KernelBeginEvent;
import com.gpuflight.gpuflbackend.model.input.KernelEndEvent;
import com.gpuflight.gpuflbackend.validator.KernelEventValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.gpuflight.gpuflbackend.util.TimeUtils.epochToInstant;

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
            case kernel_start -> kernelEventService.addKernelBeginEvent(eventWrapper);
            case kernel_end -> kernelEventService.addKernelEndEvent(eventWrapper);
            case scope_begin -> scopeEventService.addScopeEventBegin(eventWrapper);
            case scope_end -> scopeEventService.addScopeEventEnd(eventWrapper);
            case shutdown -> initEventService.shutdownEvent(eventWrapper);
            case system_start, system_stop, system_sample -> systemEventService.addSystemEvent(type, eventWrapper);
            default -> log.warn("Unhandled event type: {}", type);
        }
    }
}
