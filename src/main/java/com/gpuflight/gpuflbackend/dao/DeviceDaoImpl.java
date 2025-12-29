package com.gpuflight.gpuflbackend.dao;

import com.gpuflight.gpuflbackend.entity.DeviceEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@Slf4j
public class DeviceDaoImpl implements DeviceDao {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void saveDevice(DeviceEntity entity) {
        String sql = "INSERT INTO static_devices (session_id, uuid, device_id, vendor, name, memory_total_mib, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP) ON CONFLICT (session_id, uuid) DO UPDATE SET " +
                     "device_id = EXCLUDED.device_id, vendor = EXCLUDED.vendor, name = EXCLUDED.name, " +
                     "memory_total_mib = EXCLUDED.memory_total_mib, updated_at = CURRENT_TIMESTAMP";

        Object[] params = {
                entity.getSessionId(),
                entity.getUuid(),
                entity.getDeviceId(),
                entity.getVendor(),
                entity.getName(),
                entity.getMemoryTotalMib()
        };

        log.trace("saveDevice called for sessionId: {}, uuid: {}, name: {}", 
                  entity.getSessionId(), entity.getUuid(), entity.getName());

        jdbcTemplate.update(sql, params);
    }
}
