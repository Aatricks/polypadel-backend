package com.polypadel.joueurs.mapper;

import com.polypadel.domain.entity.Joueur;
import com.polypadel.joueurs.dto.PlayerResponse;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class JoueurMapperImplTest {
    private final JoueurMapper mapper = Mappers.getMapper(JoueurMapper.class);

    @Test
    public void toResponse_maps_fields() {
        Joueur j = new Joueur(); j.setId(UUID.randomUUID()); j.setNom("A"); j.setPrenom("B"); j.setDateNaissance(LocalDate.of(1990,1,1));
        PlayerResponse r = mapper.toResponse(j);
        assertThat(r.id()).isEqualTo(j.getId());
        assertThat(r.nom()).isEqualTo("A");
    }
}
