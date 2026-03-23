package com.gpuflight.gpuflbackend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gpuflight.gpuflbackend.config.Constants;
import com.gpuflight.gpuflbackend.dao.SystemEventDao;
import com.gpuflight.gpuflbackend.dao.DeviceMetricDao;
import com.gpuflight.gpuflbackend.dao.HostMetricDao;
import com.gpuflight.gpuflbackend.dao.InitDao;
import com.gpuflight.gpuflbackend.entity.SystemEventEntity;
import com.gpuflight.gpuflbackend.mapper.DeviceMetricMapper;
import com.gpuflight.gpuflbackend.mapper.HostMetricMapper;
import com.gpuflight.gpuflbackend.mapper.SystemEventMapper;
import com.gpuflight.gpuflbackend.model.*;
import com.gpuflight.gpuflbackend.model.presentation.DeviceMetricsDto;
import com.gpuflight.gpuflbackend.model.presentation.HostMetricsDto;
import com.gpuflight.gpuflbackend.model.presentation.SystemEventDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static com.gpuflight.gpuflbackend.util.TimeUtils.epochToInstant;

@Service
@Slf4j
public class SystemEventServiceImpl implements SystemEventService {
    private final ObjectMapper objectMapper;
    private final SystemEventDao systemEventDao;
    private final HostMetricDao hostMetricDao;
    private final DeviceMetricDao deviceMetricDao;
    private final InitDao initDao;

    public SystemEventServiceImpl(@Qualifier("ingestionObjectMapper") ObjectMapper objectMapper,
                                   SystemEventDao systemEventDao,
                                   HostMetricDao hostMetricDao,
                                   DeviceMetricDao deviceMetricDao,
                                   InitDao initDao) {
        this.objectMapper = objectMapper;
        this.systemEventDao = systemEventDao;
        this.hostMetricDao = hostMetricDao;
        this.deviceMetricDao = deviceMetricDao;
        this.initDao = initDao;
    }

    @Override
    public List<SystemEventDto> getSystemEvents(String sessionId, Instant dateFrom, Instant dateTo) {
        Set<String> sessionIds = Collections.singleton(sessionId);

        List<SystemEventEntity> systemEntities = systemEventDao.findBySessionId(sessionId);
        if (systemEntities.isEmpty()) {
            return Collections.emptyList();
        }

        List<HostMetricsDto> allHostMetrics = hostMetricDao.findBySessionIds(sessionIds).stream()
                .map(HostMetricMapper::mapToHostMetricsDto)
                .toList();
        Map<String, List<HostMetricsDto>> hostMetricsBySession = allHostMetrics.stream()
                .collect(Collectors.groupingBy(HostMetricsDto::sessionId));

        List<DeviceMetricsDto> allDeviceMetrics = deviceMetricDao.findBySessionIds(sessionIds).stream()
                .map(DeviceMetricMapper::mapToDeviceSample)
                .toList();
        Map<String, List<DeviceMetricsDto>> deviceMetricsBySession = allDeviceMetrics.stream()
                .collect(Collectors.groupingBy(DeviceMetricsDto::sessionId));

        return systemEntities.stream()
                .map(entity -> SystemEventMapper.mapToSystemEventDto(
                        entity,
                        hostMetricsBySession.getOrDefault(entity.getSessionId(), Collections.emptyList()),
                        deviceMetricsBySession.getOrDefault(entity.getSessionId(), Collections.emptyList())
                ))
                .collect(Collectors.toList());
    }

    @Override
    public void addSystemEvent(MetricType eventType, EventWrapper eventWrapper) {
        try {
            SystemSampleEvent event = objectMapper.readValue(eventWrapper.data(), SystemSampleEvent.class);
            String eventIngestionType = "";
            if (eventType == MetricType.system_start) {
                eventIngestionType = Constants.SYSTEM_START_EVENT;
            } else if (eventType == MetricType.system_stop) {
                eventIngestionType = Constants.SYSTEM_STOP_EVENT;
            }
            systemEventDao.saveSystemEvent(SystemEventEntity.builder()
                    .sessionId(event.sessionId())
                    .pid(event.pid())
                    .app(event.app())
                    .name(event.name())
                    .eventType(eventIngestionType)
                    .tsNs(event.tsNs())
                    .build());
        } catch (Exception e) {
            log.error("Failed to process system event. Data: {}", eventWrapper.data(), e);
            throw new RuntimeException("Error processing SystemEvent", e);
        }
    }
}
