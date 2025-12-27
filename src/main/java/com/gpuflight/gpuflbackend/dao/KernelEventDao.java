package com.gpuflight.gpuflbackend.dao;

import com.gpuflight.gpuflbackend.entity.KernelEventEntity;
import java.time.Instant;

public interface KernelEventDao {
    void saveKernelBegin(KernelEventEntity entity);
    void updateKernelEnd(String sessionId, long corrId, long endNs, String cudaError);
}
