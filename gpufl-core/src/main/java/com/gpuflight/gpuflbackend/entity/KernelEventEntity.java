package com.gpuflight.gpuflbackend.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

@Data
@Builder
@Table("kernel_events")
public class KernelEventEntity {
    @Id
    private String id;
    private Instant time;
    private Long startNs;
    private Long endNs;
    private Long durationNs;
    private String sessionId;
    private Integer deviceId;
    private Integer pid;
    private String app;
    private String platform;
    private String name;
    private Long corrId;
    private String cudaError;
    private Boolean hasDetails;

    private String grid;
    private String block;
    private long dynSharedBytes;
    private int numRegs;
    private long staticSharedBytes;
    private long localBytes;
    private long constBytes;
    private BigDecimal occupancy;
    private long maxActiveBlocks;

    String stackTrace;
    String userScope;
    int scopeDepth;

    // New unified kernel_event fields
    private Long streamId;
    private Long apiStartNs;
    private Long apiExitNs;
    private BigDecimal regOccupancy;
    private BigDecimal smemOccupancy;
    private BigDecimal warpOccupancy;
    private BigDecimal blockOccupancy;
    private String limitingResource;
    private Long localMemTotalBytes;
    private Long localMemPerThreadBytes;
    private Integer cacheConfigRequested;
    private Integer cacheConfigExecuted;
    private Long sharedMemExecutedBytes;

    private String extraParams; // JSONB
    private Instant createdAt;
    private Instant updatedAt;
}
