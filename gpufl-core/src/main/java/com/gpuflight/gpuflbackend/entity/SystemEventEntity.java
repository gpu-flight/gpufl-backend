package com.gpuflight.gpuflbackend.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Data
@Builder
@Table("system_events")
public class SystemEventEntity {
    @Id
    private String id;
    private String sessionId;
    private Integer pid;
    private String app;
    private String name;
    private String eventType;
    private Long tsNs;
    private Long rangeStart;
    private Long rangeEnd;
    private Instant createdAt;
    private Instant updatedAt;
}
