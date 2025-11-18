package com.polypadel.users.web;

import com.polypadel.users.dto.PasswordUpdateRequest;
import com.polypadel.users.dto.ProfileResponse;
import com.polypadel.users.dto.ProfileUpdateRequest;
import com.polypadel.users.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public ResponseEntity<ProfileResponse> getProfile() {
        return ResponseEntity.ok(profileService.getProfile());
    }

    @PutMapping
    public ResponseEntity<ProfileResponse> updateProfile(@Valid @RequestBody ProfileUpdateRequest req) {
        return ResponseEntity.ok(profileService.updateProfile(req));
    }

    @PutMapping("/password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody PasswordUpdateRequest req) {
        profileService.changePassword(req);
        return ResponseEntity.noContent().build();
    }
}
