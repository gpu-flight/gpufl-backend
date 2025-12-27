package com.gpuflight.gpuflbackend.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gpuflight.gpuflbackend.dao.*;
import com.gpuflight.gpuflbackend.model.MetricType;
import com.gpuflight.gpuflbackend.model.ShutdownEvent;
import org.springframework.stereotype.Component;

@Component
public class ShutdownEventHandler extends AbstractMetricEventHandler<ShutdownEvent> {

    public ShutdownEventHandler(SessionDao sessionDao, DeviceDao deviceDao, ScopeEventDao scopeEventDao,
                                KernelEventDao kernelEventDao, SystemMetricDao systemMetricDao, ObjectMapper objectMapper) {
        super(sessionDao, deviceDao, scopeEventDao, kernelEventDao, systemMetricDao, objectMapper);
    }

    @Override
    public void handle(ShutdownEvent event) {
        updateSessionEndTime(event.sessionId(), event.tsNs());
    }

    @Override
    public MetricType getSupportedType() {
        return MetricType.shutdown;
    }
}
