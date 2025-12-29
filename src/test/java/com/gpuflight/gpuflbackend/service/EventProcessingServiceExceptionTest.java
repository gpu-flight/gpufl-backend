package com.gpuflight.gpuflbackend.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.gpuflight.gpuflbackend.dao.*;
import com.gpuflight.gpuflbackend.exception.EventProcessingException;
import com.gpuflight.gpuflbackend.model.MetricType;
import com.gpuflight.gpuflbackend.validator.KernelEventValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataAccessResourceFailureException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests for exception handling in EventProcessingService
 */
class EventProcessingServiceExceptionTest {

    @Mock private SessionDao sessionDao;
    @Mock private DeviceDao deviceDao;
    @Mock private CudaDeviceDao cudaDeviceDao;
    @Mock private ScopeEventDao scopeEventDao;
    @Mock private KernelEventDao kernelEventDao;
    @Mock private DeviceMetricDao deviceMetricDao;
    @Mock private HostMetricDao hostMetricDao;
    @Mock private InitDao initDao;
    @Mock private KernelEventValidator kernelEventValidator;

    private EventProcessingService service;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new EventProcessingService(
                sessionDao, deviceDao, cudaDeviceDao, scopeEventDao, kernelEventDao,
                deviceMetricDao, hostMetricDao, initDao, objectMapper, kernelEventValidator
        );
    }

    @Test
    void testHandleInit_DatabaseException() {
        String json = "{\"type\":\"init\",\"pid\":35424,\"app\":\"test-app\",\"session_id\":\"test-session\",\"ts_ns\":1766946801394177000,\"system_rate_ms\":10,\"host\":{\"cpu_pct\":18.1,\"ram_used_mib\":27678,\"ram_total_mib\":32189},\"devices\":[],\"cuda_static_devices\":[]}";

        when(initDao.existsBySessionId(anyString())).thenReturn(false);
        doThrow(new DataAccessResourceFailureException("Database connection failed"))
                .when(initDao).saveInitialEvent(any());

        EventProcessingException exception = assertThrows(
                EventProcessingException.class,
                () -> service.processEvent(MetricType.init, json)
        );

        assertEquals("init", exception.getEventType());
        assertEquals("test-session", exception.getSessionId());
        assertTrue(exception.getMessage().contains("Failed to save init event to database"));
        assertNotNull(exception.getCause());
    }

    @Test
    void testHandleInit_DeviceDaoException() {
        String json = "{\"type\":\"init\",\"pid\":35424,\"app\":\"test-app\",\"session_id\":\"test-session\",\"ts_ns\":1766946801394177000,\"system_rate_ms\":10,\"host\":{\"cpu_pct\":18.1,\"ram_used_mib\":27678,\"ram_total_mib\":32189},\"devices\":[{\"id\":0,\"name\":\"GPU-0\",\"uuid\":\"uuid-0\",\"vendor\":\"NVIDIA\",\"pci_bus\":0,\"used_mib\":252,\"free_mib\":7899,\"total_mib\":8151,\"util_gpu\":0,\"util_mem\":0,\"temp_c\":37,\"power_mw\":12087,\"clk_gfx\":30,\"clk_sm\":30,\"clk_mem\":12001,\"throttle_pwr\":1,\"throttle_therm\":0,\"pcie_rx_bw\":733184,\"pcie_tx_bw\":84992}],\"cuda_static_devices\":[]}";

        when(initDao.existsBySessionId(anyString())).thenReturn(false);
        doNothing().when(initDao).saveInitialEvent(any());
        doNothing().when(sessionDao).saveSession(any());
        doThrow(new DataAccessResourceFailureException("Failed to save device"))
                .when(deviceDao).saveDevice(any());

        EventProcessingException exception = assertThrows(
                EventProcessingException.class,
                () -> service.processEvent(MetricType.init, json)
        );

        assertEquals("init", exception.getEventType());
        assertTrue(exception.getMessage().contains("database"));
    }

    @Test
    void testHandleInit_UnexpectedException() {
        String json = "{\"type\":\"init\",\"pid\":35424,\"app\":\"test-app\",\"session_id\":\"test-session\",\"ts_ns\":1766946801394177000,\"system_rate_ms\":10,\"host\":{\"cpu_pct\":18.1,\"ram_used_mib\":27678,\"ram_total_mib\":32189},\"devices\":[],\"cuda_static_devices\":[]}";

        when(initDao.existsBySessionId(anyString())).thenReturn(false);
        doThrow(new NullPointerException("Unexpected error"))
                .when(initDao).saveInitialEvent(any());

        EventProcessingException exception = assertThrows(
                EventProcessingException.class,
                () -> service.processEvent(MetricType.init, json)
        );

        assertEquals("init", exception.getEventType());
        assertTrue(exception.getMessage().contains("Unexpected error during init event processing"));
    }

    @Test
    void testHandleInit_SkipsExisting_NoException() {
        String json = "{\"type\":\"init\",\"pid\":35424,\"app\":\"test-app\",\"session_id\":\"existing-session\",\"ts_ns\":1766946801394177000,\"system_rate_ms\":10,\"host\":{\"cpu_pct\":18.1,\"ram_used_mib\":27678,\"ram_total_mib\":32189},\"devices\":[],\"cuda_static_devices\":[]}";

        when(initDao.existsBySessionId("existing-session")).thenReturn(true);

        assertDoesNotThrow(() -> service.processEvent(MetricType.init, json));

        verify(initDao, never()).saveInitialEvent(any());
        verify(sessionDao, never()).saveSession(any());
    }

    @Test
    void testEventProcessingException_WithCause() {
        Exception rootCause = new IllegalStateException("Root cause");
        EventProcessingException exception = new EventProcessingException(
                "Processing failed",
                rootCause,
                "kernel_start",
                "session-123"
        );

        assertEquals("Processing failed", exception.getMessage());
        assertEquals("kernel_start", exception.getEventType());
        assertEquals("session-123", exception.getSessionId());
        assertEquals(rootCause, exception.getCause());
    }

    @Test
    void testEventProcessingException_WithoutCause() {
        EventProcessingException exception = new EventProcessingException(
                "Processing failed",
                "system_sample",
                "session-456"
        );

        assertEquals("Processing failed", exception.getMessage());
        assertEquals("system_sample", exception.getEventType());
        assertEquals("session-456", exception.getSessionId());
        assertNull(exception.getCause());
    }
}
