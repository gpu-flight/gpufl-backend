package com.gpuflight.gpuflbackend.handler;

import com.gpuflight.gpuflbackend.model.MetricEvent;
import com.gpuflight.gpuflbackend.model.MetricType;

public interface MetricEventHandler<T extends MetricEvent> {
    void handle(T event);
    MetricType getSupportedType();
}
