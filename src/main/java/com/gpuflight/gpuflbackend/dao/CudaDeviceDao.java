package com.gpuflight.gpuflbackend.dao;

import com.gpuflight.gpuflbackend.entity.CudaStaticDeviceEntity;

import java.util.Collection;
import java.util.List;

public interface CudaDeviceDao {
    void saveCudaDevice(CudaStaticDeviceEntity entity);
    List<CudaStaticDeviceEntity> findBySessionId(String sessionId);
    List<CudaStaticDeviceEntity> findBySessionIds(Collection<String> sessionIds);
    List<CudaStaticDeviceEntity> findByInitialEventId(String eventId);
}
