package com.polypadel.service;

import com.polypadel.dto.*;
import com.polypadel.model.*;
import com.polypadel.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
public class PlayerService {
    private final PlayerRepository playerRepository;
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;

    public PlayerService(PlayerRepository playerRepository, TeamRepository teamRepository, UserRepository userRepository) {
        this.playerRepository = playerRepository;
        this.teamRepository = teamRepository;
        this.userRepository = userRepository;
    }

    public List<PlayerResponse> findAll() {
        return playerRepository.findAll().stream().map(this::toResponse).toList();
    }

    public PlayerResponse findById(Long id) {
        return toResponse(playerRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Joueur non trouvé")));
    }

    public PlayerResponse create(PlayerRequest request) {
        if (playerRepository.existsByLicenseNumber(request.licenseNumber())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ce numéro de licence existe déjà");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cet email existe déjà");
        }
        Player player = new Player();
        player.setFirstName(sanitize(request.firstName()));
        player.setLastName(sanitize(request.lastName()));
        player.setCompany(sanitize(request.company()));
        player.setLicenseNumber(request.licenseNumber());
        return toResponse(playerRepository.save(player));
    }

    public PlayerResponse update(Long id, PlayerRequest request) {
        Player player = playerRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Joueur non trouvé"));
        player.setFirstName(sanitize(request.firstName()));
        player.setLastName(sanitize(request.lastName()));
        player.setCompany(sanitize(request.company()));
        return toResponse(playerRepository.save(player));
    }

    public void delete(Long id) {
        Player player = playerRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Joueur non trouvé"));
        if (!teamRepository.findByPlayerId(id).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ce joueur appartient à une équipe");
        }
        playerRepository.delete(player);
    }

    private PlayerResponse toResponse(Player p) {
        return new PlayerResponse(p.getId(), p.getFirstName(), p.getLastName(), p.getCompany(),
            p.getLicenseNumber(), p.getBirthDate(), p.getPhotoUrl(), p.getUser() != null);
    }

    private String sanitize(String input) {
        return input == null ? null : input.replaceAll("<[^>]*>", "").trim();
    }
}
