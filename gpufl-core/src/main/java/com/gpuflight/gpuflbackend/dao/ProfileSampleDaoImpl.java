package com.gpuflight.gpuflbackend.dao;

import com.gpuflight.gpuflbackend.entity.ProfileSampleEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class ProfileSampleDaoImpl implements ProfileSampleDao {
    private final JdbcTemplate jdbcTemplate;

    public ProfileSampleDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<ProfileSampleEntity> ROW_MAPPER = (rs, n) -> ProfileSampleEntity.builder()
        .id(rs.getString("id"))
        .sessionId(rs.getString("session_id"))
        .scopeName(rs.getString("scope_name"))
        .deviceId(rs.getObject("device_id") != null ? rs.getInt("device_id") : null)
        .sampleKind(rs.getString("sample_kind"))
        .functionName(rs.getString("function_name"))
        .pcOffset(rs.getObject("pc_offset") != null ? rs.getInt("pc_offset") : null)
        .metricName(rs.getString("metric_name"))
        .metricValue(rs.getLong("metric_value"))
        .stallReason(rs.getObject("stall_reason") != null ? rs.getInt("stall_reason") : null)
        .occurrenceCount(rs.getInt("occurrence_count"))
        .createdAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toInstant() : null)
        .updatedAt(rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toInstant() : null)
        .build();

    private static final String UPSERT_SQL = """
        INSERT INTO profile_samples
            (session_id, scope_name, device_id, sample_kind, function_name,
             pc_offset, metric_name, metric_value, stall_reason, occurrence_count)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 1)
        ON CONFLICT ON CONSTRAINT uq_profile_sample_key
        DO UPDATE SET
            metric_value     = profile_samples.metric_value + EXCLUDED.metric_value,
            occurrence_count = profile_samples.occurrence_count + 1,
            updated_at       = NOW()
        """;

    @Override
    public void save(ProfileSampleEntity entity) {
        jdbcTemplate.update(UPSERT_SQL,
            entity.getSessionId(), entity.getScopeName(), entity.getDeviceId(),
            entity.getSampleKind(), entity.getFunctionName(),
            entity.getPcOffset(), entity.getMetricName(),
            entity.getMetricValue(), entity.getStallReason()
        );
    }

    @Override
    public void merge(ProfileSampleEntity entity) {
        save(entity); // same logic now
    }

    @Override
    public List<ProfileSampleEntity> findBySessionId(String sessionId) {
        return jdbcTemplate.query(
            "SELECT * FROM profile_samples WHERE session_id = ? ORDER BY created_at ASC",
            ROW_MAPPER, sessionId
        );
    }
}
