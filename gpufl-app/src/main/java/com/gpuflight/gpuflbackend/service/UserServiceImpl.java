package com.gpuflight.gpuflbackend.service;

import com.gpuflight.gpuflbackend.dao.UserDao;
import com.gpuflight.gpuflbackend.entity.UserEntity;
import com.gpuflight.gpuflbackend.model.input.LoginRequest;
import com.gpuflight.gpuflbackend.model.input.RegisterRequest;
import com.gpuflight.gpuflbackend.model.presentation.AuthResponse;
import com.gpuflight.gpuflbackend.model.presentation.UserProfileDto;
import com.gpuflight.gpuflbackend.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserDao userDao;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (userDao.findByEmail(request.email()).isPresent()) {
            throw new IllegalArgumentException("Email already in use");
        }
        if (userDao.findByUsername(request.username()).isPresent()) {
            throw new IllegalArgumentException("Username already taken");
        }
        UserEntity user = UserEntity.builder()
                .email(request.email())
                .username(request.username())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role("USER")
                .build();
        UserEntity saved = userDao.save(user);
        String token = jwtUtil.generateToken(saved.getUsername(), saved.getRole());
        return new AuthResponse(token, saved.getUsername(), saved.getRole());
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        UserEntity user = userDao.findByEmail(request.emailOrUsername())
                .or(() -> userDao.findByUsername(request.emailOrUsername()))
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
        return new AuthResponse(token, user.getUsername(), user.getRole());
    }

    @Override
    public UserProfileDto getProfile(String username) {
        UserEntity user = userDao.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return new UserProfileDto(user.getUsername(), user.getEmail(), user.getDisplayName(), user.getBio(), user.getAvatarUrl());
    }

    @Override
    public UserProfileDto updateProfile(String username, UserProfileDto dto) {
        UserEntity user = userDao.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setDisplayName(dto.displayName());
        user.setBio(dto.bio());
        user.setAvatarUrl(dto.avatarUrl());
        userDao.updateProfile(user);
        return new UserProfileDto(user.getUsername(), user.getEmail(), user.getDisplayName(), user.getBio(), user.getAvatarUrl());
    }
}
