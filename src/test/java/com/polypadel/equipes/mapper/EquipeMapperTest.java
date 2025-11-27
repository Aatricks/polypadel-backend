package com.polypadel.equipes.mapper;

import com.polypadel.domain.entity.Equipe;
import com.polypadel.domain.entity.Joueur;
import com.polypadel.domain.entity.Poule;
import com.polypadel.equipes.dto.TeamResponse;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class EquipeMapperTest {

    private final EquipeMapper mapper = Mappers.getMapper(EquipeMapper.class);

    @Test
    public void toResponse_handles_nulls() {
        Equipe e = new Equipe();
        e.setId(UUID.randomUUID());
        TeamResponse resp = mapper.toResponse(e);
        assertThat(resp).isNotNull();
        assertThat(resp.pouleId()).isNull();
        assertThat(resp.joueur1Id()).isNull();
        assertThat(resp.joueur2Id()).isNull();
    }

    @Test
    public void toResponse_maps_nested_entities() {
        Equipe e = new Equipe();
        UUID id = UUID.randomUUID(); e.setId(id);
        Poule p = new Poule(); p.setId(UUID.randomUUID()); e.setPoule(p);
        Joueur j1 = new Joueur(); j1.setId(UUID.randomUUID()); e.setJoueur1(j1);
        Joueur j2 = new Joueur(); j2.setId(UUID.randomUUID()); e.setJoueur2(j2);
        e.setEntreprise("EntX");
        TeamResponse resp = mapper.toResponse(e);
        assertThat(resp).isNotNull();
        assertThat(resp.id()).isEqualTo(id);
        assertThat(resp.pouleId()).isEqualTo(p.getId());
        assertThat(resp.joueur1Id()).isEqualTo(j1.getId());
        assertThat(resp.joueur2Id()).isEqualTo(j2.getId());
        assertThat(resp.entreprise()).isEqualTo("EntX");
    }

    @Test
    public void toResponse_returns_null_for_null_entity() {
        TeamResponse resp = mapper.toResponse(null);
        assertThat(resp).isNull();
    }

    @Test
    public void toResponse_handles_nested_entities_with_null_id() {
        Equipe e = new Equipe();
        UUID id = UUID.randomUUID(); e.setId(id);
        Poule p = new Poule(); p.setId(null); e.setPoule(p);
        Joueur j1 = new Joueur(); j1.setId(null); e.setJoueur1(j1);
        Joueur j2 = new Joueur(); j2.setId(null); e.setJoueur2(j2);
        TeamResponse resp = mapper.toResponse(e);
        assertThat(resp).isNotNull();
        assertThat(resp.pouleId()).isNull();
        assertThat(resp.joueur1Id()).isNull();
        assertThat(resp.joueur2Id()).isNull();
    }
}
