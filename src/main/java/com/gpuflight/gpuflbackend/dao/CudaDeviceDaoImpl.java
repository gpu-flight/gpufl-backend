package com.gpuflight.gpuflbackend.dao;

import com.gpuflight.gpuflbackend.entity.CudaStaticDeviceEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@Slf4j
public class CudaDeviceDaoImpl implements CudaDeviceDao {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void saveCudaDevice(CudaStaticDeviceEntity entity) {
        String sql = "INSERT INTO cuda_static_devices (session_id, uuid, compute_major, compute_minor, l2_cache_size_bytes, shared_mem_per_block_bytes, regs_per_block, multi_processor_count, warp_size, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP) ON CONFLICT (session_id, uuid) DO UPDATE SET " +
                     "compute_major = EXCLUDED.compute_major, compute_minor = EXCLUDED.compute_minor, l2_cache_size_bytes = EXCLUDED.l2_cache_size_bytes, " +
                     "shared_mem_per_block_bytes = EXCLUDED.shared_mem_per_block_bytes, regs_per_block = EXCLUDED.regs_per_block, " +
                     "multi_processor_count = EXCLUDED.multi_processor_count, warp_size = EXCLUDED.warp_size, updated_at = CURRENT_TIMESTAMP";

        Object[] params = {
                entity.getSessionId(),
                entity.getUuid(),
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
    }
}
