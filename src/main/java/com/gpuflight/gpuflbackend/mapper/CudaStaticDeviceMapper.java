package com.gpuflight.gpuflbackend.mapper;

import com.gpuflight.gpuflbackend.entity.CudaStaticDeviceEntity;
import com.gpuflight.gpuflbackend.model.presentation.CudaStaticDeviceDto;

public class CudaStaticDeviceMapper {
    public static CudaStaticDeviceDto mapToCudaStaticDeviceDto(CudaStaticDeviceEntity entity) {
        return new CudaStaticDeviceDto(
                entity.getId(),
                entity.getDeviceId(),
                entity.getName(),
                entity.getUuid(),
                entity.getComputeMajor(),
                entity.getComputeMinor(),
                entity.getL2CacheSizeBytes(),
                entity.getSharedMemPerBlockBytes(),
                entity.getRegsPerBlock(),
                entity.getMultiProcessorCount(),
                entity.getWarpSize(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
