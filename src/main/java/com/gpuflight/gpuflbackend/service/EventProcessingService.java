package com.gpuflight.gpuflbackend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gpuflight.gpuflbackend.dao.*;
import com.gpuflight.gpuflbackend.entity.*;
import com.gpuflight.gpuflbackend.exception.EventProcessingException;
import com.gpuflight.gpuflbackend.model.*;
import com.gpuflight.gpuflbackend.validator.KernelEventValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.gpuflight.gpuflbackend.config.Constants.NVIDIA_VENDOR;
import static com.gpuflight.gpuflbackend.util.TimeUtils.epochToInstant;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventProcessingService {

    private final SessionDao sessionDao;
    private final DeviceDao deviceDao;
    private final CudaDeviceDao cudaDeviceDao;
    private final ScopeEventDao scopeEventDao;
    private final KernelEventDao kernelEventDao;
    private final DeviceMetricDao deviceMetricDao;
    private final HostMetricDao hostMetricDao;
    private final InitDao initDao;
    private final ObjectMapper objectMapper;
    private final KernelEventValidator kernelEventValidator;

    @Transactional
    public void processEvent(MetricType type, String json) throws JsonProcessingException {
        switch (type) {
            case init -> handleInit(objectMapper.readValue(json, InitEvent.class), json);
            case kernel_start -> handleKernelBegin(objectMapper.readValue(json, KernelBeginEvent.class));
            case kernel_end -> handleKernelEnd(objectMapper.readValue(json, KernelEndEvent.class));
            case scope_begin -> handleScopeBegin(objectMapper.readValue(json, ScopeBeginEvent.class));
            case scope_end -> handleScopeEnd(objectMapper.readValue(json, ScopeEndEvent.class));
            case shutdown -> handleShutdown(objectMapper.readValue(json, ShutdownEvent.class));
            case system_start -> handleSystemStart(objectMapper.readValue(json, SystemStartEvent.class));
            case system_stop -> handleSystemStop(objectMapper.readValue(json, SystemStopEvent.class));
            case system_sample -> handleSystemSample(objectMapper.readValue(json, SystemSampleEvent.class));
            default -> log.warn("Unhandled event type: {}", type);
        }
    }

    private void handleInit(InitEvent event, String rawJson) {
        try {
            Instant eventTime = epochToInstant(event.tsNs());
            log.debug("Processing init event for session: {}, app: {}", event.sessionId(), event.app());

            if (initDao.existsBySessionId(event.sessionId())) {
                log.debug("Initial event already exists for session_id: {}. Skipping.", event.sessionId());
                return;
            }

            initDao.saveInitialEvent(InitialEventEntity.builder()
                    .sessionId(event.sessionId())
                    .time(eventTime)
                    .tsNs(event.tsNs())
                    .eventJson(rawJson)
                    .build());

            sessionDao.saveSession(SessionEntity.builder()
                    .sessionId(event.sessionId())
                    .appName(event.app())
                    .pid(event.pid())
                    .startTime(eventTime)
                    .build());

            if (event.devices() != null) {
                log.debug("Saving {} devices for session: {}", event.devices().size(), event.sessionId());
                for (DeviceSample ds : event.devices()) {
                    deviceDao.saveDevice(DeviceEntity.builder()
                            .sessionId(event.sessionId())
                            .uuid(ds.uuid())
                            .deviceId(ds.id())
                            .vendor(ds.vendor())
                            .name(ds.name())
                            .memoryTotalMib(ds.totalMib())
                            .build());
                }
            } else {
                log.info("No devices found in init event for session: {}", event.sessionId());
            }

            if (event.cudaStaticDevices() != null) {
                log.debug("Saving {} CUDA devices for session: {}", event.cudaStaticDevices().size(), event.sessionId());
                for (CudaStaticDevice sd : event.cudaStaticDevices()) {
                    cudaDeviceDao.saveCudaDevice(CudaStaticDeviceEntity.builder()
                            .sessionId(event.sessionId())
                            .uuid(sd.uuid())
                            .computeMajor(sd.computeMajor())
                            .computeMinor(sd.computeMinor())
                            .l2CacheSizeBytes(sd.l2CacheSize())
                            .sharedMemPerBlockBytes(sd.sharedMemPerBlock())
                            .regsPerBlock(sd.regsPerBlock())
                            .multiProcessorCount(sd.multiProcessorCount())
                            .warpSize(sd.warpSize())
                            .build());
                }
            }

            log.info("Successfully processed init event for session: {}", event.sessionId());
        } catch (DataAccessException e) {
            log.error("Database error while processing init event for session: {}", event.sessionId(), e);
            throw new EventProcessingException(
                    "Failed to save init event to database: " + e.getMessage(),
                    e,
                    "init",
                    event.sessionId()
            );
        } catch (Exception e) {
            log.error("Unexpected error while processing init event for session: {}", event.sessionId(), e);
            throw new EventProcessingException(
                    "Unexpected error during init event processing: " + e.getMessage(),
                    e,
                    "init",
                    event.sessionId()
            );
        }
    }

    private void handleKernelBegin(KernelBeginEvent event) {
        Map<String, Object> extraParams = getStringObjectMap(event);

        kernelEventValidator.validate(event.platform(), extraParams);

        kernelEventDao.saveKernelBegin(KernelEventEntity.builder()
                .sessionId(event.sessionId())
                .platform(event.platform())
                .grid(event.grid())
                .block(event.block())
                .name(event.name())
                .corrId((long) event.corrId())
                .startNs(event.tsNs())
                .time(epochToInstant(event.tsNs()))
                .hasDetails(event.hasDetails())
                .extraParams(toJson(extraParams))
                .build());
    }

    private static @NonNull Map<String, Object> getStringObjectMap(KernelBeginEvent event) {
        Map<String, Object> extraParams = new HashMap<>();
        if (event.hasDetails()) {
            if (event.grid() != null) extraParams.put("grid", event.grid());
            if (event.block() != null) extraParams.put("block", event.block());
            extraParams.put("dyn_shared_bytes", event.dynSharedBytes());
            extraParams.put("num_regs", event.numRegs());
            extraParams.put("static_shared_bytes", event.staticSharedBytes());
            extraParams.put("local_bytes", event.localBytes());
            extraParams.put("const_bytes", event.constBytes());
            if (event.occupancy() != null) extraParams.put("occupancy", event.occupancy());
            extraParams.put("max_active_blocks", event.maxActiveBlocks());
        }
        return extraParams;
    }

    private void handleKernelEnd(KernelEndEvent event) {
        kernelEventDao.updateKernelEnd(KernelEventEntity.builder()
                .sessionId(event.sessionId())
                .corrId((long) event.corrId())
                .endNs(event.tsNs())
                .cudaError(event.cudaError())
                .build());
    }

    private void handleShutdown(ShutdownEvent event) {
        sessionDao.updateSessionEndTime(SessionEntity.builder()
                .sessionId(event.sessionId())
                .endTime(epochToInstant(event.tsNs()))
                .build());
    }

    private void handleScopeBegin(ScopeBeginEvent event) {
        Instant eventTime = epochToInstant(event.tsNs());
        scopeEventDao.saveScopeEvent(ScopeEventEntity.builder()
                .tsNs(event.tsNs())
                .time(eventTime)
                .sessionId(event.sessionId())
                .type("SCOPE_BEGIN")
                .name(event.name())
                .tag(event.tag())
                .hostCpuPct(event.host() != null ? event.host().cpuPct() : null)
                .hostRamUsedMib(event.host() != null ? event.host().ramUsedMib() : null)
                .build());

        if(event.devices() != null) {
            saveDevices(event.devices(), eventTime, event.tsNs(), event.sessionId());
        }
    }

    private void handleScopeEnd(ScopeEndEvent event) {
        scopeEventDao.saveScopeEvent(ScopeEventEntity.builder()
                .tsNs(event.tsNs())
                .time(epochToInstant(event.tsNs()))
                .sessionId(event.sessionId())
                .type("SCOPE_END")
                .name(event.name())
                .tag(event.tag())
                .hostCpuPct(event.host() != null ? event.host().cpuPct() : null)
                .hostRamUsedMib(event.host() != null ? event.host().ramUsedMib() : null)
                .build());

        if(event.devices() != null) {
            saveDevices(event.devices(), epochToInstant(event.tsNs()), event.tsNs(), event.sessionId());
        }
    }

    private void handleSystemStart(SystemStartEvent event) {
        saveSystemMetrics(event.tsNs(), event.sessionId(), "SYSTEM_START", event.host(), event.devices());
    }

    private void handleSystemStop(SystemStopEvent event) {
        saveSystemMetrics(event.tsNs(), event.sessionId(), "SYSTEM_STOP", event.host(), event.devices());
    }

    private void handleSystemSample(SystemSampleEvent event) {
        saveSystemMetrics(event.tsNs(), event.sessionId(), "SYSTEM_SAMPLE", event.host(), event.devices());
    }

    private void saveSystemMetrics(long tsNs, String sessionId, String type, HostSample host, List<DeviceSample> devices) {
        log.debug("device = {}", devices);
        Instant eventTime = epochToInstant(tsNs);

        if (host != null) {
            hostMetricDao.saveHostMetric(HostMetricEntity.builder()
                    .time(eventTime)
                    .tsNs(tsNs)
                    .sessionId(sessionId)
                    .type(type)
                    .cpuPct(host.cpuPct())
                    .ramUsedMib(host.ramUsedMib())
                    .build());
        }

        if (devices != null) {
            saveDevices(devices, eventTime, tsNs, sessionId);
        }
    }

    private void saveDevices(List<DeviceSample> devices, Instant eventTime, long tsNs, String sessionId) {
        if(devices == null) return;
        for (DeviceSample ds : devices) {
            Map<String, Object> extended = new HashMap<>();
            extended.put("clk_gfx", ds.clkGfx());
            extended.put("clk_sm", ds.clkSm());
            extended.put("clk_mem", ds.clkMem());
            extended.put("throttle_pwr", ds.throttlePwr());
            extended.put("throttle_therm", ds.throttleTherm());
            extended.put("pcie_rx_bw", ds.pcieRxBw());
            extended.put("pcie_tx_bw", ds.pcieTxBw());

            deviceMetricDao.saveDeviceMetric(DeviceMetricEntity.builder()
                    .time(eventTime)
                    .tsNs(tsNs)
                    .sessionId(sessionId)
                    .deviceUuid(ds.uuid())
                    .powerWatts(ds.powerMw() / 1000.0)
                    .tempC(ds.tempC())
                    .utilGpuPct(ds.utilGpu())
                    .utilMemPct(ds.utilMem())
                    .memUsedMib(ds.usedMib())
                    .extendedMetrics(toJson(extended))
                    .build());
        }
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize to JSON", e);
            return "{}";
        }
    }

}
