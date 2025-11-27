package com.polypadel.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

public class PasswordEncoderConfigTest {
    @Test
    public void passwordEncoder_produces_encoder() {
        PasswordEncoderConfig cfg = new PasswordEncoderConfig();
        PasswordEncoder enc = cfg.passwordEncoder();
        assertThat(enc).isNotNull();
    }
}
