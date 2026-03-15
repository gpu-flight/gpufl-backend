package com.gpuflight.gpuflbackend.controller;

import com.gpuflight.gpuflbackend.dao.UserDao;
import com.gpuflight.gpuflbackend.model.input.CreateApiKeyRequest;
import com.gpuflight.gpuflbackend.model.input.LoginRequest;
import com.gpuflight.gpuflbackend.model.input.RegisterRequest;
import com.gpuflight.gpuflbackend.model.presentation.ApiKeyCreatedDto;
import com.gpuflight.gpuflbackend.model.presentation.ApiKeyDto;
import com.gpuflight.gpuflbackend.model.presentation.AuthResponse;
import com.gpuflight.gpuflbackend.model.presentation.UserProfileDto;
import com.gpuflight.gpuflbackend.service.ApiKeyService;
import com.gpuflight.gpuflbackend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final ApiKeyService apiKeyService;
    private final UserDao userDao;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(userService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(userService.login(request));
    }

    @GetMapping("/profile")
    public ResponseEntity<UserProfileDto> getProfile(@AuthenticationPrincipal String username) {
        return ResponseEntity.ok(userService.getProfile(username));
    }

    @PutMapping("/profile")
    public ResponseEntity<UserProfileDto> updateProfile(@AuthenticationPrincipal String username,
                                                         @RequestBody UserProfileDto dto) {
        return ResponseEntity.ok(userService.updateProfile(username, dto));
    }

    @PostMapping("/api-keys")
    public ResponseEntity<ApiKeyCreatedDto> createApiKey(@AuthenticationPrincipal String username,
                                                          @RequestBody CreateApiKeyRequest request) {
        UUID userId = userDao.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"))
                .getId();
        return ResponseEntity.ok(apiKeyService.createKey(userId, request.name()));
    }

    @GetMapping("/api-keys")
    public ResponseEntity<List<ApiKeyDto>> listApiKeys(@AuthenticationPrincipal String username) {
        UUID userId = userDao.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"))
                .getId();
        return ResponseEntity.ok(apiKeyService.listKeys(userId));
    }

    @DeleteMapping("/api-keys/{id}")
    public ResponseEntity<Void> revokeApiKey(@AuthenticationPrincipal String username,
                                              @PathVariable UUID id) {
        UUID userId = userDao.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"))
                .getId();
        apiKeyService.revokeKey(id, userId);
        return ResponseEntity.noContent().build();
    }
}
