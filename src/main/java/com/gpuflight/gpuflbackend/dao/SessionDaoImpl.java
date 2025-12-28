package com.gpuflight.gpuflbackend.dao;

import com.gpuflight.gpuflbackend.entity.SessionEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;

@Repository
@RequiredArgsConstructor
public class SessionDaoImpl implements SessionDao {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void saveSession(SessionEntity entity) {
        jdbcTemplate.update(
                "INSERT INTO sessions (session_id, app_name, hostname, pid, start_time, created_at, updated_at) " +
                        "VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP) ON CONFLICT (session_id) DO UPDATE SET " +
                        "app_name = EXCLUDED.app_name, pid = EXCLUDED.pid, start_time = EXCLUDED.start_time, updated_at = CURRENT_TIMESTAMP",
                entity.getSessionId(),
                entity.getAppName(),
                entity.getHostname(),
                entity.getPid(),
                entity.getStartTime() != null ? Timestamp.from(entity.getStartTime()) : null
        );
    }

    @Override
    public void updateSessionEndTime(SessionEntity entity) {
        jdbcTemplate.update(
                "UPDATE sessions SET end_time = ?, updated_at = CURRENT_TIMESTAMP WHERE session_id = ?",
                entity.getEndTime() != null ? Timestamp.from(entity.getEndTime()) : null,
                entity.getSessionId()
        );
    }
}
