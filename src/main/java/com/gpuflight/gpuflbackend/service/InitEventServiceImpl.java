package com.gpuflight.gpuflbackend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gpuflight.gpuflbackend.config.Constants;
import com.gpuflight.gpuflbackend.dao.*;
import com.gpuflight.gpuflbackend.entity.*;
import com.gpuflight.gpuflbackend.mapper.CudaStaticDeviceMapper;
import com.gpuflight.gpuflbackend.mapper.DeviceMetricMapper;
import com.gpuflight.gpuflbackend.mapper.HostMetricMapper;
import com.gpuflight.gpuflbackend.mapper.KernelEventMapper;
import com.gpuflight.gpuflbackend.mapper.ScopeEventMapper;
import com.gpuflight.gpuflbackend.mapper.SystemEventMapper;
import com.gpuflight.gpuflbackend.model.*;
import com.gpuflight.gpuflbackend.model.presentation.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static com.gpuflight.gpuflbackend.util.TimeUtils.epochToInstant;

@Component
@Slf4j
public class InitEventServiceImpl implements InitEventService {

    private final InitDao initDao;
    private final HostMetricDao hostMetricDao;
    private final CudaDeviceDao cudaDeviceDao;
    private final SessionDao sessionDao;
    private final DeviceMetricDao deviceMetricDao;
    private final CudaDeviceServiceImpl cudaDeviceService;
    private final ObjectMapper objectMapper;
    private final ScopeEventDao scopeEventDao;
    private final KernelEventDao kernelEventDao;

    public InitEventServiceImpl(InitDao initDao, HostMetricDao hostMetricDao, CudaDeviceDao cudaDeviceDao, SessionDao sessionDao, DeviceMetricDao deviceMetricDao, CudaDeviceServiceImpl cudaDeviceService, @Qualifier("ingestionObjectMapper") ObjectMapper objectMapper, ScopeEventDao scopeEventDao, KernelEventDao kernelEventDao, SystemEventDao systemEventDao) {
        this.initDao = initDao;
        this.hostMetricDao = hostMetricDao;
        this.cudaDeviceDao = cudaDeviceDao;
        this.sessionDao = sessionDao;
        this.deviceMetricDao = deviceMetricDao;
        this.cudaDeviceService = cudaDeviceService;
        this.objectMapper = objectMapper;
        this.scopeEventDao = scopeEventDao;
        this.kernelEventDao = kernelEventDao;
    }

    @Override
    public void addInitEvent(EventWrapper eventWrapper) {
        try {

            InitEvent event = objectMapper.readValue(eventWrapper.data(), InitEvent.class);
            Instant eventTime = epochToInstant(event.tsNs());

            log.debug("Processing init event for session: {}, app: {}", event.sessionId(), event.app());

            if (initDao.existsBySessionId(event.sessionId())) {
                log.debug("Initial event already exists for session_id: {}. Skipping.", event.sessionId());
                return;
            }

            initDao.saveInitialEvent(InitialEventEntity.builder()
                            .time(eventTime)
                            .sessionId(event.sessionId())
                            .pid(event.pid())
                            .app(event.app())
                            .logPath(event.logPath())
                            .systemRateMs(event.systemRateMs())
                            .tsNs(event.tsNs())
                            .build());

            sessionDao.saveSession(SessionEntity.builder()
                            .sessionId(event.sessionId())
                            .appName(event.app())
                            .hostname(eventWrapper.hostname())
                            .ipAddr(eventWrapper.ipAddr())
                            .startTime(eventTime)
                            .build());

            if (event.devices() != null) {
                log.debug("Saving {} devices for session: {}", event.devices().size(), event.sessionId());
                for (DeviceSample ds : event.devices()) {
                    deviceMetricDao.saveDeviceMetric(DeviceMetricMapper
                            .mapToDeviceMetricEntity(ds, Constants.INIT_EVENT, event.sessionId(), eventTime, event.tsNs()));
                }
            } else {
                log.info("No devices found in init event for session: {}", event.sessionId());
            }

            if(event.host() != null) {
                hostMetricDao.saveHostMetric(
                        HostMetricMapper.mapToHostMetricEntity(
                                event.host(), Constants.INIT_EVENT, eventTime, event.tsNs(), event.sessionId(),
                                eventWrapper.hostname(), eventWrapper.ipAddr())
                );
            }

            if (event.cudaStaticDevices() != null) {
                log.debug("Saving {} CUDA devices for session: {}", event.cudaStaticDevices().size(), event.sessionId());
                for (CudaStaticDevice sd : event.cudaStaticDevices()) {
                    cudaDeviceDao.saveCudaDevice(CudaStaticDeviceEntity.builder()
                            .sessionId(event.sessionId())
                            .deviceId(sd.id())
                            .name(sd.name())
                            .uuid(sd.uuid())
                            .computeMajor(sd.computeMajor())
                            .computeMinor(sd.computeMinor())
                            .l2CacheSizeBytes(sd.l2CacheSizeBytes())
                            .sharedMemPerBlockBytes(sd.sharedMemPerBlockBytes())
                            .regsPerBlock(sd.regsPerBlock())
                            .multiProcessorCount(sd.multiProcessorCount())
                            .warpSize(sd.warpSize())
                            .build());
                }
            }

            log.info("Successfully processed init event for session: {}", event.sessionId());
        } catch (JsonProcessingException e) {
            log.error("Failed to parse init event. Data: {}", eventWrapper.data(), e);
        }
    }


