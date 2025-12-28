package com.gpuflight.gpuflbackend.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Builder;
import lombok.Data;
import java.time.Instant;

@Data
@Builder
@Table("sessions")
public class SessionEntity {
    @Id
    private String sessionId;
    private String appName;
    private String hostname;
    private Integer pid;
    private Instant startTime;
    private Instant endTime;
    private Instant createdAt;
    private Instant updatedAt;
}
