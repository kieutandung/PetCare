package com.example.petcare.service;

import com.example.petcare.model.*;
import com.example.petcare.repository.UserRepository;
import com.example.petcare.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Kiểm tra email tồn tại
        if (userRepository.existsByEmail(request.getEmail())) {
            return AuthResponse.builder()
                    .success(false)
                    .message("Email đã tồn tại")
                    .build();
        }

        // Kiểm tra phone tồn tại
        if (userRepository.existsByPhone(request.getPhone())) {
            return AuthResponse.builder()
                    .success(false)
                    .message("Số điện thoại đã tồn tại")
                    .build();
        }

        // Tạo user mới
        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        try {
            user.setRole(User.Role.valueOf(request.getRole()));
        } catch (IllegalArgumentException e) {
            user.setRole(User.Role.customer);
        }

        user.setStatus(User.Status.active);

        User savedUser = userRepository.save(user);

        // Tạo JWT token
        String token = jwtUtil.generateToken(savedUser.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(savedUser.getEmail());

        return AuthResponse.builder()
                .success(true)
                .token(token)
                .refreshToken(refreshToken)
                .userId(savedUser.getId())
                .fullName(savedUser.getFullName())
                .email(savedUser.getEmail())
                .phone(savedUser.getPhone())
                .role(savedUser.getRole().name())
                .status(savedUser.getStatus().name())
                .message("Đăng ký thành công")
                .build();
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getLoginValue(), request.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            User user = (User) authentication.getPrincipal();

            // Update last login
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);

            // Generate tokens
            String token = jwtUtil.generateToken(user.getEmail());
            String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());

            return AuthResponse.builder()
                    .success(true)
                    .token(token)
                    .refreshToken(refreshToken)
                    .userId(user.getId())
                    .fullName(user.getFullName())
                    .email(user.getEmail())
                    .phone(user.getPhone())
                    .role(user.getRole().name())
                    .status(user.getStatus().name())
                    .message("Đăng nhập thành công")
                    .build();

        } catch (Exception e) {
            log.error("Login failed: {}", e.getMessage());
            return AuthResponse.builder()
                    .success(false)
                    .message("Sai email/số điện thoại hoặc mật khẩu")
                    .build();
        }
    }

    @Transactional
    public AuthResponse refreshToken(String refreshToken) {
        if (jwtUtil.validateRefreshToken(refreshToken)) {
            String email = jwtUtil.extractUsername(refreshToken);
            User user = userRepository.findByEmail(email).orElse(null);

            if (user != null && user.isEnabled()) {
                String newToken = jwtUtil.generateToken(email);
                return AuthResponse.builder()
                        .success(true)
                        .token(newToken)
                        .refreshToken(refreshToken)
                        .message("Refresh token thành công")
                        .build();
            }
        }

        return AuthResponse.builder()
                .success(false)
                .message("Refresh token không hợp lệ")
                .build();
    }

    @Transactional
    public AuthResponse changePassword(String userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId).orElse(null);

        if (user == null) {
            return AuthResponse.builder()
                    .success(false)
                    .message("Người dùng không tồn tại")
                    .build();
        }

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return AuthResponse.builder()
                    .success(false)
                    .message("Mật khẩu cũ không đúng")
                    .build();
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return AuthResponse.builder()
                .success(true)
                .message("Đổi mật khẩu thành công")
                .build();
    }
}