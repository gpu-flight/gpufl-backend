package com.gpuflight.gpuflbackend.model.presentation;

import java.util.UUID;

public record ApiKeyCreatedDto(
        UUID id,
        String name,
        String rawKey
) {}
