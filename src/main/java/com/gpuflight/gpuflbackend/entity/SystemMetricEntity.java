package com.gpuflight.gpuflbackend.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Builder;
import lombok.Data;
import java.time.Instant;

@Data
@Builder
@Table("system_metrics")
public class SystemMetricEntity {
    @Id
    private Long id;
    private Instant time;
    private Long tsNs;
    private String sessionId;
    private String deviceUuid;
    private Double powerWatts;
    private Integer tempC;
    private Integer utilGpuPct;
    private Integer utilMemPct;
    private Long memUsedMib;
    private String extendedMetrics; // JSONB
}
