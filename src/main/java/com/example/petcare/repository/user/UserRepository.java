package com.example.petcare.repository.user;

import com.example.petcare.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByPhone(String phone);

    Optional<User> findByEmailAndStatus(String email, User.Status status);

    @Query("SELECT u FROM User u WHERE u.email = :loginValue OR u.phone = :loginValue")
    Optional<User> findByEmailOrPhone(@Param("loginValue") String loginValue);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    // THÊM DÒNG NÀY
    List<User> findByRoleAndStatus(User.Role role, User.Status status);
}