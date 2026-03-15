package com.gpuflight.gpuflbackend.mapper;

import com.gpuflight.gpuflbackend.entity.ScopeEventEntity;
import com.gpuflight.gpuflbackend.model.presentation.ScopeEventDto;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class ScopeEventMapperTest {

    @Test
    void mapToScopeEventDto_mapsAllFields() {
        Instant now = Instant.now();
        ScopeEventEntity entity = ScopeEventEntity.builder()
                .id("scope-uuid")
                .time(now)
                .tsNs(123456789L)
                .sessionId("session-1")
                .name("training-step")
                .tag("phase1")
                .userScope("global|training-step")
                .scopeDepth(1)
                .createdAt(now)
                .updatedAt(now)
                .build();

        ScopeEventDto dto = ScopeEventMapper.mapToScopeEventDto(entity);

        assertEquals("scope-uuid", dto.id());
        assertEquals(now, dto.time());
        assertEquals(123456789L, dto.tsNs());
        assertEquals("session-1", dto.sessionId());
        assertEquals("training-step", dto.name());
        assertEquals("phase1", dto.tag());
        assertEquals("global|training-step", dto.userScope());
        assertEquals(1, dto.scopeDepth());
        assertEquals(now, dto.createdAt());
        assertEquals(now, dto.updatedAt());
    }

    @Test
    void mapToScopeEventDto_nullFields_handledGracefully() {
        ScopeEventEntity entity = ScopeEventEntity.builder()
                .id(null)
                .time(null)
                .tsNs(0L)
                .sessionId("session-2")
                .name("scope")
                .tag(null)
                .userScope(null)
                .scopeDepth(0)
                .createdAt(null)
                .updatedAt(null)
                .build();

        ScopeEventDto dto = ScopeEventMapper.mapToScopeEventDto(entity);

        assertNull(dto.id());
        assertNull(dto.time());
        assertEquals("session-2", dto.sessionId());
        assertNull(dto.tag());
    }
}
