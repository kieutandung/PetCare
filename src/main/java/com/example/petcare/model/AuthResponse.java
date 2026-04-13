package com.example.petcare.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private String token;
    private String refreshToken;
    private String userId;
    private String fullName;
    private String email;
    private String phone;
    private String role;
    private String status;
    private String message;
    private boolean success;
}