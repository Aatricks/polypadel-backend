package com.polypadel.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "login_attempts")
public class LoginAttempt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    private int attemptsCount = 0;
    private LocalDateTime lastAttempt;
    private LocalDateTime lockedUntil;

    public LoginAttempt() {}

    public LoginAttempt(String email) {
        this.email = email;
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public int getAttemptsCount() { return attemptsCount; }
    public void setAttemptsCount(int attemptsCount) { this.attemptsCount = attemptsCount; }
    public LocalDateTime getLastAttempt() { return lastAttempt; }
    public void setLastAttempt(LocalDateTime lastAttempt) { this.lastAttempt = lastAttempt; }
    public LocalDateTime getLockedUntil() { return lockedUntil; }
    public void setLockedUntil(LocalDateTime lockedUntil) { this.lockedUntil = lockedUntil; }
}
