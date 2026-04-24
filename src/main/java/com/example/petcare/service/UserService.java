package com.example.petcare.service;

import com.example.petcare.model.User;
import com.example.petcare.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    // Lấy user theo ID
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + id));
    }

    // Lấy user theo email
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // Lấy user theo số điện thoại
    public Optional<User> getUserByPhone(String phone) {
        return userRepository.findByPhone(phone);
    }

    // Lấy tất cả bác sĩ đang hoạt động
    public List<User> getAllActiveDoctors() {
        return userRepository.findByRoleAndStatus(User.Role.doctor, User.Status.active);
    }

    // Lấy tất cả khách hàng
    public List<User> getAllCustomers() {
        return userRepository.findByRoleAndStatus(User.Role.customer, User.Status.active);
    }

    // Lấy tất cả admin
    public List<User> getAllAdmins() {
        return userRepository.findByRoleAndStatus(User.Role.admin, User.Status.active);
    }

    // Cập nhật thông tin user
    @Transactional
    public User updateUser(Long userId, User updatedUser) {
        User user = getUserById(userId);

        if (updatedUser.getFullName() != null) {
            user.setFullName(updatedUser.getFullName());
        }
        if (updatedUser.getPhone() != null) {
            if (!user.getPhone().equals(updatedUser.getPhone()) &&
                    userRepository.existsByPhone(updatedUser.getPhone())) {
                throw new RuntimeException("Số điện thoại đã được sử dụng");
            }
            user.setPhone(updatedUser.getPhone());
        }
        if (updatedUser.getAvatarUrl() != null) {
            user.setAvatarUrl(updatedUser.getAvatarUrl());
        }

        return userRepository.save(user);
    }

    // Cập nhật trạng thái user
    @Transactional
    public User updateUserStatus(Long userId, User.Status status) {
        User user = getUserById(userId);
        user.setStatus(status);
        return userRepository.save(user);
    }

    // Kiểm tra email đã tồn tại chưa
    public boolean isEmailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    // Kiểm tra số điện thoại đã tồn tại chưa
    public boolean isPhoneExists(String phone) {
        return userRepository.existsByPhone(phone);
    }

    // Tìm user theo email hoặc phone
    public Optional<User> findByEmailOrPhone(String loginValue) {
        return userRepository.findByEmailOrPhone(loginValue);
    }
}