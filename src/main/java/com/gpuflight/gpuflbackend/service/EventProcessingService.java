package com.gpuflight.gpuflbackend.service;

import com.gpuflight.gpuflbackend.handler.MetricEventHandler;
import com.gpuflight.gpuflbackend.model.MetricEvent;
import com.gpuflight.gpuflbackend.model.MetricType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class EventProcessingService {

    private final Map<MetricType, MetricEventHandler<? extends MetricEvent>> handlerMap;

    public EventProcessingService(List<MetricEventHandler<? extends MetricEvent>> handlers) {
        this.handlerMap = handlers.stream()
                .collect(Collectors.toMap(MetricEventHandler::getSupportedType, h -> h));
    }

    @Transactional
    @SuppressWarnings("unchecked")
    public void processEvent(MetricEvent event) {
        MetricEventHandler<MetricEvent> handler = (MetricEventHandler<MetricEvent>) handlerMap.get(event.type());
        if (handler != null) {
            handler.handle(event);
        } else {
            log.warn("Unhandled event type: {}", event.type());
        }
    }
}
