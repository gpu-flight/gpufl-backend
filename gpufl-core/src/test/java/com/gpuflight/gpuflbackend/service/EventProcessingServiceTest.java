package com.gpuflight.gpuflbackend.service;

import com.gpuflight.gpuflbackend.model.EventWrapper;
import com.gpuflight.gpuflbackend.model.MetricType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventProcessingServiceTest {

    @Mock private InitEventService initEventService;
    @Mock private SystemEventService systemEventService;
    @Mock private BatchIngestionService batchIngestionService;

    private EventProcessingService service;
    private EventWrapper wrapper;

    @BeforeEach
    void setUp() {
        service = new EventProcessingService(initEventService, systemEventService, batchIngestionService);
        wrapper = new EventWrapper("{}", 0L, "localhost", "127.0.0.1");
    }

    @Test
    void processEvent_jobStart_routesToInitService() {
        service.processEvent(MetricType.job_start, wrapper);
        verify(initEventService).addInitEvent(wrapper);
        verifyNoInteractions(systemEventService, batchIngestionService);
    }

    @Test
    void processEvent_shutdown_routesToInitServiceShutdown() {
        service.processEvent(MetricType.shutdown, wrapper);
        verify(initEventService).shutdownEvent(wrapper);
        verifyNoInteractions(systemEventService, batchIngestionService);
    }

    @Test
    void processEvent_systemStart_routesToSystemService() {
        service.processEvent(MetricType.system_start, wrapper);
        verify(systemEventService).addSystemEvent(MetricType.system_start, wrapper);
        verifyNoInteractions(initEventService, batchIngestionService);
    }

    @Test
    void processEvent_systemStop_routesToSystemService() {
        service.processEvent(MetricType.system_stop, wrapper);
        verify(systemEventService).addSystemEvent(MetricType.system_stop, wrapper);
    }

    @Test
    void processEvent_dictionaryUpdate_routesToBatchService() {
        service.processEvent(MetricType.dictionary_update, wrapper);
        verify(batchIngestionService).handle(MetricType.dictionary_update, wrapper);
        verifyNoInteractions(initEventService, systemEventService);
    }

    @Test
    void processEvent_kernelEventBatch_routesToBatchService() {
        service.processEvent(MetricType.kernel_event_batch, wrapper);
        verify(batchIngestionService).handle(MetricType.kernel_event_batch, wrapper);
    }

    @Test
    void processEvent_kernelDetail_routesToBatchService() {
        service.processEvent(MetricType.kernel_detail, wrapper);
        verify(batchIngestionService).handle(MetricType.kernel_detail, wrapper);
    }

    @Test
    void processEvent_memcpyEventBatch_routesToBatchService() {
        service.processEvent(MetricType.memcpy_event_batch, wrapper);
        verify(batchIngestionService).handle(MetricType.memcpy_event_batch, wrapper);
    }

    @Test
    void processEvent_deviceMetricBatch_routesToBatchService() {
        service.processEvent(MetricType.device_metric_batch, wrapper);
        verify(batchIngestionService).handle(MetricType.device_metric_batch, wrapper);
    }

    @Test
    void processEvent_hostMetricBatch_routesToBatchService() {
        service.processEvent(MetricType.host_metric_batch, wrapper);
        verify(batchIngestionService).handle(MetricType.host_metric_batch, wrapper);
    }

    @Test
    void processEvent_scopeEventBatch_routesToBatchService() {
        service.processEvent(MetricType.scope_event_batch, wrapper);
        verify(batchIngestionService).handle(MetricType.scope_event_batch, wrapper);
    }

    @Test
    void processEvent_profileSampleBatch_routesToBatchService() {
        service.processEvent(MetricType.profile_sample_batch, wrapper);
        verify(batchIngestionService).handle(MetricType.profile_sample_batch, wrapper);
    }
}
