package com.gpuflight.gpuflbackend.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@Table("api_keys")
public class ApiKeyEntity {
    @Id
    private UUID id;
    private UUID userId;
    private String name;
    private String keyHash;
    private String keyPrefix;
    private Instant createdAt;
    private Instant lastUsedAt;
}
