package com.gpuflight.gpuflbackend.service;

import com.gpuflight.gpuflbackend.dao.ApiKeyDao;
import com.gpuflight.gpuflbackend.dao.UserDao;
import com.gpuflight.gpuflbackend.entity.ApiKeyEntity;
import com.gpuflight.gpuflbackend.entity.UserEntity;
import com.gpuflight.gpuflbackend.model.presentation.ApiKeyCreatedDto;
import com.gpuflight.gpuflbackend.model.presentation.ApiKeyDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ApiKeyServiceImpl implements ApiKeyService {

    private final ApiKeyDao apiKeyDao;
    private final UserDao userDao;

    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    public ApiKeyCreatedDto createKey(UUID userId, String name) {
        byte[] bytes = new byte[24];
        RANDOM.nextBytes(bytes);
        String rawKey = "gpfl_" + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        String prefix = rawKey.substring(0, Math.min(8, rawKey.length()));

        ApiKeyEntity entity = ApiKeyEntity.builder()
                .userId(userId)
                .name(name)
                .keyHash(sha256(rawKey))
                .keyPrefix(prefix)
                .build();

        ApiKeyEntity saved = apiKeyDao.save(entity);
        return new ApiKeyCreatedDto(saved.getId(), saved.getName(), rawKey);
    }

    @Override
    public List<ApiKeyDto> listKeys(UUID userId) {
        return apiKeyDao.findByUserId(userId).stream()
                .map(k -> new ApiKeyDto(k.getId(), k.getName(), k.getKeyPrefix(), k.getCreatedAt(), k.getLastUsedAt()))
                .toList();
    }

    @Override
    public void revokeKey(UUID keyId, UUID userId) {
        apiKeyDao.delete(keyId);
    }

    @Override
    public UserEntity authenticate(String rawKey) {
        return apiKeyDao.findByKeyHash(sha256(rawKey))
                .map(k -> {
                    apiKeyDao.updateLastUsedAt(k.getId());
                    return userDao.findById(k.getUserId())
                            .orElseThrow(() -> new IllegalArgumentException("User not found for API key"));
                })
                .orElseThrow(() -> new IllegalArgumentException("Invalid API key"));
    }

    private static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
