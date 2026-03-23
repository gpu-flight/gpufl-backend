package com.gpuflight.gpuflbackend.service;

import com.gpuflight.gpuflbackend.dao.DictionaryDao;
import com.gpuflight.gpuflbackend.model.input.DictionaryUpdateEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class DictionaryServiceImpl implements DictionaryService {
    private static final String KERNEL = "kernel";
    private static final String SCOPE_NAME = "scope_name";
    private static final String FUNCTION = "function";
    private static final String METRIC = "metric";

    private final DictionaryDao dictionaryDao;

    // In-memory cache: sessionId -> dictType -> (id -> name)
    private final ConcurrentHashMap<String, Map<String, Map<Integer, String>>> cache =
        new ConcurrentHashMap<>();

    @Override
    public void mergeDictionary(String sessionId, DictionaryUpdateEvent event) {
        mergeDict(sessionId, KERNEL,     event.kernelDict());
        mergeDict(sessionId, SCOPE_NAME, event.scopeNameDict());
        mergeDict(sessionId, FUNCTION,   event.functionDict());
        mergeDict(sessionId, METRIC,     event.metricDict());
    }

    private void mergeDict(String sessionId, String dictType, Map<String, String> incoming) {
        if (incoming == null || incoming.isEmpty()) return;
        Map<Integer, String> mem = cacheFor(sessionId, dictType);
        incoming.forEach((idStr, name) -> {
            int id = Integer.parseInt(idStr);
            mem.put(id, name);
            dictionaryDao.upsert(sessionId, dictType, id, name);
        });
    }

    @Override
    public String resolveKernel(String sessionId, int id) {
        return resolve(sessionId, KERNEL, id, "kernel#" + id);
    }

    @Override
    public String resolveScopeName(String sessionId, int id) {
        if (id == 0) return null;
        return resolve(sessionId, SCOPE_NAME, id, "scope#" + id);
    }

    @Override
    public String resolveFunction(String sessionId, int id) {
        if (id == 0) return null;
        return resolve(sessionId, FUNCTION, id, "function#" + id);
    }

    @Override
    public String resolveMetric(String sessionId, int id) {
        if (id == 0) return null;
        return resolve(sessionId, METRIC, id, null);
    }

    private String resolve(String sessionId, String dictType, int id, String fallback) {
        Map<Integer, String> mem = cacheFor(sessionId, dictType);
        String name = mem.get(id);
        if (name != null) return name;
        // Cache miss: load from DB
        Map<Integer, String> loaded = dictionaryDao.loadDict(sessionId, dictType);
        mem.putAll(loaded);
        return mem.getOrDefault(id, fallback);
    }

    private Map<Integer, String> cacheFor(String sessionId, String dictType) {
        return cache
            .computeIfAbsent(sessionId, k -> new ConcurrentHashMap<>())
            .computeIfAbsent(dictType,   k -> new ConcurrentHashMap<>());
    }
}
