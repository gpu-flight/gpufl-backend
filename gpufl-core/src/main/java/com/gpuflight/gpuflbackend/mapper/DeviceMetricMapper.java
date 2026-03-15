package com.gpuflight.gpuflbackend.mapper;

import com.gpuflight.gpuflbackend.entity.DeviceMetricEntity;
import com.gpuflight.gpuflbackend.model.DeviceSample;
import com.gpuflight.gpuflbackend.model.presentation.DeviceMetricsDto;

import java.time.Instant;
import java.util.UUID;

public class DeviceMetricMapper {

    public static DeviceMetricsDto mapToDeviceSample(DeviceMetricEntity entity) {
        return new DeviceMetricsDto(
                entity.getId().toString(),
                entity.getDeviceId(),
                entity.getSessionId(),
                entity.getTime(),
                entity.getTsNs(),
                entity.getName(),
                entity.getUuid(),
                entity.getVendor(),
                entity.getEventType(),
                entity.getPciBus(),
                entity.getUsedMib(),
                entity.getFreeMib(),
                entity.getTotalMib(),
                entity.getUtilGpuPct(),  // mapped to utilGpu in DTO
                entity.getUtilMemPct(),  // mapped to utilMem in DTO
                entity.getTempC(),
                entity.getPowerMw(),
                entity.getClkGfxMhz(),
                entity.getClkSmMhz(),
                entity.getClkMemMhz(),
                entity.getThrottlePwr(),
                entity.getThrottleTherm(),
                entity.getPcieRxBwBps(),
                entity.getPcieTxBwBps(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public static DeviceMetricEntity mapToDeviceMetricEntity(DeviceSample sample, String eventType, String sessionId, Instant time, Long tsNs) {
        return DeviceMetricEntity.builder()
                .id(UUID.randomUUID())
                .sessionId(sessionId)
                .eventType(eventType)
                .tsNs(tsNs)
                .time(time)
                .deviceId(sample.deviceId())
                .name(sample.name())
                .uuid(sample.uuid())
                .vendor(sample.vendor())
                .pciBus(sample.pciBus())
                .usedMib(sample.usedMib())
                .freeMib(sample.freeMib())
                .totalMib(sample.totalMib())
                .utilGpuPct(sample.utilGpuPct())
                .utilMemPct(sample.utilMemPct())
                .tempC(sample.tempC())
                .powerMw(sample.powerMw())
                .clkGfxMhz(sample.clkGfxMhz())
                .clkSmMhz(sample.clkSmMhz())
                .clkMemMhz(sample.clkMemMhz())
                .throttlePwr(sample.throttlePwr())
                .throttleTherm(sample.throttleTherm())
                .pcieRxBwBps(sample.pcieRxBwBps())
                .pcieTxBwBps(sample.pcieTxBwBps())
                .build();
    }

}
