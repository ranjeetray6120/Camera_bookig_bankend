package com.example.camerabooking.impl;

import com.example.camerabooking.model.User;
import com.example.camerabooking.repository.UserRepository;
import com.example.camerabooking.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JavaMailSender mailSender;

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
        if (existingUser.isPresent() &&
            passwordEncoder.matches(user.getPassword(), existingUser.get().getPassword())) {
            return existingUser.get();
        }
        return null;
    }

    @Override
    public void sendResetPasswordEmail(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            throw new RuntimeException("Email not found. Please register or check your email.");
        }

        User user = userOpt.get();

        // Generate 6-digit OTP
        String otp = String.valueOf(new Random().nextInt(900000) + 100000);
        user.setOtpCode(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(10));
        userRepository.save(user);

        // Create and send a friendlier, more readable email
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("🔐 Your OTP for Camera Booking Password Reset");

        String content = String.format("""
                Hi %s,

                We received a request to reset your password.

                👉 Your One-Time Password (OTP) is: %s

                This OTP is valid for 10 minutes. Please do not share it with anyone.

                If you did not request a password reset, you can ignore this message.

                Thanks,
                Camera Booking Support Team
                """, user.getName(), otp);

        message.setText(content);

        // Optional: Set from address if needed
        // message.setFrom("your_email@gmail.com");

        mailSender.send(message);
    }

    @Override
    public boolean verifyOtp(String email, String otp) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            return user.getOtpCode() != null &&
                   user.getOtpCode().equals(otp) &&
                   user.getOtpExpiry() != null &&
                   user.getOtpExpiry().isAfter(LocalDateTime.now());
        }
        return false;
    }

    @Override
    public void resetPasswordWithOtp(String email, String newPassword) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setPassword(passwordEncoder.encode(newPassword));
            user.setOtpCode(null); // Clear OTP after use
            user.setOtpExpiry(null);
            userRepository.save(user);
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
