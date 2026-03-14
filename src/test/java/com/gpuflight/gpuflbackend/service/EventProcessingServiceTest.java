package com.gpuflight.gpuflbackend.service;

import com.gpuflight.gpuflbackend.model.EventWrapper;
import com.gpuflight.gpuflbackend.model.MetricType;
import com.gpuflight.gpuflbackend.service.ProfileSampleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventProcessingServiceTest {

    @Mock private InitEventService initEventService;
    @Mock private KernelEventService kernelEventService;
    @Mock private ScopeEventService scopeEventService;
    @Mock private SystemEventService systemEventService;
    @Mock private ProfileSampleService profileSampleService;

    private EventProcessingService service;
    private EventWrapper wrapper;

    @BeforeEach
    void setUp() {
        service = new EventProcessingService(initEventService, kernelEventService, scopeEventService, systemEventService, profileSampleService);
        wrapper = new EventWrapper("{}", 0L, "localhost", "127.0.0.1");
    }

    @Test
    void processEvent_init_routesToInitService() {
        service.processEvent(MetricType.init, wrapper);
        verify(initEventService).addInitEvent(wrapper);
        verifyNoInteractions(kernelEventService, scopeEventService, systemEventService);
    }

    @Test
    void processEvent_kernelEvent_routesToKernelService() {
        service.processEvent(MetricType.kernel_event, wrapper);
        verify(kernelEventService).addKernelEvent(wrapper);
        verifyNoInteractions(initEventService, scopeEventService, systemEventService);
    }

    @Test
    void processEvent_scopeBegin_routesToScopeService() {
        service.processEvent(MetricType.scope_begin, wrapper);
        verify(scopeEventService).addScopeEventBegin(wrapper);
        verifyNoInteractions(initEventService, kernelEventService, systemEventService);
    }

    @Test
    void processEvent_scopeEnd_routesToScopeService() {
        service.processEvent(MetricType.scope_end, wrapper);
        verify(scopeEventService).addScopeEventEnd(wrapper);
        verifyNoInteractions(initEventService, kernelEventService, systemEventService);
    }

    @Test
    void processEvent_shutdown_routesToInitServiceShutdown() {
        service.processEvent(MetricType.shutdown, wrapper);
        verify(initEventService).shutdownEvent(wrapper);
        verifyNoInteractions(kernelEventService, scopeEventService, systemEventService);
    }

    @Test
    void processEvent_systemStart_routesToSystemService() {
        service.processEvent(MetricType.system_start, wrapper);
        verify(systemEventService).addSystemEvent(MetricType.system_start, wrapper);
        verifyNoInteractions(initEventService, kernelEventService, scopeEventService);
    }

    @Test
    void processEvent_systemStop_routesToSystemService() {
        service.processEvent(MetricType.system_stop, wrapper);
        verify(systemEventService).addSystemEvent(MetricType.system_stop, wrapper);
    }

    @Test
    void processEvent_systemSample_routesToSystemService() {
        service.processEvent(MetricType.system_sample, wrapper);
        verify(systemEventService).addSystemEvent(MetricType.system_sample, wrapper);
    }

    @Test
    void processEvent_profileSample_routesToProfileSampleService() {
        service.processEvent(MetricType.profile_sample, wrapper);
        verify(profileSampleService).addProfileSample(wrapper);
        verifyNoInteractions(initEventService, kernelEventService, scopeEventService, systemEventService);
    }
}
