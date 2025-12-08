package com.polypadel.events.repository;

import com.polypadel.domain.entity.Evenement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface EventRepository extends JpaRepository<Evenement, UUID> {

    /**
     * Trouve tous les événements qui se déroulent entre deux dates.
     * Utile pour l'affichage du calendrier (ex: du 1er au 31 du mois).
     */
    // Méthode "Magic" de Spring Data (génère le SQL automatiquement)
    List<Evenement> findByEventDateBetweenOrderByEventDateAscEventTimeAsc(LocalDate start, LocalDate end);

    /* * Alternative avec @Query manuelle si vous préférez :
     * * @Query("select e from Evenement e where e.eventDate >= :start and e.eventDate <= :end order by e.eventDate, e.eventTime")
     * List<Evenement> findInRange(@Param("start") LocalDate start, @Param("end") LocalDate end);
     */
}