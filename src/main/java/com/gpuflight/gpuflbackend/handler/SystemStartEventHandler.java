package com.gpuflight.gpuflbackend.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gpuflight.gpuflbackend.dao.*;
import com.gpuflight.gpuflbackend.model.MetricType;
import com.gpuflight.gpuflbackend.model.SystemStartEvent;
import org.springframework.stereotype.Component;

@Component
public class SystemStartEventHandler extends AbstractMetricEventHandler<SystemStartEvent> {

    public SystemStartEventHandler(SessionDao sessionDao, DeviceDao deviceDao, ScopeEventDao scopeEventDao,
                                   KernelEventDao kernelEventDao, SystemMetricDao systemMetricDao, ObjectMapper objectMapper) {
        super(sessionDao, deviceDao, scopeEventDao, kernelEventDao, systemMetricDao, objectMapper);
    }

    @Override
    public void handle(SystemStartEvent event) {
        saveScopeEvent(event.tsNs(), event.sessionId(), "SYSTEM_START", event.name(), null, event.host());
    }

    @Override
    public MetricType getSupportedType() {
        return MetricType.system_start;
    }
}
