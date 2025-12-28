package com.gpuflight.gpuflbackend.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Data
@Builder
@Table("initial_events")
public class InitialEventEntity {
    @Id
    private String sessionId;
    private Instant time;
    private Long tsNs;
    private String eventJson; // JSONB
    private Instant createdAt;
    private Instant updatedAt;
}
