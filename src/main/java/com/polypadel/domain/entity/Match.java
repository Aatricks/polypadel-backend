package com.polypadel.domain.entity;

import com.polypadel.domain.enums.MatchStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "match",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_match_event_piste_time", columnNames = {"evenement_id", "piste", "start_time"})
        },
        indexes = {
                @Index(name = "idx_match_event", columnList = "evenement_id"),
                @Index(name = "idx_match_equipe1", columnList = "equipe1_id"),
                @Index(name = "idx_match_equipe2", columnList = "equipe2_id")
        }
)
@Getter @Setter @NoArgsConstructor
public class Match {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private Integer piste;

        @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "equipe1_id", nullable = false, foreignKey = @ForeignKey(name = "fk_match_equipe1"))
    private Equipe equipe1;

        @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "equipe2_id", nullable = false, foreignKey = @ForeignKey(name = "fk_match_equipe2"))
    private Equipe equipe2;

        @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "evenement_id", nullable = false, foreignKey = @ForeignKey(name = "fk_match_evenement"))
    private Evenement evenement;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MatchStatus statut = MatchStatus.A_VENIR;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(length = 128)
    private String score1;

    @Column(length = 128)
    private String score2;
}
