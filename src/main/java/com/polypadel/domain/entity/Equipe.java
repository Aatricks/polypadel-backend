package com.polypadel.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "equipe", indexes = {
        @Index(name = "idx_equipe_poule", columnList = "poule_id")
})
@Getter @Setter @NoArgsConstructor
public class Equipe {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, length = 128)
    private String entreprise;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "poule_id", foreignKey = @ForeignKey(name = "fk_equipe_poule"))
    private Poule poule;

    @OneToOne(optional = false)
    @JoinColumn(name = "joueur1_id", nullable = false, foreignKey = @ForeignKey(name = "fk_equipe_joueur1"))
    private Joueur joueur1;

    @OneToOne(optional = false)
    @JoinColumn(name = "joueur2_id", nullable = false, foreignKey = @ForeignKey(name = "fk_equipe_joueur2"))
    private Joueur joueur2;
}
