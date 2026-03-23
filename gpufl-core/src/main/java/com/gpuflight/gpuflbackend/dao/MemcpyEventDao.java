package com.gpuflight.gpuflbackend.dao;

import com.gpuflight.gpuflbackend.entity.MemcpyEventEntity;
import java.util.List;

public interface MemcpyEventDao {
    void saveBatch(List<MemcpyEventEntity> entities);
}
