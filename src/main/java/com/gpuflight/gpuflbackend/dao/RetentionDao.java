package com.gpuflight.gpuflbackend.dao;

import java.util.List;

public interface RetentionDao {
    List<String> findExpiredSessionIds(int defaultDays);
    void deleteBySessionIds(List<String> sessionIds);
}
