package com.gpuflight.gpuflbackend.model.presentation;

public record UserProfileDto(
        String username,
        String email,
        String displayName,
        String bio,
        String avatarUrl
) {}
