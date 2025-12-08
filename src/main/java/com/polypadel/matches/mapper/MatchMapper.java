package com.polypadel.matches.mapper;

import com.polypadel.domain.entity.Equipe;
import com.polypadel.domain.entity.Joueur;
import com.polypadel.domain.entity.Match;
import com.polypadel.matches.dto.MatchResponse;
import com.polypadel.matches.dto.TeamDto;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface MatchMapper {

    @Mapping(source = "evenement.id", target = "evenementId")
    @Mapping(source = "equipe1.id", target = "equipe1Id")
    @Mapping(source = "equipe2.id", target = "equipe2Id")
    @Mapping(target = "startTime", ignore = true)
    MatchResponse toResponse(Match entity);

    @AfterMapping
    default void enrichResponse(Match match, @MappingTarget MatchResponse response) {
        // 1. Calcul de la Date
        if (match.getEvenement() != null && match.getEvenement().getEventDate() != null && match.getStartTime() != null) {
            response.startTime = match.getEvenement().getEventDate().atTime(match.getStartTime());
        }

        // 2. Mapping des Équipes (Entreprise + Joueurs)
        if (match.getEquipe1() != null) {
            response.team1 = mapTeam(match.getEquipe1());
        }
        if (match.getEquipe2() != null) {
            response.team2 = mapTeam(match.getEquipe2());
        }
    }

    // Méthode utilitaire pour convertir une Equipe Entity -> TeamDto
    default TeamDto mapTeam(Equipe equipe) {
        List<TeamDto.PlayerDto> players = List.of(
            new TeamDto.PlayerDto(equipe.getJoueur1().getPrenom(), equipe.getJoueur1().getNom()),
            new TeamDto.PlayerDto(equipe.getJoueur2().getPrenom(), equipe.getJoueur2().getNom())
        );
        return new TeamDto(equipe.getEntreprise(), players);
    }
}