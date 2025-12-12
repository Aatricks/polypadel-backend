package com.polypadel.controller;

import com.polypadel.dto.*;
import com.polypadel.model.User;
import com.polypadel.service.ProfileService;
import com.polypadel.service.UserAuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

@RestController
@RequestMapping("/profile")
public class ProfileController {
    private final ProfileService profileService;
    private final UserAuthService userAuthService;

    public ProfileController(ProfileService profileService, UserAuthService userAuthService) {
        this.profileService = profileService;
        this.userAuthService = userAuthService;
    }

    @GetMapping("/me")
    public ResponseEntity<ProfileResponse> getProfile(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(profileService.getProfile(user));
    }

    @PutMapping("/me")
    public ResponseEntity<ProfileResponse> updateProfile(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ProfileUpdateRequest request) {
        return ResponseEntity.ok(profileService.updateProfile(user, request));
    }

    @PostMapping("/me/change-password")
    public ResponseEntity<Map<String, String>> changePassword(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody PasswordChangeRequest request) {
        userAuthService.changePassword(user, request);
        return ResponseEntity.ok(Map.of("message", "Mot de passe modifié avec succès"));
    }

    @PostMapping("/me/photo")
    public ResponseEntity<Map<String, String>> uploadPhoto(
            @AuthenticationPrincipal User user,
            @RequestParam("photo") MultipartFile photo) {
        String photoUrl = profileService.uploadPhoto(user, photo);
        return ResponseEntity.ok(Map.of("message", "Photo uploadée avec succès", "photo_url", photoUrl));
    }

    @DeleteMapping("/me/photo")
    public ResponseEntity<Map<String, String>> deletePhoto(@AuthenticationPrincipal User user) {
        profileService.deletePhoto(user);
        return ResponseEntity.ok(Map.of("message", "Photo supprimée avec succès"));
    }
}
