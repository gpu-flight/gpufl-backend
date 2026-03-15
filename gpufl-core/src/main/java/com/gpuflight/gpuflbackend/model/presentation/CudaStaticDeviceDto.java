package com.gpuflight.gpuflbackend.model.presentation;

import java.time.Instant;

public record CudaStaticDeviceDto(
    String id,
    int deviceId,
    String name,
    String uuid,
    String computeMajor,
    String computeMinor,
    long l2CacheSize,
    long sharedMemPerBlock,
    int regsPerBlock,
    int multiProcessorCount,
    int warpSize,
    Instant createdAt,
    Instant updatedAt) {

}
