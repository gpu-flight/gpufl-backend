package com.gpuflight.gpuflbackend.entity;

import org.springframework.data.relational.core.mapping.Table;
import lombok.Builder;
import lombok.Data;
import java.time.Instant;

@Data
@Builder
@Table("devices")
public class DeviceEntity {
    private String sessionId;
    private String uuid;
    private String vendor;
    private String name;
    private Long memoryTotalMib;
    private String properties; // JSONB
    private Instant createdAt;
    private Instant updatedAt;
}
