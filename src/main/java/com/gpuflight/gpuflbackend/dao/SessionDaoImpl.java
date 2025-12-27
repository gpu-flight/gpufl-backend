package com.gpuflight.gpuflbackend.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
@RequiredArgsConstructor
public class SessionDaoImpl implements SessionDao {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void saveSession(String sessionId, String appName, int pid, long tsNs) {
        jdbcTemplate.update(
                "INSERT INTO sessions (session_id, app_name, hostname, pid, start_time) " +
                        "VALUES (?, ?, ?, ?, ?) ON CONFLICT (session_id) DO UPDATE SET " +
                        "app_name = EXCLUDED.app_name, pid = EXCLUDED.pid, start_time = EXCLUDED.start_time",
                sessionId,
                appName,
                null,
                pid,
                Instant.ofEpochSecond(0, tsNs)
        );
    }

    @Override
    public void updateSessionEndTime(String sessionId, long tsNs) {
        jdbcTemplate.update(
                "UPDATE sessions SET end_time = ? WHERE session_id = ?",
                Instant.ofEpochSecond(0, tsNs),
                sessionId
        );
    }
}
