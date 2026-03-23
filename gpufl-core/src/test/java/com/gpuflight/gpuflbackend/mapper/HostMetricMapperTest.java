package com.gpuflight.gpuflbackend.mapper;

import com.gpuflight.gpuflbackend.entity.HostMetricEntity;
import com.gpuflight.gpuflbackend.model.presentation.HostMetricsDto;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class HostMetricMapperTest {

    @Test
    void mapToHostMetricsDto_mapsAllFields() {
        Instant now = Instant.now();

        HostMetricEntity entity = HostMetricEntity.builder()
                .id("host-uuid-1")
                .sessionId("sess-1")
                .time(now)
                .tsNs(500_000_000L)
                .cpuPct(25.5)
                .ramUsedMib(4096L)
                .ramTotalMib(16384L)
                .createdAt(now)
                .updatedAt(now)
                .build();

        HostMetricsDto dto = HostMetricMapper.mapToHostMetricsDto(entity);

        assertThat(dto.id()).isEqualTo("host-uuid-1");
        assertThat(dto.sessionId()).isEqualTo("sess-1");
        assertThat(dto.time()).isEqualTo(now);
        assertThat(dto.tsNs()).isEqualTo(500_000_000L);
        assertThat(dto.cpuPct()).isEqualTo(25.5);
        assertThat(dto.ramUsedMib()).isEqualTo(4096L);
        assertThat(dto.ramTotalMib()).isEqualTo(16384L);
        assertThat(dto.createdAt()).isEqualTo(now);
        assertThat(dto.updatedAt()).isEqualTo(now);
    }
}
