package com.gpuflight.gpuflbackend.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Builder;
import lombok.Data;
import java.time.Instant;

@Data
@Builder
@Table("host_metrics")
public class HostMetricEntity {
    @Id
    private Long id;
    private Instant time;
    private Long tsNs;
    private String sessionId;
    private String type;
    private Double cpuPct;
    private Long ramUsedMib;
    private Instant createdAt;
    private Instant updatedAt;
}
