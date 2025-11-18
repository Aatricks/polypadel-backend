package com.polypadel.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "joueur", uniqueConstraints = {
        @UniqueConstraint(name = "uk_joueur_num_licence", columnNames = {"num_licence"})
})
public class Joueur {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "num_licence", nullable = false, length = 64)
    private String numLicence;

    @Size(min = 2, max = 50)
    private String nom;

    @Size(min = 2, max = 50)
    private String prenom;

    @Past
    private LocalDate dateNaissance;

    @Size(max = 1024)
    private String photoUrl;

    @OneToOne(optional = true)
    @JoinColumn(name = "utilisateur_id", foreignKey = @ForeignKey(name = "fk_joueur_utilisateur"))
    private Utilisateur utilisateur;

    @Column(nullable = false, length = 128)
    private String entreprise;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getNumLicence() { return numLicence; }
    public void setNumLicence(String numLicence) { this.numLicence = numLicence; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public LocalDate getDateNaissance() { return dateNaissance; }
    public void setDateNaissance(LocalDate dateNaissance) { this.dateNaissance = dateNaissance; }
    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
    public Utilisateur getUtilisateur() { return utilisateur; }
    public void setUtilisateur(Utilisateur utilisateur) { this.utilisateur = utilisateur; }
    public String getEntreprise() { return entreprise; }
    public void setEntreprise(String entreprise) { this.entreprise = entreprise; }
}
