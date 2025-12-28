package com.gpuflight.gpuflbackend.dao;

import com.gpuflight.gpuflbackend.entity.DeviceEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class DeviceDaoImpl implements DeviceDao {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void saveDevice(DeviceEntity entity) {
        jdbcTemplate.update(
                "INSERT INTO devices (session_id, uuid, vendor, name, memory_total_mib, properties, created_at, updated_at) " +
                        "VALUES (?, ?, ?, ?, ?, ?::jsonb, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP) ON CONFLICT (session_id, uuid) DO UPDATE SET " +
                        "vendor = EXCLUDED.vendor, name = EXCLUDED.name, " +
                        "memory_total_mib = EXCLUDED.memory_total_mib, static_properties = EXCLUDED.static_properties, updated_at = CURRENT_TIMESTAMP",
                entity.getSessionId(),
                entity.getUuid(),
                entity.getVendor(),
                entity.getName(),
                entity.getMemoryTotalMib(),
                entity.getProperties()
        );
    }
}
