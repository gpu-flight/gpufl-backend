package com.gpuflight.gpuflbackend.dao;

import java.util.Map;

public interface DictionaryDao {
    void upsert(String sessionId, String dictType, int dictId, String name);
    Map<Integer, String> loadDict(String sessionId, String dictType);
}
