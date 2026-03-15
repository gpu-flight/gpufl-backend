package com.gpuflight.gpuflbackend.dao;

import com.gpuflight.gpuflbackend.entity.HostMetricEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Repository
public class HostMetricDaoImpl implements HostMetricDao {
    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private static final String SELECT_COLUMNS = "id, time, event_type, ts_ns, session_id, hostname, ip_addr, cpu_pct, ram_used_mib, ram_total_mib, created_at, updated_at";

    private static final RowMapper<HostMetricEntity> ROW_MAPPER = (rs, rowNum) -> mapRow(rs);

    public HostMetricDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    }

    private static HostMetricEntity mapRow(ResultSet rs) throws SQLException {
        return HostMetricEntity.builder()
                .id(rs.getString("id"))
                .time(rs.getTimestamp("time") != null ? rs.getTimestamp("time").toInstant() : null)
                .hostname(rs.getString("hostname"))
                .ipAddr(rs.getString("ip_addr"))
                .eventType(rs.getString("event_type"))
                .tsNs(rs.getLong("ts_ns"))
                .sessionId(rs.getString("session_id"))
                .cpuPct(rs.getDouble("cpu_pct"))
                .ramUsedMib(rs.getLong("ram_used_mib"))
                .ramTotalMib(rs.getLong("ram_total_mib"))
                .createdAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toInstant() : null)
                .updatedAt(rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toInstant() : null)
                .build();
    }

    @Override
    public void saveHostMetric(HostMetricEntity entity) {
        jdbcTemplate.update(
                "INSERT INTO host_metrics (time, event_type, ts_ns, session_id, hostname, ip_addr, cpu_pct, ram_used_mib, ram_total_mib, created_at, updated_at) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                entity.getTime() != null ? Timestamp.from(entity.getTime()) : null,
                entity.getEventType(),
                entity.getTsNs(),
                entity.getSessionId(),
                entity.getHostname(),
                entity.getIpAddr(),
                entity.getCpuPct(),
                entity.getRamUsedMib(),
                entity.getRamTotalMib()
        );
    }

    @Override
    public List<HostMetricEntity> findBySessionIds(Collection<String> sessionIds) {
        if (sessionIds == null || sessionIds.isEmpty()) {
            return Collections.emptyList();
        }
        String sql = "SELECT " + SELECT_COLUMNS + " FROM host_metrics WHERE session_id IN (:sessionIds) ORDER BY time DESC";
        return namedParameterJdbcTemplate.query(sql, Map.of("sessionIds", sessionIds), ROW_MAPPER);
    }
}
