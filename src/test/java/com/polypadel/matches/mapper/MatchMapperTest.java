package com.polypadel.matches.mapper;

import com.polypadel.domain.entity.Evenement;
import com.polypadel.domain.entity.Equipe;
import com.polypadel.domain.entity.Match;
import com.polypadel.matches.dto.MatchResponse;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class MatchMapperTest {

    private final MatchMapper mapper = Mappers.getMapper(MatchMapper.class);

    @Test
    public void toResponse_handles_nulls() {
        Match m = new Match();
        m.setId(UUID.randomUUID());
        
        MatchResponse r = mapper.toResponse(m);
        
        assertThat(r).isNotNull();
        // Syntaxe Classe (r.field) vs Record (r.field())
        assertThat(r.evenementId).isNull(); 
        assertThat(r.equipe1Id).isNull();
        assertThat(r.equipe2Id).isNull();
        assertThat(r.startTime).isNull();
    }

    @Test
    public void toResponse_maps_nested_entities_and_calculates_datetime() {
        // Given
        Match m = new Match();
        m.setId(UUID.randomUUID());
        
        // 1. Configurer l'événement avec une DATE (Indispensable pour le calcul LocalDateTime)
        Evenement ev = new Evenement(); 
        ev.setId(UUID.randomUUID()); 
        ev.setEventDate(LocalDate.of(2025, 1, 1)); // Date fixe
        m.setEvenement(ev);
        
        Equipe e1 = new Equipe(); e1.setId(UUID.randomUUID()); m.setEquipe1(e1);
        Equipe e2 = new Equipe(); e2.setId(UUID.randomUUID()); m.setEquipe2(e2);
        
        // 2. Configurer le match avec une HEURE
        m.setStartTime(LocalTime.of(10, 0)); // 10h00

        // When
        MatchResponse r = mapper.toResponse(m);

        // Then
        assertThat(r.evenementId).isEqualTo(ev.getId());
        assertThat(r.equipe1Id).isEqualTo(e1.getId());
        assertThat(r.equipe2Id).isEqualTo(e2.getId());
        
        // 3. Vérification de la fusion Date + Heure
        assertThat(r.startTime).isEqualTo(LocalDateTime.of(2025, 1, 1, 10, 0));
    }

    @Test
    public void toResponse_returns_null_for_null_entity() {
        MatchResponse r = mapper.toResponse(null);
        assertThat(r).isNull();
    }

    @Test
    public void toResponse_handles_nested_entities_with_null_id() {
        Match m = new Match();
        m.setId(UUID.randomUUID());
        
        // Entités présentes mais IDs nulls
        Evenement ev = new Evenement(); ev.setId(null); m.setEvenement(ev);
        Equipe e1 = new Equipe(); e1.setId(null); m.setEquipe1(e1);
        Equipe e2 = new Equipe(); e2.setId(null); m.setEquipe2(e2);
        
        MatchResponse r = mapper.toResponse(m);
        
        assertThat(r.evenementId).isNull();
        assertThat(r.equipe1Id).isNull();
        assertThat(r.equipe2Id).isNull();
    }
}