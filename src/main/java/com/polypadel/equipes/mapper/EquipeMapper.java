package com.polypadel.equipes.mapper;

import com.polypadel.domain.entity.Equipe;
import com.polypadel.equipes.dto.TeamResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EquipeMapper {
    @Mapping(source = "poule.id", target = "pouleId")
    @Mapping(source = "joueur1.id", target = "joueur1Id")
    @Mapping(source = "joueur2.id", target = "joueur2Id")
    TeamResponse toResponse(Equipe entity);
}
