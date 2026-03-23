package com.gpuflight.gpuflbackend.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.gpuflight.gpuflbackend.dao.*;
import com.gpuflight.gpuflbackend.entity.InitialEventEntity;
import com.gpuflight.gpuflbackend.model.EventWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InitEventServiceImplTest {

    @Mock private InitDao initDao;
    @Mock private HostMetricDao hostMetricDao;
    @Mock private CudaDeviceDao cudaDeviceDao;
    @Mock private SessionDao sessionDao;
    @Mock private DeviceMetricDao deviceMetricDao;
    @Mock private CudaDeviceServiceImpl cudaDeviceService;
    @Mock private ScopeEventDao scopeEventDao;
    @Mock private KernelEventDao kernelEventDao;
    @Mock private SystemEventDao systemEventDao;

    private InitEventServiceImpl service;
    private ObjectMapper mapper;

    private static final String INIT_JSON_MINIMAL = """
            {
              "type": "init",
              "pid": 43700, "app": "demo",
              "session_id": "session-1",
              "log_path": "demo.log",
              "ts_ns": 1000000000,
              "system_rate_ms": 10
            }
            """;

    private static final String INIT_JSON_FULL = """
            {
              "type": "init",
              "pid": 43700, "app": "demo",
              "session_id": "session-2",
              "log_path": "demo.log",
              "ts_ns": 1000000000,
              "system_rate_ms": 10,
              "host": {"cpu_pct": 5.0, "ram_used_mib": 1024, "ram_total_mib": 16384},
              "devices": [
                {"id": 0, "name": "GPU0", "uuid": "uuid-0", "vendor": "NVIDIA",
                 "pci_bus": 1, "used_mib": 100, "free_mib": 7900, "total_mib": 8000,
                 "util_gpu": 0, "util_mem": 0, "temp_c": 37, "power_mw": 10000,
                 "clk_gfx": 1000, "clk_sm": 1000, "clk_mem": 9000,
                 "throttle_pwr": 0, "throttle_therm": 0,
                 "pcie_rx_bw": 1024, "pcie_tx_bw": 512}
              ],
              "cuda_static_devices": [
                {"id": 0, "name": "GPU0", "uuid": "uuid-0",
                 "compute_major": "8", "compute_minor": 6,
                 "l2_cache_size": 4194304, "shared_mem_per_block": 49152,
                 "regs_per_block": 65536, "multi_processor_count": 20,
                 "warp_size": 32}
              ]
            }
            """;

    private static final String SHUTDOWN_JSON = """
            {
              "type": "shutdown",
              "pid": 43700, "app": "demo",
              "session_id": "session-1",
              "ts_ns": 9000000000
            }
            """;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper()
                .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        service = new InitEventServiceImpl(initDao, hostMetricDao, cudaDeviceDao, sessionDao,
                deviceMetricDao, cudaDeviceService, mapper, scopeEventDao, kernelEventDao, systemEventDao);
    }

    @Test
    void addInitEvent_newSession_savesInitialEventAndSession() {
        when(initDao.existsBySessionId("session-1")).thenReturn(false);
        EventWrapper wrapper = new EventWrapper(INIT_JSON_MINIMAL, 0L, "host", "127.0.0.1");

        service.addInitEvent(wrapper);

        verify(initDao).saveInitialEvent(any(InitialEventEntity.class));
        verify(sessionDao).saveSession(any());
    }

    @Test
    void addInitEvent_existingSession_skipsAllSaves() {
        when(initDao.existsBySessionId("session-1")).thenReturn(true);
        EventWrapper wrapper = new EventWrapper(INIT_JSON_MINIMAL, 0L, "host", "127.0.0.1");

        service.addInitEvent(wrapper);

        verify(initDao, never()).saveInitialEvent(any());
        verifyNoInteractions(sessionDao, deviceMetricDao, hostMetricDao, cudaDeviceDao);
    }

    @Test
    void addInitEvent_fullPayload_savesCudaDevices() {
        when(initDao.existsBySessionId("session-2")).thenReturn(false);
        EventWrapper wrapper = new EventWrapper(INIT_JSON_FULL, 0L, "host", "127.0.0.1");

        service.addInitEvent(wrapper);

        // Device/host metrics now arrive via batch messages, not inline in init.
        verifyNoInteractions(deviceMetricDao, hostMetricDao);
        verify(cudaDeviceDao).saveCudaDevice(any());
    }

    @Test
    void addInitEvent_invalidJson_logsErrorAndDoesNotThrow() {
        EventWrapper wrapper = new EventWrapper("not-json", 0L, "host", "127.0.0.1");

        assertDoesNotThrow(() -> service.addInitEvent(wrapper));
        verifyNoInteractions(initDao);
    }

    @Test
    void shutdownEvent_validJson_callsDao() {
        EventWrapper wrapper = new EventWrapper(SHUTDOWN_JSON, 0L, "host", "127.0.0.1");

        service.shutdownEvent(wrapper);

        verify(initDao).shutdownEvent("session-1", "demo", 9_000_000_000L);
    }

    @Test
    void shutdownEvent_invalidJson_logsErrorAndDoesNotThrow() {
        EventWrapper wrapper = new EventWrapper("bad", 0L, "host", "127.0.0.1");

        assertDoesNotThrow(() -> service.shutdownEvent(wrapper));
        verifyNoInteractions(initDao);
    }

    @Test
    void getInitEvents_noResults_returnsEmptyList() {
        when(initDao.findByDateRange(any(), any())).thenReturn(Collections.emptyList());

        List<?> result = service.getInitEvents(Instant.EPOCH, Instant.now());

        assertTrue(result.isEmpty());
        verifyNoInteractions(cudaDeviceService, hostMetricDao, scopeEventDao, kernelEventDao);
    }

    @Test
    void getInitEvents_withEntities_returnsMappedDtos() {
        InitialEventEntity entity = InitialEventEntity.builder()
                .sessionId("session-1")
                .pid(100)
                .app("demo")
                .logPath("demo.log")
                .tsNs(1_000_000_000L)
                .systemRateMs(10)
                .time(Instant.EPOCH)
                .build();

        when(initDao.findByDateRange(any(), any())).thenReturn(List.of(entity));
        when(cudaDeviceService.getCudaStaticDeviceEntities(any())).thenReturn(Collections.emptyList());
        when(hostMetricDao.findBySessionIds(any())).thenReturn(Collections.emptyList());
        when(scopeEventDao.findBySessionIds(any())).thenReturn(Collections.emptyList());
        when(kernelEventDao.findBySessionIds(any())).thenReturn(Collections.emptyList());

        List<?> result = service.getInitEvents(Instant.EPOCH, Instant.now());

        assertEquals(1, result.size());
    }
}
