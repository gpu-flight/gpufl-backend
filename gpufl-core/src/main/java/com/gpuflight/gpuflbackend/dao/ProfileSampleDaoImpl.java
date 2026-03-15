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
            .scopeName(rs.getString("scope_name"))
            .sampleKind(rs.getString("sample_kind"))
            .pcOffset(rs.getString("pc_offset"))
            .functionName(rs.getString("function_name"))
            .sourceFile(rs.getString("source_file"))
            .sourceLine(rs.getObject("source_line") != null ? rs.getInt("source_line") : null)
            .instExecuted(rs.getLong("inst_executed"))
            .threadInstExecuted(rs.getLong("thread_inst_executed"))
            .stallReason(rs.getObject("stall_reason") != null ? rs.getInt("stall_reason") : null)
            .reasonName(rs.getString("reason_name"))
            .sampleCount(rs.getLong("sample_count"))
            .occurrenceCount(rs.getInt("occurrence_count"))
            .createdAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toInstant() : null)
            .updatedAt(rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toInstant() : null)
            .build();

    private static final String UPSERT_SQL = """
            INSERT INTO profile_samples
                (session_id, scope_name, sample_kind, function_name, pc_offset,
                 source_file, source_line, inst_executed, thread_inst_executed,
                 stall_reason, reason_name, sample_count, occurrence_count)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 1)
            ON CONFLICT ON CONSTRAINT uq_profile_sample_key
            DO UPDATE SET
                inst_executed        = profile_samples.inst_executed        + EXCLUDED.inst_executed,
                thread_inst_executed = profile_samples.thread_inst_executed + EXCLUDED.thread_inst_executed,
                sample_count         = profile_samples.sample_count         + EXCLUDED.sample_count,
                occurrence_count     = profile_samples.occurrence_count     + 1,
                updated_at           = NOW()
            """;

    private static final String MERGE_SQL = """
            INSERT INTO profile_samples
                (session_id, scope_name, sample_kind, function_name, pc_offset,
                 source_file, source_line, inst_executed, thread_inst_executed,
                 stall_reason, reason_name, sample_count, occurrence_count)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 1)
            ON CONFLICT ON CONSTRAINT uq_profile_sample_key
            DO UPDATE SET
                inst_executed        = profile_samples.inst_executed        + EXCLUDED.inst_executed,
                thread_inst_executed = profile_samples.thread_inst_executed + EXCLUDED.thread_inst_executed,
                sample_count         = profile_samples.sample_count         + EXCLUDED.sample_count,
                updated_at           = NOW()
            """;

    private Object[] buildParams(ProfileSampleEntity entity) {
        return new Object[]{
                entity.getSessionId(),
                entity.getScopeName(),
                entity.getSampleKind(),
                entity.getFunctionName(),
                entity.getPcOffset(),
                entity.getSourceFile(),
                entity.getSourceLine(),
                entity.getInstExecuted(),
                entity.getThreadInstExecuted(),
                entity.getStallReason(),
                entity.getReasonName(),
                entity.getSampleCount()
        };
    }

    @Override
    public void save(ProfileSampleEntity entity) {
        jdbcTemplate.update(UPSERT_SQL, buildParams(entity));
    }

    @Override
    public void merge(ProfileSampleEntity entity) {
        jdbcTemplate.update(MERGE_SQL, buildParams(entity));
    }

    @Override
    public List<ProfileSampleEntity> findBySessionId(String sessionId) {
        String sql = "SELECT * FROM profile_samples WHERE session_id = ? ORDER BY created_at ASC";
        return jdbcTemplate.query(sql, ROW_MAPPER, sessionId);
    }
}
