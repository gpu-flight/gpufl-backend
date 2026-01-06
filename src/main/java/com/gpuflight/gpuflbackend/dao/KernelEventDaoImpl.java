package com.gpuflight.gpuflbackend.dao;

import com.gpuflight.gpuflbackend.entity.KernelEventEntity;
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
public class KernelEventDaoImpl implements KernelEventDao {
    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public KernelEventDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    }

    private static final RowMapper<KernelEventEntity> ROW_MAPPER = (rs, rowNum) -> KernelEventEntity.builder()
            .id(rs.getString("id"))
            .time(rs.getTimestamp("time") != null ? rs.getTimestamp("time").toInstant() : null)
            .startNs(rs.getLong("start_ns"))
            .endNs(rs.getLong("end_ns"))
            .durationNs(rs.getLong("duration_ns"))
            .sessionId(rs.getString("session_id"))
            .deviceId(rs.getInt("device_id"))
            .pid(rs.getInt("pid"))
            .app(rs.getString("app"))
            .platform(rs.getString("platform"))
            .name(rs.getString("name"))
            .corrId(rs.getLong("corr_id"))
            .cudaError(rs.getString("cuda_error"))
            .hasDetails(rs.getBoolean("has_details"))
            .grid(rs.getString("grid"))
            .block(rs.getString("block"))
            .dynSharedBytes(rs.getLong("dyn_shared_bytes"))
            .numRegs(rs.getInt("num_regs"))
            .staticSharedBytes(rs.getLong("static_shared_bytes"))
            .localBytes(rs.getLong("local_bytes"))
            .constBytes(rs.getLong("const_bytes"))
            .occupancy(rs.getBigDecimal("occupancy"))
            .maxActiveBlocks(rs.getLong("max_active_blocks"))
            .stackTrace(rs.getString("stack_trace"))
            .userScope(rs.getString("user_scope"))
            .scopeDepth(rs.getInt("scope_depth"))
            .extraParams(rs.getString("extra_params"))
            .createdAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toInstant() : null)
            .updatedAt(rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toInstant() : null)
            .build();

    @Override
    public void saveKernelBegin(KernelEventEntity entity) {
        String sql = """
            INSERT INTO kernel_events (
                    time, start_ns, end_ns, duration_ns, session_id, device_id,
                    pid, app, platform, name, corr_id, cuda_error, has_details,
                    grid, block, dyn_shared_bytes, num_regs, static_shared_bytes,
                    local_bytes, const_bytes, occupancy, max_active_blocks,
                    stack_trace, user_scope, scope_depth,
                    extra_params, created_at, updated_at
            ) VALUES (
                    ?, ?, ?, ?, ?, ?,
                    ?, ?, ?, ?, ?, ?, ?,
                    ?, ?, ?, ?, ?,
                    ?, ?, ?, ?,
                    ?, ?, ?,
                    ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
            )
        """;

        jdbcTemplate.update(sql,
                entity.getTime() != null ? Timestamp.from(entity.getTime()) : null,
                entity.getStartNs(),
                entity.getEndNs(),
                entity.getDurationNs(),
                entity.getSessionId(),
                entity.getDeviceId(),
                entity.getPid(),
                entity.getApp(),
                entity.getPlatform(),
                entity.getName(),
                entity.getCorrId(),
                entity.getCudaError(),
                entity.getHasDetails(),
                entity.getGrid(),
                entity.getBlock(),
                entity.getDynSharedBytes(),
                entity.getNumRegs(),
                entity.getStaticSharedBytes(),
                entity.getLocalBytes(),
                entity.getConstBytes(),
                entity.getOccupancy(),
                entity.getMaxActiveBlocks(),
                entity.getStackTrace(),
                entity.getUserScope(),
                entity.getScopeDepth(),
                entity.getExtraParams()
        );
    }

    @Override
    public void updateKernelEnd(KernelEventEntity entity) {
        jdbcTemplate.update(
                "UPDATE kernel_events SET end_ns = ?, duration_ns = ? - start_ns, cuda_error = ?, updated_at = CURRENT_TIMESTAMP " +
                        "WHERE session_id = ? AND corr_id = ?",
                entity.getEndNs(),
                entity.getEndNs(),
                entity.getCudaError(),
                entity.getSessionId(),
                entity.getCorrId()
        );
    }

    @Override
    public List<KernelEventEntity> findBySessionIds(Collection<String> sessionIds) {
        if (sessionIds == null || sessionIds.isEmpty()) {
            return Collections.emptyList();
        }
        String sql = "SELECT * FROM kernel_events WHERE session_id IN (:sessionIds) ORDER BY time ASC";
        return namedParameterJdbcTemplate.query(sql, Map.of("sessionIds", sessionIds), ROW_MAPPER);
    }
}
