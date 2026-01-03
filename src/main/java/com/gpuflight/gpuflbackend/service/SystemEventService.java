package com.gpuflight.gpuflbackend.service;

import com.gpuflight.gpuflbackend.model.EventWrapper;
import com.gpuflight.gpuflbackend.model.MetricType;

public interface SystemEventService {
    void addSystemEvent(EventWrapper eventWrapper, MetricType metricType);
}
