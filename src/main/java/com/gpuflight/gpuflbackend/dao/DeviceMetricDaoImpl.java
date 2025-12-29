package com.gpuflight.gpuflbackend.dao;

import com.gpuflight.gpuflbackend.entity.DeviceMetricEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;

@Repository
@RequiredArgsConstructor
public class DeviceMetricDaoImpl implements DeviceMetricDao {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void saveDeviceMetric(DeviceMetricEntity entity) {
        jdbcTemplate.update(
                "INSERT INTO device_metrics (time, ts_ns, session_id, device_uuid, power_watts, temp_c, util_gpu_pct, util_mem_pct, mem_used_mib, extended_metrics, created_at, updated_at) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?::jsonb, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                entity.getTime() != null ? Timestamp.from(entity.getTime()) : null,
                entity.getTsNs(),
                entity.getSessionId(),
                entity.getDeviceUuid(),
                entity.getPowerWatts(),
                entity.getTempC(),
                entity.getUtilGpuPct(),
                entity.getUtilMemPct(),
                entity.getMemUsedMib(),
                entity.getExtendedMetrics()
        );
    }
}
