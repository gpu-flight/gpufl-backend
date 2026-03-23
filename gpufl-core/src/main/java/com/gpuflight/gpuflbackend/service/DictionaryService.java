package com.gpuflight.gpuflbackend.service;

import com.gpuflight.gpuflbackend.model.input.DictionaryUpdateEvent;

public interface DictionaryService {
    void mergeDictionary(String sessionId, DictionaryUpdateEvent event);
    String resolveKernel(String sessionId, int id);
    String resolveScopeName(String sessionId, int id);
    String resolveFunction(String sessionId, int id);
    String resolveMetric(String sessionId, int id);
}
