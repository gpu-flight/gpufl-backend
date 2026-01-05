package com.gpuflight.gpuflbackend.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Builder;
import lombok.Data;
import java.time.Instant;

@Data
@Builder
@Table("scope_events")
public class ScopeEventEntity {
    @Id
    private String id;
    private Instant time;
    private Long tsNs;
    private String sessionId;
    private String name;
    private String tag;
    private String userScope;
    private int scopeDepth;
    private Instant createdAt;
    private Instant updatedAt;
}
