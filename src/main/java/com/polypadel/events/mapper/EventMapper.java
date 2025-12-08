package com.polypadel.events.mapper;

import com.polypadel.domain.entity.Evenement;
import com.polypadel.events.dto.EventResponse;
import com.polypadel.matches.mapper.MatchMapper; // <--- Import indispensable
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

// On ajoute "uses = MatchMapper.class" pour qu'il sache convertir les matchs
@Mapper(componentModel = "spring", uses = {MatchMapper.class}) 
public interface EventMapper {

    // On s'assure que la liste des matchs est bien mappée
    // (Assurez-vous que votre entité Evenement a bien une liste 'matches', sinon le service devra le remplir)
    @Mapping(target = "matches", source = "matches") 
    EventResponse toResponse(Evenement entity);

    List<EventResponse> toResponseList(List<Evenement> entities);
}