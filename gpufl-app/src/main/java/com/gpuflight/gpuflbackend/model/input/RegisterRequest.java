package com.gpuflight.gpuflbackend.model.input;

public record RegisterRequest(
        String email,
        String username,
        String password
) {}
