package com.gpuflight.gpuflbackend.mapper;

import com.gpuflight.gpuflbackend.entity.DeviceMetricEntity;
import com.gpuflight.gpuflbackend.model.presentation.DeviceMetricsDto;

public class DeviceMetricMapper {

    public static DeviceMetricsDto mapToDeviceSample(DeviceMetricEntity entity) {
        return new DeviceMetricsDto(
                entity.getId() != null ? entity.getId().toString() : null,
                entity.getDeviceId(),
                entity.getSessionId(),
                entity.getTime(),
                entity.getTsNs(),
                entity.getGpuUtil(),
                entity.getMemUtil(),
                entity.getTempC(),
                entity.getPowerMw(),
                entity.getUsedMib(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
