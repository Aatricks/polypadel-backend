package com.polypadel.service;

import com.polypadel.dto.*;
import com.polypadel.model.*;
import com.polypadel.repository.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PoolService extends BaseService<Pool, Long, PoolResponse> {
    private final PoolRepository poolRepository;
    private final TeamRepository teamRepository;
    private final MatchRepository matchRepository;
    private final TeamService teamService;

    public PoolService(PoolRepository poolRepository, TeamRepository teamRepository,
                       MatchRepository matchRepository, TeamService teamService) {
        this.poolRepository = poolRepository;
        this.teamRepository = teamRepository;
        this.matchRepository = matchRepository;
        this.teamService = teamService;
    }

    @Override protected JpaRepository<Pool, Long> getRepository() { return poolRepository; }
    @Override protected String getEntityName() { return "Poule"; }

    public PoolResponse create(PoolRequest request) {
        if (poolRepository.existsByName(request.name())) {
            throw conflict("Ce nom de poule existe déjà");
        }
        if (request.teamIds() == null || request.teamIds().size() != 6) {
            throw badRequest("Une poule doit contenir exactement 6 équipes");
        }

        Pool pool = new Pool();
        pool.setName(request.name());
        pool = poolRepository.save(pool);

        for (Long teamId : request.teamIds()) {
            Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> badRequest("Équipe " + teamId + " non trouvée"));
            team.setPool(pool);
            teamRepository.save(team);
        }
        return findById(pool.getId());
    }

    public PoolResponse update(Long id, PoolRequest request) {
        Pool pool = getEntityById(id);
        validateNoPlayedMatches(pool);
        pool.setName(request.name());
        poolRepository.save(pool);
        return findById(id);
    }

    @Override
    protected void validateDelete(Pool pool) {
        validateNoPlayedMatches(pool);
        for (Team team : pool.getTeams()) {
            team.setPool(null);
            teamRepository.save(team);
        }
    }

    private void validateNoPlayedMatches(Pool pool) {
        for (Team team : pool.getTeams()) {
            if (!matchRepository.findCompletedByTeamId(team.getId()).isEmpty()) {
                throw conflict("Modification impossible: des matchs ont été joués");
            }
        }
    }

    @Override
    public PoolResponse toResponse(Pool p) {
        List<TeamResponse> teams = teamRepository.findByPoolId(p.getId()).stream()
            .map(teamService::toResponse).toList();
        return new PoolResponse(p.getId(), p.getName(), teams.size(), teams);
    }
}
