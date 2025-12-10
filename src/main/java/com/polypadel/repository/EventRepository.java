package com.polypadel.repository;

import com.polypadel.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByEventDateBetween(LocalDate start, LocalDate end);
    List<Event> findByEventDateGreaterThanEqualOrderByEventDateAsc(LocalDate date);
}
