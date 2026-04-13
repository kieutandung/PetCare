package com.example.petcare.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "Email hoặc số điện thoại không được để trống")
    private String loginValue;

    @NotBlank(message = "Mật khẩu không được để trống")
    private String password;
}