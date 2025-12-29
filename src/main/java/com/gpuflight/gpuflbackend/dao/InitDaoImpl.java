package com.gpuflight.gpuflbackend.dao;

import com.gpuflight.gpuflbackend.entity.InitialEventEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;

@Repository
@RequiredArgsConstructor
@Slf4j
public class InitDaoImpl implements InitDao {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void saveInitialEvent(InitialEventEntity entity) {
        String sql = "INSERT INTO initial_events (session_id, time, ts_ns, event_json, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?::jsonb, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP) ON CONFLICT (session_id) DO NOTHING";

        Object[] params = {
                entity.getSessionId(),
                entity.getTime() != null ? Timestamp.from(entity.getTime()) : null,
                entity.getTsNs(),
                entity.getEventJson()
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
}
