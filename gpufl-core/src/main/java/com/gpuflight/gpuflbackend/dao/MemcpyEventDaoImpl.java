package com.gpuflight.gpuflbackend.dao;

import com.gpuflight.gpuflbackend.entity.MemcpyEventEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Repository
public class MemcpyEventDaoImpl implements MemcpyEventDao {
    private final JdbcTemplate jdbcTemplate;

    public MemcpyEventDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void saveBatch(List<MemcpyEventEntity> entities) {
        jdbcTemplate.batchUpdate(
            "INSERT INTO memcpy_events (time, session_id, start_ns, duration_ns, stream_id, bytes, copy_kind, corr_id, created_at) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)",
            entities, entities.size(), (ps, e) -> {
                ps.setTimestamp(1, Timestamp.from(Instant.ofEpochSecond(0, e.getStartNs())));
                ps.setString(2, e.getSessionId());
                ps.setLong(3, e.getStartNs());
                ps.setLong(4, e.getDurationNs());
                ps.setLong(5, e.getStreamId());
                ps.setLong(6, e.getBytes());
                ps.setInt(7, e.getCopyKind());
                ps.setLong(8, e.getCorrId());
            }
        );
    }
}
