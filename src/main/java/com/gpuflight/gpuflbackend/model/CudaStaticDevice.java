package com.gpuflight.gpuflbackend.model;

public record CudaStaticDevice(int id,
                               String name,
                               String uuid,
                               String computeMajor,
                               String computeMinor,
                               long l2CacheSize,
                               long sharedMemPerBlock,
                               int regsPerBlock,
                               int multiProcessorCount,
                               int warpSize) {
}
