package com.gpuflight.gpuflbackend.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gpuflight.gpuflbackend.dao.*;
import com.gpuflight.gpuflbackend.entity.KernelEventEntity;
import com.gpuflight.gpuflbackend.model.KernelBeginEvent;
import com.gpuflight.gpuflbackend.model.MetricType;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Component
public class KernelBeginEventHandler extends AbstractMetricEventHandler<KernelBeginEvent> {

    public KernelBeginEventHandler(SessionDao sessionDao, DeviceDao deviceDao, ScopeEventDao scopeEventDao,
                                   KernelEventDao kernelEventDao, SystemMetricDao systemMetricDao, ObjectMapper objectMapper) {
        super(sessionDao, deviceDao, scopeEventDao, kernelEventDao, systemMetricDao, objectMapper);
    }

    @Override
    public void handle(KernelBeginEvent event) {
        KernelEventEntity entity = KernelEventEntity.builder()
                .sessionId(event.sessionId())
                .deviceUuid(event.uuid())
                .name(event.name())
                .corrId((long) event.corrId())
                .startNs(event.tsNs())
                .grid(event.grid())
                .block(event.block())
                .dynSharedBytes(event.dynSharedBytes())
                .numRegs(event.numRegs())
                .staticSharedBytes(event.staticSharedBytes())
                .localBytes(event.localBytes())
                .constBytes(event.constBytes())
                .occupancy(event.occupancy())
                .maxActiveBlocks(event.maxActiveBlocks())
                .build();

        saveKernelBegin(entity);
    }

    @Override
    public MetricType getSupportedType() {
        return MetricType.kernel_start;
    }
}
