package com.gpuflight.gpuflbackend.dao;

import com.gpuflight.gpuflbackend.entity.DeviceMetricEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Repository
public class DeviceMetricDaoImpl implements DeviceMetricDao {
    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private static final String SELECT_COLUMNS = "id, event_type, time, ts_ns, session_id, uuid, device_id, vendor, name, pci_bus, " +
            "used_mib, free_mib, total_mib, util_gpu, util_mem, temp_c, power_mw, " +
            "clk_gfx, clk_sm, clk_mem, throttle_pwr, throttle_therm, pcie_rx_bw, pcie_tx_bw, " +
            "extended_metrics, created_at, updated_at";

    private static final RowMapper<DeviceMetricEntity> ROW_MAPPER = (rs, rowNum) -> mapRow(rs);

    public DeviceMetricDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    }

    private static DeviceMetricEntity mapRow(ResultSet rs) throws SQLException {
        return DeviceMetricEntity.builder()
                .id(rs.getString("id"))
                .eventType(rs.getString("event_type"))
                .time(rs.getTimestamp("time") != null ? rs.getTimestamp("time").toInstant() : null)
                .tsNs(rs.getLong("ts_ns"))
                .sessionId(rs.getString("session_id"))
                .uuid(rs.getString("uuid"))
                .deviceId((Integer) rs.getObject("device_id"))
                .vendor(rs.getString("vendor"))
                .name(rs.getString("name"))
                .pciBus((Integer) rs.getObject("pci_bus"))
                .usedMib((Long) rs.getObject("used_mib"))
                .freeMib((Long) rs.getObject("free_mib"))
                .totalMib((Long) rs.getObject("total_mib"))
                .utilGpu((Integer) rs.getObject("util_gpu"))
                .utilMem((Integer) rs.getObject("util_mem"))
                .tempC((Integer) rs.getObject("temp_c"))
                .powerMw((Integer) rs.getObject("power_mw"))
                .clkGfx((Integer) rs.getObject("clk_gfx"))
                .clkSm((Integer) rs.getObject("clk_sm"))
                .clkMem((Integer) rs.getObject("clk_mem"))
                .throttlePwr((Integer) rs.getObject("throttle_pwr"))
                .throttleTherm((Integer) rs.getObject("throttle_therm"))
                .pcieRxBw((Long) rs.getObject("pcie_rx_bw"))
                .pcieTxBw((Long) rs.getObject("pcie_tx_bw"))
                .extendedMetrics(rs.getString("extended_metrics"))
                .createdAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toInstant() : null)
                .updatedAt(rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toInstant() : null)
                .build();
    }

    @Override
    public void saveDeviceMetric(DeviceMetricEntity entity) {
        jdbcTemplate.update(
                "INSERT INTO device_metrics (time, event_type, ts_ns, session_id, uuid, device_id, vendor, name, pci_bus, " +
                        "used_mib, free_mib, total_mib, util_gpu, util_mem, temp_c, power_mw, " +
                        "clk_gfx, clk_sm, clk_mem, throttle_pwr, throttle_therm, pcie_rx_bw, pcie_tx_bw, " +
                        "extended_metrics, created_at, updated_at) " +
                        "VALUES (?, ?,  ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                entity.getTime() != null ? Timestamp.from(entity.getTime()) : null,
                entity.getEventType(),
                entity.getTsNs(),
                entity.getSessionId(),
                entity.getUuid(),
                entity.getDeviceId(),
                entity.getVendor(),
                entity.getName(),
                entity.getPciBus(),
                entity.getUsedMib(),
                entity.getFreeMib(),
                entity.getTotalMib(),
                entity.getUtilGpu(),
                entity.getUtilMem(),
                entity.getTempC(),
                entity.getPowerMw(),
                entity.getClkGfx(),
                entity.getClkSm(),
                entity.getClkMem(),
                entity.getThrottlePwr(),
                entity.getThrottleTherm(),
                entity.getPcieRxBw(),
                entity.getPcieTxBw(),
                entity.getExtendedMetrics()
        );
    }

    @Override
    public List<DeviceMetricEntity> findBySessionId(String sessionId) {
        String sql = "SELECT " + SELECT_COLUMNS + " FROM device_metrics WHERE session_id = ? ORDER BY time DESC";
        return jdbcTemplate.query(sql, ROW_MAPPER, sessionId);
    }

    @Override
    public List<DeviceMetricEntity> findBySessionIds(Collection<String> sessionIds) {
        if (sessionIds == null || sessionIds.isEmpty()) {
            return Collections.emptyList();
        }
        String sql = "SELECT " + SELECT_COLUMNS + " FROM device_metrics WHERE session_id IN (:sessionIds) ORDER BY time DESC";
        return namedParameterJdbcTemplate.query(sql, Map.of("sessionIds", sessionIds), ROW_MAPPER);
    }
}
