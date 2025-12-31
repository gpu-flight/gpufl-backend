package com.gpuflight.gpuflbackend.mapper;

import com.gpuflight.gpuflbackend.entity.HostMetricEntity;
import com.gpuflight.gpuflbackend.model.HostSample;
import com.gpuflight.gpuflbackend.model.presentation.HostMetricsDto;

import java.time.Instant;

public class HostMetricMapper {
    public static HostMetricsDto mapToHostMetricsDto(HostMetricEntity entity) {
        return new HostMetricsDto(
                entity.getId(),
                entity.getSessionId(),
                entity.getTime(),
                entity.getHostname(),
                entity.getIpAddr(),
                entity.getTsNs(),
                entity.getCpuPct() != null ? entity.getCpuPct() : 0.0, // Handle potential null for primitive double
                entity.getRamUsedMib() != null ? entity.getRamUsedMib() : 0L,
                entity.getRamTotalMib() != null ? entity.getRamTotalMib() : 0L,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public static HostMetricEntity mapToHostMetricEntity(HostSample sample, String eventType, Instant time, Long tsNs, String sessionId,
                                                         String hostname, String ipAddr) {
        return HostMetricEntity.builder()
                .eventType(eventType)
                .time(time)
                .tsNs(tsNs)
                .hostname(hostname)
                .ipAddr(ipAddr)
                .sessionId(sessionId)
                .cpuPct(sample.cpuPct())
                .ramUsedMib(sample.ramUsedMib())
                .ramTotalMib(sample.ramTotalMib())
                .build();
    }
}
