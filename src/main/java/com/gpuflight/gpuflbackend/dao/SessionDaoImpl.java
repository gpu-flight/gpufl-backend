package com.gpuflight.gpuflbackend.dao;

import com.gpuflight.gpuflbackend.entity.SessionEntity;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class SessionDaoImpl implements SessionDao {
    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public SessionDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    }

    private static final RowMapper<SessionEntity> ROW_MAPPER = (rs, rowNum) -> SessionEntity.builder()
            .sessionId(rs.getString("session_id"))
            .appName(rs.getString("app_name"))
            .hostname(rs.getString("hostname"))
            .ipAddr(rs.getString("ip_addr"))
            .startTime(rs.getTimestamp("start_time") != null ? rs.getTimestamp("start_time").toInstant() : null)
            .endTime(rs.getTimestamp("end_time") != null ? rs.getTimestamp("end_time").toInstant() : null)
            .createdAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toInstant() : null)
            .updatedAt(rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toInstant() : null)
            .build();

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

    @Override
    public List<SessionEntity> findBySessionIds(Collection<String> sessionIds) {
        if (sessionIds == null || sessionIds.isEmpty()) {
            return Collections.emptyList();
        }
        String sql = "SELECT * FROM sessions WHERE session_id IN (:sessionIds)";
        return namedParameterJdbcTemplate.query(sql, Map.of("sessionIds", sessionIds), ROW_MAPPER);
    }
}
