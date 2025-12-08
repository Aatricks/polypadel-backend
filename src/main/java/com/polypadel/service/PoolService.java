package com.polypadel.service;

import com.polypadel.dto.*;
import com.polypadel.model.*;
import com.polypadel.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
public class PoolService {
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

    public List<PoolResponse> findAll() {
        return poolRepository.findAll().stream().map(this::toResponse).toList();
    }

    public PoolResponse findById(Long id) {
        return toResponse(poolRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Poule non trouvée")));
    }

    public PoolResponse create(PoolRequest request) {
        if (poolRepository.existsByName(request.name())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ce nom de poule existe déjà");
        }
        if (request.teamIds() == null || request.teamIds().size() != 6) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Une poule doit contenir exactement 6 équipes");
        }

        Pool pool = new Pool();
        pool.setName(request.name());
        pool = poolRepository.save(pool);

        for (Long teamId : request.teamIds()) {
            Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Équipe " + teamId + " non trouvée"));
            team.setPool(pool);
            teamRepository.save(team);
        }
        return findById(pool.getId());
    }

    public PoolResponse update(Long id, PoolRequest request) {
        Pool pool = poolRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Poule non trouvée"));
        
        // Check no matches played
        for (Team team : pool.getTeams()) {
            if (!matchRepository.findCompletedByTeamId(team.getId()).isEmpty()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Modification impossible: des matchs ont été joués");
            }
        }
        
        pool.setName(request.name());
        poolRepository.save(pool);
        return findById(id);
    }

    public void delete(Long id) {
        Pool pool = poolRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Poule non trouvée"));
        
        for (Team team : pool.getTeams()) {
            if (!matchRepository.findCompletedByTeamId(team.getId()).isEmpty()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Suppression impossible: des matchs ont été joués");
            }
            team.setPool(null);
            teamRepository.save(team);
        }
        poolRepository.delete(pool);
    }

    private PoolResponse toResponse(Pool p) {
        List<TeamResponse> teams = teamRepository.findByPoolId(p.getId()).stream()
            .map(teamService::toResponse).toList();
        return new PoolResponse(p.getId(), p.getName(), teams.size(), teams);
    }
}
