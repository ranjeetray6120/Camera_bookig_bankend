package com.example.camerabooking.repository;

import com.example.camerabooking.model.User;
import com.example.camerabooking.model.User.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    Optional<User> findByOtpCode(String otpCode);

    List<User> findByRole(Role role);
}
