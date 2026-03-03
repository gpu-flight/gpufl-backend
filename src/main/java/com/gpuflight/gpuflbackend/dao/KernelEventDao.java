package com.gpuflight.gpuflbackend.dao;

import com.gpuflight.gpuflbackend.entity.KernelEventEntity;

import java.util.Collection;
import java.util.List;

public interface KernelEventDao {
    void saveKernelEvent(KernelEventEntity entity);
    List<KernelEventEntity> findBySessionIds(Collection<String> sessionIds);
}
