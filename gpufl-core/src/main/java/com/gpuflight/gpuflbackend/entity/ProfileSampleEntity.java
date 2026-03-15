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
    private String sampleKind;
    // shared
    private String pcOffset;
    private String functionName;
    private String sourceFile;
    private Integer sourceLine;
    // SASS: two metric rows folded into two columns
    private long instExecuted;
    private long threadInstExecuted;
    // pc_sampling fields
    private Integer stallReason;
    private String reasonName;
    private long sampleCount;
    // aggregation
    private int occurrenceCount;
    private Instant createdAt;
    private Instant updatedAt;
}
