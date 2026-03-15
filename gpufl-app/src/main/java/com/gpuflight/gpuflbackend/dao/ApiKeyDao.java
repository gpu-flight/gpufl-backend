package com.gpuflight.gpuflbackend.dao;

import com.gpuflight.gpuflbackend.entity.ApiKeyEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ApiKeyDao {
    ApiKeyEntity save(ApiKeyEntity entity);
    List<ApiKeyEntity> findByUserId(UUID userId);
    Optional<ApiKeyEntity> findByKeyHash(String keyHash);
    void delete(UUID keyId);
    void updateLastUsedAt(UUID keyId);
}
