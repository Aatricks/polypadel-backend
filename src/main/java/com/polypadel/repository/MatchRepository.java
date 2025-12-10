package com.polypadel.repository;

import com.polypadel.model.Match;
import com.polypadel.model.MatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDate;
import java.util.List;

public interface MatchRepository extends JpaRepository<Match, Long> {
    List<Match> findByStatus(MatchStatus status);
    
    @Query("SELECT m FROM Match m WHERE m.team1.id = :teamId OR m.team2.id = :teamId")
    List<Match> findByTeamId(Long teamId);
    
    @Query("SELECT m FROM Match m WHERE m.event.eventDate BETWEEN :start AND :end")
    List<Match> findByDateRange(LocalDate start, LocalDate end);
    
    @Query("SELECT m FROM Match m WHERE m.status = 'TERMINE' AND (m.team1.id = :teamId OR m.team2.id = :teamId)")
    List<Match> findCompletedByTeamId(Long teamId);
}
