package com.gpuflight.gpuflbackend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.gpuflight.gpuflbackend.model.*;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class JsonEventParser {
    private final ObjectMapper objectMapper;

    private static final Map<MetricType, Class<? extends MetricEvent>> TYPE_TO_CLASS = Map.of(
            MetricType.init, InitEvent.class,
            MetricType.kernel_start, KernelBeginEvent.class,
            MetricType.kernel_end, KernelEndEvent.class,
            MetricType.scope_begin, ScopeBeginEvent.class,
            MetricType.scope_end, ScopeEndEvent.class,
            MetricType.shutdown, ShutdownEvent.class,
            MetricType.system_start, SystemStartEvent.class,
            MetricType.system_end, SystemStopEvent.class,
            MetricType.system_sample, SystemSampleEvent.class
    );

    public JsonEventParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public MetricEvent parseEvent(String json) throws JsonProcessingException {
        return objectMapper.readValue(json, MetricEvent.class);
    }

    public MetricEvent parseEvent(String json, MetricType type) throws JsonProcessingException {
        Class<? extends MetricEvent> clazz = TYPE_TO_CLASS.get(type);
        if (clazz == null) {
            throw new IllegalArgumentException("Unsupported metric type: " + type);
        }
        return objectMapper.readValue(json, clazz);
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }
}
