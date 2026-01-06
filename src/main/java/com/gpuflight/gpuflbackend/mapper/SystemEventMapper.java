package com.gpuflight.gpuflbackend.mapper;

import com.gpuflight.gpuflbackend.entity.SystemEventEntity;
import com.gpuflight.gpuflbackend.model.presentation.DeviceMetricsDto;
import com.gpuflight.gpuflbackend.model.presentation.HostMetricsDto;
import com.gpuflight.gpuflbackend.model.presentation.SystemEventDto;

import java.util.List;

public class SystemEventMapper {
    public static SystemEventDto mapToSystemEventDto(SystemEventEntity entity, List<HostMetricsDto> hostMetrics, List<DeviceMetricsDto> deviceMetrics) {
        return new SystemEventDto(
                entity.getSessionId(),
                entity.getPid(),
                entity.getApp(),
                entity.getName(),
                entity.getEventType(),
                entity.getTsNs(),
                entity.getRangeStart(),
                entity.getRangeEnd(),
                hostMetrics,
                deviceMetrics,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
