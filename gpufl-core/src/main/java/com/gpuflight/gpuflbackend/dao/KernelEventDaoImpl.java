package com.gpuflight.gpuflbackend.dao;

import com.gpuflight.gpuflbackend.entity.KernelEventEntity;
import com.gpuflight.gpuflbackend.model.input.KernelDetailEvent;
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
            .streamId(rs.getLong("stream_id"))
            .apiStartNs(rs.getLong("api_start_ns"))
            .apiExitNs(rs.getLong("api_exit_ns"))
            .regOccupancy(rs.getBigDecimal("reg_occupancy"))
            .smemOccupancy(rs.getBigDecimal("smem_occupancy"))
            .warpOccupancy(rs.getBigDecimal("warp_occupancy"))
            .blockOccupancy(rs.getBigDecimal("block_occupancy"))
            .limitingResource(rs.getString("limiting_resource"))
            .localMemTotalBytes(rs.getLong("local_mem_total_bytes"))
            .localMemPerThreadBytes(rs.getLong("local_mem_per_thread_bytes"))
            .cacheConfigRequested(rs.getInt("cache_config_requested"))
            .cacheConfigExecuted(rs.getInt("cache_config_executed"))
            .sharedMemExecutedBytes(rs.getLong("shared_mem_executed_bytes"))
            .createdAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toInstant() : null)
            .updatedAt(rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toInstant() : null)
            .build();

    @Override
    public void saveKernelEvent(KernelEventEntity entity) {
        String sql = """
            INSERT INTO kernel_events (
                    time, start_ns, end_ns, duration_ns,
                    api_start_ns, api_exit_ns, stream_id,
                    session_id, device_id,
                    pid, app, platform, name, corr_id, has_details,
                    grid, block, dyn_shared_bytes, num_regs, static_shared_bytes,
                    local_bytes, const_bytes, occupancy, max_active_blocks,
                    reg_occupancy, smem_occupancy, warp_occupancy, block_occupancy,
                    limiting_resource,
                    local_mem_total_bytes, local_mem_per_thread_bytes,
                    cache_config_requested, cache_config_executed,
                    shared_mem_executed_bytes,
                    stack_trace, user_scope, scope_depth,
                    created_at, updated_at
            ) VALUES (
                    ?, ?, ?, ?,
                    ?, ?, ?,
                    ?, ?,
                    ?, ?, ?, ?, ?, ?,
                    ?, ?, ?, ?, ?,
                    ?, ?, ?, ?,
                    ?, ?, ?, ?,
                    ?,
                    ?, ?,
                    ?, ?,
                    ?,
                    ?, ?, ?,
                    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
            )
        """;

        jdbcTemplate.update(sql,
                entity.getTime() != null ? Timestamp.from(entity.getTime()) : null,
                entity.getStartNs(),
                entity.getEndNs(),
                entity.getDurationNs(),
                entity.getApiStartNs(),
                entity.getApiExitNs(),
                entity.getStreamId(),
                entity.getSessionId(),
                entity.getDeviceId(),
                entity.getPid(),
                entity.getApp(),
                entity.getPlatform(),
                entity.getName(),
                entity.getCorrId(),
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
                entity.getRegOccupancy(),
                entity.getSmemOccupancy(),
                entity.getWarpOccupancy(),
                entity.getBlockOccupancy(),
                entity.getLimitingResource(),
                entity.getLocalMemTotalBytes(),
                entity.getLocalMemPerThreadBytes(),
                entity.getCacheConfigRequested(),
                entity.getCacheConfigExecuted(),
                entity.getSharedMemExecutedBytes(),
                entity.getStackTrace(),
                entity.getUserScope(),
                entity.getScopeDepth()
        );
    }

    @Override
    public void saveMinimal(String sessionId, long startNs, long endNs, long durationNs,
                            long streamId, String name, long corrId, boolean hasDetails,
                            int dynSharedBytes, int numRegs) {
        jdbcTemplate.update("""
            INSERT INTO kernel_events (time, start_ns, end_ns, duration_ns, session_id,
                device_id, name, corr_id, has_details, dyn_shared_bytes, num_regs,
                stream_id, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, 0, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            """,
            Timestamp.from(java.time.Instant.ofEpochSecond(0, startNs)),
            startNs, endNs, durationNs, sessionId,
            name, corrId, hasDetails, dynSharedBytes, numRegs, streamId
        );
    }

    @Override
    public void updateDetails(String sessionId, long corrId, KernelDetailEvent d) {
        jdbcTemplate.update("""
            UPDATE kernel_events SET
                grid=?, block=?, static_shared_bytes=?, local_bytes=?, const_bytes=?,
                occupancy=?, reg_occupancy=?, smem_occupancy=?, warp_occupancy=?, block_occupancy=?,
                limiting_resource=?, max_active_blocks=?,
                local_mem_total_bytes=?, local_mem_per_thread_bytes=?,
                cache_config_requested=?, cache_config_executed=?, shared_mem_executed_bytes=?,
                user_scope=?, stack_trace=?, updated_at=CURRENT_TIMESTAMP
            WHERE session_id=? AND corr_id=?
            """,
            d.grid(), d.block(), d.staticShared(), d.localBytes(), d.constBytes(),
            d.occupancy(), d.regOccupancy(), d.smemOccupancy(), d.warpOccupancy(), d.blockOccupancy(),
            d.limitingResource(), d.maxActiveBlocks(),
            d.localMemTotalBytes(), d.localMemPerThreadBytes(),
            d.cacheConfigRequested(), d.cacheConfigExecuted(), d.sharedMemExecutedBytes(),
            d.userScope(), d.stackTrace(),
            sessionId, corrId
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
