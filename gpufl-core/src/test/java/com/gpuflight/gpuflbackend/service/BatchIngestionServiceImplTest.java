package com.gpuflight.gpuflbackend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gpuflight.gpuflbackend.dao.*;
import com.gpuflight.gpuflbackend.entity.*;
import com.gpuflight.gpuflbackend.model.EventWrapper;
import com.gpuflight.gpuflbackend.model.MetricType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BatchIngestionServiceImplTest {

    @Mock private DictionaryService dictionaryService;
    @Mock private KernelEventDao    kernelEventDao;
    @Mock private MemcpyEventDao    memcpyEventDao;
    @Mock private DeviceMetricDao   deviceMetricDao;
    @Mock private HostMetricDao     hostMetricDao;
    @Mock private ScopeEventDao     scopeEventDao;
    @Mock private ProfileSampleDao  profileSampleDao;

    private BatchIngestionServiceImpl service;

    private static final String SESSION = "test-session";

    @BeforeEach
    void setUp() {
        service = new BatchIngestionServiceImpl(
                new ObjectMapper(),
                dictionaryService,
                kernelEventDao, memcpyEventDao,
                deviceMetricDao, hostMetricDao,
                scopeEventDao, profileSampleDao);
    }

    private EventWrapper wrap(String json) {
        return new EventWrapper(json, System.currentTimeMillis(), "localhost", "127.0.0.1");
    }

    // ── dictionary_update ─────────────────────────────────────────────────────

    @Test
    void handle_dictionaryUpdate_callsMergeDictionary() {
        String json = """
            {"session_id":"%s","kernel_dict":{"1":"vectorAdd"},
             "scope_name_dict":{},"function_dict":{},"metric_dict":{}}
            """.formatted(SESSION);

        service.handle(MetricType.dictionary_update, wrap(json));

        verify(dictionaryService).mergeDictionary(eq(SESSION), any());
    }

    // ── kernel_event_batch ────────────────────────────────────────────────────

    @Test
    void handle_kernelEventBatch_savesMinimalForEachRow() {
        when(dictionaryService.resolveKernel(SESSION, 1)).thenReturn("vectorAdd");
        when(dictionaryService.resolveKernel(SESSION, 2)).thenReturn("matrixMul");

        String json = """
            {"session_id":"%s","batch_id":1,"base_time_ns":1000,
             "columns":["dt_ns","kernel_id","stream_id","duration_ns","corr_id","dyn_shared","num_regs","has_details"],
             "rows":[[0,1,0,1000,101,0,32,1],[2000,2,0,2000,102,1024,64,0]]}
            """.formatted(SESSION);

        service.handle(MetricType.kernel_event_batch, wrap(json));

        verify(kernelEventDao).saveMinimal(SESSION, 1000L, 2000L, 1000L, 0L, "vectorAdd", 101L, true,  0,  32);
        verify(kernelEventDao).saveMinimal(SESSION, 3000L, 5000L, 2000L, 0L, "matrixMul", 102L, false, 1024, 64);
    }

    @Test
    void handle_kernelEventBatch_emptyRows_savesNothing() {
        String json = """
            {"session_id":"%s","batch_id":1,"base_time_ns":0,
             "columns":["dt_ns","kernel_id","stream_id","duration_ns","corr_id","dyn_shared","num_regs","has_details"],
             "rows":[]}
            """.formatted(SESSION);

        service.handle(MetricType.kernel_event_batch, wrap(json));

        verifyNoInteractions(kernelEventDao);
    }

    // ── kernel_detail ─────────────────────────────────────────────────────────

    @Test
    void handle_kernelDetail_callsUpdateDetails() {
        String json = """
            {"session_id":"%s","pid":1234,"app":"test","corr_id":101,
             "grid":"(1,1,1)","block":"(256,1,1)",
             "static_shared":0,"local_bytes":0,"const_bytes":0,
             "occupancy":0.8,"reg_occupancy":0.9,"smem_occupancy":1.0,
             "warp_occupancy":0.8,"block_occupancy":0.8,
             "limiting_resource":"REGISTERS","max_active_blocks":4,
             "local_mem_total_bytes":0,"local_mem_per_thread_bytes":0,
             "cache_config_requested":0,"cache_config_executed":0,
             "shared_mem_executed_bytes":0,
             "user_scope":"global|main","stack_trace":"main"}
            """.formatted(SESSION);

        service.handle(MetricType.kernel_detail, wrap(json));

        verify(kernelEventDao).updateDetails(eq(SESSION), eq(101L), any());
    }

    // ── memcpy_event_batch ────────────────────────────────────────────────────

    @Test
    void handle_memcpyEventBatch_savesBatchWithCorrectFields() {
        String json = """
            {"session_id":"%s","batch_id":1,"base_time_ns":500,
             "columns":["dt_ns","stream_id","duration_ns","bytes","copy_kind","corr_id"],
             "rows":[[0,0,400,4096,1,100]]}
            """.formatted(SESSION);

        service.handle(MetricType.memcpy_event_batch, wrap(json));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<MemcpyEventEntity>> captor = ArgumentCaptor.forClass(List.class);
        verify(memcpyEventDao).saveBatch(captor.capture());

        List<MemcpyEventEntity> saved = captor.getValue();
        assertThat(saved).hasSize(1);
        MemcpyEventEntity e = saved.get(0);
        assertThat(e.getSessionId()).isEqualTo(SESSION);
        assertThat(e.getStartNs()).isEqualTo(500L);
        assertThat(e.getDurationNs()).isEqualTo(400L);
        assertThat(e.getBytes()).isEqualTo(4096L);
        assertThat(e.getCopyKind()).isEqualTo(1);
        assertThat(e.getCorrId()).isEqualTo(100L);
    }

    @Test
    void handle_memcpyEventBatch_emptyRows_doesNotCallDao() {
        String json = """
            {"session_id":"%s","batch_id":1,"base_time_ns":500,
             "columns":["dt_ns","stream_id","duration_ns","bytes","copy_kind","corr_id"],
             "rows":[]}
            """.formatted(SESSION);

        service.handle(MetricType.memcpy_event_batch, wrap(json));

        verifyNoInteractions(memcpyEventDao);
    }

    // ── device_metric_batch ───────────────────────────────────────────────────

    @Test
    void handle_deviceMetricBatch_savesBatchWithCorrectFields() {
        String json = """
            {"session_id":"%s","batch_id":1,"base_time_ns":1000,
             "columns":["dt_ns","device_id","gpu_util","mem_util","temp_c","power_mw","used_mib"],
             "rows":[[0,0,50,30,70,150000,1024],[3000,0,80,40,75,200000,2048]]}
            """.formatted(SESSION);

        service.handle(MetricType.device_metric_batch, wrap(json));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<DeviceMetricEntity>> captor = ArgumentCaptor.forClass(List.class);
        verify(deviceMetricDao).saveBatch(captor.capture());

        List<DeviceMetricEntity> saved = captor.getValue();
        assertThat(saved).hasSize(2);

        DeviceMetricEntity first = saved.get(0);
        assertThat(first.getSessionId()).isEqualTo(SESSION);
        assertThat(first.getTsNs()).isEqualTo(1000L);
        assertThat(first.getDeviceId()).isEqualTo(0);
        assertThat(first.getGpuUtil()).isEqualTo(50);
        assertThat(first.getMemUtil()).isEqualTo(30);
        assertThat(first.getTempC()).isEqualTo(70);
        assertThat(first.getPowerMw()).isEqualTo(150000);
        assertThat(first.getUsedMib()).isEqualTo(1024L);

        DeviceMetricEntity second = saved.get(1);
        assertThat(second.getTsNs()).isEqualTo(4000L);
        assertThat(second.getGpuUtil()).isEqualTo(80);
    }

    // ── host_metric_batch ─────────────────────────────────────────────────────

    @Test
    void handle_hostMetricBatch_dividesCpuPctBy100AndSaves() {
        String json = """
            {"session_id":"%s","batch_id":1,"base_time_ns":1000,
             "columns":["dt_ns","cpu_pct_x100","ram_used_mib","ram_total_mib"],
             "rows":[[0,2500,4096,16384]]}
            """.formatted(SESSION);

        service.handle(MetricType.host_metric_batch, wrap(json));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<HostMetricEntity>> captor = ArgumentCaptor.forClass(List.class);
        verify(hostMetricDao).saveBatch(captor.capture());

        List<HostMetricEntity> saved = captor.getValue();
        assertThat(saved).hasSize(1);
        HostMetricEntity e = saved.get(0);
        assertThat(e.getSessionId()).isEqualTo(SESSION);
        assertThat(e.getTsNs()).isEqualTo(1000L);
        assertThat(e.getCpuPct()).isEqualTo(25.0);
        assertThat(e.getRamUsedMib()).isEqualTo(4096L);
        assertThat(e.getRamTotalMib()).isEqualTo(16384L);
    }

    // ── scope_event_batch ─────────────────────────────────────────────────────

    @Test
    void handle_scopeEventBatch_beginSavesEntity_endCallsUpdate() {
        when(dictionaryService.resolveScopeName(SESSION, 1)).thenReturn("main_loop");

        String json = """
            {"session_id":"%s","batch_id":1,"base_time_ns":500,
             "columns":["dt_ns","scope_instance_id","name_id","event_type","depth"],
             "rows":[[0,1,1,0,0],[5500,1,1,1,0]]}
            """.formatted(SESSION);

        service.handle(MetricType.scope_event_batch, wrap(json));

        ArgumentCaptor<ScopeEventEntity> entityCaptor = ArgumentCaptor.forClass(ScopeEventEntity.class);
        verify(scopeEventDao).saveWithInstanceId(entityCaptor.capture());
        ScopeEventEntity beginEntity = entityCaptor.getValue();
        assertThat(beginEntity.getName()).isEqualTo("main_loop");
        assertThat(beginEntity.getScopeInstanceId()).isEqualTo(1L);
        assertThat(beginEntity.getTsNs()).isEqualTo(500L);

        verify(scopeEventDao).updateEndByInstanceId(SESSION, 1L, 6000L);
    }

    // ── profile_sample_batch ──────────────────────────────────────────────────

    @Test
    void handle_profileSampleBatch_resolvesNamesAndSaves() {
        when(dictionaryService.resolveFunction(SESSION, 2)).thenReturn("myKernel");
        when(dictionaryService.resolveMetric(SESSION, 1)).thenReturn("smsp__sass_inst_executed");
        when(dictionaryService.resolveScopeName(SESSION, 3)).thenReturn("main_loop");

        // columns: dt_ns, corr_id, device_id, function_id, pc_offset, metric_id, metric_value,
        //          stall_reason, sample_kind, scope_name_id
        String json = """
            {"session_id":"%s","batch_id":1,"base_time_ns":0,
             "columns":["dt_ns","corr_id","device_id","function_id","pc_offset","metric_id",
                        "metric_value","stall_reason","sample_kind","scope_name_id"],
             "rows":[[0,101,0,2,1024,1,500,0,1,3]]}
            """.formatted(SESSION);

        service.handle(MetricType.profile_sample_batch, wrap(json));

        ArgumentCaptor<ProfileSampleEntity> captor = ArgumentCaptor.forClass(ProfileSampleEntity.class);
        verify(profileSampleDao).save(captor.capture());

        ProfileSampleEntity saved = captor.getValue();
        assertThat(saved.getSessionId()).isEqualTo(SESSION);
        assertThat(saved.getFunctionName()).isEqualTo("myKernel");
        assertThat(saved.getMetricName()).isEqualTo("smsp__sass_inst_executed");
        assertThat(saved.getScopeName()).isEqualTo("main_loop");
        assertThat(saved.getMetricValue()).isEqualTo(500L);
        assertThat(saved.getSampleKind()).isEqualTo("sass_metric");
        assertThat(saved.getPcOffset()).isEqualTo(1024);
        assertThat(saved.getStallReason()).isNull(); // stall_reason=0 → null
    }

    @Test
    void handle_profileSampleBatch_pcSamplingKind_setsCorrectSampleKind() {
        when(dictionaryService.resolveFunction(SESSION, 1)).thenReturn("myFunc");
        when(dictionaryService.resolveMetric(SESSION, 0)).thenReturn(null);
        when(dictionaryService.resolveScopeName(SESSION, 0)).thenReturn(null);

        // sample_kind=0 → pc_sampling; stall_reason=4 → present; pc_offset=0 → null
        String json = """
            {"session_id":"%s","batch_id":1,"base_time_ns":0,
             "columns":["dt_ns","corr_id","device_id","function_id","pc_offset","metric_id",
                        "metric_value","stall_reason","sample_kind","scope_name_id"],
             "rows":[[0,201,0,1,0,0,1,4,0,0]]}
            """.formatted(SESSION);

        service.handle(MetricType.profile_sample_batch, wrap(json));

        ArgumentCaptor<ProfileSampleEntity> captor = ArgumentCaptor.forClass(ProfileSampleEntity.class);
        verify(profileSampleDao).save(captor.capture());

        ProfileSampleEntity saved = captor.getValue();
        assertThat(saved.getSampleKind()).isEqualTo("pc_sampling");
        assertThat(saved.getStallReason()).isEqualTo(4);
        assertThat(saved.getPcOffset()).isNull(); // 0 → null
    }

    // ── error handling ────────────────────────────────────────────────────────

    @Test
    void handle_invalidJson_doesNotThrow() {
        // Should log an error and swallow the exception
        service.handle(MetricType.kernel_event_batch, wrap("not-json"));

        verifyNoInteractions(kernelEventDao);
    }

    @Test
    void handle_unknownType_doesNotThrow() {
        // switch default branch — just logs a warning
        service.handle(MetricType.shutdown, wrap("{}"));

        verifyNoInteractions(kernelEventDao, memcpyEventDao, deviceMetricDao,
                hostMetricDao, scopeEventDao, profileSampleDao, dictionaryService);
    }
}
