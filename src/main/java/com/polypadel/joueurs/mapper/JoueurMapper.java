package com.polypadel.joueurs.mapper;

import com.polypadel.domain.entity.Joueur;
import com.polypadel.joueurs.dto.PlayerResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface JoueurMapper {
    PlayerResponse toResponse(Joueur entity);
}
