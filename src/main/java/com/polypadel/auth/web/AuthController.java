package com.polypadel.auth.web;

import com.polypadel.auth.dto.LoginRequest;
import com.polypadel.auth.dto.LoginResponse;
import com.polypadel.auth.dto.RegisterRequest;
import com.polypadel.auth.service.AuthService;
import com.polypadel.security.JwtService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import java.time.Duration;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
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
    private final JwtService jwtService;
    private final boolean secureCookie;

    public AuthController(AuthService authService,
                          java.util.Optional<JWTLogoutHelper> logoutHelper,
                          JwtService jwtService,
                          @Value("${security.cookie.secure:false}") boolean secureCookie) {
        this.authService = authService;
        this.logoutHelper = logoutHelper.orElse(null);
        this.jwtService = jwtService;
        this.secureCookie = secureCookie;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletResponse httpResponse) {
        LoginResponse resp = authService.login(request.email().trim().toLowerCase(), request.password());
        if (resp != null && resp.token() != null) {
            try {
                Instant exp = jwtService.getExpiration(resp.token());
                long maxAge = Math.max(0, Duration.between(Instant.now(), exp).getSeconds());
                ResponseCookie cookie = ResponseCookie.from("JWT", resp.token())
                        .httpOnly(true)
                        .secure(this.secureCookie)
                        .path("/")
                        .sameSite("Lax")
                        .maxAge(maxAge)
                        .build();
                httpResponse.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
            } catch (Exception ignored) {
            }
        }
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@Valid @RequestBody RegisterRequest request, HttpServletResponse httpResponse) {
        LoginResponse resp = authService.register(request);
        if (resp != null && resp.token() != null) {
            try {
                Instant exp = jwtService.getExpiration(resp.token());
                long maxAge = Math.max(0, Duration.between(Instant.now(), exp).getSeconds());
                ResponseCookie cookie = ResponseCookie.from("JWT", resp.token())
                        .httpOnly(true)
                        .secure(this.secureCookie)
                        .path("/")
                        .sameSite("Lax")
                        .maxAge(maxAge)
                        .build();
                httpResponse.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
            } catch (Exception ignored) {
            }
        }
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
