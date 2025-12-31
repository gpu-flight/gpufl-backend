package com.gpuflight.gpuflbackend.dao;

import com.gpuflight.gpuflbackend.entity.InitialEventEntity;

import java.time.Instant;
import java.util.List;

public interface InitDao {
    void saveInitialEvent(InitialEventEntity entity);
    boolean existsBySessionId(String sessionId);
    List<InitialEventEntity> findByDateRange(Instant dateFrom, Instant dateTo);
    void shutdownEvent(String sessionId, String app, Long shutdownTsNs);
}
