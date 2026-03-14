package com.gpuflight.gpuflbackend.dao;

import com.gpuflight.gpuflbackend.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@Slf4j
public class UserDaoImpl implements UserDao {

    private final JdbcTemplate jdbcTemplate;

    private static final RowMapper<UserEntity> ROW_MAPPER = (rs, rowNum) -> UserEntity.builder()
            .id(UUID.fromString(rs.getString("id")))
            .email(rs.getString("email"))
            .username(rs.getString("username"))
            .passwordHash(rs.getString("password_hash"))
            .role(rs.getString("role"))
            .displayName(rs.getString("display_name"))
            .bio(rs.getString("bio"))
            .avatarUrl(rs.getString("avatar_url"))
            .createdAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toInstant() : null)
            .updatedAt(rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toInstant() : null)
            .build();

    @Override
    public Optional<UserEntity> findById(UUID id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        List<UserEntity> results = jdbcTemplate.query(sql, ROW_MAPPER, id);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public Optional<UserEntity> findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        List<UserEntity> results = jdbcTemplate.query(sql, ROW_MAPPER, email);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public Optional<UserEntity> findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        List<UserEntity> results = jdbcTemplate.query(sql, ROW_MAPPER, username);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public UserEntity save(UserEntity entity) {
        String sql = """
                INSERT INTO users (email, username, password_hash, role, display_name, bio, avatar_url, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, NOW(), NOW())
                RETURNING *
                """;
        return jdbcTemplate.queryForObject(sql, ROW_MAPPER,
                entity.getEmail(),
                entity.getUsername(),
                entity.getPasswordHash(),
                entity.getRole(),
                entity.getDisplayName(),
                entity.getBio(),
                entity.getAvatarUrl());
    }

    @Override
    public void updateProfile(UserEntity entity) {
        String sql = """
                UPDATE users SET display_name = ?, bio = ?, avatar_url = ?, updated_at = NOW()
                WHERE id = ?
                """;
        jdbcTemplate.update(sql,
                entity.getDisplayName(),
                entity.getBio(),
                entity.getAvatarUrl(),
                entity.getId());
    }
}
