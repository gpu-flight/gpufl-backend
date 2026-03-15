package com.gpuflight.gpuflbackend.dao;

import com.gpuflight.gpuflbackend.entity.ProfileSampleEntity;

import java.util.List;

public interface ProfileSampleDao {
    /** UPSERT: accumulates values and increments occurrence_count. */
    void save(ProfileSampleEntity entity);
    /** UPSERT: accumulates values without incrementing occurrence_count (used for paired metric rows). */
    void merge(ProfileSampleEntity entity);
    List<ProfileSampleEntity> findBySessionId(String sessionId);
}
