package com.gpuflight.gpuflbackend.service;

import com.gpuflight.gpuflbackend.entity.UserEntity;
import com.gpuflight.gpuflbackend.model.presentation.ApiKeyCreatedDto;
import com.gpuflight.gpuflbackend.model.presentation.ApiKeyDto;

import java.util.List;
import java.util.UUID;

public interface ApiKeyService {
    ApiKeyCreatedDto createKey(UUID userId, String name);
    List<ApiKeyDto> listKeys(UUID userId);
    void revokeKey(UUID keyId, UUID userId);
    UserEntity authenticate(String rawKey);
}
