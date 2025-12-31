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

    private String extraParams; // JSONB
    private Instant createdAt;
    private Instant updatedAt;
}
