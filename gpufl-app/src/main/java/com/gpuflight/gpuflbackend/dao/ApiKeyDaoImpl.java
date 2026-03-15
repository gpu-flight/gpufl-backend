package com.gpuflight.gpuflbackend.dao;

import com.gpuflight.gpuflbackend.entity.ApiKeyEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@Slf4j
public class ApiKeyDaoImpl implements ApiKeyDao {

    private final JdbcTemplate jdbcTemplate;

    private static final RowMapper<ApiKeyEntity> ROW_MAPPER = (rs, rowNum) -> ApiKeyEntity.builder()
            .id(UUID.fromString(rs.getString("id")))
            .userId(UUID.fromString(rs.getString("user_id")))
            .name(rs.getString("name"))
            .keyHash(rs.getString("key_hash"))
            .keyPrefix(rs.getString("key_prefix"))
            .createdAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toInstant() : null)
            .lastUsedAt(rs.getTimestamp("last_used_at") != null ? rs.getTimestamp("last_used_at").toInstant() : null)
            .build();

    @Override
    public ApiKeyEntity save(ApiKeyEntity entity) {
        String sql = """
                INSERT INTO api_keys (user_id, name, key_hash, key_prefix, created_at)
                VALUES (?, ?, ?, ?, NOW())
                RETURNING *
                """;
        return jdbcTemplate.queryForObject(sql, ROW_MAPPER,
                entity.getUserId(),
                entity.getName(),
                entity.getKeyHash(),
                entity.getKeyPrefix());
    }

    @Override
    public List<ApiKeyEntity> findByUserId(UUID userId) {
        String sql = "SELECT * FROM api_keys WHERE user_id = ? ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, ROW_MAPPER, userId);
    }

    @Override
    public Optional<ApiKeyEntity> findByKeyHash(String keyHash) {
        String sql = "SELECT * FROM api_keys WHERE key_hash = ?";
        List<ApiKeyEntity> results = jdbcTemplate.query(sql, ROW_MAPPER, keyHash);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public void delete(UUID keyId) {
        String sql = "DELETE FROM api_keys WHERE id = ?";
        jdbcTemplate.update(sql, keyId);
    }

    @Override
    public void updateLastUsedAt(UUID keyId) {
        String sql = "UPDATE api_keys SET last_used_at = NOW() WHERE id = ?";
        jdbcTemplate.update(sql, keyId);
    }
}
