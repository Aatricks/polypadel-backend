package com.polypadel.model;

import jakarta.persistence.*;

@Entity
@Table(name = "matches")
public class Match {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne
    @JoinColumn(name = "team1_id", nullable = false)
    private Team team1;

    @ManyToOne
    @JoinColumn(name = "team2_id", nullable = false)
    private Team team2;

    @Column(nullable = false)
    private Integer courtNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MatchStatus status = MatchStatus.A_VENIR;

    private String scoreTeam1;
    private String scoreTeam2;

    public Match() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Event getEvent() { return event; }
    public void setEvent(Event event) { this.event = event; }
    public Team getTeam1() { return team1; }
    public void setTeam1(Team team1) { this.team1 = team1; }
    public Team getTeam2() { return team2; }
    public void setTeam2(Team team2) { this.team2 = team2; }
    public Integer getCourtNumber() { return courtNumber; }
    public void setCourtNumber(Integer courtNumber) { this.courtNumber = courtNumber; }
    public MatchStatus getStatus() { return status; }
    public void setStatus(MatchStatus status) { this.status = status; }
    public String getScoreTeam1() { return scoreTeam1; }
    public void setScoreTeam1(String scoreTeam1) { this.scoreTeam1 = scoreTeam1; }
    public String getScoreTeam2() { return scoreTeam2; }
    public void setScoreTeam2(String scoreTeam2) { this.scoreTeam2 = scoreTeam2; }
}
