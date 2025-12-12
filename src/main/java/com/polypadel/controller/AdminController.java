package com.polypadel.controller;

import com.polypadel.service.UserAuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {
    private final UserAuthService userAuthService;

    public AdminController(UserAuthService userAuthService) {
        this.userAuthService = userAuthService;
    }

    @PostMapping("/accounts/create")
    public ResponseEntity<UserAuthService.AccountResponse> createAccount(@RequestBody Map<String, Object> body) {
        Long playerId = ((Number) body.get("player_id")).longValue();
        String role = (String) body.get("role");
        return ResponseEntity.status(HttpStatus.CREATED).body(userAuthService.createAccount(playerId, role));
    }

    @PostMapping("/accounts/{userId}/reset-password")
    public ResponseEntity<UserAuthService.AccountResponse> resetPassword(@PathVariable Long userId) {
        return ResponseEntity.ok(userAuthService.resetPassword(userId));
    }
}
