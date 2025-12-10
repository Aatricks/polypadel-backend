package com.polypadel.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pools")
public class Pool {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @OneToMany(mappedBy = "pool")
    private List<Team> teams = new ArrayList<>();

    public Pool() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public List<Team> getTeams() { return teams; }
    public void setTeams(List<Team> teams) { this.teams = teams; }
}
