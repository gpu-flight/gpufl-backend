package com.gpuflight.gpuflbackend.entity;

import org.springframework.data.relational.core.mapping.Table;
import lombok.Builder;
import lombok.Data;
import java.time.Instant;

@Data
@Builder
@Table("cuda_static_devices")
public class CudaStaticDeviceEntity {
    private String sessionId;
    private String uuid;
    private String computeMajor;
    private String computeMinor;
    private Long l2CacheSizeBytes;
    private Long sharedMemPerBlockBytes;
    private Integer regsPerBlock;
    private Integer multiProcessorCount;
    private Integer warpSize;
    private Instant createdAt;
    private Instant updatedAt;
}
