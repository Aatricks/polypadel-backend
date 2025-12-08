package com.polypadel.domain.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "json_token", indexes = {
    @Index(name = "idx_jsontoken_user", columnList = "utilisateur_id"),
    @Index(name = "idx_jsontoken_jti", columnList = "jti")
})
public class JSONToken {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "utilisateur_id", nullable = false, foreignKey = @ForeignKey(name = "fk_jsontoken_user"))
    private Utilisateur utilisateur;

    @Column(name = "jti", nullable = false, length = 64)
    private String jti;

    @Column(name = "expires_at", nullable = false)
    private Instant expiration;

    @Column(nullable = false)
    private boolean revoked = true;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Utilisateur getUtilisateur() { return utilisateur; }
    public void setUtilisateur(Utilisateur utilisateur) { this.utilisateur = utilisateur; }

    public String getJti() { return jti; }
    public void setJti(String jti) { this.jti = jti; }

    public Instant getExpiration() { return expiration; }
    public void setExpiration(Instant expiration) { this.expiration = expiration; }

    public boolean isRevoked() { return revoked; }
    public void setRevoked(boolean revoked) { this.revoked = revoked; }
}
