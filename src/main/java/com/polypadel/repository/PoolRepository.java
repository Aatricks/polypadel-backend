package com.polypadel.repository;

import com.polypadel.model.Pool;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PoolRepository extends JpaRepository<Pool, Long> {
    Optional<Pool> findByName(String name);
    boolean existsByName(String name);
}
