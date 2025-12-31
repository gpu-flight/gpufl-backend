package com.gpuflight.gpuflbackend.service;

import com.gpuflight.gpuflbackend.entity.DeviceMetricEntity;
import com.gpuflight.gpuflbackend.model.DeviceSample;

import java.time.Instant;

public interface DeviceMetricService {
    void saveDeviceMetric(DeviceSample deviceSample, String eventType, String sessionId, Instant time, Long tsNs);
}
