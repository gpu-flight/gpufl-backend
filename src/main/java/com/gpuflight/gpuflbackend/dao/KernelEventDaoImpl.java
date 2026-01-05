package com.gpuflight.gpuflbackend.dao;

import com.gpuflight.gpuflbackend.entity.KernelEventEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;

@Repository
@RequiredArgsConstructor
public class KernelEventDaoImpl implements KernelEventDao {
    private final JdbcTemplate jdbcTemplate;

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
}
