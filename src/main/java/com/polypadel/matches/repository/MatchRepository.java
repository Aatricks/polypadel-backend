package com.polypadel.matches.repository;

import com.polypadel.domain.entity.Match;
import com.polypadel.domain.enums.MatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public interface MatchRepository extends JpaRepository<Match, UUID> {



    boolean existsByEvenementIdAndPisteAndStartTime(UUID evenementId, Integer piste, LocalTime startTime);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    long countByEvenementId(UUID evenementId);

    boolean existsByEvenementIdAndEquipe1IdOrEvenementIdAndEquipe2Id(UUID eventId1, UUID teamId1, UUID eventId2, UUID teamId2);

    boolean existsByStatutAndEquipe1IdOrStatutAndEquipe2Id(MatchStatus statut1, UUID equipe1Id, MatchStatus statut2, UUID equipe2Id);

    boolean existsByEvenementIdAndStatut(UUID eventId, MatchStatus statut);

    @Query("select m from Match m where m.statut in :statuses and (m.equipe1.id in :teamIds or m.equipe2.id in :teamIds) order by m.startTime")
    List<Match> findUpcomingForTeams(@Param("statuses") List<MatchStatus> statuses, @Param("teamIds") List<UUID> teamIds);

    List<Match> findByEvenementIdOrderByStartTimeAsc(UUID eventId);

    @Query("select m from Match m where m.statut = :statut and m.equipe1.id in :teamIds and m.equipe2.id in :teamIds")
    List<Match> findFinishedWithinTeams(@Param("statut") MatchStatus statut, @Param("teamIds") List<UUID> teamIds);

    
    List<Match> findByEquipe1IdInOrEquipe2IdIn(List<UUID> teamIds1, List<UUID> teamIds2);

   
    List<Match> findByEquipe1IdOrEquipe2Id(UUID equipe1Id, UUID equipe2Id);
}