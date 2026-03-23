package com.gpuflight.gpuflbackend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gpuflight.gpuflbackend.dao.*;
import com.gpuflight.gpuflbackend.entity.*;
import com.gpuflight.gpuflbackend.model.EventWrapper;
import com.gpuflight.gpuflbackend.model.MetricType;
import com.gpuflight.gpuflbackend.model.input.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static com.gpuflight.gpuflbackend.util.TimeUtils.epochToInstant;

@Service
@Slf4j
public class BatchIngestionServiceImpl implements BatchIngestionService {
    private final ObjectMapper objectMapper;
    private final DictionaryService dictionaryService;
    private final KernelEventDao kernelEventDao;
    private final MemcpyEventDao memcpyEventDao;
    private final DeviceMetricDao deviceMetricDao;
    private final HostMetricDao hostMetricDao;
    private final ScopeEventDao scopeEventDao;
    private final ProfileSampleDao profileSampleDao;

    public BatchIngestionServiceImpl(
            @Qualifier("ingestionObjectMapper") ObjectMapper objectMapper,
            DictionaryService dictionaryService,
            KernelEventDao kernelEventDao,
            MemcpyEventDao memcpyEventDao,
            DeviceMetricDao deviceMetricDao,
            HostMetricDao hostMetricDao,
            ScopeEventDao scopeEventDao,
            ProfileSampleDao profileSampleDao) {
        this.objectMapper = objectMapper;
        this.dictionaryService = dictionaryService;
        this.kernelEventDao = kernelEventDao;
        this.memcpyEventDao = memcpyEventDao;
        this.deviceMetricDao = deviceMetricDao;
        this.hostMetricDao = hostMetricDao;
        this.scopeEventDao = scopeEventDao;
        this.profileSampleDao = profileSampleDao;
    }

    @Override
    public void handle(MetricType type, EventWrapper wrapper) {
        try {
            switch (type) {
                case dictionary_update    -> handleDictionary(wrapper);
                case kernel_event_batch   -> handleKernelBatch(wrapper);
                case kernel_detail        -> handleKernelDetail(wrapper);
                case memcpy_event_batch   -> handleMemcpyBatch(wrapper);
                case device_metric_batch  -> handleDeviceBatch(wrapper);
                case host_metric_batch    -> handleHostBatch(wrapper);
                case scope_event_batch    -> handleScopeBatch(wrapper);
                case profile_sample_batch -> handleProfileBatch(wrapper);
                default -> log.warn("BatchIngestionService: unhandled type {}", type);
            }
        } catch (Exception e) {
            log.error("Failed to handle batch event type {}: {}", type, e.getMessage(), e);
        }
    }

    private void handleDictionary(EventWrapper w) throws Exception {
        DictionaryUpdateEvent event = objectMapper.readValue(w.data(), DictionaryUpdateEvent.class);
        dictionaryService.mergeDictionary(event.sessionId(), event);
    }

    private void handleKernelBatch(EventWrapper w) throws Exception {
        KernelEventBatch batch = objectMapper.readValue(w.data(), KernelEventBatch.class);
        // columns: ["dt_ns","kernel_id","stream_id","duration_ns","corr_id","dyn_shared","num_regs","has_details"]
        for (List<Number> row : batch.rows()) {
            long startNs    = batch.baseTimeNs() + row.get(0).longValue();
            int  kernelId   = row.get(1).intValue();
            long streamId   = row.get(2).longValue();
            long durationNs = row.get(3).longValue();
            long corrId     = row.get(4).longValue();
            int  dynShared  = row.get(5).intValue();
            int  numRegs    = row.get(6).intValue();
            boolean hasDetails = row.get(7).intValue() == 1;

            String name = dictionaryService.resolveKernel(batch.sessionId(), kernelId);
            kernelEventDao.saveMinimal(batch.sessionId(), startNs, startNs + durationNs, durationNs,
                streamId, name, corrId, hasDetails, dynShared, numRegs);
        }
    }

    private void handleKernelDetail(EventWrapper w) throws Exception {
        KernelDetailEvent detail = objectMapper.readValue(w.data(), KernelDetailEvent.class);
        kernelEventDao.updateDetails(detail.sessionId(), detail.corrId(), detail);
    }

    private void handleMemcpyBatch(EventWrapper w) throws Exception {
        MemcpyEventBatch batch = objectMapper.readValue(w.data(), MemcpyEventBatch.class);
        // columns: ["dt_ns","stream_id","duration_ns","bytes","copy_kind","corr_id"]
        List<MemcpyEventEntity> entities = new ArrayList<>();
        for (List<Number> row : batch.rows()) {
            long startNs    = batch.baseTimeNs() + row.get(0).longValue();
            long streamId   = row.get(1).longValue();
            long durationNs = row.get(2).longValue();
            long bytes      = row.get(3).longValue();
            int  copyKind   = row.get(4).intValue();
            long corrId     = row.get(5).longValue();
            entities.add(MemcpyEventEntity.builder()
                .sessionId(batch.sessionId()).startNs(startNs)
                .durationNs(durationNs).streamId(streamId)
                .bytes(bytes).copyKind(copyKind).corrId(corrId).build());
        }
        if (!entities.isEmpty()) memcpyEventDao.saveBatch(entities);
    }

