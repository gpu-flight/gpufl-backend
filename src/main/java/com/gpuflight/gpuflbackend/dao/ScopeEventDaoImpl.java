package com.gpuflight.gpuflbackend.dao;

import com.gpuflight.gpuflbackend.entity.ScopeEventEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;

@Repository
@RequiredArgsConstructor
public class ScopeEventDaoImpl implements ScopeEventDao {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void saveScopeEvent(ScopeEventEntity entity) {
        jdbcTemplate.update(
                "INSERT INTO scope_events (time, start_ns, session_id, name, tag, user_scope, scope_depth, created_at, updated_at) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                entity.getTime() != null ? Timestamp.from(entity.getTime()) : null,
                entity.getTsNs(),
                entity.getSessionId(),
                entity.getName(),
                entity.getTag(),
                entity.getUserScope(),
                entity.getScopeDepth()
        );
    }

    @Override
    public void updateScopeEventEnd(ScopeEventEntity entity) {
        jdbcTemplate.update(
                "UPDATE scope_events SET end_ns = ?, updated_at = CURRENT_TIMESTAMP WHERE session_id = ? AND name = ? AND user_scope = ?",
                entity.getTsNs(),
                entity.getSessionId(),
                entity.getName(),
                entity.getUserScope()
        );
    }
}
