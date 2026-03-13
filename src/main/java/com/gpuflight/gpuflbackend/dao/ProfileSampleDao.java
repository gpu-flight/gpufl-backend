package com.gpuflight.gpuflbackend.dao;

import com.gpuflight.gpuflbackend.entity.ProfileSampleEntity;

import java.util.List;

public interface ProfileSampleDao {
    void save(ProfileSampleEntity entity);
    List<ProfileSampleEntity> findBySessionId(String sessionId);
}
