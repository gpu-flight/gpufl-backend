package com.gpuflight.gpuflbackend.model.presentation;

public record AuthResponse(
        String token,
        String username,
        String role
) {}
