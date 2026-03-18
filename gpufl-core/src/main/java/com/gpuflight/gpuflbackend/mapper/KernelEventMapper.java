package com.gpuflight.gpuflbackend.mapper;

import com.gpuflight.gpuflbackend.entity.KernelEventEntity;
import com.gpuflight.gpuflbackend.model.input.KernelEvent;
import com.gpuflight.gpuflbackend.model.presentation.KernelEventDto;

import static com.gpuflight.gpuflbackend.util.TimeUtils.epochToInstant;

public class KernelEventMapper {
    public static KernelEventDto mapToKernelEventDto(KernelEventEntity entity) {
        return new KernelEventDto(
                entity.getId(),
                entity.getTime(),
                entity.getStartNs(),
                entity.getEndNs(),
                entity.getDurationNs(),
                entity.getApiStartNs(),
                entity.getApiExitNs(),
                entity.getStreamId(),
                entity.getSessionId(),
                entity.getDeviceId(),
                entity.getPid(),
                entity.getApp(),
                entity.getPlatform(),
                entity.getName(),
                entity.getCorrId(),
                entity.getCudaError(),
                entity.getHasDetails() != null && entity.getHasDetails(), // Handle nullable Boolean
                entity.getGrid(),
                entity.getBlock(),
                entity.getDynSharedBytes(),
                entity.getNumRegs(),
                entity.getStaticSharedBytes(),
                entity.getLocalBytes(),
                entity.getConstBytes(),
                entity.getOccupancy(),
                entity.getMaxActiveBlocks(),
                entity.getExtraParams(),
                entity.getStackTrace(),
                entity.getUserScope(),
                entity.getScopeDepth(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getRegOccupancy(),
                entity.getSmemOccupancy(),
                entity.getWarpOccupancy(),
                entity.getBlockOccupancy(),
                entity.getLimitingResource(),
                entity.getLocalMemTotalBytes()
        );
    }

    public static KernelEventEntity mapToKernelEventEntityFromEvent(KernelEvent event) {
        return KernelEventEntity.builder()
                .pid(event.pid())
                .app(event.app())
                .time(epochToInstant(event.startNs()))
                .startNs(event.startNs())
                .endNs(event.endNs())
                .durationNs(event.endNs() - event.startNs())
                .apiStartNs(event.apiStartNs())
                .apiExitNs(event.apiExitNs())
                .streamId(event.streamId())
                .sessionId(event.sessionId())
                .deviceId(event.deviceId())
                .name(event.name())
                .platform(event.platform())
                .hasDetails(event.hasDetails())
                .grid(event.grid())
                .block(event.block())
                .dynSharedBytes(event.dynSharedBytes())
                .numRegs(event.numRegs())
                .staticSharedBytes(event.staticSharedBytes())
                .localBytes(event.localBytes())
                .constBytes(event.constBytes())
                .occupancy(event.occupancy())
                .regOccupancy(event.regOccupancy())
                .smemOccupancy(event.smemOccupancy())
                .warpOccupancy(event.warpOccupancy())
                .blockOccupancy(event.blockOccupancy())
                .limitingResource(event.limitingResource())
                .maxActiveBlocks(event.maxActiveBlocks())
                .corrId(event.corrId())
                .stackTrace(event.stackTrace())
                .userScope(event.userScope())
                .scopeDepth(event.scopeDepth())
                .localMemTotalBytes(event.localMemTotalBytes())
                .cacheConfigRequested(event.cacheConfigRequested())
                .cacheConfigExecuted(event.cacheConfigExecuted())
                .sharedMemExecutedBytes(event.sharedMemExecutedBytes())
                .build();
    }
}
