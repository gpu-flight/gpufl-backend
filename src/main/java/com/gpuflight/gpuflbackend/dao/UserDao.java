package com.gpuflight.gpuflbackend.dao;

import com.gpuflight.gpuflbackend.entity.UserEntity;

import java.util.Optional;
import java.util.UUID;

public interface UserDao {
    Optional<UserEntity> findById(UUID id);
    Optional<UserEntity> findByEmail(String email);
    Optional<UserEntity> findByUsername(String username);
    UserEntity save(UserEntity entity);
    void updateProfile(UserEntity entity);
}
