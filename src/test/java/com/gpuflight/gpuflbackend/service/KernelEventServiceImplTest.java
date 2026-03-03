package com.gpuflight.gpuflbackend.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.gpuflight.gpuflbackend.dao.KernelEventDao;
import com.gpuflight.gpuflbackend.entity.KernelEventEntity;
import com.gpuflight.gpuflbackend.model.EventWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KernelEventServiceImplTest {

    @Mock private KernelEventDao kernelEventDao;

    private KernelEventServiceImpl service;

    private static final String VALID_KERNEL_JSON = """
            {
              "pid": 43700, "app": "demo", "session_id": "s1",
              "name": "vectorAdd", "platform": "cuda", "device_id": 0,
              "start_ns": 1000000000, "end_ns": 2000000000,
              "api_start_ns": 900000000, "api_exit_ns": 2100000000,
              "stream_id": 1, "has_details": true,
              "grid": "(4,1,1)", "block": "(256,1,1)",
              "dyn_shared_bytes": 0, "num_regs": 16,
              "static_shared_bytes": 0, "local_bytes": 0, "const_bytes": 0,
              "occupancy": 0.75,
              "reg_occupancy": 0.8, "smem_occupancy": 1.0,
              "warp_occupancy": 0.75, "block_occupancy": 1.0,
              "limiting_resource": "warps",
              "max_active_blocks": 8, "corr_id": 132,
              "local_mem_total": 0, "cache_config_requested": 0,
              "cache_config_executed": 0, "shared_mem_executed": 0
            }
            """;

    @BeforeEach
    void setUp() {
        ObjectMapper ingestionMapper = new ObjectMapper()
                .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        service = new KernelEventServiceImpl(ingestionMapper, kernelEventDao);
    }

    @Test
    void addKernelEvent_validJson_savesEntityWithCorrectFields() {
        EventWrapper wrapper = new EventWrapper(VALID_KERNEL_JSON, 0L, "localhost", "127.0.0.1");

        service.addKernelEvent(wrapper);

        ArgumentCaptor<KernelEventEntity> captor = ArgumentCaptor.forClass(KernelEventEntity.class);
        verify(kernelEventDao).saveKernelEvent(captor.capture());

        KernelEventEntity entity = captor.getValue();
        assertEquals("vectorAdd", entity.getName());
        assertEquals("s1", entity.getSessionId());
        assertEquals(1_000_000_000L, entity.getStartNs());
        assertEquals(2_000_000_000L, entity.getEndNs());
        assertEquals(1_000_000_000L, entity.getDurationNs()); // end - start
        assertEquals(132L, entity.getCorrId());
        assertEquals(16, entity.getNumRegs());
        assertEquals("warps", entity.getLimitingResource());
    }

    @Test
    void addKernelEvent_invalidJson_logsErrorAndDoesNotThrow() {
        EventWrapper wrapper = new EventWrapper("not-valid-json", 0L, "localhost", "127.0.0.1");

        assertDoesNotThrow(() -> service.addKernelEvent(wrapper));
        verifyNoInteractions(kernelEventDao);
    }
}
