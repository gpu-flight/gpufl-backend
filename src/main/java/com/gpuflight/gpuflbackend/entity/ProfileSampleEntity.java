package com.gpuflight.gpuflbackend.entity;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class ProfileSampleEntity {
    private String id;
    private String sessionId;
    private long tsNs;
    private int deviceId;
    private long corrId;
    private String sampleKind;
    // sass_metric fields
    private String metricName;
    private Long metricValue;
    private String pcOffset;
    // shared
    private String functionName;
    private String sourceFile;
    private Integer sourceLine;
    // pc_sampling fields
    private Integer sampleCount;
    private Integer stallReason;
    private String reasonName;
    private Instant createdAt;
}
