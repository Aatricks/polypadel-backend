package com.polypadel.domain.entity;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "equipe", indexes = {
        @Index(name = "idx_equipe_poule", columnList = "poule_id")
})
public class Equipe {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, length = 128)
    private String entreprise;

    @ManyToOne(optional = true)
    @JoinColumn(name = "poule_id", foreignKey = @ForeignKey(name = "fk_equipe_poule"))
    private Poule poule;

    @OneToOne(optional = false)
    @JoinColumn(name = "joueur1_id", nullable = false, foreignKey = @ForeignKey(name = "fk_equipe_joueur1"))
    private Joueur joueur1;

    @OneToOne(optional = false)
    @JoinColumn(name = "joueur2_id", nullable = false, foreignKey = @ForeignKey(name = "fk_equipe_joueur2"))
    private Joueur joueur2;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getEntreprise() { return entreprise; }
    public void setEntreprise(String entreprise) { this.entreprise = entreprise; }
    public Poule getPoule() { return poule; }
    public void setPoule(Poule poule) { this.poule = poule; }
    public Joueur getJoueur1() { return joueur1; }
    public void setJoueur1(Joueur joueur1) { this.joueur1 = joueur1; }
    public Joueur getJoueur2() { return joueur2; }
    public void setJoueur2(Joueur joueur2) { this.joueur2 = joueur2; }
}
