package com.gpuflight.gpuflbackend.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Repository
@Slf4j
public class RetentionDaoImpl implements RetentionDao {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public RetentionDaoImpl(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    @Override
    public List<String> findExpiredSessionIds(int defaultDays) {
        String sql = "SELECT session_id FROM sessions " +
                     "WHERE start_time < NOW() - (COALESCE(retention_override_days, :defaultDays) || ' days')::INTERVAL";
        return namedParameterJdbcTemplate.queryForList(sql, Map.of("defaultDays", defaultDays), String.class);
    }

    @Override
    @Transactional
    public void deleteBySessionIds(List<String> sessionIds) {
        MapSqlParameterSource params = new MapSqlParameterSource("ids", sessionIds);

        namedParameterJdbcTemplate.update("DELETE FROM profile_samples     WHERE session_id IN (:ids)", params);
        namedParameterJdbcTemplate.update("DELETE FROM kernel_events        WHERE session_id IN (:ids)", params);
        namedParameterJdbcTemplate.update("DELETE FROM scope_events         WHERE session_id IN (:ids)", params);
        namedParameterJdbcTemplate.update("DELETE FROM host_metrics         WHERE session_id IN (:ids)", params);
        namedParameterJdbcTemplate.update("DELETE FROM device_metrics       WHERE session_id IN (:ids)", params);
        namedParameterJdbcTemplate.update("DELETE FROM system_events        WHERE session_id IN (:ids)", params);
        namedParameterJdbcTemplate.update("DELETE FROM cuda_static_devices  WHERE session_id IN (:ids)", params);
        namedParameterJdbcTemplate.update("DELETE FROM initial_events       WHERE session_id IN (:ids)", params);
        namedParameterJdbcTemplate.update("DELETE FROM sessions             WHERE session_id IN (:ids)", params);

        log.info("Deleted {} expired session(s) and all associated event data", sessionIds.size());
    }
}
