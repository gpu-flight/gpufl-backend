package com.gpuflight.gpuflbackend.entity;

import org.springframework.data.relational.core.mapping.Table;
import lombok.Builder;
import lombok.Data;
import java.time.Instant;

@Data
@Builder
@Table("device_metrics")
public class DeviceMetricEntity {
    private String id;
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
    private Integer utilGpu;
    private Integer utilMem;
    private Integer tempC;
    private Integer powerMw;
    private Integer clkGfx;
    private Integer clkMem;
    private Integer clkSm;
    private Integer throttlePwr;
    private Integer throttleTherm;
    private Long pcieRxBw;
    private Long pcieTxBw;
    private String extendedMetrics;
    private Instant createdAt;
    private Instant updatedAt;
}
