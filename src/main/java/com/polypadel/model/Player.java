package com.polypadel.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Table(name = "players")
@Data
public class Player {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    private String company;

    @Column(unique = true, nullable = false)
    private String licenseNumber;

    @Column(unique = true)
    private String email;

    private LocalDate birthDate;
    private String photoUrl;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;
}
