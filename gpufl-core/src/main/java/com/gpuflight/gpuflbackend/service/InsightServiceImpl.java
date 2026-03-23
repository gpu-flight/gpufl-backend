package com.gpuflight.gpuflbackend.service;

import com.gpuflight.gpuflbackend.dao.DeviceMetricDao;
import com.gpuflight.gpuflbackend.dao.KernelEventDao;
import com.gpuflight.gpuflbackend.dao.ProfileSampleDao;
import com.gpuflight.gpuflbackend.entity.DeviceMetricEntity;
import com.gpuflight.gpuflbackend.entity.KernelEventEntity;
import com.gpuflight.gpuflbackend.entity.ProfileSampleEntity;
import com.gpuflight.gpuflbackend.model.InsightDto;
import com.gpuflight.gpuflbackend.model.SessionInsightsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InsightServiceImpl implements InsightService {

    private static final BigDecimal LOW_OCCUPANCY_THRESHOLD = new BigDecimal("0.5");
    private static final BigDecimal LOW_REG_OCCUPANCY_THRESHOLD = new BigDecimal("0.6");
    private static final int HIGH_REG_COUNT_THRESHOLD = 32;
    private static final double STALL_DOMINANT_PCT = 0.40;
    /** Thread efficiency below this is flagged as significant warp divergence */
    private static final double LOW_THREAD_EFFICIENCY_THRESHOLD = 0.75;

    private static final List<String> MEMORY_STALL_REASONS = List.of(
            "MEM_DEP", "MEM_DEPENDENCY", "MEMORY", "L1_MISS", "L2_MISS", "TEXTURE"
    );

    private final KernelEventDao kernelEventDao;
    private final ProfileSampleDao profileSampleDao;
    private final DeviceMetricDao deviceMetricDao;

    @Override
    public SessionInsightsDto getInsights(String sessionId) {
        List<InsightDto> insights = new ArrayList<>();

        insights.addAll(analyzeKernelEvents(sessionId));
        insights.addAll(analyzeProfileSamples(sessionId));
        insights.addAll(analyzeSassDivergence(sessionId));
        insights.addAll(analyzeDeviceMetrics(sessionId));

        // Sort HIGH before MEDIUM
        insights.sort(Comparator.comparingInt(r -> "HIGH".equals(r.getSeverity()) ? 0 : 1));

        return SessionInsightsDto.builder()
                .sessionId(sessionId)
                .insights(insights)
                .build();
    }

    private List<InsightDto> analyzeKernelEvents(String sessionId) {
        List<KernelEventEntity> events = kernelEventDao.findBySessionIds(List.of(sessionId));
        List<InsightDto> results = new ArrayList<>();

        for (KernelEventEntity e : events) {
            String kernelName = e.getName();

            // Rule 1: low occupancy
            if (e.getOccupancy() != null && e.getOccupancy().compareTo(LOW_OCCUPANCY_THRESHOLD) < 0) {
                String limiting = e.getLimitingResource() != null ? e.getLimitingResource() : "unknown";
                String advice = limitingResourceAdvice(limiting);
                results.add(InsightDto.builder()
                        .severity("HIGH")
                        .category("OCCUPANCY")
                        .kernelName(kernelName)
                        .title("Low occupancy: limited by " + limiting)
                        .message(String.format(
                                "Occupancy is %.0f%% — limited by %s. %s",
                                e.getOccupancy().doubleValue() * 100, limiting, advice))
                        .metric(String.format("occupancy=%.2f, limitingResource=%s", e.getOccupancy(), limiting))
                        .build());
            }

            // Rule 2: register spilling to local memory
            if (e.getLocalMemTotalBytes() != null && e.getLocalMemTotalBytes() > 0) {
                results.add(InsightDto.builder()
                        .severity("MEDIUM")
                        .category("MEMORY")
                        .kernelName(kernelName)
                        .title("Register spilling to local memory")
                        .message(String.format(
                                "Kernel spilled %d bytes to local (off-chip) memory. Reduce register pressure or use shared memory to avoid costly memory traffic.",
                                e.getLocalMemTotalBytes()))
                        .metric(String.format("localMemTotalBytes=%d", e.getLocalMemTotalBytes()))
                        .build());
            }

            // Rule 3: high register count with low reg occupancy — only when registers are
            // actually the limiting resource (avoids false positives from divergence, etc.)
            String limitingForRule3 = e.getLimitingResource() != null ? e.getLimitingResource().toUpperCase() : "";
            boolean isRegLimited = limitingForRule3.contains("REG");
            if (isRegLimited
                    && e.getNumRegs() > HIGH_REG_COUNT_THRESHOLD
                    && e.getRegOccupancy() != null
                    && e.getRegOccupancy().compareTo(LOW_REG_OCCUPANCY_THRESHOLD) < 0) {
                results.add(InsightDto.builder()
                        .severity("MEDIUM")
                        .category("OCCUPANCY")
                        .kernelName(kernelName)
                        .title("Register pressure limiting occupancy")
                        .message(String.format(
                                "Occupancy is %.0f%% — limited by registers (%d regs/thread). Consider using __launch_bounds__(maxThreadsPerBlock) to cap register usage and improve occupancy.",
                                e.getRegOccupancy().doubleValue() * 100, e.getNumRegs()))
                        .metric(String.format("numRegs=%d, regOccupancy=%.2f", e.getNumRegs(), e.getRegOccupancy()))
                        .build());
            }
        }

        return results;
    }

    private List<InsightDto> analyzeProfileSamples(String sessionId) {
        List<ProfileSampleEntity> samples = profileSampleDao.findBySessionId(sessionId);
        List<InsightDto> results = new ArrayList<>();

        // Group by function name, aggregate sample counts per stall reason
        Map<String, List<ProfileSampleEntity>> byFunction = samples.stream()
                .filter(s -> s.getFunctionName() != null)
                .collect(Collectors.groupingBy(ProfileSampleEntity::getFunctionName));

        for (Map.Entry<String, List<ProfileSampleEntity>> entry : byFunction.entrySet()) {
            String functionName = entry.getKey();
            List<ProfileSampleEntity> functionSamples = entry.getValue();

            long totalSamples = functionSamples.stream().mapToLong(ProfileSampleEntity::getSampleCount).sum();
            if (totalSamples == 0) continue;

            // Group by stall reason within the function
            Map<String, Long> stallCounts = functionSamples.stream()
                    .filter(s -> s.getReasonName() != null)
                    .collect(Collectors.groupingBy(
                            ProfileSampleEntity::getReasonName,
                            Collectors.summingLong(ProfileSampleEntity::getSampleCount)));

            for (Map.Entry<String, Long> stall : stallCounts.entrySet()) {
                String reasonName = stall.getKey();
                long count = stall.getValue();
                double pct = (double) count / totalSamples;

                if (pct > STALL_DOMINANT_PCT) {
                    boolean isMemoryStall = MEMORY_STALL_REASONS.stream()
                            .anyMatch(r -> reasonName.toUpperCase().contains(r));
                    String severity = isMemoryStall ? "HIGH" : "MEDIUM";
                    String advice = stallAdvice(reasonName);

                    results.add(InsightDto.builder()
                            .severity(severity)
                            .category("STALL")
                            .functionName(functionName)
                            .title(String.format("%s stalls dominate in %s", reasonName, functionName))
                            .message(String.format(
                                    "%.0f%% of PC samples in '%s' are stalled on %s. %s",
                                    pct * 100, functionName, reasonName, advice))
                            .metric(String.format("stallReason=%s, samplePct=%.0f%%", reasonName, pct * 100))
                            .build());
                }
            }
        }

        return results;
    }

    private List<InsightDto> analyzeSassDivergence(String sessionId) {
        List<ProfileSampleEntity> samples = profileSampleDao.findBySessionId(sessionId);
        List<InsightDto> results = new ArrayList<>();

        // Group sass_metric samples by function name and compute aggregate thread efficiency
        Map<String, List<ProfileSampleEntity>> byFunction = samples.stream()
                .filter(s -> "sass_metric".equals(s.getSampleKind()) && s.getFunctionName() != null)
                .collect(Collectors.groupingBy(ProfileSampleEntity::getFunctionName));

        for (Map.Entry<String, List<ProfileSampleEntity>> entry : byFunction.entrySet()) {
            String functionName = entry.getKey();
            List<ProfileSampleEntity> funcSamples = entry.getValue();

            long totalInstExec = funcSamples.stream().mapToLong(ProfileSampleEntity::getInstExecuted).sum();
            long totalThreadInstExec = funcSamples.stream().mapToLong(ProfileSampleEntity::getThreadInstExecuted).sum();

            // thread efficiency = thread_inst_executed / (inst_executed * warp_size)
            // A fully converged kernel scores 1.0; heavy divergence scores near 0.
            if (totalInstExec == 0) continue;
            double efficiency = (double) totalThreadInstExec / ((double) totalInstExec * 32.0);
            if (efficiency >= LOW_THREAD_EFFICIENCY_THRESHOLD) continue;

            String severity = efficiency < 0.5 ? "HIGH" : "MEDIUM";
            results.add(InsightDto.builder()
                    .severity(severity)
                    .category("DIVERGENCE")
                    .functionName(functionName)
                    .title(String.format("Warp divergence in %s — %.0f%% thread efficiency",
                            functionName, efficiency * 100))
                    .message(String.format(
                            "SASS analysis shows only %.0f%% thread efficiency in '%s' — on average %.0f%% of threads "
                            + "in each warp are inactive per instruction due to branch divergence. "
                            + "Minimize if/else branches whose condition differs within a warp, "
                            + "use predicated execution (e.g. ternary or __ballot_sync), "
                            + "or restructure data so threads in the same warp follow the same code path.",
                            efficiency * 100, functionName, (1.0 - efficiency) * 100))
                    .metric(String.format("threadEfficiency=%.1f%%, instExecuted=%d, threadInstExecuted=%d",
                            efficiency * 100, totalInstExec, totalThreadInstExec))
                    .build());
        }

        return results;
    }

    private List<InsightDto> analyzeDeviceMetrics(String sessionId) {
        List<DeviceMetricEntity> metrics = deviceMetricDao.findBySessionIds(List.of(sessionId));
        List<InsightDto> results = new ArrayList<>();

        boolean powerThrottle = metrics.stream()
                .anyMatch(m -> m.getThrottlePwr() != null && m.getThrottlePwr() > 0);
        boolean thermalThrottle = metrics.stream()
                .anyMatch(m -> m.getThrottleTherm() != null && m.getThrottleTherm() > 0);

        if (powerThrottle || thermalThrottle) {
            String kind = powerThrottle && thermalThrottle ? "power and thermal"
                    : powerThrottle ? "power" : "thermal";
            results.add(InsightDto.builder()
                    .severity("HIGH")
                    .category("THROTTLING")
                    .title("GPU " + kind + " throttling detected")
                    .message(String.format(
                            "The GPU is being %s-throttled and running below its rated clocks. "
                                    + "Check cooling, power delivery, and TDP limits. "
                                    + "Profiling results may not reflect peak achievable performance.",
                            kind))
                    .metric(String.format("throttlePwr=%s, throttleTherm=%s",
                            powerThrottle ? ">0" : "0", thermalThrottle ? ">0" : "0"))
                    .build());
        }

        return results;
    }

    private String limitingResourceAdvice(String limitingResource) {
        return switch (limitingResource.toUpperCase()) {
            case "REGISTERS", "REG" ->
                    "Use __launch_bounds__(maxThreadsPerBlock) or reduce register pressure to improve occupancy.";
            case "SHARED_MEMORY", "SMEM", "SHARED" ->
                    "Reduce shared memory usage per block to allow more blocks to run concurrently.";
            case "WARPS", "WARP" ->
                    "Warp count is limiting occupancy — check for branch divergence (if/else inside warps). "
                    + "Minimize divergent branches, restructure control flow, or increase block size.";
            case "BLOCKS", "BLOCK" ->
                    "Increase the number of blocks or reduce per-block resource usage.";
            default ->
                    "Profile with NSight Compute to identify the specific resource bottleneck.";
        };
    }

    private String stallAdvice(String reasonName) {
        String upper = reasonName.toUpperCase();
        if (upper.contains("MEM") || upper.contains("MEMORY") || upper.contains("L1") || upper.contains("L2")) {
            return "Verify global memory accesses are coalesced and consider using shared memory to reduce redundant loads.";
        }
        if (upper.contains("DIVERGE") || upper.contains("BRANCH_DIV")) {
            return "Warp divergence is serializing execution. Minimize if/else branches within a warp, "
                    + "use predicated execution, or restructure data so threads in the same warp take the same path.";
        }
        if (upper.contains("SYNC") || upper.contains("BARRIER")) {
            return "Reduce __syncthreads() calls or restructure the kernel to minimize barrier synchronization.";
        }
        if (upper.contains("EXEC") || upper.contains("PIPE")) {
            return "Increase instruction-level parallelism (ILP) by interleaving independent operations.";
        }
        if (upper.contains("TEXTURE") || upper.contains("TEX")) {
            return "Consider caching texture data in shared memory or using L2-friendly access patterns.";
        }
        if (upper.contains("CONSTANT")) {
            return "Ensure all threads in a warp access the same constant memory address to avoid serialization.";
        }
        return "Analyze the stall reason in NSight Compute and restructure the kernel to reduce stall cycles.";
    }
}
