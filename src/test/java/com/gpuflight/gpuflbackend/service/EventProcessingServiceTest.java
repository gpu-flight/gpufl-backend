package com.gpuflight.gpuflbackend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gpuflight.gpuflbackend.dao.*;
import com.gpuflight.gpuflbackend.model.DeviceSample;
import com.gpuflight.gpuflbackend.model.HostSample;
import com.gpuflight.gpuflbackend.model.MetricType;
import com.gpuflight.gpuflbackend.model.SystemSampleEvent;
import com.gpuflight.gpuflbackend.validator.KernelEventValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class EventProcessingServiceTest {

    @Mock private SessionDao sessionDao;
    @Mock private DeviceDao deviceDao;
    @Mock private ScopeEventDao scopeEventDao;
    @Mock private KernelEventDao kernelEventDao;
    @Mock private SystemMetricDao systemMetricDao;
    @Mock private InitDao initDao;
    @Mock private KernelEventValidator kernelEventValidator;
    
    private EventProcessingService service;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new EventProcessingService(
                sessionDao, deviceDao, scopeEventDao, kernelEventDao,
                systemMetricDao, initDao, objectMapper, kernelEventValidator
        );
    }

    @Test
    void testHandleSystemSample_SavesToSystemMetricDao() throws Exception {
        HostSample host = new HostSample(10.5, 1024, 8192);
        DeviceSample ds = new DeviceSample(0, "GPU-0", "uuid-0", 0, 512, 1536, 2048, 50, 20, 60, 100000, 1500, 1500, 5000, 0, 0, 100, 100);
        SystemSampleEvent event = new SystemSampleEvent(
                MetricType.system_sample, 1234, "test-app", "session-1", "sample", 1000000000L,
                host, List.of(ds)
        );

        String json = objectMapper.writeValueAsString(event);
        service.processEvent(MetricType.system_sample, json);

        verify(systemMetricDao, times(1)).saveSystemMetric(any());
    }

    @Test
    void testHandleSystemStart_SavesOnlyToSystemMetricDao() throws Exception {
        com.gpuflight.gpuflbackend.model.SystemStartEvent event = new com.gpuflight.gpuflbackend.model.SystemStartEvent(
                MetricType.system_start, 1234, "test-app", "start-event", "session-1", 1000000000L,
                new HostSample(5.0, 512, 4096), List.of(new DeviceSample(0, "GPU-0", "uuid-0", 0, 256, 768, 1024, 30, 10, 50, 50000, 1200, 1200, 4000, 0, 0, 50, 50))
        );

        String json = objectMapper.writeValueAsString(event);
        service.processEvent(MetricType.system_start, json);

        verify(scopeEventDao, never()).saveScopeEvent(any());
        verify(systemMetricDao, times(1)).saveSystemMetric(argThat(entity -> "SYSTEM_START".equals(entity.getType())));
    }
}
