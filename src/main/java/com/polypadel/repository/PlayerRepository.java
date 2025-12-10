package com.polypadel.repository;

import com.polypadel.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PlayerRepository extends JpaRepository<Player, Long> {
    Optional<Player> findByLicenseNumber(String licenseNumber);
    Optional<Player> findByUserId(Long userId);
    boolean existsByLicenseNumber(String licenseNumber);
}
