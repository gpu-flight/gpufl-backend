package com.gpuflight.gpuflbackend.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gpuflight.gpuflbackend.dao.*;
import com.gpuflight.gpuflbackend.entity.SystemMetricEntity;
import com.gpuflight.gpuflbackend.model.DeviceSample;
import com.gpuflight.gpuflbackend.model.MetricType;
import com.gpuflight.gpuflbackend.model.SystemSampleEvent;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Component
public class SystemSampleEventHandler extends AbstractMetricEventHandler<SystemSampleEvent> {

    public SystemSampleEventHandler(SessionDao sessionDao, DeviceDao deviceDao, ScopeEventDao scopeEventDao,
                                    KernelEventDao kernelEventDao, SystemMetricDao systemMetricDao, ObjectMapper objectMapper) {
        super(sessionDao, deviceDao, scopeEventDao, kernelEventDao, systemMetricDao, objectMapper);
    }

    @Override
    public void handle(SystemSampleEvent event) {
        if (event.devices() != null) {
            for (DeviceSample ds : event.devices()) {
                Map<String, Object> extended = new HashMap<>();
                extended.put("clk_gfx", ds.clkGfx());
                extended.put("clk_sm", ds.clkSm());
                extended.put("clk_mem", ds.clkMem());
                extended.put("throttle_pwr", ds.throttlePwr());
                extended.put("throttle_therm", ds.throttleTherm());
                extended.put("pcie_rx", ds.pcieRxBw());
                extended.put("pcie_tx", ds.pcieTxBw());

                saveSystemMetric(
                        Instant.ofEpochSecond(0, event.tsNs()),
                        event.tsNs(),
                        event.sessionId(),
                        ds.uuid(),
                        ds.powerMw() / 1000.0,
                        ds.tempC(),
                        ds.utilGpu(),
                        ds.utilMem(),
                        ds.usedMib(),
                        toJson(extended)
                );
            }
        }
    }

    @Override
    public MetricType getSupportedType() {
        return MetricType.system_sample;
    }
}
