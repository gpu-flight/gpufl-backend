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
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InsightServiceImpl implements InsightService {

    private static final BigDecimal LOW_OCCUPANCY_THRESHOLD = new BigDecimal("0.5");
    private static final BigDecimal LOW_REG_OCCUPANCY_THRESHOLD = new BigDecimal("0.6");
    private static final int HIGH_REG_COUNT_THRESHOLD = 32;
    private static final double STALL_DOMINANT_PCT = 0.40;
    private static final double LOW_THREAD_EFFICIENCY_THRESHOLD = 0.75;

    // CUPTI CUpti_ActivityPCSamplingStallReason — skip 0 (invalid) and 1 (none)
    private static final Map<Integer, String> STALL_NAMES = Map.ofEntries(
        Map.entry(2,  "Instruction Fetch"),
        Map.entry(3,  "Execution Dependency"),
        Map.entry(4,  "Memory Dependency"),
        Map.entry(5,  "Texture"),
        Map.entry(6,  "Sync"),
        Map.entry(7,  "Constant Memory"),
        Map.entry(8,  "Pipe Busy"),
        Map.entry(9,  "Memory Throttle"),
        Map.entry(10, "Branch Resolving"),
        Map.entry(11, "Wait"),
        Map.entry(12, "Barrier"),
        Map.entry(13, "Sleeping")
    );

    private static final Set<Integer> MEMORY_STALL_IDS = Set.of(4, 5, 7, 9);

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

        insights.sort(Comparator.comparingInt(r -> "HIGH".equals(r.getSeverity()) ? 0 : 1));

        return SessionInsightsDto.builder()
                .sessionId(sessionId)
                .insights(insights)
                .build();
    }

    // ── Kernel occupancy + spill + register pressure ──────────────────────────

    private List<InsightDto> analyzeKernelEvents(String sessionId) {
        List<KernelEventEntity> events = kernelEventDao.findBySessionIds(List.of(sessionId));
        List<InsightDto> results = new ArrayList<>();

        for (KernelEventEntity e : events) {
            String kernelName = e.getName();

            // Rule 1: low occupancy
            if (e.getOccupancy() != null && e.getOccupancy().compareTo(LOW_OCCUPANCY_THRESHOLD) < 0) {
                String limiting = e.getLimitingResource() != null ? e.getLimitingResource() : "unknown";
                results.add(InsightDto.builder()
                        .severity("HIGH")
                        .category("OCCUPANCY")
                        .kernelName(kernelName)
                        .title("Low occupancy: limited by " + limiting)
                        .message(String.format(
                                "Occupancy is %.0f%% — limited by %s. %s",
                                e.getOccupancy().doubleValue() * 100, limiting,
                                limitingResourceAdvice(limiting)))
                        .metric(String.format("occupancy=%.2f, limitingResource=%s",
                                e.getOccupancy(), limiting))
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
                                "Kernel spilled %d bytes to local (off-chip) memory. "
                                + "Reduce register pressure or use shared memory to avoid costly memory traffic.",
                                e.getLocalMemTotalBytes()))
                        .metric(String.format("localMemTotalBytes=%d", e.getLocalMemTotalBytes()))
                        .build());
            }

            // Rule 3: high register count + low reg occupancy when registers are the limiter
            String limiting = e.getLimitingResource() != null
                    ? e.getLimitingResource().toUpperCase() : "";
            if (limiting.contains("REG")
                    && e.getNumRegs() > HIGH_REG_COUNT_THRESHOLD
                    && e.getRegOccupancy() != null
                    && e.getRegOccupancy().compareTo(LOW_REG_OCCUPANCY_THRESHOLD) < 0) {
                results.add(InsightDto.builder()
                        .severity("MEDIUM")
                        .category("OCCUPANCY")
                        .kernelName(kernelName)
                        .title("Register pressure limiting occupancy")
                        .message(String.format(
                                "Occupancy is %.0f%% — limited by registers (%d regs/thread). "
                                + "Consider using __launch_bounds__(maxThreadsPerBlock) to cap register usage.",
                                e.getRegOccupancy().doubleValue() * 100, e.getNumRegs()))
                        .metric(String.format("numRegs=%d, regOccupancy=%.2f",
                                e.getNumRegs(), e.getRegOccupancy()))
                        .build());
            }
        }

        return results;
    }

    // ── PC-sampling stall reasons ─────────────────────────────────────────────

    private List<InsightDto> analyzeProfileSamples(String sessionId) {
        List<ProfileSampleEntity> samples = profileSampleDao.findBySessionId(sessionId);
        List<InsightDto> results = new ArrayList<>();

        // Only PC sampling rows with a real stall reason (>1 skips Invalid/None)
        List<ProfileSampleEntity> pcSamples = samples.stream()
                .filter(s -> "pc_sampling".equals(s.getSampleKind())
                        && s.getStallReason() != null
                        && s.getStallReason() > 1)
                .toList();

        if (pcSamples.isEmpty()) return results;

        // Group by scopeName + stallReason, sum metricValue (accumulated sample count)
        Map<String, Map<Integer, Long>> byScopeAndReason = new LinkedHashMap<>();
        for (ProfileSampleEntity s : pcSamples) {
            String scope = s.getScopeName() != null ? s.getScopeName() : "(global)";
            byScopeAndReason
                    .computeIfAbsent(scope, k -> new HashMap<>())
                    .merge(s.getStallReason(), s.getMetricValue(), Long::sum);
        }

        for (Map.Entry<String, Map<Integer, Long>> scopeEntry : byScopeAndReason.entrySet()) {
            Map<Integer, Long> reasons = scopeEntry.getValue();
            long total = reasons.values().stream().mapToLong(Long::longValue).sum();
            if (total == 0) continue;

            Map.Entry<Integer, Long> top = reasons.entrySet().stream()
                    .max(Map.Entry.comparingByValue()).orElse(null);
            if (top == null) continue;

            double pct = (double) top.getValue() / total;
            if (pct <= STALL_DOMINANT_PCT) continue;

            String reasonName = STALL_NAMES.getOrDefault(top.getKey(), "Stall#" + top.getKey());
            boolean isMemory = MEMORY_STALL_IDS.contains(top.getKey());
            String severity = isMemory ? "HIGH" : "MEDIUM";

            results.add(InsightDto.builder()
                    .severity(severity)
                    .category("STALL")
                    .title(String.format("%s stalls dominate in scope '%s'",
                            reasonName, scopeEntry.getKey()))
                    .message(String.format(
                            "%.0f%% of PC samples in scope '%s' are stalled on '%s'. %s",
                            pct * 100, scopeEntry.getKey(), reasonName,
                            stallAdvice(top.getKey())))
                    .metric(String.format("stallReason=%s, samplePct=%.0f%%",
                            reasonName, pct * 100))
                    .build());
        }

        return results;
    }

    // ── SASS warp divergence ──────────────────────────────────────────────────

    private List<InsightDto> analyzeSassDivergence(String sessionId) {
        List<ProfileSampleEntity> samples = profileSampleDao.findBySessionId(sessionId);
        List<InsightDto> results = new ArrayList<>();

        // Aggregate inst_executed and thread_inst_executed per (functionName, scopeName)
        record Key(String functionName, String scopeName) {}

        Map<Key, long[]> agg = new LinkedHashMap<>(); // [instExec, threadInstExec]
        for (ProfileSampleEntity s : samples) {
            if (!"sass_metric".equals(s.getSampleKind()) || s.getFunctionName() == null) continue;
            String scope = s.getScopeName() != null ? s.getScopeName() : "(global)";
            Key key = new Key(s.getFunctionName(), scope);
            long[] v = agg.computeIfAbsent(key, k -> new long[2]);
            if ("smsp__sass_inst_executed".equals(s.getMetricName())) {
                v[0] += s.getMetricValue();
            } else if ("smsp__sass_thread_inst_executed".equals(s.getMetricName())) {
                v[1] += s.getMetricValue();
            }
        }

        for (Map.Entry<Key, long[]> e : agg.entrySet()) {
            long instExec = e.getValue()[0];
            if (instExec == 0) continue;
            double efficiency = (double) e.getValue()[1] / ((double) instExec * 32.0);
            if (efficiency >= LOW_THREAD_EFFICIENCY_THRESHOLD) continue;

            String severity = efficiency < 0.5 ? "HIGH" : "MEDIUM";
            String fnDisplay = shortFunctionName(e.getKey().functionName());

            results.add(InsightDto.builder()
                    .severity(severity)
                    .category("DIVERGENCE")
                    .functionName(e.getKey().functionName())
                    .title(String.format("Warp divergence in %s — %.0f%% thread efficiency",
                            fnDisplay, efficiency * 100))
                    .message(String.format(
                            "SASS analysis shows only %.0f%% thread efficiency in '%s' (scope '%s'). "
                            + "On average %.0f%% of threads in each warp are inactive per instruction. "
                            + "Minimize if/else branches whose condition differs within a warp, "
                            + "use predicated execution, or restructure data so threads in the same warp "
                            + "follow the same code path.",
                            efficiency * 100, fnDisplay, e.getKey().scopeName(),
                            (1.0 - efficiency) * 100))
                    .metric(String.format("threadEfficiency=%.1f%%, instExecuted=%d, threadInstExecuted=%d",
                            efficiency * 100, instExec, e.getValue()[1]))
                    .build());
        }

        return results;
    }

    // ── Device metrics ────────────────────────────────────────────────────────

    private List<InsightDto> analyzeDeviceMetrics(String sessionId) {
        List<DeviceMetricEntity> metrics = deviceMetricDao.findBySessionIds(List.of(sessionId));
        List<InsightDto> results = new ArrayList<>();

        // High sustained temperature
        OptionalDouble maxTemp = metrics.stream()
                .filter(m -> m.getTempC() != null)
                .mapToInt(DeviceMetricEntity::getTempC)
                .average();
        if (maxTemp.isPresent() && maxTemp.getAsDouble() >= 85) {
            results.add(InsightDto.builder()
                    .severity("HIGH")
                    .category("THROTTLING")
                    .title("High GPU temperature detected")
                    .message(String.format(
                            "Average GPU temperature is %.0f°C. "
                            + "Sustained high temperatures can trigger thermal throttling and reduce clock speeds. "
                            + "Check cooling solution and ensure adequate airflow.",
                            maxTemp.getAsDouble()))
                    .metric(String.format("avgTempC=%.0f", maxTemp.getAsDouble()))
                    .build());
        }

        return results;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static String shortFunctionName(String raw) {
        if (raw == null) return "(unknown)";
        // format is "name@sourceFile"
        int at = raw.lastIndexOf('@');
        String name = at >= 0 ? raw.substring(0, at) : raw;
        int lt = name.indexOf('<');
        String base = lt >= 0 ? name.substring(0, lt) : name;
        int lastColon = base.lastIndexOf("::");
        return lastColon >= 0 ? base.substring(lastColon + 2) : base;
    }

    private static String limitingResourceAdvice(String limitingResource) {
        return switch (limitingResource.toUpperCase()) {
            case "REGISTERS", "REG" ->
                    "Use __launch_bounds__(maxThreadsPerBlock) or reduce register pressure to improve occupancy.";
            case "SHARED_MEMORY", "SMEM", "SHARED" ->
                    "Reduce shared memory usage per block to allow more blocks to run concurrently.";
            case "WARPS", "WARP" ->
                    "Warp count is limiting occupancy — minimize divergent branches or increase block size.";
            case "BLOCKS", "BLOCK" ->
                    "Increase the number of blocks or reduce per-block resource usage.";
            default ->
                    "Profile with Nsight Compute to identify the specific resource bottleneck.";
        };
    }

    private static String stallAdvice(int reasonCode) {
        return switch (reasonCode) {
            case 4 -> "Verify global memory accesses are coalesced and consider using shared memory tiling.";
            case 5 -> "Consider caching texture data in shared memory or using L2-friendly access patterns.";
            case 7 -> "Ensure all threads in a warp access the same constant memory address.";
            case 9 -> "Global memory bandwidth is saturated — reduce memory traffic or use compression.";
            case 3 -> "Interleave independent operations to fill the instruction pipeline.";
            case 8 -> "Consider mixed precision or restructuring computation to reduce pipe contention.";
            case 6 -> "Reduce __syncthreads() frequency or redesign data sharing.";
            case 12 -> "Reduce barrier synchronization frequency.";
            default -> "Analyze with Nsight Compute to identify the specific bottleneck.";
        };
    }
}
