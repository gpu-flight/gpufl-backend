package com.gpuflight.gpuflbackend.dao;

public interface DeviceDao {
    void saveDevice(String sessionId, String uuid, String vendor, String name, Long memoryTotalMib, String staticPropsJson);
}
