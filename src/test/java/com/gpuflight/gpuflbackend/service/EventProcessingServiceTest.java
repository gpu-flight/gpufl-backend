package com.gpuflight.gpuflbackend.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.gpuflight.gpuflbackend.dao.*;
import com.gpuflight.gpuflbackend.entity.*;
import com.gpuflight.gpuflbackend.model.*;
import com.gpuflight.gpuflbackend.validator.KernelEventValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EventProcessingServiceTest {

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
    void testHandleInit_SavesSessionDevicesAndCudaDevices() throws Exception {
        // Test data from actual log: gfl_block.log.system.0.log line 1
        String json = "{\"type\":\"init\",\"pid\":35424,\"app\":\"block_style_demo\",\"session_id\":\"43ccf4b6-5e3f-4f9b-b002-274b27b357d8\",\"logPath\":\"gfl_block.log\",\"ts_ns\":1766946801394177000,\"system_rate_ms\":10,\"host\":{\"cpu_pct\":18.1,\"ram_used_mib\":27678,\"ram_total_mib\":32189},\"devices\":[{\"id\":0,\"name\":\"NVIDIA GeForce RTX 5060 Laptop GPU\",\"uuid\":\"GPU-fc2e721b-a3b1-dc5b-63f0-76b7e624f037\",\"vendor\":\"NVIDIA\",\"pci_bus\":2,\"used_mib\":252,\"free_mib\":7899,\"total_mib\":8151,\"util_gpu\":0,\"util_mem\":0,\"temp_c\":37,\"power_mw\":12087,\"clk_gfx\":30,\"clk_sm\":30,\"clk_mem\":12001,\"throttle_pwr\":1,\"throttle_therm\":0,\"pcie_rx_bw\":733184,\"pcie_tx_bw\":84992}],\"cuda_static_devices\":[{\"id\":0,\"name\":\"NVIDIA GeForce RTX 5060 Laptop GPU\",\"uuid\":\"GPU-fc2e721b-a3b1-dc5b-63f0-76b7e624f037\",\"compute_major\":\"12\",\"compute_minor\":0,\"l2_cache_size\":33554432,\"shared_mem_per_block\":49152,\"regs_per_block\":65536,\"multi_processor_count\":26,\"warp_size\":32}]}";

        when(initDao.existsBySessionId(anyString())).thenReturn(false);

        service.processEvent(MetricType.init, json);

        ArgumentCaptor<InitialEventEntity> initCaptor = ArgumentCaptor.forClass(InitialEventEntity.class);
        verify(initDao).saveInitialEvent(initCaptor.capture());
        assertEquals("43ccf4b6-5e3f-4f9b-b002-274b27b357d8", initCaptor.getValue().getSessionId());

        ArgumentCaptor<SessionEntity> sessionCaptor = ArgumentCaptor.forClass(SessionEntity.class);
        verify(sessionDao).saveSession(sessionCaptor.capture());
        assertEquals("block_style_demo", sessionCaptor.getValue().getAppName());
        assertEquals(35424, sessionCaptor.getValue().getPid());

        ArgumentCaptor<DeviceEntity> deviceCaptor = ArgumentCaptor.forClass(DeviceEntity.class);
        verify(deviceDao).saveDevice(deviceCaptor.capture());
        assertEquals("GPU-fc2e721b-a3b1-dc5b-63f0-76b7e624f037", deviceCaptor.getValue().getUuid());
        assertEquals("NVIDIA", deviceCaptor.getValue().getVendor());

        ArgumentCaptor<CudaStaticDeviceEntity> cudaCaptor = ArgumentCaptor.forClass(CudaStaticDeviceEntity.class);
        verify(cudaDeviceDao).saveCudaDevice(cudaCaptor.capture());
        assertEquals("12", cudaCaptor.getValue().getComputeMajor());
        assertEquals(26, cudaCaptor.getValue().getMultiProcessorCount());
    }

    @Test
    void testHandleInit_SkipsIfAlreadyExists() throws Exception {
        String json = "{\"type\":\"init\",\"pid\":35424,\"app\":\"block_style_demo\",\"session_id\":\"43ccf4b6-5e3f-4f9b-b002-274b27b357d8\",\"logPath\":\"gfl_block.log\",\"ts_ns\":1766946801394177000,\"system_rate_ms\":10,\"host\":{\"cpu_pct\":18.1,\"ram_used_mib\":27678,\"ram_total_mib\":32189},\"devices\":[],\"cuda_static_devices\":[]}";

        when(initDao.existsBySessionId("43ccf4b6-5e3f-4f9b-b002-274b27b357d8")).thenReturn(true);

        service.processEvent(MetricType.init, json);

        verify(initDao, never()).saveInitialEvent(any());
        verify(sessionDao, never()).saveSession(any());
    }

    @Test
    void testHandleKernelBegin_SavesKernelEvent() throws Exception {
        // Test data from actual log: gfl_block.log.kernel.0.log line 2
        String json = "{\"type\":\"kernel_start\",\"pid\":35424,\"app\":\"block_style_demo\",\"session_id\":\"43ccf4b6-5e3f-4f9b-b002-274b27b357d8\",\"name\":\"_Z9vectorAddPiS_S_i\",\"platform\":\"cuda\",\"has_details\":true,\"device_id\":\"0\",\"uuid\":\"GPU-fc2e721b-a3b1-dc5b-63f0-76b7e624f037\",\"ts_ns\":1766946801620143166,\"duration_ns\":0,\"grid\":\"(4,1,1)\",\"block\":\"(256,1,1)\",\"dyn_shared_bytes\":0,\"num_regs\":16,\"static_shared_bytes\":0,\"local_bytes\":0,\"const_bytes\":0,\"occupancy\":0,\"max_active_blocks\":0,\"corr_id\":132,\"cuda_error\":\"\"}";

        doNothing().when(kernelEventValidator).validate(anyString(), anyMap());

        service.processEvent(MetricType.kernel_start, json);

        ArgumentCaptor<KernelEventEntity> captor = ArgumentCaptor.forClass(KernelEventEntity.class);
        verify(kernelEventDao).saveKernelBegin(captor.capture());

        KernelEventEntity entity = captor.getValue();
        assertEquals("_Z9vectorAddPiS_S_i", entity.getName());
        assertEquals("cuda", entity.getPlatform());
        assertEquals("(4,1,1)", entity.getGrid());
        assertEquals("(256,1,1)", entity.getBlock());
        assertEquals(132L, entity.getCorrId());
        assertTrue(entity.getHasDetails());
    }

    @Test
    void testHandleKernelEnd_UpdatesKernelEvent() throws Exception {
        // Test data from actual log: gfl_block.log.kernel.0.log line 3
        String json = "{\"type\":\"kernel_end\",\"pid\":35424,\"app\":\"block_style_demo\",\"session_id\":\"43ccf4b6-5e3f-4f9b-b002-274b27b357d8\",\"name\":\"_Z9vectorAddPiS_S_i\",\"ts_ns\":1766946801620144255,\"corr_id\":132,\"cuda_error\":\"\"}";

        service.processEvent(MetricType.kernel_end, json);

        ArgumentCaptor<KernelEventEntity> captor = ArgumentCaptor.forClass(KernelEventEntity.class);
        verify(kernelEventDao).updateKernelEnd(captor.capture());

        KernelEventEntity entity = captor.getValue();
        assertEquals(132L, entity.getCorrId());
        assertEquals(1766946801620144255L, entity.getEndNs());
        assertEquals("", entity.getCudaError());
    }

    @Test
    void testHandleScopeBegin_SavesScopeEvent() throws Exception {
        // Test data from actual log: gfl_block.log.scope.0.log line 2
        String json = "{\"type\":\"scope_begin\",\"pid\":35424,\"app\":\"block_style_demo\",\"session_id\":\"43ccf4b6-5e3f-4f9b-b002-274b27b357d8\",\"name\":\"computation-phase-1\",\"tag\":\"\",\"ts_ns\":1766946801618545800,\"host\":{\"cpu_pct\":10.8,\"ram_used_mib\":27772,\"ram_total_mib\":32189},\"devices\":[{\"id\":0,\"name\":\"NVIDIA GeForce RTX 5060 Laptop GPU\",\"uuid\":\"GPU-fc2e721b-a3b1-dc5b-63f0-76b7e624f037\",\"vendor\":\"NVIDIA\",\"pci_bus\":2,\"used_mib\":365,\"free_mib\":7785,\"total_mib\":8151,\"util_gpu\":0,\"util_mem\":0,\"temp_c\":37,\"power_mw\":12087,\"clk_gfx\":30,\"clk_sm\":30,\"clk_mem\":12001,\"throttle_pwr\":1,\"throttle_therm\":0,\"pcie_rx_bw\":11264,\"pcie_tx_bw\":94208}]}";

        service.processEvent(MetricType.scope_begin, json);

        ArgumentCaptor<ScopeEventEntity> captor = ArgumentCaptor.forClass(ScopeEventEntity.class);
        verify(scopeEventDao).saveScopeEvent(captor.capture());

        ScopeEventEntity entity = captor.getValue();
        assertEquals("computation-phase-1", entity.getName());
        assertEquals("SCOPE_BEGIN", entity.getType());
        assertEquals(10.8, entity.getHostCpuPct());
        assertEquals(27772, entity.getHostRamUsedMib());
    }

    @Test
    void testHandleScopeEnd_SavesScopeEvent() throws Exception {
        // Test data from actual log: gfl_block.log.scope.0.log line 3
        String json = "{\"type\":\"scope_end\",\"pid\":35424,\"app\":\"block_style_demo\",\"session_id\":\"43ccf4b6-5e3f-4f9b-b002-274b27b357d8\",\"name\":\"computation-phase-1\",\"tag\":\"\",\"ts_ns\":1766946801620180903,\"host\":{\"cpu_pct\":0.0,\"ram_used_mib\":27772,\"ram_total_mib\":32189},\"devices\":[{\"id\":0,\"name\":\"NVIDIA GeForce RTX 5060 Laptop GPU\",\"uuid\":\"GPU-fc2e721b-a3b1-dc5b-63f0-76b7e624f037\",\"vendor\":\"NVIDIA\",\"pci_bus\":2,\"used_mib\":365,\"free_mib\":7785,\"total_mib\":8151,\"util_gpu\":0,\"util_mem\":0,\"temp_c\":37,\"power_mw\":12087,\"clk_gfx\":30,\"clk_sm\":30,\"clk_mem\":12001,\"throttle_pwr\":1,\"throttle_therm\":0,\"pcie_rx_bw\":2329600,\"pcie_tx_bw\":4096}]}";

        service.processEvent(MetricType.scope_end, json);

        ArgumentCaptor<ScopeEventEntity> captor = ArgumentCaptor.forClass(ScopeEventEntity.class);
        verify(scopeEventDao).saveScopeEvent(captor.capture());

        ScopeEventEntity entity = captor.getValue();
        assertEquals("computation-phase-1", entity.getName());
        assertEquals("SCOPE_END", entity.getType());
        assertEquals(0.0, entity.getHostCpuPct());
        assertEquals(27772, entity.getHostRamUsedMib());
    }

    @Test
    void testHandleShutdown_UpdatesSessionEndTime() throws Exception {
        // Test data from actual log: gfl_block.log.system.0.log line 16
        String json = "{\"type\":\"shutdown\",\"pid\":35424,\"app\":\"block_style_demo\",\"session_id\":\"43ccf4b6-5e3f-4f9b-b002-274b27b357d8\",\"ts_ns\":1766946802353042900}";

        service.processEvent(MetricType.shutdown, json);

        ArgumentCaptor<SessionEntity> captor = ArgumentCaptor.forClass(SessionEntity.class);
        verify(sessionDao).updateSessionEndTime(captor.capture());

        SessionEntity entity = captor.getValue();
        assertEquals("43ccf4b6-5e3f-4f9b-b002-274b27b357d8", entity.getSessionId());
        assertNotNull(entity.getEndTime());
    }

    @Test
    void testHandleSystemStart_SavesMetrics() throws Exception {
        // Test data from actual log: gfl_block.log.system.0.log line 2
        String json = "{\"type\":\"system_start\",\"pid\":35424,\"app\":\"block_style_demo\",\"session_id\":\"\",\"name\":\"sampling_start\",\"ts_ns\":1766946801482414800,\"host\":{\"cpu_pct\":12.9,\"ram_used_mib\":27678,\"ram_total_mib\":32189},\"devices\":[{\"id\":0,\"name\":\"NVIDIA GeForce RTX 5060 Laptop GPU\",\"uuid\":\"GPU-fc2e721b-a3b1-dc5b-63f0-76b7e624f037\",\"vendor\":\"NVIDIA\",\"pci_bus\":2,\"used_mib\":252,\"free_mib\":7899,\"total_mib\":8151,\"util_gpu\":0,\"util_mem\":0,\"temp_c\":37,\"power_mw\":12087,\"clk_gfx\":30,\"clk_sm\":30,\"clk_mem\":12001,\"throttle_pwr\":1,\"throttle_therm\":0,\"pcie_rx_bw\":11264,\"pcie_tx_bw\":917504}]}";

        service.processEvent(MetricType.system_start, json);

        ArgumentCaptor<HostMetricEntity> hostCaptor = ArgumentCaptor.forClass(HostMetricEntity.class);
        verify(hostMetricDao).saveHostMetric(hostCaptor.capture());
        assertEquals("SYSTEM_START", hostCaptor.getValue().getType());
        assertEquals(12.9, hostCaptor.getValue().getCpuPct());

        ArgumentCaptor<DeviceMetricEntity> deviceCaptor = ArgumentCaptor.forClass(DeviceMetricEntity.class);
        verify(deviceMetricDao).saveDeviceMetric(deviceCaptor.capture());
        assertEquals("SYSTEM_START", deviceCaptor.getValue().getType());
        assertEquals(12.087, deviceCaptor.getValue().getPowerWatts(), 0.001);
    }

    @Test
    void testHandleSystemStop_SavesMetrics() throws Exception {
        // Test data from actual log: gfl_block.log.system.0.log line 15
        String json = "{\"type\":\"system_stop\",\"pid\":35424,\"app\":\"block_style_demo\",\"session_id\":\"43ccf4b6-5e3f-4f9b-b002-274b27b357d8\",\"name\":\"sampling_end\",\"tag\":\"\",\"ts_ns\":1766946802291874400,\"host\":{\"cpu_pct\":0.0,\"ram_used_mib\":27771,\"ram_total_mib\":32189},\"devices\":[{\"id\":0,\"name\":\"NVIDIA GeForce RTX 5060 Laptop GPU\",\"uuid\":\"GPU-fc2e721b-a3b1-dc5b-63f0-76b7e624f037\",\"vendor\":\"NVIDIA\",\"pci_bus\":2,\"used_mib\":365,\"free_mib\":7785,\"total_mib\":8151,\"util_gpu\":0,\"util_mem\":0,\"temp_c\":36,\"power_mw\":11174,\"clk_gfx\":1612,\"clk_sm\":1612,\"clk_mem\":9001,\"throttle_pwr\":0,\"throttle_therm\":0,\"pcie_rx_bw\":1163264,\"pcie_tx_bw\":1024}]}";

        service.processEvent(MetricType.system_stop, json);

        ArgumentCaptor<HostMetricEntity> hostCaptor = ArgumentCaptor.forClass(HostMetricEntity.class);
        verify(hostMetricDao).saveHostMetric(hostCaptor.capture());
        assertEquals("SYSTEM_STOP", hostCaptor.getValue().getType());
        assertEquals(0.0, hostCaptor.getValue().getCpuPct());

        ArgumentCaptor<DeviceMetricEntity> deviceCaptor = ArgumentCaptor.forClass(DeviceMetricEntity.class);
        verify(deviceMetricDao).saveDeviceMetric(deviceCaptor.capture());
        assertEquals("SYSTEM_STOP", deviceCaptor.getValue().getType());
        assertEquals(11.174, deviceCaptor.getValue().getPowerWatts(), 0.001);
    }

    @Test
    void testHandleSystemSample_SavesMetrics() throws Exception {
        // Test data from actual log: gfl_block.log.system.0.log line 3
        String json = "{\"type\":\"system_sample\",\"pid\":35424,\"app\":\"block_style_demo\",\"session_id\":\"43ccf4b6-5e3f-4f9b-b002-274b27b357d8\",\"name\":\"block_style_demo\",\"ts_ns\":1766946801544628700,\"host\":{\"cpu_pct\":0.0,\"ram_used_mib\":0,\"ram_total_mib\":0},\"devices\":[{\"id\":0,\"name\":\"NVIDIA GeForce RTX 5060 Laptop GPU\",\"uuid\":\"GPU-fc2e721b-a3b1-dc5b-63f0-76b7e624f037\",\"vendor\":\"NVIDIA\",\"pci_bus\":2,\"used_mib\":252,\"free_mib\":7899,\"total_mib\":8151,\"util_gpu\":0,\"util_mem\":0,\"temp_c\":37,\"power_mw\":12087,\"clk_gfx\":30,\"clk_sm\":30,\"clk_mem\":12001,\"throttle_pwr\":1,\"throttle_therm\":0,\"pcie_rx_bw\":219533312,\"pcie_tx_bw\":27842560}]}";

        service.processEvent(MetricType.system_sample, json);

        verify(hostMetricDao).saveHostMetric(argThat(entity ->
            "SYSTEM_SAMPLE".equals(entity.getType()) && entity.getCpuPct() == 0.0
        ));
        verify(deviceMetricDao).saveDeviceMetric(argThat(entity ->
            "SYSTEM_SAMPLE".equals(entity.getType()) && entity.getTempC() == 37
        ));
    }

    @Test
    void testHandleSystemSample_WithMultipleDevices() throws Exception {
        HostSample host = new HostSample(10.5, 1024, 8192);
        DeviceSample ds1 = new DeviceSample(0, "GPU-0", "uuid-0", "NVIDIA", 0, 512, 1536, 2048, 50, 20, 60, 100000, 1500, 1500, 5000, 0, 0, 100, 100);
        DeviceSample ds2 = new DeviceSample(1, "GPU-1", "uuid-1", "NVIDIA", 1, 600, 1800, 2400, 60, 30, 70, 110000, 1600, 1600, 6000, 1, 0, 200, 200);
        SystemSampleEvent event = new SystemSampleEvent(
                MetricType.system_sample, 1234, "test-app", "session-1", "sample", 1000000000L,
                host, List.of(ds1, ds2)
        );

        String json = objectMapper.writeValueAsString(event);
        service.processEvent(MetricType.system_sample, json);

        verify(deviceMetricDao, times(2)).saveDeviceMetric(any());
        verify(hostMetricDao, times(1)).saveHostMetric(any());
    }

    @Test
    void testHandleSystemSample_WithNullHost() throws Exception {
        DeviceSample ds = new DeviceSample(0, "GPU-0", "uuid-0", "NVIDIA", 0, 512, 1536, 2048, 50, 20, 60, 100000, 1500, 1500, 5000, 0, 0, 100, 100);
        SystemSampleEvent event = new SystemSampleEvent(
                MetricType.system_sample, 1234, "test-app", "session-1", "sample", 1000000000L,
                null, List.of(ds)
        );

        String json = objectMapper.writeValueAsString(event);
        service.processEvent(MetricType.system_sample, json);

        verify(hostMetricDao, never()).saveHostMetric(any());
        verify(deviceMetricDao, times(1)).saveDeviceMetric(any());
    }

    @Test
    void testHandleSystemSample_WithNullDevices() throws Exception {
        HostSample host = new HostSample(10.5, 1024, 8192);
        SystemSampleEvent event = new SystemSampleEvent(
                MetricType.system_sample, 1234, "test-app", "session-1", "sample", 1000000000L,
                host, null
        );

        String json = objectMapper.writeValueAsString(event);
        service.processEvent(MetricType.system_sample, json);

        verify(hostMetricDao, times(1)).saveHostMetric(any());
        verify(deviceMetricDao, never()).saveDeviceMetric(any());
    }

    @Test
    void testHandleKernelBegin_WithoutDetails() throws Exception {
        KernelBeginEvent event = new KernelBeginEvent(
                MetricType.kernel_start, 1234, "test-app", "session-1", "kernel-name",
                "cuda", "0", "uuid-0", 1000000000L, 0L, false,
                null, null, 0L, 0, 0L, 0L, 0L, null, 0L, 100, ""
        );

        String json = objectMapper.writeValueAsString(event);
        service.processEvent(MetricType.kernel_start, json);

        ArgumentCaptor<KernelEventEntity> captor = ArgumentCaptor.forClass(KernelEventEntity.class);
        verify(kernelEventDao).saveKernelBegin(captor.capture());
        assertFalse(captor.getValue().getHasDetails());
    }

    @Test
    void testHandleScopeBegin_WithNullHost() throws Exception {
        ScopeBeginEvent event = new ScopeBeginEvent(
                MetricType.scope_begin, 1234, "test-app", "session-1",
                "scope-name", "tag", 1000000000L, null, null
        );

        String json = objectMapper.writeValueAsString(event);
        service.processEvent(MetricType.scope_begin, json);

        ArgumentCaptor<ScopeEventEntity> captor = ArgumentCaptor.forClass(ScopeEventEntity.class);
        verify(scopeEventDao).saveScopeEvent(captor.capture());
        assertNull(captor.getValue().getHostCpuPct());
        assertNull(captor.getValue().getHostRamUsedMib());
    }

    @Test
    void testHandleScopeEnd_WithNullHost() throws Exception {
        ScopeEndEvent event = new ScopeEndEvent(
                MetricType.scope_end, 1234, "test-app", "session-1",
                "scope-name", "tag", 1000000000L, null, null
        );

        String json = objectMapper.writeValueAsString(event);
        service.processEvent(MetricType.scope_end, json);

        ArgumentCaptor<ScopeEventEntity> captor = ArgumentCaptor.forClass(ScopeEventEntity.class);
        verify(scopeEventDao).saveScopeEvent(captor.capture());
        assertNull(captor.getValue().getHostCpuPct());
        assertNull(captor.getValue().getHostRamUsedMib());
    }

    @Test
    void testProcessEvent_WithInvalidJson() throws Exception {
        // This should throw a JsonProcessingException
        String json = "{invalid json}";
        assertThrows(Exception.class, () -> service.processEvent(MetricType.init, json));
    }

    @Test
    void testHandleKernelBegin_ValidatesEvent() throws Exception {
        String json = "{\"type\":\"kernel_start\",\"pid\":35424,\"app\":\"block_style_demo\",\"session_id\":\"43ccf4b6-5e3f-4f9b-b002-274b27b357d8\",\"name\":\"_Z9vectorAddPiS_S_i\",\"platform\":\"cuda\",\"has_details\":true,\"device_id\":\"0\",\"uuid\":\"GPU-fc2e721b-a3b1-dc5b-63f0-76b7e624f037\",\"ts_ns\":1766946801620143166,\"duration_ns\":0,\"grid\":\"(4,1,1)\",\"block\":\"(256,1,1)\",\"dyn_shared_bytes\":0,\"num_regs\":16,\"static_shared_bytes\":0,\"local_bytes\":0,\"const_bytes\":0,\"occupancy\":0,\"max_active_blocks\":0,\"corr_id\":132,\"cuda_error\":\"\"}";

        service.processEvent(MetricType.kernel_start, json);

        verify(kernelEventValidator).validate(eq("cuda"), anyMap());
    }

    @Test
    void testHandleInit_WithEmptyDevices() throws Exception {
        String json = "{\"type\":\"init\",\"pid\":35424,\"app\":\"block_style_demo\",\"session_id\":\"43ccf4b6-5e3f-4f9b-b002-274b27b357d8\",\"logPath\":\"gfl_block.log\",\"ts_ns\":1766946801394177000,\"system_rate_ms\":10,\"host\":{\"cpu_pct\":18.1,\"ram_used_mib\":27678,\"ram_total_mib\":32189},\"devices\":null,\"cuda_static_devices\":null}";

        when(initDao.existsBySessionId(anyString())).thenReturn(false);

        service.processEvent(MetricType.init, json);

        verify(deviceDao, never()).saveDevice(any());
        verify(cudaDeviceDao, never()).saveCudaDevice(any());
        verify(sessionDao).saveSession(any());
    }

    @Test
    void testDeviceMetrics_ExtendedMetricsFormat() throws Exception {
        String json = "{\"type\":\"system_sample\",\"pid\":35424,\"app\":\"block_style_demo\",\"session_id\":\"43ccf4b6-5e3f-4f9b-b002-274b27b357d8\",\"name\":\"block_style_demo\",\"ts_ns\":1766946801544628700,\"host\":{\"cpu_pct\":0.0,\"ram_used_mib\":0,\"ram_total_mib\":0},\"devices\":[{\"id\":0,\"name\":\"NVIDIA GeForce RTX 5060 Laptop GPU\",\"uuid\":\"GPU-fc2e721b-a3b1-dc5b-63f0-76b7e624f037\",\"vendor\":\"NVIDIA\",\"pci_bus\":2,\"used_mib\":252,\"free_mib\":7899,\"total_mib\":8151,\"util_gpu\":0,\"util_mem\":0,\"temp_c\":37,\"power_mw\":12087,\"clk_gfx\":30,\"clk_sm\":30,\"clk_mem\":12001,\"throttle_pwr\":1,\"throttle_therm\":0,\"pcie_rx_bw\":219533312,\"pcie_tx_bw\":27842560}]}";

        service.processEvent(MetricType.system_sample, json);

        ArgumentCaptor<DeviceMetricEntity> captor = ArgumentCaptor.forClass(DeviceMetricEntity.class);
        verify(deviceMetricDao).saveDeviceMetric(captor.capture());

        String extendedMetrics = captor.getValue().getExtendedMetrics();
        assertNotNull(extendedMetrics);
        assertTrue(extendedMetrics.contains("clk_gfx"));
        assertTrue(extendedMetrics.contains("pcie_rx_bw"));
    }
}
