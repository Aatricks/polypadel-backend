package com.polypadel.equipes.repository;

import com.polypadel.domain.entity.Equipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;
import java.util.List;

public interface EquipeRepository extends JpaRepository<Equipe, UUID> {
    boolean existsByJoueur1IdOrJoueur2Id(UUID joueur1Id, UUID joueur2Id);

    @Query("select count(e) > 0 from Equipe e where e.joueur1.id=:playerId or e.joueur2.id=:playerId")
    boolean playerInAnyTeam(UUID playerId);

    int countByPouleId(UUID pouleId);

    List<Equipe> findByPouleId(UUID pouleId);

    @Query("select e.id from Equipe e where e.joueur1.id=:playerId or e.joueur2.id=:playerId")
    java.util.List<UUID> findIdsByPlayer(UUID playerId);

    boolean existsByPouleId(UUID pouleId);
}
