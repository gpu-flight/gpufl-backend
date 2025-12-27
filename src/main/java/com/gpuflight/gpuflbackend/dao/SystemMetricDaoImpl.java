package com.gpuflight.gpuflbackend.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
@RequiredArgsConstructor
public class SystemMetricDaoImpl implements SystemMetricDao {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void saveSystemMetric(Instant time, long tsNs, String sessionId, String deviceUuid, Double powerWatts, Integer tempC, Integer utilGpuPct, Integer utilMemPct, Long memUsedMib, String extendedMetricsJson) {
        jdbcTemplate.update(
                "INSERT INTO system_metrics (time, ts_ns, session_id, device_uuid, power_watts, temp_c, util_gpu_pct, util_mem_pct, mem_used_mib, extended_metrics) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?::jsonb)",
                time,
                tsNs,
                sessionId,
                deviceUuid,
                powerWatts,
                tempC,
                utilGpuPct,
                utilMemPct,
                memUsedMib,
                extendedMetricsJson
        );
    }
}
