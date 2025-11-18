package com.polypadel.admin.web;

import com.polypadel.admin.dto.AdminCreateUserRequest;
import com.polypadel.admin.dto.AdminCreateUserResponse;
import com.polypadel.admin.dto.AdminResetPasswordResponse;
import com.polypadel.admin.service.AdminUserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/admin/users")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @PostMapping
    public ResponseEntity<AdminCreateUserResponse> create(@Valid @RequestBody AdminCreateUserRequest req) {
        return ResponseEntity.ok(adminUserService.create(req));
    }

    @PostMapping("/{id}/reset-password")
    public ResponseEntity<AdminResetPasswordResponse> reset(@PathVariable UUID id) {
        return ResponseEntity.ok(adminUserService.resetPassword(id));
    }
}
