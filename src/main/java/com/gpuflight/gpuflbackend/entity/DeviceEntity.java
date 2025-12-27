package com.gpuflight.gpuflbackend.entity;

import org.springframework.data.relational.core.mapping.Table;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Table("devices")
public class DeviceEntity {
    private String sessionId;
    private String uuid;
    private String vendor;
    private String name;
    private Long memoryTotalMib;
    private String staticProperties; // JSONB
}
