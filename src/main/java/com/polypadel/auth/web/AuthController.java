package com.polypadel.auth.web;

import com.polypadel.auth.dto.LoginRequest;
import com.polypadel.auth.dto.LoginResponse;
import com.polypadel.auth.dto.RegisterRequest;
import com.polypadel.auth.service.AuthService;
import com.polypadel.security.JwtService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    public AuthController(AuthService authService, JwtService jwtService) {
        this.authService = authService;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        LoginResponse resp = authService.login(request.email().trim().toLowerCase(), request.password());
        setCookie(resp.token(), response);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@Valid @RequestBody RegisterRequest request, HttpServletResponse response) {
        LoginResponse resp = authService.register(request);
        setCookie(resp.token(), response);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("JWT", "").httpOnly(true).path("/").maxAge(0).build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return ResponseEntity.noContent().build();
    }

    private void setCookie(String token, HttpServletResponse response) {
        if (token == null) return;
        try {
            Instant exp = jwtService.getExpiration(token);
            long maxAge = Math.max(0, Duration.between(Instant.now(), exp).getSeconds());
            ResponseCookie cookie = ResponseCookie.from("JWT", token).httpOnly(true).path("/").maxAge(maxAge).build();
            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        } catch (Exception ignored) {}
    }
}
