package com.gpuflight.gpuflbackend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gpuflight.gpuflbackend.config.Constants;
import com.gpuflight.gpuflbackend.dao.HostMetricDao;
import com.gpuflight.gpuflbackend.dao.SystemEventDao;
import com.gpuflight.gpuflbackend.entity.SystemEventEntity;
import com.gpuflight.gpuflbackend.mapper.HostMetricMapper;
import com.gpuflight.gpuflbackend.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;

import static com.gpuflight.gpuflbackend.util.TimeUtils.epochToInstant;

@Service
@Slf4j
@RequiredArgsConstructor
public class SystemEventServiceImpl implements SystemEventService {
    private final ObjectMapper objectMapper;
    private final SystemEventDao systemEventDao;
    private final HostMetricDao hostMetricDao;
    private final DeviceMetricService deviceMetricService;

    @Override
    public void addSystemEvent(EventWrapper eventWrapper, MetricType metricType) {
        try {
            SystemSampleEvent event = objectMapper.readValue(eventWrapper.data(), SystemSampleEvent.class);
            Instant eventTime = epochToInstant(event.tsNs());
            String eventType = "";
            if(metricType == MetricType.system_start) {
                eventType = Constants.SYSTEM_START_EVENT;
            } else if(metricType == MetricType.system_stop) {
                eventType = Constants.SYSTEM_STOP_EVENT;
            } else if(metricType == MetricType.system_sample) {
                eventType = Constants.SYSTEM_SAMPLE_EVENT;
            }
            log.debug("Processing system event: {}", eventType);
            systemEventDao.saveSystemEvent(SystemEventEntity.builder()
                    .sessionId(event.sessionId())
                    .pid(event.pid())
                    .app(event.app())
                    .name(event.name())
                    .eventType(eventType)
                    .tsNs(event.tsNs())
                    .build());

            if (event.host() != null) {
                hostMetricDao.saveHostMetric(
                        HostMetricMapper.mapToHostMetricEntity(
                                event.host(), Constants.SYSTEM_START_EVENT, eventTime, event.tsNs(), event.sessionId(),
                                eventWrapper.hostname(), eventWrapper.ipAddr())
                );
            }

            if (event.devices() != null) {
                for (DeviceSample deviceSample : event.devices()) {
                    deviceMetricService.saveDeviceMetric(deviceSample, Constants.SYSTEM_START_EVENT, event.sessionId(), eventTime, event.tsNs());
                }
            }
        } catch (Exception e) {
            log.error("Failed to process system start event. Data: {}", eventWrapper.data(), e);
            throw new RuntimeException("Error processing SystemStartEvent", e);
        }
    }
}
