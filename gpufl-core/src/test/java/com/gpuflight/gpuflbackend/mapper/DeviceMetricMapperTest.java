package com.gpuflight.gpuflbackend.mapper;

import com.gpuflight.gpuflbackend.entity.DeviceMetricEntity;
import com.gpuflight.gpuflbackend.model.presentation.DeviceMetricsDto;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DeviceMetricMapperTest {

    @Test
    void mapToDeviceSample_mapsAllFields() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();

        DeviceMetricEntity entity = DeviceMetricEntity.builder()
                .id(id)
                .sessionId("sess-1")
                .time(now)
                .tsNs(123_000_000L)
                .deviceId(0)
                .gpuUtil(75)
                .memUtil(40)
                .tempC(68)
                .powerMw(180_000)
                .usedMib(2048L)
                .createdAt(now)
                .updatedAt(now)
                .build();

        DeviceMetricsDto dto = DeviceMetricMapper.mapToDeviceSample(entity);

        assertThat(dto.id()).isEqualTo(id.toString());
        assertThat(dto.sessionId()).isEqualTo("sess-1");
        assertThat(dto.time()).isEqualTo(now);
        assertThat(dto.tsNs()).isEqualTo(123_000_000L);
        assertThat(dto.deviceId()).isEqualTo(0);
        assertThat(dto.gpuUtil()).isEqualTo(75);
        assertThat(dto.memUtil()).isEqualTo(40);
        assertThat(dto.tempC()).isEqualTo(68);
        assertThat(dto.powerMw()).isEqualTo(180_000);
        assertThat(dto.usedMib()).isEqualTo(2048L);
        assertThat(dto.createdAt()).isEqualTo(now);
        assertThat(dto.updatedAt()).isEqualTo(now);
    }

    @Test
    void mapToDeviceSample_nullId_producesNullStringId() {
        DeviceMetricEntity entity = DeviceMetricEntity.builder()
                .id(null)
                .sessionId("sess-x")
                .time(Instant.EPOCH)
                .build();

        DeviceMetricsDto dto = DeviceMetricMapper.mapToDeviceSample(entity);

        assertThat(dto.id()).isNull();
    }
}
