package com.gpuflight.gpuflbackend.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gpuflight.gpuflbackend.dao.*;
import com.gpuflight.gpuflbackend.model.CudaStaticDevice;
import com.gpuflight.gpuflbackend.model.DeviceSample;
import com.gpuflight.gpuflbackend.model.InitEvent;
import com.gpuflight.gpuflbackend.model.MetricType;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class InitEventHandler extends AbstractMetricEventHandler<InitEvent> {

    public InitEventHandler(SessionDao sessionDao, DeviceDao deviceDao, ScopeEventDao scopeEventDao,
                            KernelEventDao kernelEventDao, SystemMetricDao systemMetricDao, ObjectMapper objectMapper) {
        super(sessionDao, deviceDao, scopeEventDao, kernelEventDao, systemMetricDao, objectMapper);
    }

    @Override
    public void handle(InitEvent event) {
        saveSession(event.sessionId(), event.app(), event.pid(), event.tsNs());

        if (event.cudaStaticDevices() != null) {
            for (CudaStaticDevice sd : event.cudaStaticDevices()) {
                Map<String, Object> staticProps = getStringObjectMap(sd);

                // Try to find total memory from DeviceSamples in the same InitEvent if available
                Long memoryTotalMib = null;
                if (event.devices() != null) {
                    memoryTotalMib = event.devices().stream()
                            .filter(d -> d.uuid().equals(sd.uuid()))
                            .findFirst()
                            .map(DeviceSample::totalMib)
                            .orElse(null);
                }

                saveDevice(event.sessionId(), sd.uuid(), "NVIDIA", sd.name(), memoryTotalMib, toJson(staticProps));
            }
        }

        saveScopeEvent(event.tsNs(), event.sessionId(), "INIT", "Init", null, event.host());
    }

    @Override
    public MetricType getSupportedType() {
        return MetricType.init;
    }
}
