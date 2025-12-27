package com.gpuflight.gpuflbackend.dao;

import com.gpuflight.gpuflbackend.model.HostSample;

public interface ScopeEventDao {
    void saveScopeEvent(long tsNs, String sessionId, String type, String name, String tag, HostSample host);
    void updateScopeEvent(String sessionId, String name, String type, long tsNs);
}
