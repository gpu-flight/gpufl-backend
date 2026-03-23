package com.gpuflight.gpuflbackend.dao;

import com.gpuflight.gpuflbackend.entity.KernelEventEntity;
import com.gpuflight.gpuflbackend.model.input.KernelDetailEvent;

import java.util.Collection;
import java.util.List;

public interface KernelEventDao {
    void saveKernelEvent(KernelEventEntity entity);
    void saveMinimal(String sessionId, long startNs, long endNs, long durationNs,
                     long streamId, String name, long corrId, boolean hasDetails,
                     int dynSharedBytes, int numRegs);
    void updateDetails(String sessionId, long corrId, KernelDetailEvent detail);
    List<KernelEventEntity> findBySessionIds(Collection<String> sessionIds);
}
