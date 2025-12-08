package com.polypadel.model;

import jakarta.persistence.*;

@Entity
@Table(name = "teams")
public class Team {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String company;

    @ManyToOne
    @JoinColumn(name = "player1_id", nullable = false)
    private Player player1;

    @ManyToOne
    @JoinColumn(name = "player2_id", nullable = false)
    private Player player2;

    @ManyToOne
    @JoinColumn(name = "pool_id")
    private Pool pool;

    public Team() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }
    public Player getPlayer1() { return player1; }
    public void setPlayer1(Player player1) { this.player1 = player1; }
    public Player getPlayer2() { return player2; }
    public void setPlayer2(Player player2) { this.player2 = player2; }
    public Pool getPool() { return pool; }
    public void setPool(Pool pool) { this.pool = pool; }
}
