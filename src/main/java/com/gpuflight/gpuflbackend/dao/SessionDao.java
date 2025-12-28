package com.gpuflight.gpuflbackend.dao;

import com.gpuflight.gpuflbackend.entity.SessionEntity;

public interface SessionDao {
    void saveSession(SessionEntity entity);
    void updateSessionEndTime(SessionEntity entity);
}
