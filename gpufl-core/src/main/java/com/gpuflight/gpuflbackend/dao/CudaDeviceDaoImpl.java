package com.gpuflight.gpuflbackend.dao;

import com.gpuflight.gpuflbackend.entity.CudaStaticDeviceEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Repository
@Slf4j
public class CudaDeviceDaoImpl implements CudaDeviceDao {
    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private static final String SELECT_COLUMNS = "id, name, session_id, uuid, device_id, compute_major, compute_minor, " +
            "l2_cache_size_bytes, shared_mem_per_block_bytes, regs_per_block, multi_processor_count, warp_size, " +
            "created_at, updated_at";

    private static final RowMapper<CudaStaticDeviceEntity> ROW_MAPPER = (rs, rowNum) -> mapRow(rs);

    public CudaDeviceDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    }

    private static CudaStaticDeviceEntity mapRow(ResultSet rs) throws SQLException {
        return CudaStaticDeviceEntity.builder()
                .id(rs.getString("id"))
                .sessionId(rs.getString("session_id"))
                .uuid(rs.getString("uuid"))
                .name(rs.getString("name"))
                .deviceId((Integer) rs.getObject("device_id"))
                .computeMajor(rs.getString("compute_major"))
                .computeMinor(rs.getString("compute_minor"))
                .l2CacheSizeBytes((Long) rs.getObject("l2_cache_size_bytes"))
                .sharedMemPerBlockBytes((Long) rs.getObject("shared_mem_per_block_bytes"))
                .regsPerBlock((Integer) rs.getObject("regs_per_block"))
                .multiProcessorCount((Integer) rs.getObject("multi_processor_count"))
                .warpSize((Integer) rs.getObject("warp_size"))
                .createdAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toInstant() : null)
                .updatedAt(rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toInstant() : null)
                .build();
    }

    @Override
    public void saveCudaDevice(CudaStaticDeviceEntity entity) {
        String sql = "INSERT INTO cuda_static_devices (session_id, name, uuid, device_id, compute_major, compute_minor, " +
                     "l2_cache_size_bytes, shared_mem_per_block_bytes, regs_per_block, multi_processor_count, warp_size, " +
                     "created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP) " +
                     "ON CONFLICT (session_id, uuid) DO UPDATE SET " +
                     "device_id = EXCLUDED.device_id, compute_major = EXCLUDED.compute_major, compute_minor = EXCLUDED.compute_minor, " +
                     "l2_cache_size_bytes = EXCLUDED.l2_cache_size_bytes, shared_mem_per_block_bytes = EXCLUDED.shared_mem_per_block_bytes, " +
                     "regs_per_block = EXCLUDED.regs_per_block, multi_processor_count = EXCLUDED.multi_processor_count, " +
                     "warp_size = EXCLUDED.warp_size, updated_at = CURRENT_TIMESTAMP";

        Object[] params = {
                entity.getSessionId(),
                entity.getName(),
                entity.getUuid(),
                entity.getDeviceId(),
                entity.getComputeMajor(),
                entity.getComputeMinor(),
                entity.getL2CacheSizeBytes(),
                entity.getSharedMemPerBlockBytes(),
                entity.getRegsPerBlock(),
                entity.getMultiProcessorCount(),
                entity.getWarpSize()
        };

        log.trace("saveCudaDevice called for sessionId: {}, uuid: {}, compute: {}.{}",
                  entity.getSessionId(), entity.getUuid(), entity.getComputeMajor(), entity.getComputeMinor());

        jdbcTemplate.update(sql, params);
    }

    @Override
    public List<CudaStaticDeviceEntity> findBySessionId(String sessionId) {
        String sql = "SELECT " + SELECT_COLUMNS + " FROM cuda_static_devices WHERE session_id = ?";
        return jdbcTemplate.query(sql, ROW_MAPPER, sessionId);
    }

    @Override
    public List<CudaStaticDeviceEntity> findBySessionIds(Collection<String> sessionIds) {
        if (sessionIds == null || sessionIds.isEmpty()) {
            return Collections.emptyList();
        }
        String sql = "SELECT " + SELECT_COLUMNS + " FROM cuda_static_devices WHERE session_id IN (:sessionIds)";
        return namedParameterJdbcTemplate.query(sql, Map.of("sessionIds", sessionIds), ROW_MAPPER);
    }

    @Override
    public List<CudaStaticDeviceEntity> findByInitialEventId(String eventId) {
        if (eventId == null || eventId.isEmpty()) {
            return Collections.emptyList();
        }
        String sql = "SELECT " + SELECT_COLUMNS + " FROM cuda_static_devices a " +
                     "INNER JOIN initial_events_cuda_static_devices b ON a.id = b.cuda_static_device_id " +
                     "WHERE b.initial_event_id = ?";
        return jdbcTemplate.query(sql, ROW_MAPPER, eventId);
    }
}
