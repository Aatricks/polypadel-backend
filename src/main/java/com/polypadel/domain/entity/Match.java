package com.polypadel.domain.entity;

import com.polypadel.domain.enums.MatchStatus;
import jakarta.persistence.*;

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
public class Match {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private Integer piste;

    @ManyToOne(optional = false)
    @JoinColumn(name = "equipe1_id", nullable = false, foreignKey = @ForeignKey(name = "fk_match_equipe1"))
    private Equipe equipe1;

    @ManyToOne(optional = false)
    @JoinColumn(name = "equipe2_id", nullable = false, foreignKey = @ForeignKey(name = "fk_match_equipe2"))
    private Equipe equipe2;

    @ManyToOne(optional = false)
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

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Integer getPiste() { return piste; }
    public void setPiste(Integer piste) { this.piste = piste; }
    public Equipe getEquipe1() { return equipe1; }
    public void setEquipe1(Equipe equipe1) { this.equipe1 = equipe1; }
    public Equipe getEquipe2() { return equipe2; }
    public void setEquipe2(Equipe equipe2) { this.equipe2 = equipe2; }
    public Evenement getEvenement() { return evenement; }
    public void setEvenement(Evenement evenement) { this.evenement = evenement; }
    public MatchStatus getStatut() { return statut; }
    public void setStatut(MatchStatus statut) { this.statut = statut; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public String getScore1() { return score1; }
    public void setScore1(String score1) { this.score1 = score1; }
    public String getScore2() { return score2; }
    public void setScore2(String score2) { this.score2 = score2; }
}
