package com.gpuflight.gpuflbackend.dao;

import com.gpuflight.gpuflbackend.entity.InitialEventEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class InitDaoImpl implements InitDao {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void saveInitialEvent(InitialEventEntity entity) {
        String sql = "INSERT INTO initial_events (session_id, pid, app, log_path, system_rate_ms, ts_ns, shutdown_ts_ns, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP) ON CONFLICT (session_id) DO NOTHING";

        Object[] params = {
                entity.getSessionId(),
                entity.getPid(),
                entity.getApp(),
                entity.getLogPath(),
                entity.getSystemRateMs(),
                entity.getTsNs(),
                entity.getShutdownTsNs()
        };

        log.trace("saveInitialEvent called for sessionId: {}", entity.getSessionId());

        jdbcTemplate.update(sql, params);
    }

    @Override
    public boolean existsBySessionId(String sessionId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM initial_events WHERE session_id = ?",
                Integer.class,
                sessionId
        );
        return count != null && count > 0;
    }

    @Override
    public List<InitialEventEntity> findByDateRange(Instant dateFrom, Instant dateTo) {
        String sql = "SELECT session_id, pid, app, log_path, system_rate_ms, time, ts_ns, created_at, updated_at " +
                     "FROM initial_events " +
                     "WHERE time >= ? AND time <= ? " +
                     "ORDER BY time DESC";

        return jdbcTemplate.query(sql, (rs, rowNum) -> InitialEventEntity.builder()
                .sessionId(rs.getString("session_id"))
                .pid(rs.getInt("pid"))
                .app(rs.getString("app"))
                .logPath(rs.getString("log_path"))
                .systemRateMs(rs.getInt("system_rate_ms"))
                .tsNs(rs.getLong("ts_ns"))
                .shutdownTsNs(rs.getLong("shutdown_ts_ns"))
                .createdAt(rs.getTimestamp("created_at").toInstant())
                .updatedAt(rs.getTimestamp("updated_at").toInstant())
                .build(),
                Timestamp.from(dateFrom), Timestamp.from(dateTo));
    }

    public void shutdownEvent(String sessionId, String app, Long shutdownTsNs) {
        jdbcTemplate.update("UPDATE initial_events SET shutdown_ts_ns = ?, app = ? WHERE session_id = ?", shutdownTsNs, app, sessionId);
    }
}
