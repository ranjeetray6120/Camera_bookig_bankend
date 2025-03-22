package com.example.camerabooking.service;

import com.example.camerabooking.model.User;
import java.util.List;
import java.util.Optional;

public interface UserService {
    
    User registerUser(User user); // Register a new user

    boolean existsByEmail(String email); // Check if an email already exists

    Optional<User> findByEmail(String email); // Find user by email

    List<User> getAllUsers(); // Get all users

    Optional<User> getUserById(Long id); // Find user by ID

    void updateUserById(Long id, User user); // Update user by ID

    void deleteUserById(Long id); // Delete user by ID

    User authenticateUser(User user); // Authenticate user (Login)

    void sendResetPasswordEmail(String email); // Send reset password email

    void resetPassword(String token, String newPassword); // Reset password using token

    List<User> getAllAdmins(); // Get all Admin users

    List<User> getAllCustomers(); // Get all Customer users
}
