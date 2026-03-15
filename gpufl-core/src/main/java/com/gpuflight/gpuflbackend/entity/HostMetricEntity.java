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
    private String id;
    private Instant time;
    private String eventType;
    private Long tsNs;
    private String hostname;
    private String ipAddr;
    private String sessionId;
    private Double cpuPct;
    private Long ramUsedMib;
    private Long ramTotalMib;
    private Instant createdAt;
    private Instant updatedAt;
}
