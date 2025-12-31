package com.gpuflight.gpuflbackend.dao;

import com.gpuflight.gpuflbackend.entity.SessionEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;

@Repository
@RequiredArgsConstructor
@Slf4j
public class SessionDaoImpl implements SessionDao {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void saveSession(SessionEntity entity) {
        String sql = "INSERT INTO sessions (session_id, app_name, hostname, ip_addr, start_time, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP) ON CONFLICT (session_id) DO UPDATE SET " +
                     "app_name = EXCLUDED.app_name, start_time = EXCLUDED.start_time, updated_at = CURRENT_TIMESTAMP";

        Object[] params = {
                entity.getSessionId(),
                entity.getAppName(),
                entity.getHostname(),
                entity.getIpAddr(),
                entity.getStartTime() != null ? Timestamp.from(entity.getStartTime()) : null
        };

        log.trace("saveSession called for sessionId: {}, appName: {}", entity.getSessionId(), entity.getAppName());

        jdbcTemplate.update(sql, params);
    }

    @Override
    public void updateSessionEndTime(SessionEntity entity) {
        String sql = "UPDATE sessions SET end_time = ?, updated_at = CURRENT_TIMESTAMP WHERE session_id = ?";

        Object[] params = {
                entity.getEndTime() != null ? Timestamp.from(entity.getEndTime()) : null,
                entity.getSessionId()
        };

        log.trace("updateSessionEndTime called for sessionId: {}", entity.getSessionId());

        jdbcTemplate.update(sql, params);
    }
}
