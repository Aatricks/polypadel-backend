package com.polypadel.service;

import com.polypadel.dto.*;
import com.polypadel.model.*;
import com.polypadel.repository.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

@Service
public class PlayerService extends BaseService<Player, Long, PlayerResponse> {
    private final PlayerRepository playerRepository;
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;

    public PlayerService(PlayerRepository playerRepository, TeamRepository teamRepository, UserRepository userRepository) {
        this.playerRepository = playerRepository;
        this.teamRepository = teamRepository;
        this.userRepository = userRepository;
    }

    @Override protected JpaRepository<Player, Long> getRepository() { return playerRepository; }
    @Override protected String getEntityName() { return "Joueur"; }

    public PlayerResponse create(PlayerRequest request) {
        if (playerRepository.existsByLicenseNumber(request.licenseNumber())) {
            throw conflict("Ce numéro de licence existe déjà");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw conflict("Cet email existe déjà");
        }
        Player player = new Player();
        player.setFirstName(sanitize(request.firstName()));
        player.setLastName(sanitize(request.lastName()));
        player.setCompany(sanitize(request.company()));
        player.setLicenseNumber(request.licenseNumber());
        return toResponse(playerRepository.save(player));
    }

    public PlayerResponse update(Long id, PlayerRequest request) {
        Player player = getEntityById(id);
        player.setFirstName(sanitize(request.firstName()));
        player.setLastName(sanitize(request.lastName()));
        player.setCompany(sanitize(request.company()));
        return toResponse(playerRepository.save(player));
    }

    @Override
    protected void validateDelete(Player player) {
        if (!teamRepository.findByPlayerId(player.getId()).isEmpty()) {
            throw conflict("Ce joueur appartient à une équipe");
        }
    }

    @Override
    public PlayerResponse toResponse(Player p) {
        return new PlayerResponse(p.getId(), p.getFirstName(), p.getLastName(), p.getCompany(),
            p.getLicenseNumber(), p.getBirthDate(), p.getPhotoUrl(), p.getUser() != null);
    }
}
