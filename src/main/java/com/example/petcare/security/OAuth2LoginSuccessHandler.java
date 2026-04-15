package com.example.petcare.security;

import com.example.petcare.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        log.info("🎯 OAuth2LoginSuccessHandler called!");

        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        User user = oAuth2User.getUser();

        log.info("👤 User: {}", user.getEmail());

        // Tạo JWT token
        String token = jwtUtil.generateToken(user.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());

        // Encode các params
        String encodedFullName = URLEncoder.encode(user.getFullName(), StandardCharsets.UTF_8.toString());

        // Redirect về React login page với params
        String targetUrl = frontendUrl + "/login?oauth2_success=true"
                + "&token=" + token
                + "&refreshToken=" + refreshToken
                + "&userId=" + user.getId()
                + "&fullName=" + encodedFullName
                + "&email=" + user.getEmail()
                + "&role=" + user.getRole().name();

        // Thêm phone nếu có
        if (user.getPhone() != null) {
            targetUrl += "&phone=" + user.getPhone();
        }

        // Thêm avatar nếu có
        if (user.getAvatarUrl() != null) {
            String encodedAvatar = URLEncoder.encode(user.getAvatarUrl(), StandardCharsets.UTF_8.toString());
            targetUrl += "&avatarUrl=" + encodedAvatar;
        }

        log.info("🔀 Redirecting to: {}", targetUrl);

        response.setStatus(HttpServletResponse.SC_FOUND);
        response.setHeader("Location", targetUrl);
        response.sendRedirect(targetUrl);
    }
}