package com.gpuflight.gpuflbackend.dao;

import com.gpuflight.gpuflbackend.entity.InitialEventEntity;

public interface InitDao {
    void saveInitialEvent(InitialEventEntity entity);
    boolean existsBySessionId(String sessionId);
}
