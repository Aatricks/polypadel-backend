package com.polypadel.domain.entity;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "poule")
public class Poule {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, length = 64)
    private String nom;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
}