    public List<InitEventDto> getInitEvents(Instant dateFrom, Instant dateTo) {
        List<InitialEventEntity> entities = initDao.findByDateRange(dateFrom, dateTo);
        if (entities.isEmpty()) {
            return Collections.emptyList();
        }

        Set<String> sessionIds = entities.stream()
                .map(InitialEventEntity::getSessionId)
                .collect(Collectors.toSet());

        List<CudaStaticDeviceEntity> allCudaEntities = cudaDeviceService.getCudaStaticDeviceEntities(sessionIds);
        Map<String, List<CudaStaticDeviceEntity>> cudaBySession = allCudaEntities.stream()
                .collect(Collectors.groupingBy(CudaStaticDeviceEntity::getSessionId));

        List<HostMetricsDto> hostMetrics = hostMetricDao.findBySessionIds(sessionIds).stream()
                .map(HostMetricMapper::mapToHostMetricsDto)
                .toList();
        Map<String, List<HostMetricsDto>> hostMetricsBySession = hostMetrics.stream()
                .collect(Collectors.groupingBy(HostMetricsDto::sessionId));

        List<ScopeEventDto> allScopeDtos = scopeEventDao.findBySessionIds(sessionIds).stream()
                .map(ScopeEventMapper::mapToScopeEventDto)
                .toList();
        Map<String, List<ScopeEventDto>> scopesBySession = allScopeDtos.stream()
                .collect(Collectors.groupingBy(ScopeEventDto::sessionId));

        List<KernelEventDto> allKernelDtos = kernelEventDao.findBySessionIds(sessionIds).stream()
                .map(KernelEventMapper::mapToKernelEventDto)
                .toList();
        Map<String, List<KernelEventDto>> kernelsBySession = allKernelDtos.stream()
                .collect(Collectors.groupingBy(KernelEventDto::sessionId));


        List<InitEventDto> result = new ArrayList<>();

        for (InitialEventEntity entity : entities) {
            String sessionId = entity.getSessionId();
            List<CudaStaticDeviceEntity> sessionCuda = cudaBySession.getOrDefault(sessionId, Collections.emptyList());
            List<HostMetricsDto> hostMetricsForSession = hostMetricsBySession.getOrDefault(sessionId, Collections.emptyList());
            List<ScopeEventDto> scopesForSession = scopesBySession.getOrDefault(sessionId, Collections.emptyList());
            List<KernelEventDto> kernelsForSession = kernelsBySession.getOrDefault(sessionId, Collections.emptyList());

            List<CudaStaticDeviceDto> cudaStaticDevices = sessionCuda.stream()
                    .map(CudaStaticDeviceMapper::mapToCudaStaticDeviceDto)
                    .collect(Collectors.toList());

            InitEventDto reconstructedEvent = new InitEventDto(
                    entity.getPid() != null ? entity.getPid() : 0,
                    entity.getApp(),
                    entity.getSessionId(),
                    entity.getLogPath(),
                    entity.getTime(),
                    entity.getTsNs(),
                    entity.getShutdownTsNs(),
                    entity.getSystemRateMs() != null ? entity.getSystemRateMs() : 0,
                    hostMetricsForSession,
                    cudaStaticDevices,
                    scopesForSession,
                    kernelsForSession,
                    entity.getCreatedAt(),
                    entity.getUpdatedAt()
            );

            result.add(reconstructedEvent);
        }
        return result;
    }

    @Override
    public void shutdownEvent(EventWrapper eventWrapper) {
        try {
            ShutdownEvent event = objectMapper.readValue(eventWrapper.data(), ShutdownEvent.class);
            initDao.shutdownEvent(event.sessionId(), event.app(), event.tsNs());
        } catch (JsonProcessingException e) {
            log.error("Failed to parse shutdown event. Data: {}", eventWrapper.data(), e);
        }
    }

}
