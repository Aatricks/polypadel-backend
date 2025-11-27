package com.polypadel.auth.web;

import com.polypadel.auth.dto.LoginRequest;
import com.polypadel.auth.dto.LoginResponse;
import com.polypadel.auth.dto.RegisterRequest;
import com.polypadel.auth.service.AuthService;
import com.polypadel.security.JWTLogoutHelper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final JWTLogoutHelper logoutHelper;

    public AuthController(AuthService authService, java.util.Optional<JWTLogoutHelper> logoutHelper) {
        this.authService = authService;
        this.logoutHelper = logoutHelper.orElse(null);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletResponse httpResponse) {
        LoginResponse resp = authService.login(request.email().trim().toLowerCase(), request.password());
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@Valid @RequestBody RegisterRequest request, HttpServletResponse httpResponse) {
        LoginResponse resp = authService.register(request);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        if (logoutHelper != null) {
            logoutHelper.logout(request, response);
        }
        return ResponseEntity.noContent().build();
    }
}
