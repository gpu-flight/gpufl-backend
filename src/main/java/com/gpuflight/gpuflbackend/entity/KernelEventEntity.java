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
    private Long id;
    private Instant time;
    private Long corrId;
    private String grid;
    private String block;
    private String sessionId;
    private String deviceUuid;
    private String cudaError;
    private String platform; // cuda or rocm
    private Integer pid;
    private String app;
    private String name;
    private Long startNs;
    private Long endNs;
    private Long durationNs;
    private Boolean hasDetails;
    private String extraParams; // JSONB

    private Instant createdAt;
    private Instant updatedAt;
}
