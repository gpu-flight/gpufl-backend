package com.gpuflight.gpuflbackend.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.gpuflight.gpuflbackend.dao.HostMetricDao;
import com.gpuflight.gpuflbackend.dao.ScopeEventDao;
import com.gpuflight.gpuflbackend.entity.ScopeEventEntity;
import com.gpuflight.gpuflbackend.model.EventWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScopeEventServiceImplTest {

    @Mock private ScopeEventDao scopeEventDao;
    @Mock private DeviceMetricService deviceMetricService;
    @Mock private HostMetricDao hostMetricDao;

    private ScopeEventServiceImpl service;

    private static final String SCOPE_BEGIN_JSON = """
            {
              "pid": 100, "app": "demo",
              "session_id": "session-1",
              "name": "training-step", "tag": "phase1",
              "ts_ns": 1000000000,
              "user_scope": "global|training-step", "scope_depth": 1
            }
            """;

    private static final String SCOPE_BEGIN_WITH_HOST_AND_DEVICES_JSON = """
            {
              "pid": 100, "app": "demo",
              "session_id": "session-1",
              "name": "training-step", "tag": "",
              "ts_ns": 1000000000,
              "user_scope": "global", "scope_depth": 0,
              "host": {"cpu_pct": 5.0, "ram_used_mib": 1024, "ram_total_mib": 16384},
              "devices": [
                {"id": 0, "name": "GPU0", "uuid": "uuid-0", "vendor": "NVIDIA",
                 "pci_bus": 1, "used_mib": 100, "free_mib": 7900, "total_mib": 8000,
                 "util_gpu": 50, "util_mem": 20, "temp_c": 65, "power_mw": 80000,
                 "clk_gfx": 1500, "clk_sm": 1500, "clk_mem": 9000,
                 "throttle_pwr": 0, "throttle_therm": 0,
                 "pcie_rx_bw": 1024, "pcie_tx_bw": 512}
              ]
            }
            """;

    private static final String SCOPE_END_JSON = """
            {
              "pid": 100, "app": "demo",
              "session_id": "session-1",
              "name": "training-step",
              "ts_ns": 2000000000,
              "user_scope": "global|training-step", "scope_depth": 1
            }
            """;

    @BeforeEach
    void setUp() {
        ObjectMapper mapper = new ObjectMapper()
                .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        service = new ScopeEventServiceImpl(scopeEventDao, mapper, deviceMetricService, hostMetricDao);
    }

    @Test
    void addScopeEventBegin_minimal_savesScopeEvent() {
        EventWrapper wrapper = new EventWrapper(SCOPE_BEGIN_JSON, 0L, "host", "127.0.0.1");

        service.addScopeEventBegin(wrapper);

        verify(scopeEventDao).saveScopeEvent(any(ScopeEventEntity.class));
        verifyNoInteractions(deviceMetricService, hostMetricDao);
    }

    @Test
    void addScopeEventBegin_withHostAndDevices_savesAll() {
        EventWrapper wrapper = new EventWrapper(SCOPE_BEGIN_WITH_HOST_AND_DEVICES_JSON, 0L, "host", "127.0.0.1");

        service.addScopeEventBegin(wrapper);

        verify(scopeEventDao).saveScopeEvent(any(ScopeEventEntity.class));
        verify(deviceMetricService).saveDeviceMetric(any(), any(), any(), any(), any());
        verify(hostMetricDao).saveHostMetric(any());
    }

    @Test
    void addScopeEventBegin_invalidJson_throwsRuntimeException() {
        EventWrapper wrapper = new EventWrapper("invalid", 0L, "host", "127.0.0.1");

        assertThrows(RuntimeException.class, () -> service.addScopeEventBegin(wrapper));
        verifyNoInteractions(scopeEventDao);
    }

    @Test
    void addScopeEventEnd_validJson_updatesScope() {
        EventWrapper wrapper = new EventWrapper(SCOPE_END_JSON, 0L, "host", "127.0.0.1");

        service.addScopeEventEnd(wrapper);

        verify(scopeEventDao).updateScopeEventEnd(any(ScopeEventEntity.class));
    }

    @Test
    void addScopeEventEnd_withHostAndDevices_savesAll() {
        EventWrapper wrapper = new EventWrapper(SCOPE_BEGIN_WITH_HOST_AND_DEVICES_JSON, 0L, "host", "127.0.0.1");

        service.addScopeEventEnd(wrapper);

        verify(scopeEventDao).updateScopeEventEnd(any(ScopeEventEntity.class));
        verify(deviceMetricService).saveDeviceMetric(any(), any(), any(), any(), any());
        verify(hostMetricDao).saveHostMetric(any());
    }

    @Test
    void addScopeEventEnd_invalidJson_throwsRuntimeException() {
        EventWrapper wrapper = new EventWrapper("invalid", 0L, "host", "127.0.0.1");

        assertThrows(RuntimeException.class, () -> service.addScopeEventEnd(wrapper));
        verifyNoInteractions(scopeEventDao);
    }
}
