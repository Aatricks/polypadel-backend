package com.polypadel.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "events") 
@Getter @Setter @NoArgsConstructor
public class Evenement {

    @Id
    @GeneratedValue
    private UUID id;

    
    
    @Column(name = "event_date", nullable = false)
    private LocalDate eventDate; 

    @Column(name = "event_time", nullable = false)
    private LocalTime eventTime; 

  
    @OneToMany(mappedBy = "evenement", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Match> matches = new ArrayList<>();

    
    public void addMatch(Match match) {
        matches.add(match);
        match.setEvenement(this);
    }
}