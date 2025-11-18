package com.polypadel.events.repository;

import com.polypadel.domain.entity.Evenement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface EventRepository extends JpaRepository<Evenement, UUID> {
    @Query("select e from Evenement e where e.dateDebut <= :end and e.dateFin >= :start order by e.dateDebut")
    List<Evenement> findInRange(LocalDate start, LocalDate end);
}
