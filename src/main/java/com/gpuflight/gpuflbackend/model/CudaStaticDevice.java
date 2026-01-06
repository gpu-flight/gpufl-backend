package com.gpuflight.gpuflbackend.model;

public record CudaStaticDevice(int id,
                               String name,
                               String uuid,
                               String computeMajor,
                               String computeMinor,
                               long l2CacheSizeBytes,
                               long sharedMemPerBlockBytes,
                               int regsPerBlock,
                               int multiProcessorCount,
                               int warpSize) {
}
