package com.gpuflight.gpuflbackend.dao;

import com.gpuflight.gpuflbackend.entity.KernelEventEntity;

public interface KernelEventDao {
    void saveKernelBegin(KernelEventEntity entity);
    void updateKernelEnd(KernelEventEntity entity);
}
