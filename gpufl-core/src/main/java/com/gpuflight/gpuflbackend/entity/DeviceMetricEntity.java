package com.gpuflight.gpuflbackend.entity;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class DeviceMetricEntity {
    private UUID id;
    private Instant time;
    private String sessionId;
    private long tsNs;
    private Integer deviceId;
    private Integer gpuUtil;
    private Integer memUtil;
    private Integer tempC;
    private Integer powerMw;
    private Long usedMib;
    private Instant createdAt;
    private Instant updatedAt;
}
