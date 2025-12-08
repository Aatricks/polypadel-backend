package com.polypadel.service;

import com.polypadel.dto.*;
import com.polypadel.model.*;
import com.polypadel.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProfileService {
    private final UserRepository userRepository;
    private final PlayerRepository playerRepository;

    public ProfileService(UserRepository userRepository, PlayerRepository playerRepository) {
        this.userRepository = userRepository;
        this.playerRepository = playerRepository;
    }

    public ProfileResponse getProfile(User user) {
        Player player = playerRepository.findByUserId(user.getId()).orElse(null);
        PlayerResponse pr = player != null ? new PlayerResponse(
            player.getId(), player.getFirstName(), player.getLastName(), player.getCompany(),
            player.getLicenseNumber(), player.getBirthDate(), player.getPhotoUrl(), true
        ) : null;
        return new ProfileResponse(
            new ProfileResponse.UserInfo(user.getId(), user.getEmail(), user.getRole().name()), pr);
    }

    public ProfileResponse updateProfile(User user, ProfileUpdateRequest request) {
        Player player = playerRepository.findByUserId(user.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profil joueur non trouvé"));
        
        if (request.firstName() != null) player.setFirstName(request.firstName());
        if (request.lastName() != null) player.setLastName(request.lastName());
        if (request.birthDate() != null) player.setBirthDate(request.birthDate());
        if (request.email() != null && !request.email().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.email())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Cet email existe déjà");
            }
            user.setEmail(request.email());
            userRepository.save(user);
        }
        playerRepository.save(player);
        return getProfile(user);
    }
}
