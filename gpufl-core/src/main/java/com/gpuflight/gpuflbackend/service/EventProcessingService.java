package com.gpuflight.gpuflbackend.service;

import com.gpuflight.gpuflbackend.model.EventWrapper;
import com.gpuflight.gpuflbackend.model.MetricType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class EventProcessingService {
    private final InitEventService initEventService;
    private final SystemEventService systemEventService;
    private final BatchIngestionService batchIngestionService;

    public EventProcessingService(InitEventService initEventService,
                                  SystemEventService systemEventService,
                                  BatchIngestionService batchIngestionService) {
        this.initEventService = initEventService;
        this.systemEventService = systemEventService;
        this.batchIngestionService = batchIngestionService;
    }

    @Transactional
    public void processEvent(MetricType type, EventWrapper eventWrapper) {
        switch (type) {
            case job_start         -> initEventService.addInitEvent(eventWrapper);
            case shutdown          -> initEventService.shutdownEvent(eventWrapper);
            case system_start,
                 system_stop       -> systemEventService.addSystemEvent(type, eventWrapper);
            case dictionary_update,
                 kernel_event_batch,
                 kernel_detail,
                 memcpy_event_batch,
                 device_metric_batch,
                 host_metric_batch,
                 scope_event_batch,
                 profile_sample_batch -> batchIngestionService.handle(type, eventWrapper);
            default -> log.warn("Unhandled event type: {}", type);
        }
    }
}
