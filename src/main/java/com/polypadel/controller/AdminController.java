package com.polypadel.controller;

import com.polypadel.service.AdminService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {
    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/accounts/create")
    public ResponseEntity<AdminService.CreateAccountResponse> createAccount(@RequestBody Map<String, Object> body) {
        Long playerId = ((Number) body.get("player_id")).longValue();
        String role = (String) body.get("role");
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createAccount(playerId, role));
    }

    @PostMapping("/accounts/{userId}/reset-password")
    public ResponseEntity<AdminService.CreateAccountResponse> resetPassword(@PathVariable Long userId) {
        return ResponseEntity.ok(adminService.resetPassword(userId));
    }
}
