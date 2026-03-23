package com.gpuflight.gpuflbackend.dao;

import com.gpuflight.gpuflbackend.entity.HostMetricEntity;

import java.util.Collection;
import java.util.List;

public interface HostMetricDao {
    void saveHostMetric(HostMetricEntity entity);
    void saveBatch(List<HostMetricEntity> entities);
    List<HostMetricEntity> findBySessionIds(Collection<String> sessionIds);
}
