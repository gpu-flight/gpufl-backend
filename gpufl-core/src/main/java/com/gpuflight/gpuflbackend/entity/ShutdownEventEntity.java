package com.gpuflight.gpuflbackend.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Builder
@Table("shutdown_events")
public class ShutdownEventEntity {
    private String app;

}
