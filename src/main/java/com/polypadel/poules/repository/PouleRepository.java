package com.polypadel.poules.repository;

import com.polypadel.domain.entity.Poule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PouleRepository extends JpaRepository<Poule, UUID> {
}
