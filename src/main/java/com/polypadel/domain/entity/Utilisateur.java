package com.polypadel.domain.entity;

import com.polypadel.domain.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "utilisateur", uniqueConstraints = {
        @UniqueConstraint(name = "uk_utilisateur_email", columnNames = {"email"})
})
@Getter @Setter @NoArgsConstructor
public class Utilisateur {

    @Id
    @GeneratedValue
    private UUID id;

    @Email
    @NotBlank
    @Size(max = 255)
    @Column(nullable = false)
    private String email;

    @Column(name = "email_hash", length = 64, nullable = false)
    private String emailHash;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Role role;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    private Instant updatedAt;
    private Instant lastLoginAt;
    @Column(name = "failed_login_attempts", nullable = false)
    private int failedLoginAttempts = 0;

    @Column(name = "lockout_until")
    private Instant lockoutUntil;

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
