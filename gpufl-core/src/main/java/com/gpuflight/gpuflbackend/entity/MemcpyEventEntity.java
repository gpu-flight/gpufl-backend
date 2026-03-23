package com.gpuflight.gpuflbackend.entity;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;

@Data
@Builder
public class MemcpyEventEntity {
    private String id;
    private Instant time;
    private String sessionId;
    private long startNs;
    private long durationNs;
    private long streamId;
    private long bytes;
    private int copyKind;
    private long corrId;
}
