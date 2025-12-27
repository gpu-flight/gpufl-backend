package com.gpuflight.gpuflbackend.dao;

import java.time.Instant;

public interface SystemMetricDao {
    void saveSystemMetric(Instant time, long tsNs, String sessionId, String deviceUuid, Double powerWatts, Integer tempC, Integer utilGpuPct, Integer utilMemPct, Long memUsedMib, String extendedMetricsJson);
}
