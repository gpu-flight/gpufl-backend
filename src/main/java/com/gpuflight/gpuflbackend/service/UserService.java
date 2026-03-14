package com.gpuflight.gpuflbackend.service;

import com.gpuflight.gpuflbackend.model.input.LoginRequest;
import com.gpuflight.gpuflbackend.model.input.RegisterRequest;
import com.gpuflight.gpuflbackend.model.presentation.AuthResponse;
import com.gpuflight.gpuflbackend.model.presentation.UserProfileDto;

public interface UserService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    UserProfileDto getProfile(String username);
    UserProfileDto updateProfile(String username, UserProfileDto dto);
}
