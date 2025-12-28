package com.gpuflight.gpuflbackend.dao;

import com.gpuflight.gpuflbackend.entity.KernelEventEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;

@Repository
@RequiredArgsConstructor
public class KernelEventDaoImpl implements KernelEventDao {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void saveKernelBegin(KernelEventEntity entity) {
        jdbcTemplate.update(
                "INSERT INTO kernel_events (time, start_ns, session_id, device_uuid, name, corr_id, has_details, extra_params, created_at, updated_at) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?::jsonb, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                entity.getTime() != null ? Timestamp.from(entity.getTime()) : null,
                entity.getStartNs(),
                entity.getSessionId(),
                entity.getDeviceUuid(),
                entity.getName(),
                entity.getCorrId(),
                entity.getHasDetails(),
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
