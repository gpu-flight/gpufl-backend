package com.gpuflight.gpuflbackend.mapper;

import com.gpuflight.gpuflbackend.entity.KernelEventEntity;
import com.gpuflight.gpuflbackend.model.input.KernelEvent;
import com.gpuflight.gpuflbackend.model.presentation.KernelEventDto;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static com.gpuflight.gpuflbackend.util.TimeUtils.epochToInstant;
import static org.junit.jupiter.api.Assertions.*;

class KernelEventMapperTest {

    private static final long START_NS = 1_000_000_000L;
    private static final long END_NS   = 2_000_000_000L;

    private KernelEvent buildEvent() {
        return new KernelEvent(
                43700, "myApp", "session-1", "vectorAdd", "cuda",
                0,
                START_NS, END_NS, 900_000_000L, 2_100_000_000L,
                1L,
                true,
                "(4,1,1)", "(256,1,1)",
                0L, 16, 0L, 0L, 0L,
                new BigDecimal("0.75"),
                new BigDecimal("0.80"), new BigDecimal("1.0"),
                new BigDecimal("0.75"), new BigDecimal("1.0"),
                "warps",
                8L, 132L,
                "stackTrace", "global|vectorAdd", 1,
                0L, 0, 0, 0L
        );
    }

    @Test
    void mapToKernelEventEntityFromEvent_mapsAllTimingFields() {
        KernelEventEntity entity = KernelEventMapper.mapToKernelEventEntityFromEvent(buildEvent());

        assertEquals(START_NS, entity.getStartNs());
        assertEquals(END_NS, entity.getEndNs());
        assertEquals(END_NS - START_NS, entity.getDurationNs());
        assertEquals(900_000_000L, entity.getApiStartNs());
        assertEquals(2_100_000_000L, entity.getApiExitNs());
        assertEquals(epochToInstant(START_NS), entity.getTime());
    }

    @Test
    void mapToKernelEventEntityFromEvent_mapsIdentityFields() {
        KernelEventEntity entity = KernelEventMapper.mapToKernelEventEntityFromEvent(buildEvent());

        assertEquals(43700, entity.getPid());
        assertEquals("myApp", entity.getApp());
        assertEquals("session-1", entity.getSessionId());
        assertEquals("vectorAdd", entity.getName());
        assertEquals("cuda", entity.getPlatform());
        assertEquals(0, entity.getDeviceId());
        assertEquals(1L, entity.getStreamId());
        assertEquals(132L, entity.getCorrId());
        assertTrue(entity.getHasDetails());
    }

    @Test
    void mapToKernelEventEntityFromEvent_mapsKernelDetailsFields() {
        KernelEventEntity entity = KernelEventMapper.mapToKernelEventEntityFromEvent(buildEvent());

        assertEquals("(4,1,1)", entity.getGrid());
        assertEquals("(256,1,1)", entity.getBlock());
        assertEquals(0L, entity.getDynSharedBytes());
        assertEquals(16, entity.getNumRegs());
        assertEquals(0L, entity.getStaticSharedBytes());
        assertEquals(0L, entity.getLocalBytes());
        assertEquals(0L, entity.getConstBytes());
        assertEquals(new BigDecimal("0.75"), entity.getOccupancy());
        assertEquals(8L, entity.getMaxActiveBlocks());
    }

    @Test
    void mapToKernelEventEntityFromEvent_mapsOccupancyBreakdown() {
        KernelEventEntity entity = KernelEventMapper.mapToKernelEventEntityFromEvent(buildEvent());

        assertEquals(new BigDecimal("0.80"), entity.getRegOccupancy());
        assertEquals(new BigDecimal("1.0"), entity.getSmemOccupancy());
        assertEquals(new BigDecimal("0.75"), entity.getWarpOccupancy());
        assertEquals(new BigDecimal("1.0"), entity.getBlockOccupancy());
        assertEquals("warps", entity.getLimitingResource());
    }

    @Test
    void mapToKernelEventEntityFromEvent_mapsPhase1aFields() {
        KernelEventEntity entity = KernelEventMapper.mapToKernelEventEntityFromEvent(buildEvent());

        assertEquals(0L, entity.getLocalMemTotal());
        assertEquals(0, entity.getCacheConfigRequested());
        assertEquals(0, entity.getCacheConfigExecuted());
        assertEquals(0L, entity.getSharedMemExecuted());
    }

    @Test
    void mapToKernelEventEntityFromEvent_mapsScopeFields() {
        KernelEventEntity entity = KernelEventMapper.mapToKernelEventEntityFromEvent(buildEvent());

        assertEquals("stackTrace", entity.getStackTrace());
        assertEquals("global|vectorAdd", entity.getUserScope());
        assertEquals(1, entity.getScopeDepth());
    }

    @Test
    void mapToKernelEventDto_mapsEntityToDto() {
        KernelEventEntity entity = KernelEventEntity.builder()
                .id("uuid-1")
                .time(Instant.EPOCH)
                .startNs(START_NS)
                .endNs(END_NS)
                .durationNs(END_NS - START_NS)
                .sessionId("session-1")
                .deviceId(0)
                .pid(100)
                .app("myApp")
                .platform("cuda")
                .name("vectorAdd")
                .corrId(42L)
                .cudaError(null)
                .hasDetails(true)
                .grid("(1,1,1)")
                .block("(128,1,1)")
                .dynSharedBytes(0L)
                .numRegs(32)
                .staticSharedBytes(0L)
                .localBytes(0L)
                .constBytes(0L)
                .occupancy(new BigDecimal("0.5"))
                .maxActiveBlocks(4L)
                .stackTrace(null)
                .userScope("global")
                .scopeDepth(0)
                .build();

        KernelEventDto dto = KernelEventMapper.mapToKernelEventDto(entity);

        assertEquals("uuid-1", dto.id());
        assertEquals(START_NS, dto.startNs());
        assertEquals(END_NS - START_NS, dto.durationNs());
        assertEquals("session-1", dto.sessionId());
        assertEquals("vectorAdd", dto.name());
        assertEquals(42L, dto.corrId());
        assertTrue(dto.hasDetails());
        assertEquals(new BigDecimal("0.5"), dto.occupancy());
    }

    @Test
    void mapToKernelEventDto_nullHasDetails_returnsFalse() {
        KernelEventEntity entity = KernelEventEntity.builder()
                .id("id")
                .time(Instant.EPOCH)
                .startNs(0L)
                .endNs(0L)
                .durationNs(0L)
                .sessionId("s")
                .deviceId(0)
                .pid(0)
                .name("k")
                .corrId(0L)
                .hasDetails(null)
                .dynSharedBytes(0L)
                .numRegs(0)
                .staticSharedBytes(0L)
                .localBytes(0L)
                .constBytes(0L)
                .maxActiveBlocks(0L)
                .scopeDepth(0)
                .build();

        KernelEventDto dto = KernelEventMapper.mapToKernelEventDto(entity);

        assertFalse(dto.hasDetails());
    }
}
