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
    private Long id;
    private Instant time;
    private Long tsNs;
    private String sessionId;
    private String type;
    private String name;
    private String tag;
    private Double hostCpuPct;
    private Long hostRamUsedMib;
}
