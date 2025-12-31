package com.gpuflight.gpuflbackend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gpuflight.gpuflbackend.model.*;
import com.gpuflight.gpuflbackend.model.input.KernelBeginEvent;
import com.gpuflight.gpuflbackend.service.JsonEventParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JsonEventParserTest {

    private final JsonEventParser parser = new JsonEventParser(new ObjectMapper());

    @Test
    void testParseInitEvent() throws Exception {
        String json = "{\"type\":\"init\",\"pid\":43700,\"app\":\"block_style_demo\",\"logPath\":\"gfl_block.log\",\"ts_ns\":1766907920508090978,\"system_rate_ms\":10,\"host\":{\"cpu_pct\":7.5,\"ram_used_mib\":25499,\"ram_total_mib\":32189},\"devices\":[{\"id\":0,\"name\":\"NVIDIA GeForce RTX 5060 Laptop GPU\",\"uuid\":\"GPU-fc2e721b-a3b1-dc5b-63f0-76b7e624f037\",\"pci_bus\":2,\"used_mib\":252,\"free_mib\":7899,\"total_mib\":8151,\"util_gpu\":0,\"util_mem\":0,\"temp_c\":37,\"power_mw\":10735,\"clk_gfx\":562,\"clk_sm\":562,\"clk_mem\":12001,\"throttle_pwr\":1,\"throttle_therm\":0,\"pcie_rx_bw\":252928,\"pcie_tx_bw\":1024}],\"cuda_static_devices\":[{\"id\":0,\"name\":\"NVIDIA GeForce RTX 5060 Laptop GPU\",\"uuid\":\"GPU-fc2e721b-a3b1-dc5b-63f0-76b7e624f037\",\"compute_major\":\"12\",\"compute_minor\":0,\"l2_cache_size\":33554432,\"shared_mem_per_block\":49152,\"regs_per_block\":65536,\"multi_processor_count\":26,\"warp_size\":32}]}";
        MetricEvent event = parser.getObjectMapper().readValue(json, MetricEvent.class);
        assertInstanceOf(InitEvent.class, event);
        InitEvent initEvent = (InitEvent) event;
        assertEquals(43700, initEvent.pid());
        assertEquals("block_style_demo", initEvent.app());
        assertEquals(381092867467000L, initEvent.tsNs());
        assertEquals(10, initEvent.systemRateMs());
    }

    @Test
    void testParseSystemSampleEvent() throws Exception {
        String json = "{\"type\":\"system_sample\",\"pid\":43700,\"app\":\"block_style_demo\",\"session_id\":\"43ccf4b6-5e3f-4f9b-b002-274b27b357d8\",\"name\":\"system\",\"ts_ns\":1766907920508090978,\"host\":{\"cpu_pct\":2.1,\"ram_used_mib\":25500,\"ram_total_mib\":32189},\"devices\":[{\"id\":0,\"name\":\"NVIDIA GeForce RTX 5060 Laptop GPU\",\"uuid\":\"GPU-fc2e721b-a3b1-dc5b-63f0-76b7e624f037\",\"pci_bus\":2,\"used_mib\":252,\"free_mib\":7899,\"total_mib\":8151,\"util_gpu\":0,\"util_mem\":0,\"temp_c\":37,\"power_mw\":10735,\"clk_gfx\":562,\"clk_sm\":562,\"clk_mem\":12001,\"throttle_pwr\":1,\"throttle_therm\":0,\"pcie_rx_bw\":892928,\"pcie_tx_bw\":1024}]}";
        MetricEvent event = parser.getObjectMapper().readValue(json, MetricEvent.class);
        assertInstanceOf(SystemSampleEvent.class, event);
        SystemSampleEvent systemSampleEvent = (SystemSampleEvent) event;
        assertEquals("system", systemSampleEvent.name());
        assertEquals("43ccf4b6-5e3f-4f9b-b002-274b27b357d8", systemSampleEvent.sessionId());
    }

    @Test
    void testParseKernelStartEvent() throws Exception {
        String json = "{\"type\":\"kernel_start\",\"pid\":43700,\"app\":\"block_style_demo\",\"name\":\"_Z9vectorAddPiS_S_i\",\"uuid\":\"\",\"ts_ns\":1766907920508090978,\"duration_ns\":0,\"grid\":\"(4,1,1)\",\"block\":\"(256,1,1)\",\"dyn_shared_bytes\":0,\"num_regs\":16,\"static_shared_bytes\":0,\"local_bytes\":0,\"const_bytes\":0,\"occupancy\":0,\"max_active_blocks\":0,\"corr_id\":132,\"cuda_error\":\"\"}";
        MetricEvent event = parser.getObjectMapper().readValue(json, MetricEvent.class);
        assertInstanceOf(KernelBeginEvent.class, event);
        KernelBeginEvent kernelBeginEvent = (KernelBeginEvent) event;
        assertEquals("_Z9vectorAddPiS_S_i", kernelBeginEvent.name());
        assertEquals(16, kernelBeginEvent.numRegs());
    }

    @Test
    void testParseShutdownEvent() throws Exception {
        String json = "{\"type\":\"shutdown\",\"pid\":43700,\"app\":\"block_style_demo\",\"ts_ns\":1766907920508090978}";
        MetricEvent event = parser.getObjectMapper().readValue(json, MetricEvent.class);
        assertInstanceOf(ShutdownEvent.class, event);
        ShutdownEvent shutdownEvent = (ShutdownEvent) event;
        assertEquals(43700, shutdownEvent.pid());
    }

    @Test
    void testParseEventWithType() throws Exception {
        String json = "{\"type\":\"shutdown\",\"pid\":43700,\"app\":\"block_style_demo\",\"ts_ns\":1766907920508090978}";
        ShutdownEvent event = parser.getObjectMapper().readValue(json, ShutdownEvent.class);
        assertInstanceOf(ShutdownEvent.class, event);
        assertEquals(43700, event.pid());
    }
    @Test
    void testParseScopeBeginEventFromIssue() throws Exception {
        String json = "{\"type\":\"scope_begin\",\"pid\":35424,\"app\":\"block_style_demo\",\"session_id\":\"43ccf4b6-5e3f-4f9b-b002-274b27b357d8\",\"name\":\"computation-phase-1\",\"tag\":\"\",\"ts_ns\":1766946801618545800,\"host\":{\"cpu_pct\":10.8,\"ram_used_mib\":27772,\"ram_total_mib\":32189},\"devices\":[{\"id\":0,\"name\":\"NVIDIA GeForce RTX 5060 Laptop GPU\",\"uuid\":\"GPU-fc2e721b-a3b1-dc5b-63f0-76b7e624f037\",\"vendor\":\"NVIDIA\",\"pci_bus\":2,\"used_mib\":365,\"free_mib\":7785,\"total_mib\":8151,\"util_gpu\":0,\"util_mem\":0,\"temp_c\":37,\"power_mw\":12087,\"clk_gfx\":30,\"clk_sm\":30,\"clk_mem\":12001,\"throttle_pwr\":1,\"throttle_therm\":0,\"pcie_rx_bw\":11264,\"pcie_tx_bw\":94208}]}";
        ScopeBeginEvent event = parser.getObjectMapper().readValue(json, ScopeBeginEvent.class);
        assertNotNull(event);
        assertEquals(MetricType.scope_begin, event.type());
        assertEquals("43ccf4b6-5e3f-4f9b-b002-274b27b357d8", event.sessionId());
        assertEquals(1, event.devices().size());
        assertEquals(0, event.devices().get(0).deviceId());
    }
}
