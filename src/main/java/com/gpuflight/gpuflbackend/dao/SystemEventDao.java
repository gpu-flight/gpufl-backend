package com.gpuflight.gpuflbackend.dao;

import com.gpuflight.gpuflbackend.entity.SystemEventEntity;

import java.util.Collection;
import java.util.List;

public interface SystemEventDao {
    void saveSystemEvent(SystemEventEntity entity);
    List<SystemEventEntity> findBySessionIds(Collection<String> sessionIds);
    List<SystemEventEntity> findBySessionId(String sessionId);
}
