package com.polypadel.admin.service;

import com.polypadel.admin.dto.AdminCreateUserRequest;
import com.polypadel.admin.dto.AdminCreateUserResponse;
import com.polypadel.admin.dto.AdminResetPasswordResponse;
import com.polypadel.common.exception.BusinessException;
import com.polypadel.common.exception.ErrorCodes;
import com.polypadel.common.exception.NotFoundException;
import com.polypadel.domain.entity.Utilisateur;
import com.polypadel.domain.enums.Role;
import com.polypadel.users.repository.UtilisateurRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.Locale;
import java.util.UUID;

@Service
public class AdminUserService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom random = new SecureRandom();

    public AdminUserService(UtilisateurRepository utilisateurRepository, PasswordEncoder passwordEncoder) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AdminCreateUserResponse create(AdminCreateUserRequest req) {
        String email = req.email().trim().toLowerCase(Locale.ROOT);
        if (utilisateurRepository.findByEmail(email).isPresent()) {
            throw new BusinessException("USER_EMAIL_EXISTS", "Email already registered");
        }
        Role role;
        try {
            role = Role.valueOf(req.role().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new BusinessException("USER_ROLE_INVALID", "Invalid role");
        }
        String tempPwd = generateStrongPassword();
        Utilisateur u = new Utilisateur();
        u.setEmail(email);
        u.setEmailHash(hashEmail(email));
        u.setPasswordHash(passwordEncoder.encode(tempPwd));
        u.setRole(role);
        u.setActive(true);
        Utilisateur saved = utilisateurRepository.save(u);
        return new AdminCreateUserResponse(saved.getId(), saved.getEmail(), saved.getRole().name(), tempPwd);
    }

    @Transactional
    public AdminResetPasswordResponse resetPassword(UUID userId) {
        Utilisateur u = utilisateurRepository.findById(userId).orElseThrow(() -> new NotFoundException(ErrorCodes.USER_NOT_FOUND, "Utilisateur not found: " + userId));
        String tempPwd = generateStrongPassword();
        u.setPasswordHash(passwordEncoder.encode(tempPwd));
        utilisateurRepository.save(u);
        return new AdminResetPasswordResponse(tempPwd);
    }

    private String hashEmail(String email) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(email.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String generateStrongPassword() {
        // 12+ chars with lower, upper, digits, symbols
        final String lowers = "abcdefghijklmnopqrstuvwxyz";
        final String uppers = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        final String digits = "0123456789";
        final String symbols = "!@#$%^&*()-_=+[]{}|;:,.<>?";
        String all = lowers + uppers + digits + symbols;
        StringBuilder sb = new StringBuilder();
        // ensure each category at least once
        sb.append(randomChar(lowers));
        sb.append(randomChar(uppers));
        sb.append(randomChar(digits));
        sb.append(randomChar(symbols));
        for (int i = 0; i < 12; i++) sb.append(randomChar(all));
        return sb.toString();
    }

    private char randomChar(String s) {
        return s.charAt(random.nextInt(s.length()));
    }
}
