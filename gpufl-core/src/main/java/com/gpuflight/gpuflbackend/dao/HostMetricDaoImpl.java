package com.gpuflight.gpuflbackend.dao;

import com.gpuflight.gpuflbackend.entity.HostMetricEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import java.sql.Timestamp;
import java.util.*;

@Repository
public class HostMetricDaoImpl implements HostMetricDao {
    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public HostMetricDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    }

    @Override
    public void saveHostMetric(HostMetricEntity entity) {
        jdbcTemplate.update(
            "INSERT INTO host_metrics (time, session_id, ts_ns, cpu_pct, ram_used_mib, ram_total_mib, created_at, updated_at) " +
            "VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
            entity.getTime() != null ? Timestamp.from(entity.getTime()) : null,
            entity.getSessionId(), entity.getTsNs(),
            entity.getCpuPct(), entity.getRamUsedMib(), entity.getRamTotalMib()
        );
    }

    @Override
    public void saveBatch(List<HostMetricEntity> entities) {
        jdbcTemplate.batchUpdate(
            "INSERT INTO host_metrics (time, session_id, ts_ns, cpu_pct, ram_used_mib, ram_total_mib, created_at, updated_at) " +
            "VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
            entities, entities.size(), (ps, e) -> {
                ps.setTimestamp(1, e.getTime() != null ? Timestamp.from(e.getTime()) : null);
                ps.setString(2, e.getSessionId());
                ps.setLong(3, e.getTsNs());
                ps.setDouble(4, e.getCpuPct());
                ps.setLong(5, e.getRamUsedMib());
                ps.setLong(6, e.getRamTotalMib());
            }
        );
    }

    @Override
    public List<HostMetricEntity> findBySessionIds(Collection<String> sessionIds) {
        if (sessionIds == null || sessionIds.isEmpty()) return Collections.emptyList();
        return namedParameterJdbcTemplate.query(
            "SELECT id, time, session_id, ts_ns, cpu_pct, ram_used_mib, ram_total_mib, created_at, updated_at FROM host_metrics WHERE session_id IN (:sessionIds) ORDER BY time DESC",
            Map.of("sessionIds", sessionIds),
            (rs, n) -> HostMetricEntity.builder()
                .id(rs.getString("id"))
                .time(rs.getTimestamp("time") != null ? rs.getTimestamp("time").toInstant() : null)
                .sessionId(rs.getString("session_id"))
                .tsNs(rs.getLong("ts_ns"))
                .cpuPct(rs.getDouble("cpu_pct"))
                .ramUsedMib(rs.getLong("ram_used_mib"))
                .ramTotalMib(rs.getLong("ram_total_mib"))
                .build()
        );
    }
}
