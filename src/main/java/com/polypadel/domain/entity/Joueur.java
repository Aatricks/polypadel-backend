package com.polypadel.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "joueur", uniqueConstraints = {
        @UniqueConstraint(name = "uk_joueur_num_licence", columnNames = {"num_licence"})
})
@Getter @Setter @NoArgsConstructor
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
}
