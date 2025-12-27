package com.gpuflight.gpuflbackend.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gpuflight.gpuflbackend.dao.*;
import com.gpuflight.gpuflbackend.model.MetricType;
import com.gpuflight.gpuflbackend.model.ScopeEndEvent;
import org.springframework.stereotype.Component;

@Component
public class ScopeEndEventHandler extends AbstractMetricEventHandler<ScopeEndEvent> {

    public ScopeEndEventHandler(SessionDao sessionDao, DeviceDao deviceDao, ScopeEventDao scopeEventDao,
                                KernelEventDao kernelEventDao, SystemMetricDao systemMetricDao, ObjectMapper objectMapper) {
        super(sessionDao, deviceDao, scopeEventDao, kernelEventDao, systemMetricDao, objectMapper);
    }

    @Override
    public void handle(ScopeEndEvent event) {
        saveScopeEvent(event.tsNs(), event.sessionId(), "SCOPE_END", event.name(), event.tag(), event.host());
    }

    @Override
    public MetricType getSupportedType() {
        return MetricType.scope_end;
    }
}
