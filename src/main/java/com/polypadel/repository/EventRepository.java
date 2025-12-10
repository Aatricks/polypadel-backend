package com.polypadel.repository;

import com.polypadel.model.Event;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface EventRepository extends JpaRepository<Event, Long> {
    @Query(
        "SELECT DISTINCT e FROM Event e LEFT JOIN FETCH e.matches WHERE e.eventDate BETWEEN :start AND :end"
    )
    List<Event> findByEventDateBetween(LocalDate start, LocalDate end);

    @Query("SELECT DISTINCT e FROM Event e LEFT JOIN FETCH e.matches")
    List<Event> findAllWithMatches();

    List<Event> findByEventDateGreaterThanEqualOrderByEventDateAsc(
        LocalDate date
    );
}
