package com.polypadel.matches.mapper;

import com.polypadel.domain.entity.Evenement;
import com.polypadel.domain.entity.Equipe;
import com.polypadel.domain.entity.Match;
import com.polypadel.domain.enums.MatchStatus;
import com.polypadel.matches.dto.MatchResponse;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class MatchMapperImplTest {
    
    private final MatchMapper mapper = Mappers.getMapper(MatchMapper.class);

    @Test
    public void toResponse_maps_fields_and_calculates_datetime() {
        // Given
        UUID eventId = UUID.randomUUID();
        UUID team1Id = UUID.randomUUID();
        
        // 1. Configurer l'événement avec une DATE (Indispensable pour le calcul)
        Evenement ev = new Evenement(); 
        ev.setId(eventId);
        ev.setEventDate(LocalDate.of(2025, 11, 15)); // Date fixe pour le test
        
        Equipe e1 = new Equipe(); e1.setId(team1Id);
        Equipe e2 = new Equipe(); e2.setId(UUID.randomUUID());
        
        Match m = new Match();
        m.setId(UUID.randomUUID());
        m.setEvenement(ev);
        m.setEquipe1(e1);
        m.setEquipe2(e2);
        m.setPiste(2);
        
        // 2. Configurer le match avec une HEURE
        m.setStartTime(LocalTime.of(9, 30));
        m.setStatut(MatchStatus.A_VENIR);

        // When
        MatchResponse r = mapper.toResponse(m);

        // Then
        // (Adaptez r.evenementId ou r.evenementId() selon que vous utilisez Class ou Record)
        assertThat(r.evenementId).isEqualTo(eventId);
        assertThat(r.equipe1Id).isEqualTo(team1Id);
        assertThat(r.piste).isEqualTo(2);
        
        // 3. VÉRIFICATION CLÉ : Date + Heure combinées
        // On s'attend à 2025-11-15T09:30:00
        assertThat(r.startTime).isEqualTo(LocalDateTime.of(2025, 11, 15, 9, 30));
        
        assertThat(r.statut).isEqualTo(MatchStatus.A_VENIR);
    }
}