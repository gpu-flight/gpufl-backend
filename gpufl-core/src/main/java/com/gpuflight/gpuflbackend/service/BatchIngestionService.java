package com.gpuflight.gpuflbackend.service;

import com.gpuflight.gpuflbackend.model.EventWrapper;
import com.gpuflight.gpuflbackend.model.MetricType;

public interface BatchIngestionService {
    void handle(MetricType type, EventWrapper wrapper);
}
