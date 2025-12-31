package com.gpuflight.gpuflbackend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gpuflight.gpuflbackend.config.Constants;
import com.gpuflight.gpuflbackend.dao.HostMetricDao;
import com.gpuflight.gpuflbackend.dao.HostMetricDaoImpl;
import com.gpuflight.gpuflbackend.dao.ScopeEventDao;
import com.gpuflight.gpuflbackend.entity.HostMetricEntity;
import com.gpuflight.gpuflbackend.entity.ScopeEventEntity;
import com.gpuflight.gpuflbackend.mapper.HostMetricMapper;
import com.gpuflight.gpuflbackend.model.DeviceSample;
import com.gpuflight.gpuflbackend.model.EventWrapper;
import com.gpuflight.gpuflbackend.model.ScopeBeginEvent;
import com.gpuflight.gpuflbackend.model.ScopeEndEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;

import static com.gpuflight.gpuflbackend.util.TimeUtils.epochToInstant;

@Service
@Slf4j
public class ScopeEventServiceImpl implements ScopeEventService {
    private final ScopeEventDao scopeEventDao;
    private final ObjectMapper objectMapper;
    private final DeviceMetricService deviceMetricService;
    private final HostMetricDao hostMetricDao;

    public ScopeEventServiceImpl(ScopeEventDao scopeEventDao, ObjectMapper objectMapper, DeviceMetricService deviceMetricService, HostMetricDao hostMetricDao) {
        this.scopeEventDao = scopeEventDao;
        this.objectMapper = objectMapper;
        this.deviceMetricService = deviceMetricService;
        this.hostMetricDao = hostMetricDao;
    }

    @Override
    public void addScopeEventBegin(EventWrapper eventWrapper) {
        try {
            ScopeBeginEvent event = objectMapper.readValue(eventWrapper.data(), ScopeBeginEvent.class);
            Instant eventTime = epochToInstant(event.tsNs());
            scopeEventDao.saveScopeEvent(ScopeEventEntity.builder()
                    .tsNs(event.tsNs())
                    .time(eventTime)
                    .sessionId(event.sessionId())
                    .name(event.name())
                    .tag(event.tag())
                    .build());

            if (event.devices() != null) {
                for (DeviceSample deviceSample : event.devices()) {
                    deviceMetricService.saveDeviceMetric(deviceSample, Constants.SCOPE_BEGIN_EVENT, event.sessionId(), eventTime, event.tsNs());
                }
            }
            if(event.host() != null) {
                hostMetricDao.saveHostMetric(
                    HostMetricMapper.mapToHostMetricEntity(
                            event.host(), Constants.SCOPE_BEGIN_EVENT, eventTime, event.tsNs(), event.sessionId(),
                            eventWrapper.hostname(), eventWrapper.ipAddr())
                );
            }
        } catch (Exception e) {
            log.error("Failed to process ScopeBeginEvent. Data: {}", eventWrapper.data(), e);
            throw new RuntimeException("Error processing ScopeBeginEvent", e);
        }
    }

    @Override
    public void addScopeEventEnd(EventWrapper eventWrapper) {
        try {
            ScopeEndEvent event = objectMapper.readValue(eventWrapper.data(), ScopeEndEvent.class);

            Instant eventTime = epochToInstant(event.tsNs());
            scopeEventDao.updateScopeEventEnd(ScopeEventEntity.builder()
                    .tsNs(event.tsNs())
                    .sessionId(event.sessionId())
                    .name(event.name())
                    .build()
            );
            if (event.devices() != null) {
                for (DeviceSample deviceSample : event.devices()) {
                    deviceMetricService.saveDeviceMetric(deviceSample, Constants.SCOPE_END_EVENT, event.sessionId(), eventTime, event.tsNs());
                }
            }
            if(event.host() != null) {
                hostMetricDao.saveHostMetric(
                        HostMetricMapper.mapToHostMetricEntity(event.host(), Constants.SCOPE_BEGIN_EVENT, eventTime, event.tsNs(), event.sessionId(),
                                eventWrapper.hostname(), eventWrapper.ipAddr())
                );
            }
        } catch (Exception e) {
            log.error("Failed to process ScopeEndEvent. Data: {}", eventWrapper.data(), e);
            throw new RuntimeException("Error processing ScopeEndEvent", e);
        }
    }
}
