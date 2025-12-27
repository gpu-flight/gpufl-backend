package com.gpuflight.gpuflbackend.dao;

public interface SessionDao {
    void saveSession(String sessionId, String appName, int pid, long tsNs);
    void updateSessionEndTime(String sessionId, long tsNs);
}
