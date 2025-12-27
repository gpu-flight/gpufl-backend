package com.gpuflight.gpuflbackend.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gpuflight.gpuflbackend.dao.*;
import com.gpuflight.gpuflbackend.model.MetricType;
import com.gpuflight.gpuflbackend.model.SystemStopEvent;
import org.springframework.stereotype.Component;

@Component
public class SystemStopEventHandler extends AbstractMetricEventHandler<SystemStopEvent> {

    public SystemStopEventHandler(SessionDao sessionDao, DeviceDao deviceDao, ScopeEventDao scopeEventDao,
                                  KernelEventDao kernelEventDao, SystemMetricDao systemMetricDao, ObjectMapper objectMapper) {
        super(sessionDao, deviceDao, scopeEventDao, kernelEventDao, systemMetricDao, objectMapper);
    }

    @Override
    public void handle(SystemStopEvent event) {
        saveScopeEvent(event.tsNs(), event.sessionId(), "SYSTEM_STOP", event.name(), event.tag(), event.host());
    }

    @Override
    public MetricType getSupportedType() {
        return MetricType.system_end;
    }
}
