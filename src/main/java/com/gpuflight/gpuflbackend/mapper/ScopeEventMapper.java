package com.gpuflight.gpuflbackend.mapper;

import com.gpuflight.gpuflbackend.entity.ScopeEventEntity;
import com.gpuflight.gpuflbackend.model.presentation.ScopeEventDto;

public class ScopeEventMapper {
    public static ScopeEventDto mapToScopeEventDto(ScopeEventEntity entity) {
        return new ScopeEventDto(
                entity.getId(),
                entity.getTime(),
                entity.getTsNs(),
                entity.getSessionId(),
                entity.getName(),
                entity.getTag(),
                entity.getUserScope(),
                entity.getScopeDepth(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
