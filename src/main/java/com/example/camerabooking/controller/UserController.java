package com.example.camerabooking.controller;

import com.example.camerabooking.model.User;
import com.example.camerabooking.service.UserService;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://127.0.0.1:5500")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<User> registerUser(@RequestBody User user) {
        return ResponseEntity.ok(userService.registerUser(user));
    }

    @GetMapping("/exists/{email}")
    public ResponseEntity<Boolean> existsByEmail(@PathVariable String email) {
        return ResponseEntity.ok(userService.existsByEmail(email));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<Optional<User>> findByEmail(@PathVariable String email) {
        return ResponseEntity.ok(userService.findByEmail(email));
    }

    @GetMapping("/all")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Optional<User>> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Void> updateUserById(@PathVariable Long id, @RequestBody User user) {
        userService.updateUserById(id, user);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteUserById(@PathVariable Long id) {
        userService.deleteUserById(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody User user) {
        User authenticatedUser = userService.authenticateUser(user);

        if (authenticatedUser != null) {
            Map<String, Object> response = new HashMap<>();
            response.put("userId", authenticatedUser.getId());
            response.put("email", authenticatedUser.getEmail());
            response.put("name", authenticatedUser.getName());
            response.put("role", authenticatedUser.getRole().name());

            return ResponseEntity.ok(response);
        }

        // If authentication fails
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("message", "Invalid email or password");
        return ResponseEntity.status(HttpStatus.SC_UNAUTHORIZED).body(errorResponse);
    }

    // ✅ Step 1: Send OTP to email
    @PostMapping("/send-otp")
    public ResponseEntity<Map<String, String>> sendOtp(@RequestParam String email) {
        try {
            userService.sendResetPasswordEmail(email);
            return ResponseEntity.ok(Collections.singletonMap("message", "OTP sent to your email."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    // ✅ Step 2: Verify OTP
    @PostMapping("/verify-otp")
    public ResponseEntity<Map<String, Object>> verifyOtp(@RequestParam String email, @RequestParam String otp) {
        boolean isValid = userService.verifyOtp(email, otp);
        Map<String, Object> response = new HashMap<>();
        response.put("valid", isValid);
        response.put("message", isValid ? "OTP verified" : "Invalid or expired OTP");
        return ResponseEntity.ok(response);
    }

    // ✅ Step 3: Reset password using OTP
    @PostMapping("/reset-password-with-otp")
    public ResponseEntity<Map<String, String>> resetPasswordWithOtp(
            @RequestParam String email,
            @RequestParam String newPassword) {

        userService.resetPasswordWithOtp(email, newPassword);
        return ResponseEntity.ok(Collections.singletonMap("message", "Password reset successfully"));
    }

    @GetMapping("/admins")
    public ResponseEntity<List<User>> getAllAdmins() {
        return ResponseEntity.ok(userService.getAllAdmins());
    }

    @GetMapping("/customers")
    public ResponseEntity<List<User>> getAllCustomers() {
        return ResponseEntity.ok(userService.getAllCustomers());
    }
}
