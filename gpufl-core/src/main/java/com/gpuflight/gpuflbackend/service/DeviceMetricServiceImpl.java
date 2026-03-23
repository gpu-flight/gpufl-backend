package com.gpuflight.gpuflbackend.service;

import com.gpuflight.gpuflbackend.dao.DeviceMetricDao;
import com.gpuflight.gpuflbackend.model.DeviceSample;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class DeviceMetricServiceImpl implements DeviceMetricService {
    private final DeviceMetricDao deviceMetricDao;

    public DeviceMetricServiceImpl(DeviceMetricDao deviceMetricDao) {
        this.deviceMetricDao = deviceMetricDao;
    }

    @Override
    public void saveDeviceMetric(DeviceSample deviceSample, String eventType, String sessionId, Instant time, Long tsNs) {
        // Device metrics are now ingested via device_metric_batch messages.
        // This method is kept for interface compatibility.
    }
}
