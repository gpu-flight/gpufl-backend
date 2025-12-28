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
                "INSERT INTO scope_events (time, ts_ns, session_id, type, name, tag, host_cpu_pct, host_ram_used_mib, created_at, updated_at) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                entity.getTime() != null ? Timestamp.from(entity.getTime()) : null,
                entity.getTsNs(),
                entity.getSessionId(),
                entity.getType(),
                entity.getName(),
                entity.getTag(),
                entity.getHostCpuPct(),
                entity.getHostRamUsedMib()
        );
    }

    @Override
    public void updateScopeEvent(ScopeEventEntity entity) {
        jdbcTemplate.update(
                "UPDATE scope_events SET time = ?, ts_ns = ?, updated_at = CURRENT_TIMESTAMP WHERE session_id = ? AND name = ? AND type = ?",
                entity.getTime() != null ? Timestamp.from(entity.getTime()) : null,
                entity.getTsNs(),
                entity.getSessionId(),
                entity.getName(),
                entity.getType()
        );
    }
}
