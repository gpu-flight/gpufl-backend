package com.gpuflight.gpuflbackend.dao;

import com.gpuflight.gpuflbackend.entity.DeviceMetricEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import java.sql.Timestamp;
import java.util.*;

@Repository
public class DeviceMetricDaoImpl implements DeviceMetricDao {
    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public DeviceMetricDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    }

    @Override
    public void saveDeviceMetric(DeviceMetricEntity entity) {
        jdbcTemplate.update(
            "INSERT INTO device_metrics (time, session_id, ts_ns, device_id, gpu_util, mem_util, temp_c, power_mw, used_mib, created_at, updated_at) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
            entity.getTime() != null ? Timestamp.from(entity.getTime()) : null,
            entity.getSessionId(), entity.getTsNs(), entity.getDeviceId(),
            entity.getGpuUtil(), entity.getMemUtil(), entity.getTempC(),
            entity.getPowerMw(), entity.getUsedMib()
        );
    }

    @Override
    public void saveBatch(List<DeviceMetricEntity> entities) {
        jdbcTemplate.batchUpdate(
            "INSERT INTO device_metrics (time, session_id, ts_ns, device_id, gpu_util, mem_util, temp_c, power_mw, used_mib, created_at, updated_at) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
            entities, entities.size(), (ps, e) -> {
                ps.setTimestamp(1, e.getTime() != null ? Timestamp.from(e.getTime()) : null);
                ps.setString(2, e.getSessionId());
                ps.setLong(3, e.getTsNs());
                ps.setInt(4, e.getDeviceId());
                ps.setObject(5, e.getGpuUtil());
                ps.setObject(6, e.getMemUtil());
                ps.setObject(7, e.getTempC());
                ps.setObject(8, e.getPowerMw());
                ps.setObject(9, e.getUsedMib());
            }
        );
    }

    @Override
    public List<DeviceMetricEntity> findBySessionId(String sessionId) {
        return jdbcTemplate.query(
            "SELECT id, time, session_id, ts_ns, device_id, gpu_util, mem_util, temp_c, power_mw, used_mib, created_at, updated_at FROM device_metrics WHERE session_id = ? ORDER BY time DESC",
            (rs, n) -> DeviceMetricEntity.builder()
                .id(UUID.fromString(rs.getString("id")))
                .time(rs.getTimestamp("time") != null ? rs.getTimestamp("time").toInstant() : null)
                .sessionId(rs.getString("session_id"))
                .tsNs(rs.getLong("ts_ns"))
                .deviceId((Integer) rs.getObject("device_id"))
                .gpuUtil((Integer) rs.getObject("gpu_util"))
                .memUtil((Integer) rs.getObject("mem_util"))
                .tempC((Integer) rs.getObject("temp_c"))
                .powerMw((Integer) rs.getObject("power_mw"))
                .usedMib((Long) rs.getObject("used_mib"))
                .build(),
            sessionId
        );
    }

    @Override
    public List<DeviceMetricEntity> findBySessionIds(Collection<String> sessionIds) {
        if (sessionIds == null || sessionIds.isEmpty()) return Collections.emptyList();
        return namedParameterJdbcTemplate.query(
            "SELECT id, time, session_id, ts_ns, device_id, gpu_util, mem_util, temp_c, power_mw, used_mib, created_at, updated_at FROM device_metrics WHERE session_id IN (:sessionIds) ORDER BY time DESC",
            Map.of("sessionIds", sessionIds),
            (rs, n) -> DeviceMetricEntity.builder()
                .id(UUID.fromString(rs.getString("id")))
                .time(rs.getTimestamp("time") != null ? rs.getTimestamp("time").toInstant() : null)
                .sessionId(rs.getString("session_id"))
                .tsNs(rs.getLong("ts_ns"))
                .deviceId((Integer) rs.getObject("device_id"))
                .gpuUtil((Integer) rs.getObject("gpu_util"))
                .memUtil((Integer) rs.getObject("mem_util"))
                .tempC((Integer) rs.getObject("temp_c"))
                .powerMw((Integer) rs.getObject("power_mw"))
                .usedMib((Long) rs.getObject("used_mib"))
                .build()
        );
    }
}
