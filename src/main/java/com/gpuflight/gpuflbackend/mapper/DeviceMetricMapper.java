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
                entity.getUtilGpu(),
                entity.getUtilMem(),
                entity.getTempC(),
                entity.getPowerMw(),
                entity.getClkGfx(),
                entity.getClkSm(),
                entity.getClkMem(),
                entity.getThrottlePwr(),
                entity.getThrottleTherm(),
                entity.getPcieRxBw(),
                entity.getPcieTxBw(),
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
                .utilGpu(sample.utilGpu())
                .utilMem(sample.utilMem())
                .tempC(sample.tempC())
                .powerMw(sample.powerMw())
                .clkGfx(sample.clkGfx())
                .clkSm(sample.clkSm())
                .clkMem(sample.clkMem())
                .throttlePwr(sample.throttlePwr())
                .throttleTherm(sample.throttleTherm())
                .pcieRxBw(sample.pcieRxBw())
                .pcieTxBw(sample.pcieTxBw())
                .build();
    }

}
