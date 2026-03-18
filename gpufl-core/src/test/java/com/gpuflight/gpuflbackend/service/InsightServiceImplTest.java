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
        assertThat(insight.getMessage()).contains("divergence");
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

    @Test
    void memoryStall_dominant_generatesHighInsight() {
        ProfileSampleEntity stall = ProfileSampleEntity.builder()
                .functionName("myFunc")
                .sampleKind("pc_sampling")
                .reasonName("MEM_DEP")
                .sampleCount(80)
                .build();
        ProfileSampleEntity other = ProfileSampleEntity.builder()
                .functionName("myFunc")
                .sampleKind("pc_sampling")
                .reasonName("EXEC")
                .sampleCount(20)
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
    void divergeStall_dominant_givesDivergenceAdvice() {
        ProfileSampleEntity stall = ProfileSampleEntity.builder()
                .functionName("branchFunc")
                .sampleKind("pc_sampling")
                .reasonName("WARP_DIVERGE")
                .sampleCount(90)
                .build();
        ProfileSampleEntity other = ProfileSampleEntity.builder()
                .functionName("branchFunc")
                .sampleKind("pc_sampling")
                .reasonName("EXEC")
                .sampleCount(10)
                .build();
        when(kernelEventDao.findBySessionIds(anyList())).thenReturn(List.of());
        when(profileSampleDao.findBySessionId(SESSION_ID)).thenReturn(List.of(stall, other));
        when(deviceMetricDao.findBySessionIds(anyList())).thenReturn(List.of());

        SessionInsightsDto result = service.getInsights(SESSION_ID);

        assertThat(result.getInsights()).hasSize(1);
        assertThat(result.getInsights().get(0).getMessage()).containsIgnoringCase("divergence");
    }

    @Test
    void syncStall_dominant_givesSyncAdvice() {
        ProfileSampleEntity stall = ProfileSampleEntity.builder()
                .functionName("syncFunc")
                .sampleKind("pc_sampling")
                .reasonName("SYNC_BARRIER")
                .sampleCount(90)
                .build();
        ProfileSampleEntity other = ProfileSampleEntity.builder()
                .functionName("syncFunc")
                .sampleKind("pc_sampling")
                .reasonName("EXEC")
                .sampleCount(10)
                .build();
        when(kernelEventDao.findBySessionIds(anyList())).thenReturn(List.of());
        when(profileSampleDao.findBySessionId(SESSION_ID)).thenReturn(List.of(stall, other));
        when(deviceMetricDao.findBySessionIds(anyList())).thenReturn(List.of());

        SessionInsightsDto result = service.getInsights(SESSION_ID);

        assertThat(result.getInsights()).hasSize(1);
        assertThat(result.getInsights().get(0).getMessage()).containsIgnoringCase("syncthreads");
    }

    @Test
    void execPipeStall_dominant_givesIlpAdvice() {
        ProfileSampleEntity stall = ProfileSampleEntity.builder()
                .functionName("pipeFunc")
                .sampleKind("pc_sampling")
                .reasonName("EXEC_PIPELINE")
                .sampleCount(90)
                .build();
        ProfileSampleEntity other = ProfileSampleEntity.builder()
                .functionName("pipeFunc")
                .sampleKind("pc_sampling")
                .reasonName("OTHER")
                .sampleCount(10)
                .build();
        when(kernelEventDao.findBySessionIds(anyList())).thenReturn(List.of());
        when(profileSampleDao.findBySessionId(SESSION_ID)).thenReturn(List.of(stall, other));
        when(deviceMetricDao.findBySessionIds(anyList())).thenReturn(List.of());

        SessionInsightsDto result = service.getInsights(SESSION_ID);

        assertThat(result.getInsights()).hasSize(1);
        assertThat(result.getInsights().get(0).getMessage()).containsIgnoringCase("ILP");
    }

    @Test
    void textureStall_dominant_givesTextureAdvice() {
        ProfileSampleEntity stall = ProfileSampleEntity.builder()
                .functionName("texFunc")
                .sampleKind("pc_sampling")
                .reasonName("TEXTURE_FETCH")
                .sampleCount(90)
                .build();
        ProfileSampleEntity other = ProfileSampleEntity.builder()
                .functionName("texFunc")
                .sampleKind("pc_sampling")
                .reasonName("OTHER")
                .sampleCount(10)
                .build();
        when(kernelEventDao.findBySessionIds(anyList())).thenReturn(List.of());
        when(profileSampleDao.findBySessionId(SESSION_ID)).thenReturn(List.of(stall, other));
        when(deviceMetricDao.findBySessionIds(anyList())).thenReturn(List.of());

        SessionInsightsDto result = service.getInsights(SESSION_ID);

        assertThat(result.getInsights()).hasSize(1);
        assertThat(result.getInsights().get(0).getMessage()).containsIgnoringCase("texture");
    }

    @Test
    void constantStall_dominant_givesConstantAdvice() {
        // Use a reason name that contains CONSTANT but not MEM/L1/L2 so the memory
        // stall branch doesn't fire first.
        ProfileSampleEntity stall = ProfileSampleEntity.builder()
                .functionName("constFunc")
                .sampleKind("pc_sampling")
                .reasonName("CONSTANT_CACHE")
                .sampleCount(90)
                .build();
        ProfileSampleEntity other = ProfileSampleEntity.builder()
                .functionName("constFunc")
                .sampleKind("pc_sampling")
                .reasonName("OTHER")
                .sampleCount(10)
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
                .functionName("someFunc")
                .sampleKind("pc_sampling")
                .reasonName("SOME_UNKNOWN_REASON")
                .sampleCount(90)
                .build();
        ProfileSampleEntity other = ProfileSampleEntity.builder()
                .functionName("someFunc")
                .sampleKind("pc_sampling")
                .reasonName("OTHER")
                .sampleCount(10)
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
        ProfileSampleEntity stall = ProfileSampleEntity.builder()
                .functionName("myFunc")
                .sampleKind("pc_sampling")
                .reasonName("MEM_DEP")
                .sampleCount(33)
                .build();
        ProfileSampleEntity other = ProfileSampleEntity.builder()
                .functionName("myFunc")
                .sampleKind("pc_sampling")
                .reasonName("EXEC")
                .sampleCount(33)
                .build();
        ProfileSampleEntity third = ProfileSampleEntity.builder()
                .functionName("myFunc")
                .sampleKind("pc_sampling")
                .reasonName("OTHER")
                .sampleCount(34)
                .build();
        when(kernelEventDao.findBySessionIds(anyList())).thenReturn(List.of());
        when(profileSampleDao.findBySessionId(SESSION_ID)).thenReturn(List.of(stall, other, third));
        when(deviceMetricDao.findBySessionIds(anyList())).thenReturn(List.of());

        assertThat(service.getInsights(SESSION_ID).getInsights()).isEmpty();
    }

    // ── SASS divergence analysis ───────────────────────────────────────────────

    @Test
    void sassDivergence_low_efficiency_generatesHighInsight() {
        // 25% thread efficiency (< 50% → HIGH)
        ProfileSampleEntity s = ProfileSampleEntity.builder()
                .functionName("branchKernel")
                .sampleKind("sass_metric")
                .instExecuted(100)
                .threadInstExecuted(800)   // 800 / (100*32) = 25%
                .sampleCount(0)
                .build();
        when(kernelEventDao.findBySessionIds(anyList())).thenReturn(List.of());
        when(profileSampleDao.findBySessionId(SESSION_ID)).thenReturn(List.of(s));
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
        // 65% thread efficiency (≥ 50% but < 75% → MEDIUM)
        ProfileSampleEntity s = ProfileSampleEntity.builder()
                .functionName("branchKernel")
                .sampleKind("sass_metric")
                .instExecuted(100)
                .threadInstExecuted(2080)  // 2080 / (100*32) = 65%
                .sampleCount(0)
                .build();
        when(kernelEventDao.findBySessionIds(anyList())).thenReturn(List.of());
        when(profileSampleDao.findBySessionId(SESSION_ID)).thenReturn(List.of(s));
        when(deviceMetricDao.findBySessionIds(anyList())).thenReturn(List.of());

        SessionInsightsDto result = service.getInsights(SESSION_ID);

        assertThat(result.getInsights()).hasSize(1);
        assertThat(result.getInsights().get(0).getSeverity()).isEqualTo("MEDIUM");
    }

    @Test
    void sassDivergence_high_efficiency_noInsight() {
        // 95% thread efficiency → no insight
        ProfileSampleEntity s = ProfileSampleEntity.builder()
                .functionName("goodKernel")
                .sampleKind("sass_metric")
                .instExecuted(100)
                .threadInstExecuted(3040)  // 3040 / (100*32) = 95%
                .sampleCount(0)
                .build();
        when(kernelEventDao.findBySessionIds(anyList())).thenReturn(List.of());
        when(profileSampleDao.findBySessionId(SESSION_ID)).thenReturn(List.of(s));
        when(deviceMetricDao.findBySessionIds(anyList())).thenReturn(List.of());

        assertThat(service.getInsights(SESSION_ID).getInsights()).isEmpty();
    }

    @Test
    void sassDivergence_zeroInstExecuted_skipped() {
        ProfileSampleEntity s = ProfileSampleEntity.builder()
                .functionName("emptyKernel")
                .sampleKind("sass_metric")
                .instExecuted(0)
                .threadInstExecuted(0)
                .sampleCount(0)
                .build();
        when(kernelEventDao.findBySessionIds(anyList())).thenReturn(List.of());
        when(profileSampleDao.findBySessionId(SESSION_ID)).thenReturn(List.of(s));
        when(deviceMetricDao.findBySessionIds(anyList())).thenReturn(List.of());

        assertThat(service.getInsights(SESSION_ID).getInsights()).isEmpty();
    }

    // ── Device metric throttling ───────────────────────────────────────────────

    @Test
    void powerThrottling_generatesHighInsight() {
        DeviceMetricEntity metric = DeviceMetricEntity.builder()
                .throttlePwr(1)
                .throttleTherm(0)
                .build();
        when(kernelEventDao.findBySessionIds(anyList())).thenReturn(List.of());
        when(profileSampleDao.findBySessionId(SESSION_ID)).thenReturn(List.of());
        when(deviceMetricDao.findBySessionIds(anyList())).thenReturn(List.of(metric));

        SessionInsightsDto result = service.getInsights(SESSION_ID);

        assertThat(result.getInsights()).hasSize(1);
        InsightDto insight = result.getInsights().get(0);
        assertThat(insight.getSeverity()).isEqualTo("HIGH");
        assertThat(insight.getCategory()).isEqualTo("THROTTLING");
        assertThat(insight.getTitle()).containsIgnoringCase("power");
    }

    @Test
    void thermalThrottling_generatesHighInsight() {
        DeviceMetricEntity metric = DeviceMetricEntity.builder()
                .throttlePwr(0)
                .throttleTherm(1)
                .build();
        when(kernelEventDao.findBySessionIds(anyList())).thenReturn(List.of());
        when(profileSampleDao.findBySessionId(SESSION_ID)).thenReturn(List.of());
        when(deviceMetricDao.findBySessionIds(anyList())).thenReturn(List.of(metric));

        SessionInsightsDto result = service.getInsights(SESSION_ID);

        assertThat(result.getInsights()).hasSize(1);
        assertThat(result.getInsights().get(0).getTitle()).containsIgnoringCase("thermal");
    }

    @Test
    void powerAndThermalThrottling_mentionsBoth() {
        DeviceMetricEntity metric = DeviceMetricEntity.builder()
                .throttlePwr(1)
                .throttleTherm(1)
                .build();
        when(kernelEventDao.findBySessionIds(anyList())).thenReturn(List.of());
        when(profileSampleDao.findBySessionId(SESSION_ID)).thenReturn(List.of());
        when(deviceMetricDao.findBySessionIds(anyList())).thenReturn(List.of(metric));

        SessionInsightsDto result = service.getInsights(SESSION_ID);

        assertThat(result.getInsights()).hasSize(1);
        assertThat(result.getInsights().get(0).getTitle()).containsIgnoringCase("power and thermal");
    }

    @Test
    void noThrottling_noInsight() {
        DeviceMetricEntity metric = DeviceMetricEntity.builder()
                .throttlePwr(0)
                .throttleTherm(0)
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
                .localMemTotalBytes(1024L)   // MEDIUM
                .numRegs(10)
                .build();
        DeviceMetricEntity metric = DeviceMetricEntity.builder()
                .throttlePwr(1)
                .throttleTherm(0)
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
