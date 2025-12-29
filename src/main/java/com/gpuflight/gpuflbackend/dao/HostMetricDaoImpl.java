package com.gpuflight.gpuflbackend.dao;

import com.gpuflight.gpuflbackend.entity.HostMetricEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;

@Repository
@RequiredArgsConstructor
public class HostMetricDaoImpl implements HostMetricDao {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void saveHostMetric(HostMetricEntity entity) {
        jdbcTemplate.update(
                "INSERT INTO host_metrics (time, ts_ns, session_id, type, cpu_pct, ram_used_mib, created_at, updated_at) " +
                        "VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                entity.getTime() != null ? Timestamp.from(entity.getTime()) : null,
                entity.getTsNs(),
                entity.getSessionId(),
                entity.getType(),
                entity.getCpuPct(),
                entity.getRamUsedMib()
        );
    }
}
