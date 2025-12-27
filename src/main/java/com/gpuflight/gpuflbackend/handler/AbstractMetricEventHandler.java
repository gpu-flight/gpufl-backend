package com.gpuflight.gpuflbackend.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gpuflight.gpuflbackend.dao.*;
import com.gpuflight.gpuflbackend.entity.KernelEventEntity;
import com.gpuflight.gpuflbackend.model.CudaStaticDevice;
import com.gpuflight.gpuflbackend.model.HostSample;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Slf4j
public abstract class AbstractMetricEventHandler<T extends com.gpuflight.gpuflbackend.model.MetricEvent> implements MetricEventHandler<T> {

    protected final SessionDao sessionDao;
    protected final DeviceDao deviceDao;
    protected final ScopeEventDao scopeEventDao;
    protected final KernelEventDao kernelEventDao;
    protected final SystemMetricDao systemMetricDao;
    protected final ObjectMapper objectMapper;

    protected void saveSession(String sessionId, String appName, int pid, long tsNs) {
        sessionDao.saveSession(sessionId, appName, pid, tsNs);
    }

    protected void updateSessionEndTime(String sessionId, long tsNs) {
        sessionDao.updateSessionEndTime(sessionId, tsNs);
    }

    protected void saveDevice(String sessionId, String uuid, String vendor, String name, Long memoryTotalMib, String staticPropsJson) {
        deviceDao.saveDevice(sessionId, uuid, vendor, name, memoryTotalMib, staticPropsJson);
    }

    protected void saveScopeEvent(long tsNs, String sessionId, String type, String name, String tag, HostSample host) {
        scopeEventDao.saveScopeEvent(tsNs, sessionId, type, name, tag, host);
    }

    protected void updateScopeEvent(String sessionId, String name, String type, long tsNs) {
        scopeEventDao.updateScopeEvent(sessionId, name, type, tsNs);
    }

    protected void saveKernelBegin(KernelEventEntity entity) {
        kernelEventDao.saveKernelBegin(entity);
    }

    protected void updateKernelEnd(String sessionId, long corrId, long endNs, String cudaError) {
        kernelEventDao.updateKernelEnd(sessionId, corrId, endNs, cudaError);
    }

    protected void saveSystemMetric(Instant time, long tsNs, String sessionId, String deviceUuid, Double powerWatts, Integer tempC, Integer utilGpuPct, Integer utilMemPct, Long memUsedMib, String extendedMetricsJson) {
        systemMetricDao.saveSystemMetric(time, tsNs, sessionId, deviceUuid, powerWatts, tempC, utilGpuPct, utilMemPct, memUsedMib, extendedMetricsJson);
    }

    protected String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize to JSON", e);
            return "{}";
        }
    }

    protected static @NonNull Map<String, Object> getStringObjectMap(CudaStaticDevice sd) {
        Map<String, Object> staticProps = new HashMap<>();
        staticProps.put("compute_cap", sd.computeMajor() + "." + sd.computeMinor());
        staticProps.put("l2_cache", sd.l2CacheSize());
        staticProps.put("regs_per_block", sd.regsPerBlock());
        staticProps.put("warp_size", sd.warpSize());
        staticProps.put("multi_processor_count", sd.multiProcessorCount());
        staticProps.put("shared_mem_per_block", sd.sharedMemPerBlock());
        return staticProps;
    }
}
