package com.polypadel.matches.mapper;

import com.polypadel.domain.entity.Evenement;
import com.polypadel.domain.entity.Equipe;
import com.polypadel.domain.entity.Match;
import com.polypadel.domain.enums.MatchStatus;
import com.polypadel.matches.dto.MatchResponse;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class MatchMapperImplTest {
    private final MatchMapper mapper = Mappers.getMapper(MatchMapper.class);

    @Test
    public void toResponse_maps_fields() {
        Match m = new Match();
        UUID id = UUID.randomUUID(); m.setId(id);
        Evenement ev = new Evenement(); ev.setId(UUID.randomUUID()); m.setEvenement(ev);
        Equipe e1 = new Equipe(); e1.setId(UUID.randomUUID()); m.setEquipe1(e1);
        Equipe e2 = new Equipe(); e2.setId(UUID.randomUUID()); m.setEquipe2(e2);
        m.setPiste(2);
        m.setStartTime(LocalTime.of(9, 30));
        m.setStatut(MatchStatus.A_VENIR);
        MatchResponse r = mapper.toResponse(m);
        assertThat(r.evenementId()).isEqualTo(ev.getId());
        assertThat(r.equipe1Id()).isEqualTo(e1.getId());
        assertThat(r.piste()).isEqualTo(2);
        assertThat(r.startTime()).isEqualTo(LocalTime.of(9, 30));
        assertThat(r.statut()).isEqualTo(MatchStatus.A_VENIR);
    }
}
