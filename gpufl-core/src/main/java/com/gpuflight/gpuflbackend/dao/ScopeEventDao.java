package com.gpuflight.gpuflbackend.dao;

import com.gpuflight.gpuflbackend.entity.ScopeEventEntity;

import java.util.Collection;
import java.util.List;

public interface ScopeEventDao {
    void saveScopeEvent(ScopeEventEntity entity);
    void updateScopeEventEnd(ScopeEventEntity entity);
    void saveWithInstanceId(ScopeEventEntity entity);
    void updateEndByInstanceId(String sessionId, long scopeInstanceId, long endNs);
    List<ScopeEventEntity> findBySessionIds(Collection<String> sessionIds);
    ScopeEventEntity findLatestCompletedBefore(String sessionId, long tsNs);
}
