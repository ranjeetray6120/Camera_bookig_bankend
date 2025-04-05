package com.example.camerabooking.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {

    public User(Long id, String email, String name, String role) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.role = Role.valueOf(role); // Convert role string to Enum
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name cannot be empty")
    private String name;

    @Column(unique = true, nullable = false)
    @Email(message = "Invalid email format")
    @NotNull(message = "Email cannot be null")
    private String email;

    @Column(nullable = false)
    @NotNull(message = "Password cannot be null")
    private String password; // Encrypted password

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @CreationTimestamp
    private Timestamp createdAt;

    @Column(name = "mobile_number", unique = true)
    @NotBlank(message = "Mobile number cannot be empty")
    private String mobileNumber;

    // ✅ OTP for password reset
    @Column(name = "otp_code")
    private String otpCode;

    // ✅ OTP expiration time
    @Column(name = "otp_expiry")
    private LocalDateTime otpExpiry;

    public enum Role {
        ADMIN, CUSTOMER, DEVELOPER
    }
}
