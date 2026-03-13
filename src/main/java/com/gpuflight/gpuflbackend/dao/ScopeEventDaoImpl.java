package com.gpuflight.gpuflbackend.dao;

import com.gpuflight.gpuflbackend.entity.ScopeEventEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Repository
public class ScopeEventDaoImpl implements ScopeEventDao {
    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public ScopeEventDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    }

    private static final RowMapper<ScopeEventEntity> ROW_MAPPER = (rs, rowNum) -> ScopeEventEntity.builder()
            .id(rs.getString("id"))
            .time(rs.getTimestamp("time") != null ? rs.getTimestamp("time").toInstant() : null)
            .tsNs(rs.getLong("start_ns"))
            .endNs(rs.getObject("end_ns") != null ? rs.getLong("end_ns") : null)
            .sessionId(rs.getString("session_id"))
            .name(rs.getString("name"))
            .tag(rs.getString("tag"))
            .userScope(rs.getString("user_scope"))
            .scopeDepth(rs.getInt("scope_depth"))
            .createdAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toInstant() : null)
            .updatedAt(rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toInstant() : null)
            .build();

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

    @Override
    public List<ScopeEventEntity> findBySessionIds(Collection<String> sessionIds) {
        if (sessionIds == null || sessionIds.isEmpty()) {
            return Collections.emptyList();
        }
        String sql = "SELECT * FROM scope_events WHERE session_id IN (:sessionIds) ORDER BY time ASC";
        return namedParameterJdbcTemplate.query(sql, Map.of("sessionIds", sessionIds), ROW_MAPPER);
    }
}
