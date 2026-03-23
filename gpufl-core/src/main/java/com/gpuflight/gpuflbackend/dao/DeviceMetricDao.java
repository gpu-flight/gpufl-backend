package com.gpuflight.gpuflbackend.dao;

import com.gpuflight.gpuflbackend.entity.DeviceMetricEntity;

import java.util.Collection;
import java.util.List;

public interface DeviceMetricDao {
    void saveDeviceMetric(DeviceMetricEntity entity);
    void saveBatch(List<DeviceMetricEntity> entities);
    List<DeviceMetricEntity> findBySessionId(String sessionId);
    List<DeviceMetricEntity> findBySessionIds(Collection<String> sessionIds);
}
