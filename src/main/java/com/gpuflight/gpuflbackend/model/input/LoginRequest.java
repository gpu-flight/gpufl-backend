package com.gpuflight.gpuflbackend.model.input;

public record LoginRequest(
        String emailOrUsername,
        String password
) {}
