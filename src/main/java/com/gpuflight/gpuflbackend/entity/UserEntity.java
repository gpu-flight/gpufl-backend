package com.gpuflight.gpuflbackend.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@Table("users")
public class UserEntity {
    @Id
    private UUID id;
    private String email;
    private String username;
    private String passwordHash;
    private String role;
    private String displayName;
    private String bio;
    private String avatarUrl;
    private Instant createdAt;
    private Instant updatedAt;
}
