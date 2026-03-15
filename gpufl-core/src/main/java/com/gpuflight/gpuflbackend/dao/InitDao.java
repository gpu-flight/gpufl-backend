package com.gpuflight.gpuflbackend.dao;

import com.gpuflight.gpuflbackend.entity.InitialEventEntity;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

public interface InitDao {
    void saveInitialEvent(InitialEventEntity entity);
    boolean existsBySessionId(String sessionId);
    List<InitialEventEntity> findByDateRange(Instant dateFrom, Instant dateTo);
    List<InitialEventEntity> findBySessionIds(Collection<String> sessionIds);
    void shutdownEvent(String sessionId, String app, Long shutdownTsNs);
}
