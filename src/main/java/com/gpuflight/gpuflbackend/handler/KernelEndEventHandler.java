package com.gpuflight.gpuflbackend.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gpuflight.gpuflbackend.dao.*;
import com.gpuflight.gpuflbackend.model.KernelEndEvent;
import com.gpuflight.gpuflbackend.model.MetricType;
import org.springframework.stereotype.Component;

@Component
public class KernelEndEventHandler extends AbstractMetricEventHandler<KernelEndEvent> {

    public KernelEndEventHandler(SessionDao sessionDao, DeviceDao deviceDao, ScopeEventDao scopeEventDao,
                                 KernelEventDao kernelEventDao, SystemMetricDao systemMetricDao, ObjectMapper objectMapper) {
        super(sessionDao, deviceDao, scopeEventDao, kernelEventDao, systemMetricDao, objectMapper);
    }

    @Override
    public void handle(KernelEndEvent event) {
        updateKernelEnd(event.sessionId(), (long) event.corrId(), event.tsNs(), event.cudaError());
    }

    @Override
    public MetricType getSupportedType() {
        return MetricType.kernel_end;
    }
}
