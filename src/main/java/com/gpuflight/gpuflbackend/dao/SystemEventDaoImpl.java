package com.gpuflight.gpuflbackend.dao;

import com.gpuflight.gpuflbackend.entity.SystemEventEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SystemEventDaoImpl implements SystemEventDao {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void saveSystemEvent(SystemEventEntity entity) {
        jdbcTemplate.update(
                "INSERT INTO system_events (session_id, pid, app, name, event_type, ts_ns, created_at, updated_at) " +
                        "VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                entity.getSessionId(),
                entity.getPid(),
                entity.getApp(),
                entity.getName(),
                entity.getEventType(),
                entity.getTsNs()
        );
    }
}
