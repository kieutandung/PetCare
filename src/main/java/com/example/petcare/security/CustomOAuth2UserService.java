package com.example.petcare.security;

import com.example.petcare.model.User;
import com.example.petcare.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        Map<String, Object> attributes = oAuth2User.getAttributes();
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        String picture = (String) attributes.get("picture");

        log.info("🔐 OAuth2 Login attempt: email={}, name={}", email, name);

        Optional<User> existingUser = userRepository.findByEmail(email);
        User user;

        if (existingUser.isPresent()) {
            user = existingUser.get();
            log.info("✅ Existing user found: {}", email);

            if (picture != null && !picture.equals(user.getAvatarUrl())) {
                user.setAvatarUrl(picture);
                userRepository.save(user);
            }
        } else {
            user = new User();
            user.setFullName(name);
            user.setEmail(email);
            user.setPhone(null);
            user.setPasswordHash("");
            user.setRole(User.Role.customer);
            user.setStatus(User.Status.active);
            user.setAvatarUrl(picture);

            user = userRepository.save(user);
            log.info("✅ New user created via Google OAuth2: {}", email);
        }

        return new CustomOAuth2User(oAuth2User, user);
    }
}