package com.gpuflight.gpuflbackend.entity;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;

@Data
@Builder
public class ProfileSampleEntity {
    private String id;
    private String sessionId;
    private String scopeName;
    private Integer deviceId;
    private String sampleKind;
    private String functionName;
    private Integer pcOffset;
    private String metricName;
    private long metricValue;
    private Integer stallReason;
    private int occurrenceCount;
    private Instant createdAt;
    private Instant updatedAt;
}
