package com.gpuflight.gpuflbackend.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class DeviceDaoImpl implements DeviceDao {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void saveDevice(String sessionId, String uuid, String vendor, String name, Long memoryTotalMib, String staticPropsJson) {
        jdbcTemplate.update(
                "INSERT INTO devices (session_id, uuid, vendor, name, memory_total_mib, static_properties) " +
                        "VALUES (?, ?, ?, ?, ?, ?::jsonb) ON CONFLICT (session_id, uuid) DO UPDATE SET " +
                        "vendor = EXCLUDED.vendor, name = EXCLUDED.name, " +
                        "memory_total_mib = EXCLUDED.memory_total_mib, static_properties = EXCLUDED.static_properties",
                sessionId,
                uuid,
                vendor,
                name,
                memoryTotalMib,
                staticPropsJson
        );
    }
}
