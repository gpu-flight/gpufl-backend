package com.gpuflight.gpuflbackend.dao;

import com.gpuflight.gpuflbackend.entity.ScopeEventEntity;

public interface ScopeEventDao {
    void saveScopeEvent(ScopeEventEntity entity);
    void updateScopeEventEnd(ScopeEventEntity entity);
}
