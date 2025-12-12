package com.polypadel.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "login_attempts")
@Getter
@Setter
@NoArgsConstructor
public class LoginAttempt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    private int attemptsCount = 0;
    private LocalDateTime lastAttempt;
    private LocalDateTime lockedUntil;

    public LoginAttempt(String email) {
        this.email = email;
    }
}
