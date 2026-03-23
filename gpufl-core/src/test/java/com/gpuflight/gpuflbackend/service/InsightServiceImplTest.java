package com.gpuflight.gpuflbackend.service;

import com.gpuflight.gpuflbackend.dao.DeviceMetricDao;
import com.gpuflight.gpuflbackend.dao.KernelEventDao;
import com.gpuflight.gpuflbackend.dao.ProfileSampleDao;
import com.gpuflight.gpuflbackend.entity.DeviceMetricEntity;
import com.gpuflight.gpuflbackend.entity.KernelEventEntity;
import com.gpuflight.gpuflbackend.entity.ProfileSampleEntity;
import com.gpuflight.gpuflbackend.model.InsightDto;
import com.gpuflight.gpuflbackend.model.SessionInsightsDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InsightServiceImplTest {

    @Mock private KernelEventDao kernelEventDao;
    @Mock private ProfileSampleDao profileSampleDao;
    @Mock private DeviceMetricDao deviceMetricDao;

    private InsightServiceImpl service;

    private static final String SESSION_ID = "test-session";

    // CUPTI stall reason codes used across tests
    private static final int STALL_EXEC_DEP  = 3;   // Execution Dependency
    private static final int STALL_MEM_DEP   = 4;   // Memory Dependency (memory stall → HIGH)
    private static final int STALL_TEXTURE   = 5;   // Texture            (memory stall → HIGH)
    private static final int STALL_SYNC      = 6;   // Sync
    private static final int STALL_CONST     = 7;   // Constant Memory    (memory stall → HIGH)
    private static final int STALL_PIPE_BUSY = 8;   // Pipe Busy
    private static final int STALL_BRANCH    = 10;  // Branch Resolving
    private static final int STALL_WAIT      = 11;  // Wait
    private static final int STALL_BARRIER   = 12;  // Barrier
    private static final int STALL_UNKNOWN   = 99;  // Unknown (falls to default advice)

    @BeforeEach
    void setUp() {
        service = new InsightServiceImpl(kernelEventDao, profileSampleDao, deviceMetricDao);
    }

    // ── Kernel analysis ────────────────────────────────────────────────────────

    @Test
    void lowOccupancy_limitedByWarps_generatesHighInsight() {
        KernelEventEntity kernel = KernelEventEntity.builder()
                .name("myKernel")
                .occupancy(new BigDecimal("0.30"))
                .limitingResource("warps")
                .localMemTotalBytes(0L)
                .numRegs(10)
                .build();
        when(kernelEventDao.findBySessionIds(anyList())).thenReturn(List.of(kernel));
        when(profileSampleDao.findBySessionId(SESSION_ID)).thenReturn(List.of());
        when(deviceMetricDao.findBySessionIds(anyList())).thenReturn(List.of());

        SessionInsightsDto result = service.getInsights(SESSION_ID);

        assertThat(result.getInsights()).hasSize(1);
        InsightDto insight = result.getInsights().get(0);
        assertThat(insight.getSeverity()).isEqualTo("HIGH");
        assertThat(insight.getCategory()).isEqualTo("OCCUPANCY");
        assertThat(insight.getTitle()).contains("warps");
        assertThat(insight.getMessage()).contains("divergent");
    }

    @Test
    void lowOccupancy_limitedByRegisters_givesRegisterAdvice() {
        KernelEventEntity kernel = KernelEventEntity.builder()
                .name("myKernel")
                .occupancy(new BigDecimal("0.40"))
                .limitingResource("registers")
                .localMemTotalBytes(0L)
                .numRegs(10)
                .build();
        when(kernelEventDao.findBySessionIds(anyList())).thenReturn(List.of(kernel));
        when(profileSampleDao.findBySessionId(SESSION_ID)).thenReturn(List.of());
        when(deviceMetricDao.findBySessionIds(anyList())).thenReturn(List.of());

        SessionInsightsDto result = service.getInsights(SESSION_ID);

        assertThat(result.getInsights()).hasSize(1);
        assertThat(result.getInsights().get(0).getMessage()).contains("launch_bounds");
    }

    @Test
    void lowOccupancy_limitedBySharedMemory_givesSharedMemAdvice() {
        KernelEventEntity kernel = KernelEventEntity.builder()
                .name("myKernel")
                .occupancy(new BigDecimal("0.20"))
                .limitingResource("shared_memory")
                .localMemTotalBytes(0L)
                .numRegs(10)
                .build();
        when(kernelEventDao.findBySessionIds(anyList())).thenReturn(List.of(kernel));
        when(profileSampleDao.findBySessionId(SESSION_ID)).thenReturn(List.of());
        when(deviceMetricDao.findBySessionIds(anyList())).thenReturn(List.of());

        SessionInsightsDto result = service.getInsights(SESSION_ID);

        assertThat(result.getInsights().get(0).getMessage()).containsIgnoringCase("shared memory");
    }

    @Test
    void lowOccupancy_limitedByBlocks_givesBlocksAdvice() {
        KernelEventEntity kernel = KernelEventEntity.builder()
                .name("myKernel")
                .occupancy(new BigDecimal("0.10"))
                .limitingResource("blocks")
                .localMemTotalBytes(0L)
                .numRegs(10)
                .build();
        when(kernelEventDao.findBySessionIds(anyList())).thenReturn(List.of(kernel));
        when(profileSampleDao.findBySessionId(SESSION_ID)).thenReturn(List.of());
        when(deviceMetricDao.findBySessionIds(anyList())).thenReturn(List.of());

        SessionInsightsDto result = service.getInsights(SESSION_ID);

        assertThat(result.getInsights().get(0).getMessage()).containsIgnoringCase("blocks");
    }

    @Test
    void lowOccupancy_unknownLimitingResource_givesGenericAdvice() {
        KernelEventEntity kernel = KernelEventEntity.builder()
                .name("myKernel")
                .occupancy(new BigDecimal("0.30"))
                .limitingResource("unknown_resource")
                .localMemTotalBytes(0L)
                .numRegs(10)
                .build();
        when(kernelEventDao.findBySessionIds(anyList())).thenReturn(List.of(kernel));
        when(profileSampleDao.findBySessionId(SESSION_ID)).thenReturn(List.of());
        when(deviceMetricDao.findBySessionIds(anyList())).thenReturn(List.of());

        SessionInsightsDto result = service.getInsights(SESSION_ID);

        assertThat(result.getInsights().get(0).getMessage()).containsIgnoringCase("NSight");
    }

    @Test
    void highOccupancy_noInsightsGenerated() {
        KernelEventEntity kernel = KernelEventEntity.builder()
                .name("myKernel")
                .occupancy(new BigDecimal("0.90"))
                .limitingResource("warps")
                .localMemTotalBytes(0L)
                .numRegs(10)
                .build();
        when(kernelEventDao.findBySessionIds(anyList())).thenReturn(List.of(kernel));
        when(profileSampleDao.findBySessionId(SESSION_ID)).thenReturn(List.of());
        when(deviceMetricDao.findBySessionIds(anyList())).thenReturn(List.of());

        SessionInsightsDto result = service.getInsights(SESSION_ID);

        assertThat(result.getInsights()).isEmpty();
    }

    @Test
    void localMemSpill_generatesMediumMemoryInsight() {
        KernelEventEntity kernel = KernelEventEntity.builder()
                .name("spillKernel")
                .occupancy(new BigDecimal("1.0"))
                .limitingResource("warps")
                .localMemTotalBytes(1024L)
                .numRegs(10)
                .build();
        when(kernelEventDao.findBySessionIds(anyList())).thenReturn(List.of(kernel));
        when(profileSampleDao.findBySessionId(SESSION_ID)).thenReturn(List.of());
        when(deviceMetricDao.findBySessionIds(anyList())).thenReturn(List.of());

        SessionInsightsDto result = service.getInsights(SESSION_ID);

        assertThat(result.getInsights()).hasSize(1);
        InsightDto insight = result.getInsights().get(0);
        assertThat(insight.getSeverity()).isEqualTo("MEDIUM");
        assertThat(insight.getCategory()).isEqualTo("MEMORY");
        assertThat(insight.getMessage()).contains("1024");
    }

    @Test
    void registerPressure_whenRegLimited_generatesMediumInsight() {
        KernelEventEntity kernel = KernelEventEntity.builder()
                .name("regKernel")
                .occupancy(new BigDecimal("1.0"))
                .limitingResource("registers")
                .localMemTotalBytes(0L)
                .numRegs(40)
                .regOccupancy(new BigDecimal("0.50"))
                .build();
        when(kernelEventDao.findBySessionIds(anyList())).thenReturn(List.of(kernel));
        when(profileSampleDao.findBySessionId(SESSION_ID)).thenReturn(List.of());
        when(deviceMetricDao.findBySessionIds(anyList())).thenReturn(List.of());

        SessionInsightsDto result = service.getInsights(SESSION_ID);

        assertThat(result.getInsights()).hasSize(1);
        InsightDto insight = result.getInsights().get(0);
        assertThat(insight.getSeverity()).isEqualTo("MEDIUM");
        assertThat(insight.getCategory()).isEqualTo("OCCUPANCY");
        assertThat(insight.getMessage()).contains("40");
    }

    @Test
    void registerPressure_whenNotRegLimited_noInsight() {
        KernelEventEntity kernel = KernelEventEntity.builder()
                .name("divergeKernel")
                .occupancy(new BigDecimal("1.0"))
                .limitingResource("warps")  // not registers
                .localMemTotalBytes(0L)
                .numRegs(40)
                .regOccupancy(new BigDecimal("0.50"))
                .build();
        when(kernelEventDao.findBySessionIds(anyList())).thenReturn(List.of(kernel));
        when(profileSampleDao.findBySessionId(SESSION_ID)).thenReturn(List.of());
        when(deviceMetricDao.findBySessionIds(anyList())).thenReturn(List.of());

        SessionInsightsDto result = service.getInsights(SESSION_ID);

        assertThat(result.getInsights()).isEmpty();
    }

    // ── PC sampling stall analysis ─────────────────────────────────────────────
    // stall reasons are CUPTI integer codes; metricValue is the sample count.

    @Test
    void memoryStall_dominant_generatesHighInsight() {
        // STALL_MEM_DEP (4) is in MEMORY_STALL_IDS → severity HIGH
        ProfileSampleEntity stall = ProfileSampleEntity.builder()
                .sampleKind("pc_sampling")
                .stallReason(STALL_MEM_DEP)
                .metricValue(80)
                .build();
        ProfileSampleEntity other = ProfileSampleEntity.builder()
                .sampleKind("pc_sampling")
                .stallReason(STALL_EXEC_DEP)
                .metricValue(20)
                .build();
        when(kernelEventDao.findBySessionIds(anyList())).thenReturn(List.of());
        when(profileSampleDao.findBySessionId(SESSION_ID)).thenReturn(List.of(stall, other));
        when(deviceMetricDao.findBySessionIds(anyList())).thenReturn(List.of());

        SessionInsightsDto result = service.getInsights(SESSION_ID);

        assertThat(result.getInsights()).hasSize(1);
        InsightDto insight = result.getInsights().get(0);
        assertThat(insight.getSeverity()).isEqualTo("HIGH");
        assertThat(insight.getCategory()).isEqualTo("STALL");
        assertThat(insight.getMessage()).containsIgnoringCase("coalesced");
    }

    @Test
    void branchResolvingStall_dominant_showsReasonInMessage() {
        // STALL_BRANCH (10) is not a memory stall → severity MEDIUM; falls to default advice
        ProfileSampleEntity stall = ProfileSampleEntity.builder()
                .sampleKind("pc_sampling")
                .stallReason(STALL_BRANCH)
                .metricValue(90)
                .build();
        ProfileSampleEntity other = ProfileSampleEntity.builder()
                .sampleKind("pc_sampling")
                .stallReason(STALL_EXEC_DEP)
                .metricValue(10)
                .build();
        when(kernelEventDao.findBySessionIds(anyList())).thenReturn(List.of());
        when(profileSampleDao.findBySessionId(SESSION_ID)).thenReturn(List.of(stall, other));
        when(deviceMetricDao.findBySessionIds(anyList())).thenReturn(List.of());

        SessionInsightsDto result = service.getInsights(SESSION_ID);

        assertThat(result.getInsights()).hasSize(1);
        InsightDto insight = result.getInsights().get(0);
        assertThat(insight.getSeverity()).isEqualTo("MEDIUM");
        assertThat(insight.getMessage()).containsIgnoringCase("Branch Resolving");
    }

    @Test
    void syncStall_dominant_givesSyncAdvice() {
        ProfileSampleEntity stall = ProfileSampleEntity.builder()
                .sampleKind("pc_sampling")
                .stallReason(STALL_SYNC)
                .metricValue(90)
                .build();
        ProfileSampleEntity other = ProfileSampleEntity.builder()
                .sampleKind("pc_sampling")
                .stallReason(STALL_EXEC_DEP)
                .metricValue(10)
                .build();
        when(kernelEventDao.findBySessionIds(anyList())).thenReturn(List.of());
        when(profileSampleDao.findBySessionId(SESSION_ID)).thenReturn(List.of(stall, other));
        when(deviceMetricDao.findBySessionIds(anyList())).thenReturn(List.of());

        SessionInsightsDto result = service.getInsights(SESSION_ID);

        assertThat(result.getInsights()).hasSize(1);
        assertThat(result.getInsights().get(0).getMessage()).containsIgnoringCase("syncthreads");
    }

    @Test
    void pipeBusyStall_dominant_givesMixedPrecisionAdvice() {
        // STALL_PIPE_BUSY (8) — advice mentions "mixed precision"
        ProfileSampleEntity stall = ProfileSampleEntity.builder()
                .sampleKind("pc_sampling")
                .stallReason(STALL_PIPE_BUSY)
                .metricValue(90)
                .build();
        ProfileSampleEntity other = ProfileSampleEntity.builder()
                .sampleKind("pc_sampling")
                .stallReason(STALL_WAIT)
                .metricValue(10)
                .build();
        when(kernelEventDao.findBySessionIds(anyList())).thenReturn(List.of());
        when(profileSampleDao.findBySessionId(SESSION_ID)).thenReturn(List.of(stall, other));
        when(deviceMetricDao.findBySessionIds(anyList())).thenReturn(List.of());

        SessionInsightsDto result = service.getInsights(SESSION_ID);

        assertThat(result.getInsights()).hasSize(1);
        assertThat(result.getInsights().get(0).getMessage()).containsIgnoringCase("mixed precision");
    }

    @Test
    void textureStall_dominant_givesTextureAdvice() {
        // STALL_TEXTURE (5) is in MEMORY_STALL_IDS → HIGH; advice mentions "texture"
        ProfileSampleEntity stall = ProfileSampleEntity.builder()
                .sampleKind("pc_sampling")
                .stallReason(STALL_TEXTURE)
                .metricValue(90)
                .build();
        ProfileSampleEntity other = ProfileSampleEntity.builder()
                .sampleKind("pc_sampling")
                .stallReason(STALL_WAIT)
                .metricValue(10)
                .build();
        when(kernelEventDao.findBySessionIds(anyList())).thenReturn(List.of());
        when(profileSampleDao.findBySessionId(SESSION_ID)).thenReturn(List.of(stall, other));
        when(deviceMetricDao.findBySessionIds(anyList())).thenReturn(List.of());

        SessionInsightsDto result = service.getInsights(SESSION_ID);

        assertThat(result.getInsights()).hasSize(1);
        assertThat(result.getInsights().get(0).getMessage()).containsIgnoringCase("texture");
    }

    @Test
    void constantMemoryStall_dominant_givesConstantAdvice() {
        // STALL_CONST (7) is in MEMORY_STALL_IDS → HIGH; advice mentions "constant memory"
        ProfileSampleEntity stall = ProfileSampleEntity.builder()
                .sampleKind("pc_sampling")
                .stallReason(STALL_CONST)
                .metricValue(90)
                .build();
        ProfileSampleEntity other = ProfileSampleEntity.builder()
                .sampleKind("pc_sampling")
                .stallReason(STALL_WAIT)
                .metricValue(10)
                .build();
        when(kernelEventDao.findBySessionIds(anyList())).thenReturn(List.of());
        when(profileSampleDao.findBySessionId(SESSION_ID)).thenReturn(List.of(stall, other));
        when(deviceMetricDao.findBySessionIds(anyList())).thenReturn(List.of());

        SessionInsightsDto result = service.getInsights(SESSION_ID);

        assertThat(result.getInsights()).hasSize(1);
        assertThat(result.getInsights().get(0).getMessage()).containsIgnoringCase("constant memory");
    }

    @Test
    void unknownStall_dominant_givesGenericAdvice() {
        ProfileSampleEntity stall = ProfileSampleEntity.builder()
                .sampleKind("pc_sampling")
                .stallReason(STALL_UNKNOWN)
                .metricValue(90)
                .build();
        ProfileSampleEntity other = ProfileSampleEntity.builder()
                .sampleKind("pc_sampling")
                .stallReason(STALL_WAIT)
                .metricValue(10)
                .build();
        when(kernelEventDao.findBySessionIds(anyList())).thenReturn(List.of());
        when(profileSampleDao.findBySessionId(SESSION_ID)).thenReturn(List.of(stall, other));
        when(deviceMetricDao.findBySessionIds(anyList())).thenReturn(List.of());

        SessionInsightsDto result = service.getInsights(SESSION_ID);

        assertThat(result.getInsights()).hasSize(1);
        assertThat(result.getInsights().get(0).getMessage()).containsIgnoringCase("NSight");
    }

    @Test
    void stall_belowThreshold_noInsight() {
        // Three reasons each at ~33% — none exceeds the 40% threshold
        ProfileSampleEntity s1 = ProfileSampleEntity.builder()
                .sampleKind("pc_sampling")
                .stallReason(STALL_MEM_DEP)
                .metricValue(33)
                .build();
        ProfileSampleEntity s2 = ProfileSampleEntity.builder()
                .sampleKind("pc_sampling")
                .stallReason(STALL_EXEC_DEP)
                .metricValue(33)
                .build();
        ProfileSampleEntity s3 = ProfileSampleEntity.builder()
                .sampleKind("pc_sampling")
                .stallReason(STALL_WAIT)
                .metricValue(34)
                .build();
        when(kernelEventDao.findBySessionIds(anyList())).thenReturn(List.of());
        when(profileSampleDao.findBySessionId(SESSION_ID)).thenReturn(List.of(s1, s2, s3));
        when(deviceMetricDao.findBySessionIds(anyList())).thenReturn(List.of());

        assertThat(service.getInsights(SESSION_ID).getInsights()).isEmpty();
    }

    // ── SASS divergence analysis ───────────────────────────────────────────────
    // Each kernel requires two separate records: one for smsp__sass_inst_executed
    // and one for smsp__sass_thread_inst_executed.

    @Test
    void sassDivergence_low_efficiency_generatesHighInsight() {
        // 25% thread efficiency: 800 / (100 * 32) = 0.25  →  HIGH (< 50%)
        ProfileSampleEntity inst = ProfileSampleEntity.builder()
                .sampleKind("sass_metric")
                .functionName("branchKernel")
                .metricName("smsp__sass_inst_executed")
                .metricValue(100)
                .build();
        ProfileSampleEntity threadInst = ProfileSampleEntity.builder()
                .sampleKind("sass_metric")
                .functionName("branchKernel")
                .metricName("smsp__sass_thread_inst_executed")
                .metricValue(800)
                .build();
        when(kernelEventDao.findBySessionIds(anyList())).thenReturn(List.of());
        when(profileSampleDao.findBySessionId(SESSION_ID)).thenReturn(List.of(inst, threadInst));
        when(deviceMetricDao.findBySessionIds(anyList())).thenReturn(List.of());

        SessionInsightsDto result = service.getInsights(SESSION_ID);

        assertThat(result.getInsights()).hasSize(1);
        InsightDto insight = result.getInsights().get(0);
        assertThat(insight.getSeverity()).isEqualTo("HIGH");
        assertThat(insight.getCategory()).isEqualTo("DIVERGENCE");
        assertThat(insight.getMessage()).containsIgnoringCase("25%");
    }

    @Test
    void sassDivergence_medium_efficiency_generatesMediumInsight() {
        // 65% thread efficiency: 2080 / (100 * 32) = 0.65  →  MEDIUM (≥ 50%, < 75%)
        ProfileSampleEntity inst = ProfileSampleEntity.builder()
                .sampleKind("sass_metric")
                .functionName("branchKernel")
                .metricName("smsp__sass_inst_executed")
                .metricValue(100)
                .build();
        ProfileSampleEntity threadInst = ProfileSampleEntity.builder()
                .sampleKind("sass_metric")
                .functionName("branchKernel")
                .metricName("smsp__sass_thread_inst_executed")
                .metricValue(2080)
                .build();
        when(kernelEventDao.findBySessionIds(anyList())).thenReturn(List.of());
        when(profileSampleDao.findBySessionId(SESSION_ID)).thenReturn(List.of(inst, threadInst));
        when(deviceMetricDao.findBySessionIds(anyList())).thenReturn(List.of());

        SessionInsightsDto result = service.getInsights(SESSION_ID);

        assertThat(result.getInsights()).hasSize(1);
        assertThat(result.getInsights().get(0).getSeverity()).isEqualTo("MEDIUM");
    }

    @Test
    void sassDivergence_high_efficiency_noInsight() {
        // 95% thread efficiency: 3040 / (100 * 32) = 0.95  →  no insight
        ProfileSampleEntity inst = ProfileSampleEntity.builder()
                .sampleKind("sass_metric")
                .functionName("goodKernel")
                .metricName("smsp__sass_inst_executed")
                .metricValue(100)
                .build();
        ProfileSampleEntity threadInst = ProfileSampleEntity.builder()
                .sampleKind("sass_metric")
                .functionName("goodKernel")
                .metricName("smsp__sass_thread_inst_executed")
                .metricValue(3040)
                .build();
        when(kernelEventDao.findBySessionIds(anyList())).thenReturn(List.of());
        when(profileSampleDao.findBySessionId(SESSION_ID)).thenReturn(List.of(inst, threadInst));
        when(deviceMetricDao.findBySessionIds(anyList())).thenReturn(List.of());

        assertThat(service.getInsights(SESSION_ID).getInsights()).isEmpty();
    }

    @Test
    void sassDivergence_zeroInstExecuted_skipped() {
        ProfileSampleEntity inst = ProfileSampleEntity.builder()
                .sampleKind("sass_metric")
                .functionName("emptyKernel")
                .metricName("smsp__sass_inst_executed")
                .metricValue(0)
                .build();
        ProfileSampleEntity threadInst = ProfileSampleEntity.builder()
                .sampleKind("sass_metric")
                .functionName("emptyKernel")
                .metricName("smsp__sass_thread_inst_executed")
                .metricValue(0)
                .build();
        when(kernelEventDao.findBySessionIds(anyList())).thenReturn(List.of());
        when(profileSampleDao.findBySessionId(SESSION_ID)).thenReturn(List.of(inst, threadInst));
        when(deviceMetricDao.findBySessionIds(anyList())).thenReturn(List.of());

        assertThat(service.getInsights(SESSION_ID).getInsights()).isEmpty();
    }

    // ── Device metric temperature analysis ────────────────────────────────────
    // The new schema has no throttlePwr/throttleTherm columns.
    // High sustained temperature (avgTempC >= 85) is used as the throttling proxy.

    @Test
    void highTemperature_generatesHighThrottlingInsight() {
        DeviceMetricEntity metric = DeviceMetricEntity.builder()
                .tempC(90)
                .build();
        when(kernelEventDao.findBySessionIds(anyList())).thenReturn(List.of());
        when(profileSampleDao.findBySessionId(SESSION_ID)).thenReturn(List.of());
        when(deviceMetricDao.findBySessionIds(anyList())).thenReturn(List.of(metric));

        SessionInsightsDto result = service.getInsights(SESSION_ID);

        assertThat(result.getInsights()).hasSize(1);
        InsightDto insight = result.getInsights().get(0);
        assertThat(insight.getSeverity()).isEqualTo("HIGH");
        assertThat(insight.getCategory()).isEqualTo("THROTTLING");
        assertThat(insight.getTitle()).containsIgnoringCase("temperature");
    }

    @Test
    void multipleHighTempSamples_averageAboveThreshold_generatesInsight() {
        DeviceMetricEntity m1 = DeviceMetricEntity.builder().tempC(88).build();
        DeviceMetricEntity m2 = DeviceMetricEntity.builder().tempC(92).build();
        when(kernelEventDao.findBySessionIds(anyList())).thenReturn(List.of());
        when(profileSampleDao.findBySessionId(SESSION_ID)).thenReturn(List.of());
        when(deviceMetricDao.findBySessionIds(anyList())).thenReturn(List.of(m1, m2));

        SessionInsightsDto result = service.getInsights(SESSION_ID);

        assertThat(result.getInsights()).hasSize(1);
        assertThat(result.getInsights().get(0).getTitle()).containsIgnoringCase("temperature");
    }

    @Test
    void averageTemperatureJustAboveThreshold_generatesInsight() {
        // avg = (84 + 86) / 2 = 85 — exactly at threshold
        DeviceMetricEntity m1 = DeviceMetricEntity.builder().tempC(84).build();
        DeviceMetricEntity m2 = DeviceMetricEntity.builder().tempC(86).build();
        when(kernelEventDao.findBySessionIds(anyList())).thenReturn(List.of());
        when(profileSampleDao.findBySessionId(SESSION_ID)).thenReturn(List.of());
        when(deviceMetricDao.findBySessionIds(anyList())).thenReturn(List.of(m1, m2));

        SessionInsightsDto result = service.getInsights(SESSION_ID);

        assertThat(result.getInsights()).hasSize(1);
        assertThat(result.getInsights().get(0).getSeverity()).isEqualTo("HIGH");
    }

    @Test
    void normalTemperature_noInsight() {
        DeviceMetricEntity metric = DeviceMetricEntity.builder()
                .tempC(70)
                .build();
        when(kernelEventDao.findBySessionIds(anyList())).thenReturn(List.of());
        when(profileSampleDao.findBySessionId(SESSION_ID)).thenReturn(List.of());
        when(deviceMetricDao.findBySessionIds(anyList())).thenReturn(List.of(metric));

        assertThat(service.getInsights(SESSION_ID).getInsights()).isEmpty();
    }

    // ── Ordering ───────────────────────────────────────────────────────────────

    @Test
    void highSeverityInsights_sortedBeforeMedium() {
        KernelEventEntity kernel = KernelEventEntity.builder()
                .name("k")
                .occupancy(new BigDecimal("1.0"))
                .limitingResource("warps")
                .localMemTotalBytes(1024L)   // MEDIUM spill insight
                .numRegs(10)
                .build();
        DeviceMetricEntity metric = DeviceMetricEntity.builder()
                .tempC(90)                   // HIGH temperature insight
                .build();
        when(kernelEventDao.findBySessionIds(anyList())).thenReturn(List.of(kernel));
        when(profileSampleDao.findBySessionId(SESSION_ID)).thenReturn(List.of());
        when(deviceMetricDao.findBySessionIds(anyList())).thenReturn(List.of(metric));

        List<InsightDto> insights = service.getInsights(SESSION_ID).getInsights();

        assertThat(insights).hasSize(2);
        assertThat(insights.get(0).getSeverity()).isEqualTo("HIGH");
        assertThat(insights.get(1).getSeverity()).isEqualTo("MEDIUM");
    }

    @Test
    void sessionId_isPreservedInResponse() {
        when(kernelEventDao.findBySessionIds(anyList())).thenReturn(List.of());
        when(profileSampleDao.findBySessionId(SESSION_ID)).thenReturn(List.of());
        when(deviceMetricDao.findBySessionIds(anyList())).thenReturn(List.of());

        assertThat(service.getInsights(SESSION_ID).getSessionId()).isEqualTo(SESSION_ID);
    }
}
