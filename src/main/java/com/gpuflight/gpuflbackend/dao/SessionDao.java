package com.gpuflight.gpuflbackend.dao;

import com.gpuflight.gpuflbackend.entity.SessionEntity;

import java.util.Collection;
import java.util.List;

public interface SessionDao {
    void saveSession(SessionEntity entity);
    void updateSessionEndTime(SessionEntity entity);
    List<SessionEntity> findBySessionIds(Collection<String> sessionIds);
}
