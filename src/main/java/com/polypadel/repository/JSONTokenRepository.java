package com.polypadel.repository;

import com.polypadel.domain.entity.JSONToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JSONTokenRepository extends JpaRepository<JSONToken, UUID> {
	boolean existsByJtiAndRevokedTrue(String jti);
}
