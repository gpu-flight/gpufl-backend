package com.gpuflight.gpuflbackend.dao;

import com.gpuflight.gpuflbackend.model.HostSample;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
@RequiredArgsConstructor
public class ScopeEventDaoImpl implements ScopeEventDao {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void saveScopeEvent(long tsNs, String sessionId, String type, String name, String tag, HostSample host) {
        jdbcTemplate.update(
                "INSERT INTO scope_events (time, ts_ns, session_id, type, name, tag, host_cpu_pct, host_ram_used_mib) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                Instant.ofEpochSecond(0, tsNs),
                tsNs,
                sessionId,
                type,
                name,
                tag,
                host != null ? host.cpuPct() : null,
                host != null ? host.ramUsedMib() : null
        );
    }

    @Override
    public void updateScopeEvent(String sessionId, String name, String type, long tsNs) {
        jdbcTemplate.update(
                "UPDATE scope_events SET time = ?, ts_ns = ? WHERE session_id = ? AND name = ? AND type = ?",
                Instant.ofEpochSecond(0, tsNs),
                tsNs,
                sessionId,
                name,
                type
        );
    }
}
