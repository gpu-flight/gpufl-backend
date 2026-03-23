package com.gpuflight.gpuflbackend.mapper;

import com.gpuflight.gpuflbackend.entity.HostMetricEntity;
import com.gpuflight.gpuflbackend.model.presentation.HostMetricsDto;

public class HostMetricMapper {
    public static HostMetricsDto mapToHostMetricsDto(HostMetricEntity entity) {
        return new HostMetricsDto(
                entity.getId(),
                entity.getSessionId(),
                entity.getTime(),
                entity.getTsNs(),
                entity.getCpuPct(),
                entity.getRamUsedMib(),
                entity.getRamTotalMib(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
