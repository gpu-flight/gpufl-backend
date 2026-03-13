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

    private static final RowMapper<ProfileSampleEntity> ROW_MAPPER = (rs, rowNum) -> ProfileSampleEntity.builder()
            .id(rs.getString("id"))
            .sessionId(rs.getString("session_id"))
            .tsNs(rs.getLong("ts_ns"))
            .deviceId(rs.getInt("device_id"))
            .corrId(rs.getLong("corr_id"))
            .sampleKind(rs.getString("sample_kind"))
            .metricName(rs.getString("metric_name"))
            .metricValue(rs.getObject("metric_value") != null ? rs.getLong("metric_value") : null)
            .pcOffset(rs.getString("pc_offset"))
            .functionName(rs.getString("function_name"))
            .sourceFile(rs.getString("source_file"))
            .sourceLine(rs.getObject("source_line") != null ? rs.getInt("source_line") : null)
            .sampleCount(rs.getObject("sample_count") != null ? rs.getInt("sample_count") : null)
            .stallReason(rs.getObject("stall_reason") != null ? rs.getInt("stall_reason") : null)
            .reasonName(rs.getString("reason_name"))
            .createdAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toInstant() : null)
            .build();

    @Override
    public void save(ProfileSampleEntity entity) {
        String sql = """
            INSERT INTO profile_samples (
                session_id, ts_ns, device_id, corr_id, sample_kind,
                metric_name, metric_value, pc_offset,
                function_name, source_file, source_line,
                sample_count, stall_reason, reason_name
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        jdbcTemplate.update(sql,
                entity.getSessionId(),
                entity.getTsNs(),
                entity.getDeviceId(),
                entity.getCorrId(),
                entity.getSampleKind(),
                entity.getMetricName(),
                entity.getMetricValue(),
                entity.getPcOffset(),
                entity.getFunctionName(),
                entity.getSourceFile(),
                entity.getSourceLine(),
                entity.getSampleCount(),
                entity.getStallReason(),
                entity.getReasonName()
        );
    }

    @Override
    public List<ProfileSampleEntity> findBySessionId(String sessionId) {
        String sql = "SELECT * FROM profile_samples WHERE session_id = ? ORDER BY ts_ns ASC";
        return jdbcTemplate.query(sql, ROW_MAPPER, sessionId);
    }
}
