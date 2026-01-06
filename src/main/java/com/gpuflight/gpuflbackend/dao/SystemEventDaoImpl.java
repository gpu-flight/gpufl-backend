package com.gpuflight.gpuflbackend.dao;

import com.gpuflight.gpuflbackend.entity.SystemEventEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Repository
public class SystemEventDaoImpl implements SystemEventDao {
    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public SystemEventDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    }

    private static final RowMapper<SystemEventEntity> ROW_MAPPER = (rs, _) -> {
        SystemEventEntity.SystemEventEntityBuilder builder = SystemEventEntity.builder()
            .sessionId(rs.getString("session_id"))
            .pid(rs.getInt("pid"))
            .app(rs.getString("app"))
            .name(rs.getString("name"))
            .eventType(rs.getString("event_type"))
            .tsNs(rs.getLong("ts_ns"))
            .createdAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toInstant() : null)
            .updatedAt(rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toInstant() : null);

        try {
            builder.rangeStart(rs.getLong("range_start"));
            builder.rangeEnd(rs.getLong("range_end"));
        } catch (Exception ignored) {
            // These might not exist in all queries
        }

        return builder.build();
    };

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

    @Override
    public List<SystemEventEntity> findBySessionIds(Collection<String> sessionIds) {
        if (sessionIds == null || sessionIds.isEmpty()) {
            return Collections.emptyList();
        }
        String sql = """
            WITH group_ident AS (
                SELECT *,
                       CASE WHEN 
                            session_id = LAG(session_id) OVER (PARTITION BY session_id ORDER BY ts_ns) AND
                            pid = LAG(pid) OVER (PARTITION BY session_id ORDER BY ts_ns) AND
                            app = LAG(app) OVER (PARTITION BY session_id ORDER BY ts_ns) AND
                            name = LAG(name) OVER (PARTITION BY session_id ORDER BY ts_ns) AND
                            event_type = LAG(event_type) OVER (PARTITION BY session_id ORDER BY ts_ns)
                       THEN 0 ELSE 1 END as is_new_group
                FROM system_events
                WHERE session_id IN (:sessionIds)
            ),
            groups AS (
                SELECT *,
                       SUM(is_new_group) OVER (PARTITION BY session_id ORDER BY ts_ns) as group_id
                FROM group_ident
            )
            SELECT 
                session_id, pid, app, name, event_type,
                MIN(ts_ns) as ts_ns,
                MIN(ts_ns) as range_start,
                MAX(ts_ns) as range_end,
                MIN(created_at) as created_at,
                MAX(updated_at) as updated_at
            FROM groups
            GROUP BY session_id, group_id, pid, app, name, event_type
            ORDER BY ts_ns ASC
        """;
        return namedParameterJdbcTemplate.query(sql, Map.of("sessionIds", sessionIds), ROW_MAPPER);
    }

    @Override
    public List<SystemEventEntity> findBySessionId(String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) {
            return Collections.emptyList();
        }
        String sql = """
            WITH group_ident AS (
                SELECT *,
                       CASE WHEN 
                            session_id = LAG(session_id) OVER (PARTITION BY session_id ORDER BY ts_ns) AND
                            pid = LAG(pid) OVER (PARTITION BY session_id ORDER BY ts_ns) AND
                            app = LAG(app) OVER (PARTITION BY session_id ORDER BY ts_ns) AND
                            name = LAG(name) OVER (PARTITION BY session_id ORDER BY ts_ns) AND
                            event_type = LAG(event_type) OVER (PARTITION BY session_id ORDER BY ts_ns)
                       THEN 0 ELSE 1 END as is_new_group
                FROM system_events
                WHERE session_id = ?
            ),
            groups AS (
                SELECT *,
                       SUM(is_new_group) OVER (PARTITION BY session_id ORDER BY ts_ns) as group_id
                FROM group_ident
            )
            SELECT 
                session_id, pid, app, name, event_type,
                MIN(ts_ns) as ts_ns,
                MIN(ts_ns) as range_start,
                MAX(ts_ns) as range_end,
                MIN(created_at) as created_at,
                MAX(updated_at) as updated_at
            FROM groups
            GROUP BY session_id, group_id, pid, app, name, event_type
            ORDER BY ts_ns ASC
        """;
        return jdbcTemplate.query(sql, ROW_MAPPER, sessionId);
    }
}
