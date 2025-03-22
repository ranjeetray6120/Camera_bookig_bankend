package com.example.camerabooking.impl;


import com.example.camerabooking.model.User;
import com.example.camerabooking.repository.UserRepository;
import com.example.camerabooking.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public User registerUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public void updateUserById(Long id, User user) {
        Optional<User> existingUser = userRepository.findById(id);
        existingUser.ifPresent(u -> {
            u.setName(user.getName());
            u.setEmail(user.getEmail());
            u.setRole(user.getRole());
            if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                u.setPassword(passwordEncoder.encode(user.getPassword()));
            }
            userRepository.save(u);
        });
    }

    @Override
    public void deleteUserById(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public User authenticateUser(User user) {
        Optional<User> existingUser = userRepository.findByEmail(user.getEmail());
        if (existingUser.isPresent() && passwordEncoder.matches(user.getPassword(), existingUser.get().getPassword())) {
            return existingUser.get();
        }
        return null;
    }

    @Override
    public void sendResetPasswordEmail(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent()) {
            String resetToken = UUID.randomUUID().toString();
            user.get().setResetToken(resetToken);
            userRepository.save(user.get());
            // Logic to send email with the reset link
        }
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        Optional<User> user = userRepository.findByResetToken(token);
        if (user.isPresent()) {
            user.get().setPassword(passwordEncoder.encode(newPassword));
            user.get().setResetToken(null);
            userRepository.save(user.get());
        }
    }

    @Override
    public List<User> getAllAdmins() {
        return userRepository.findByRole(User.Role.ADMIN);
    }

    @Override
    public List<User> getAllCustomers() {
        return userRepository.findByRole(User.Role.CUSTOMER);
    }
}
