package com.polypadel.auth.web;

import com.polypadel.auth.dto.LoginRequest;
import com.polypadel.auth.dto.LoginResponse;
import com.polypadel.auth.service.AuthService;
import com.polypadel.security.JWTLogoutHelper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseCookie;
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

    public AuthController(AuthService authService) {
        this.authService = authService;
        this.logoutHelper = null; // will be autowired via setter
    }

    public AuthController(AuthService authService, JWTLogoutHelper logoutHelper) {
        this.authService = authService;
        this.logoutHelper = logoutHelper;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletResponse httpResponse) {
        LoginResponse resp = authService.login(request.getEmail().trim().toLowerCase(), request.getPassword());
        // Set HttpOnly cookie
        ResponseCookie cookie = ResponseCookie.from("JWT", resp.getToken())
                .httpOnly(true)
                .secure(false) // set true behind HTTPS
                .path("/")
                .sameSite("Lax")
                .maxAge(24 * 60 * 60)
                .build();
        httpResponse.addHeader("Set-Cookie", cookie.toString());
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
