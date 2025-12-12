package com.polypadel.service;

import com.polypadel.dto.*;
import com.polypadel.model.*;
import com.polypadel.repository.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TeamService extends BaseService<Team, Long, TeamResponse> {
    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;
    private final PoolRepository poolRepository;
    private final MatchRepository matchRepository;

    public TeamService(TeamRepository teamRepository, PlayerRepository playerRepository,
                       PoolRepository poolRepository, MatchRepository matchRepository) {
        this.teamRepository = teamRepository;
        this.playerRepository = playerRepository;
        this.poolRepository = poolRepository;
        this.matchRepository = matchRepository;
    }

    @Override protected JpaRepository<Team, Long> getRepository() { return teamRepository; }
    @Override protected String getEntityName() { return "Équipe"; }

    public List<TeamResponse> findAll(Long poolId, String company) {
        List<Team> teams = poolId != null ? teamRepository.findByPoolId(poolId)
            : company != null ? teamRepository.findByCompany(company)
            : teamRepository.findAll();
        return teams.stream().map(this::toResponse).toList();
    }

    public TeamResponse create(TeamRequest request) {
        if (request.player1Id().equals(request.player2Id())) {
            throw badRequest("Les deux joueurs doivent être différents");
        }
        Player p1 = playerRepository.findById(request.player1Id())
            .orElseThrow(() -> badRequest("Joueur 1 non trouvé"));
        Player p2 = playerRepository.findById(request.player2Id())
            .orElseThrow(() -> badRequest("Joueur 2 non trouvé"));

        if (!p1.getCompany().equals(p2.getCompany())) {
            throw badRequest("Les joueurs doivent appartenir à la même entreprise");
        }
        if (!teamRepository.findByPlayerId(p1.getId()).isEmpty() || !teamRepository.findByPlayerId(p2.getId()).isEmpty()) {
            throw conflict("Un joueur est déjà dans une équipe");
        }

        Team team = new Team();
        team.setCompany(request.company());
        team.setPlayer1(p1);
        team.setPlayer2(p2);
        if (request.poolId() != null) {
            team.setPool(poolRepository.findById(request.poolId()).orElse(null));
        }
        return toResponse(teamRepository.save(team));
    }

    public TeamResponse update(Long id, TeamRequest request) {
        Team team = getEntityById(id);
        if (!matchRepository.findByTeamId(id).isEmpty()) {
            throw conflict("Modification impossible: des matchs existent");
        }
        team.setCompany(request.company());
        if (request.poolId() != null) {
            team.setPool(poolRepository.findById(request.poolId()).orElse(null));
        }
        return toResponse(teamRepository.save(team));
    }

    @Override
    protected void validateDelete(Team team) {
        if (!matchRepository.findByTeamId(team.getId()).isEmpty()) {
            throw conflict("Suppression impossible: des matchs existent");
        }
    }

    @Override
    public TeamResponse toResponse(Team t) {
        return new TeamResponse(t.getId(), t.getCompany(),
            List.of(new TeamResponse.PlayerInfo(t.getPlayer1().getId(), t.getPlayer1().getFirstName(), t.getPlayer1().getLastName()),
                    new TeamResponse.PlayerInfo(t.getPlayer2().getId(), t.getPlayer2().getFirstName(), t.getPlayer2().getLastName())),
            t.getPool() != null ? new TeamResponse.PoolInfo(t.getPool().getId(), t.getPool().getName()) : null);
    }
}