    private void handleDeviceBatch(EventWrapper w) throws Exception {
        DeviceMetricBatch batch = objectMapper.readValue(w.data(), DeviceMetricBatch.class);
        // columns: ["dt_ns","device_id","gpu_util","mem_util","temp_c","power_mw","used_mib"]
        List<DeviceMetricEntity> entities = new ArrayList<>();
        for (List<Number> row : batch.rows()) {
            long tsNs    = batch.baseTimeNs() + row.get(0).longValue();
            int deviceId = row.get(1).intValue();
            int gpuUtil  = row.get(2).intValue();
            int memUtil  = row.get(3).intValue();
            int tempC    = row.get(4).intValue();
            int powerMw  = row.get(5).intValue();
            long usedMib = row.get(6).longValue();
            entities.add(DeviceMetricEntity.builder()
                .time(epochToInstant(tsNs)).sessionId(batch.sessionId())
                .tsNs(tsNs).deviceId(deviceId).gpuUtil(gpuUtil)
                .memUtil(memUtil).tempC(tempC).powerMw(powerMw).usedMib(usedMib).build());
        }
        if (!entities.isEmpty()) deviceMetricDao.saveBatch(entities);
    }

    private void handleHostBatch(EventWrapper w) throws Exception {
        HostMetricBatch batch = objectMapper.readValue(w.data(), HostMetricBatch.class);
        // columns: ["dt_ns","cpu_pct_x100","ram_used_mib","ram_total_mib"]
        List<HostMetricEntity> entities = new ArrayList<>();
        for (List<Number> row : batch.rows()) {
            long tsNs     = batch.baseTimeNs() + row.get(0).longValue();
            double cpuPct = row.get(1).longValue() / 100.0;
            long ramUsed  = row.get(2).longValue();
            long ramTotal = row.get(3).longValue();
            entities.add(HostMetricEntity.builder()
                .time(epochToInstant(tsNs)).sessionId(batch.sessionId())
                .tsNs(tsNs).cpuPct(cpuPct).ramUsedMib(ramUsed).ramTotalMib(ramTotal).build());
        }
        if (!entities.isEmpty()) hostMetricDao.saveBatch(entities);
    }

    private void handleScopeBatch(EventWrapper w) throws Exception {
        ScopeEventBatch batch = objectMapper.readValue(w.data(), ScopeEventBatch.class);
        // columns: ["dt_ns","scope_instance_id","name_id","event_type","depth"]
        for (List<Number> row : batch.rows()) {
            long tsNs            = batch.baseTimeNs() + row.get(0).longValue();
            long scopeInstanceId = row.get(1).longValue();
            int  nameId          = row.get(2).intValue();
            int  eventType       = row.get(3).intValue(); // 0=begin, 1=end
            int  depth           = row.get(4).intValue();

            String name = dictionaryService.resolveScopeName(batch.sessionId(), nameId);

            if (eventType == 0) {
                // begin
                ScopeEventEntity entity = ScopeEventEntity.builder()
                    .time(epochToInstant(tsNs))
                    .tsNs(tsNs)
                    .sessionId(batch.sessionId())
                    .name(name)
                    .scopeDepth(depth)
                    .scopeInstanceId(scopeInstanceId)
                    .build();
                scopeEventDao.saveWithInstanceId(entity);
            } else {
                // end
                scopeEventDao.updateEndByInstanceId(batch.sessionId(), scopeInstanceId, tsNs);
            }
        }
    }

    private void handleProfileBatch(EventWrapper w) throws Exception {
        ProfileSampleBatch batch = objectMapper.readValue(w.data(), ProfileSampleBatch.class);
        // columns: ["dt_ns","corr_id","device_id","function_id","pc_offset","metric_id",
        //           "metric_value","stall_reason","sample_kind","scope_name_id"]
        for (List<Number> row : batch.rows()) {
            int  deviceId    = row.get(2).intValue();
            int  functionId  = row.get(3).intValue();
            int  pcOffset    = row.get(4).intValue();
            int  metricId    = row.get(5).intValue();
            long metricValue = row.get(6).longValue();
            int  stallReason = row.get(7).intValue();
            int  sampleKind  = row.get(8).intValue();
            int  scopeNameId = row.get(9).intValue();

            String functionName = dictionaryService.resolveFunction(batch.sessionId(), functionId);
            String metricName   = dictionaryService.resolveMetric(batch.sessionId(), metricId);
            String scopeName    = dictionaryService.resolveScopeName(batch.sessionId(), scopeNameId);
            String kindStr      = sampleKind == 1 ? "sass_metric" : "pc_sampling";

            profileSampleDao.save(ProfileSampleEntity.builder()
                .sessionId(batch.sessionId())
                .scopeName(scopeName)
                .deviceId(deviceId)
                .sampleKind(kindStr)
                .functionName(functionName)
                .pcOffset(pcOffset > 0 ? pcOffset : null)
                .metricName(metricName)
                .metricValue(metricValue)
                .stallReason(stallReason > 0 ? stallReason : null)
                .build());
        }
    }
}
