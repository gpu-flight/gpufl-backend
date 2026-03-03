package com.gpuflight.gpuflbackend.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.gpuflight.gpuflbackend.dao.DeviceMetricDao;
import com.gpuflight.gpuflbackend.dao.HostMetricDao;
import com.gpuflight.gpuflbackend.dao.InitDao;
import com.gpuflight.gpuflbackend.dao.SystemEventDao;
import com.gpuflight.gpuflbackend.entity.SystemEventEntity;
import com.gpuflight.gpuflbackend.model.EventWrapper;
import com.gpuflight.gpuflbackend.model.MetricType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SystemEventServiceImplTest {

    @Mock private SystemEventDao systemEventDao;
    @Mock private HostMetricDao hostMetricDao;
    @Mock private DeviceMetricService deviceMetricService;
    @Mock private DeviceMetricDao deviceMetricDao;
    @Mock private InitDao initDao;

    private SystemEventServiceImpl service;

    private static final String SYSTEM_SAMPLE_JSON = """
            {
              "pid": 100, "app": "demo",
              "session_id": "session-1",
              "name": "system",
              "ts_ns": 1000000000
            }
            """;

    private static final String SYSTEM_SAMPLE_WITH_HOST_AND_DEVICES_JSON = """
            {
              "pid": 100, "app": "demo",
              "session_id": "session-1",
              "name": "system",
              "ts_ns": 1000000000,
              "host": {"cpu_pct": 10.5, "ram_used_mib": 2048, "ram_total_mib": 16384},
              "devices": [
                {"id": 0, "name": "GPU0", "uuid": "uuid-0", "vendor": "NVIDIA",
                 "pci_bus": 1, "used_mib": 200, "free_mib": 7800, "total_mib": 8000,
                 "util_gpu": 80, "util_mem": 30, "temp_c": 75, "power_mw": 120000,
                 "clk_gfx": 1800, "clk_sm": 1800, "clk_mem": 9000,
                 "throttle_pwr": 0, "throttle_therm": 0,
                 "pcie_rx_bw": 2048, "pcie_tx_bw": 1024}
              ]
            }
            """;

    @BeforeEach
    void setUp() {
        ObjectMapper mapper = new ObjectMapper()
                .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        service = new SystemEventServiceImpl(mapper, systemEventDao, hostMetricDao, deviceMetricService, deviceMetricDao, initDao);
    }

    @Test
    void addSystemEvent_systemStart_savesWithCorrectType() {
        EventWrapper wrapper = new EventWrapper(SYSTEM_SAMPLE_JSON, 0L, "host", "127.0.0.1");

        service.addSystemEvent(MetricType.system_start, wrapper);

        ArgumentCaptor<SystemEventEntity> captor = ArgumentCaptor.forClass(SystemEventEntity.class);
        verify(systemEventDao).saveSystemEvent(captor.capture());
        assertEquals("SYSTEM_START_EVENT", captor.getValue().getEventType());
    }

    @Test
    void addSystemEvent_systemStop_savesWithCorrectType() {
        EventWrapper wrapper = new EventWrapper(SYSTEM_SAMPLE_JSON, 0L, "host", "127.0.0.1");

        service.addSystemEvent(MetricType.system_stop, wrapper);

        ArgumentCaptor<SystemEventEntity> captor = ArgumentCaptor.forClass(SystemEventEntity.class);
        verify(systemEventDao).saveSystemEvent(captor.capture());
        assertEquals("SYSTEM_STOP_EVENT", captor.getValue().getEventType());
    }

    @Test
    void addSystemEvent_systemSample_savesWithCorrectType() {
        EventWrapper wrapper = new EventWrapper(SYSTEM_SAMPLE_JSON, 0L, "host", "127.0.0.1");

        service.addSystemEvent(MetricType.system_sample, wrapper);

        ArgumentCaptor<SystemEventEntity> captor = ArgumentCaptor.forClass(SystemEventEntity.class);
        verify(systemEventDao).saveSystemEvent(captor.capture());
        assertEquals("SYSTEM_SAMPLE_EVENT", captor.getValue().getEventType());
    }

    @Test
    void addSystemEvent_withHostAndDevices_savesAll() {
        EventWrapper wrapper = new EventWrapper(SYSTEM_SAMPLE_WITH_HOST_AND_DEVICES_JSON, 0L, "host", "127.0.0.1");

        service.addSystemEvent(MetricType.system_sample, wrapper);

        verify(systemEventDao).saveSystemEvent(any());
        verify(hostMetricDao).saveHostMetric(any());
        verify(deviceMetricService).saveDeviceMetric(any(), any(), any(), any(), any());
    }

    @Test
    void addSystemEvent_invalidJson_throwsRuntimeException() {
        EventWrapper wrapper = new EventWrapper("invalid", 0L, "host", "127.0.0.1");

        assertThrows(RuntimeException.class, () -> service.addSystemEvent(MetricType.system_sample, wrapper));
        verifyNoInteractions(systemEventDao);
    }

    @Test
    void getSystemEvents_noEvents_returnsEmptyList() {
        when(systemEventDao.findBySessionId("session-1")).thenReturn(Collections.emptyList());

        List<?> result = service.getSystemEvents("session-1", null, null);

        assertTrue(result.isEmpty());
        verifyNoInteractions(hostMetricDao, deviceMetricDao);
    }

    @Test
    void getSystemEvents_withEvents_returnsMappedDtos() {
        SystemEventEntity entity = SystemEventEntity.builder()
                .sessionId("session-1")
                .pid(100)
                .app("demo")
                .name("system")
                .eventType("system_sample")
                .tsNs(1_000_000_000L)
                .build();

        when(systemEventDao.findBySessionId("session-1")).thenReturn(List.of(entity));
        when(hostMetricDao.findBySessionIds(any())).thenReturn(Collections.emptyList());
        when(deviceMetricDao.findBySessionIds(any())).thenReturn(Collections.emptyList());

        List<?> result = service.getSystemEvents("session-1", null, null);

        assertEquals(1, result.size());
    }
}
