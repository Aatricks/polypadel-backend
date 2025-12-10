package com.polypadel.repository;

import com.polypadel.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface TeamRepository extends JpaRepository<Team, Long> {
    List<Team> findByPoolId(Long poolId);
    List<Team> findByCompany(String company);
    
    @Query("SELECT t FROM Team t WHERE t.player1.id = :playerId OR t.player2.id = :playerId")
    List<Team> findByPlayerId(Long playerId);
}
