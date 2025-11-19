package com.polypadel.matches.mapper;

import com.polypadel.domain.entity.Match;
import com.polypadel.matches.dto.MatchResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MatchMapper {
    @Mapping(source = "evenement.id", target = "evenementId")
    @Mapping(source = "equipe1.id", target = "equipe1Id")
    @Mapping(source = "equipe2.id", target = "equipe2Id")
    MatchResponse toResponse(Match entity);
}
