package com.gpuflight.gpuflbackend.dao;

import com.gpuflight.gpuflbackend.entity.KernelEventEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
@RequiredArgsConstructor
public class KernelEventDaoImpl implements KernelEventDao {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void saveKernelBegin(KernelEventEntity entity) {
        jdbcTemplate.update(
                "INSERT INTO kernel_events (time, start_ns, session_id, device_uuid, name, corr_id, " +
                        "grid, block, dyn_shared_bytes, num_regs, static_shared_bytes, local_bytes, " +
                        "const_bytes, occupancy, max_active_blocks) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                Instant.ofEpochSecond(0, entity.getStartNs()),
                entity.getStartNs(),
                entity.getSessionId(),
                entity.getDeviceUuid(),
                entity.getName(),
                entity.getCorrId(),
                entity.getGrid(),
                entity.getBlock(),
                entity.getDynSharedBytes(),
                entity.getNumRegs(),
                entity.getStaticSharedBytes(),
                entity.getLocalBytes(),
                entity.getConstBytes(),
                entity.getOccupancy() != null ? entity.getOccupancy().doubleValue() : null,
                entity.getMaxActiveBlocks()
        );
    }

    @Override
    public void updateKernelEnd(String sessionId, long corrId, long endNs, String cudaError) {
        jdbcTemplate.update(
                "UPDATE kernel_events SET end_ns = ?, duration_ns = ? - start_ns, cuda_error = ? " +
                        "WHERE session_id = ? AND corr_id = ?",
                endNs,
                endNs,
                cudaError,
                sessionId,
                corrId
        );
    }
}
