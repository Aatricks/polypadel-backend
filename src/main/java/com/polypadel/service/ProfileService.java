package com.polypadel.service;

import com.polypadel.dto.*;
import com.polypadel.model.*;
import com.polypadel.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class ProfileService {
    private final UserRepository userRepository;
    private final PlayerRepository playerRepository;
    
    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

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

    public String uploadPhoto(User user, MultipartFile photo) {
        Player player = playerRepository.findByUserId(user.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profil joueur non trouvé"));

        if (photo.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Fichier vide");
        }

        String contentType = photo.getContentType();
        if (contentType == null || (!contentType.equals("image/jpeg") && 
            !contentType.equals("image/png") && !contentType.equals("image/jpg"))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Format invalide. Utilisez JPG ou PNG");
        }

        if (photo.getSize() > 2 * 1024 * 1024) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Taille maximale: 2MB");
        }

        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Delete old photo if exists
            if (player.getPhotoUrl() != null) {
                try {
                    Path oldPhoto = Paths.get(uploadDir, player.getPhotoUrl().replace("/uploads/", ""));
                    Files.deleteIfExists(oldPhoto);
                } catch (Exception ignored) {}
            }

            String extension = contentType.equals("image/png") ? ".png" : ".jpg";
            String filename = "player_" + player.getId() + "_" + UUID.randomUUID() + extension;
            Path filePath = uploadPath.resolve(filename);
            Files.copy(photo.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            String photoUrl = "/uploads/" + filename;
            player.setPhotoUrl(photoUrl);
            playerRepository.save(player);

            return photoUrl;
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur lors de l'upload");
        }
    }

    public void deletePhoto(User user) {
        Player player = playerRepository.findByUserId(user.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profil joueur non trouvé"));

        if (player.getPhotoUrl() != null) {
            try {
                Path photoPath = Paths.get(uploadDir, player.getPhotoUrl().replace("/uploads/", ""));
                Files.deleteIfExists(photoPath);
            } catch (Exception ignored) {}
            
            player.setPhotoUrl(null);
            playerRepository.save(player);
        }
    }
}
