package com.gpuflight.gpuflbackend.entity;

import org.springframework.data.relational.core.mapping.Table;
import lombok.Builder;
import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@Table("device_metrics")
public class DeviceMetricEntity {
    private UUID id;
    private String eventType;
    private Instant time;
    private Long tsNs;
    private String sessionId;
    private String uuid;
    private Integer deviceId;
    private String vendor;
    private String name;
    private Integer pciBus;
    private Long usedMib;
    private Long freeMib;
    private Long totalMib;
    private Integer utilGpuPct;
    private Integer utilMemPct;
    private Integer tempC;
    private Integer powerMw;
    private Integer clkGfxMhz;
    private Integer clkSmMhz;
    private Integer clkMemMhz;
    private Integer throttlePwr;
    private Integer throttleTherm;
    private Long pcieRxBwBps;
    private Long pcieTxBwBps;
    private String extendedMetrics;
    private Instant createdAt;
    private Instant updatedAt;
}
