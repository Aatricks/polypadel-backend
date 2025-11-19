package com.polypadel.equipes.mapper;

import com.polypadel.domain.entity.Equipe;
import com.polypadel.domain.entity.Joueur;
import com.polypadel.domain.entity.Poule;
import com.polypadel.equipes.dto.TeamResponse;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class EquipeMapperImplTest {
    private final EquipeMapper mapper = Mappers.getMapper(EquipeMapper.class);

    @Test
    public void toResponse_maps_fields() {
        Equipe e = new Equipe();
        UUID id = UUID.randomUUID(); e.setId(id);
        e.setEntreprise("EntX");
        Joueur j1 = new Joueur(); j1.setId(UUID.randomUUID()); e.setJoueur1(j1);
        Joueur j2 = new Joueur(); j2.setId(UUID.randomUUID()); e.setJoueur2(j2);
        Poule p = new Poule(); p.setId(UUID.randomUUID()); e.setPoule(p);
        TeamResponse resp = mapper.toResponse(e);
        assertThat(resp.id()).isEqualTo(id);
        assertThat(resp.pouleId()).isEqualTo(p.getId());
        assertThat(resp.joueur1Id()).isEqualTo(j1.getId());
        assertThat(resp.joueur2Id()).isEqualTo(j2.getId());
        assertThat(resp.entreprise()).isEqualTo("EntX");
    }
}
