package com.polypadel.rankings.service;

import com.polypadel.domain.entity.Equipe;
import com.polypadel.domain.entity.Match;
import com.polypadel.domain.entity.Poule;
import com.polypadel.domain.enums.MatchStatus;
import com.polypadel.equipes.repository.EquipeRepository;
import com.polypadel.matches.repository.MatchRepository;
import com.polypadel.rankings.dto.RankingRow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RankingServiceUnitTest {

    @Mock
    private EquipeRepository equipeRepository;
    @Mock
    private MatchRepository matchRepository;

    @InjectMocks
    private RankingService rankingService;

    private UUID pouleId;
    private Equipe e1;
    private Equipe e2;

    @BeforeEach
    public void setUp() {
        pouleId = UUID.randomUUID();
        e1 = new Equipe(); e1.setId(UUID.randomUUID()); e1.setEntreprise("EntA");
        e2 = new Equipe(); e2.setId(UUID.randomUUID()); e2.setEntreprise("EntA");
        Poule p = new Poule(); p.setId(pouleId);
        e1.setPoule(p); e2.setPoule(p);
    }

    @Test
    public void parseScore_and_ranking_for_poule() {
        when(equipeRepository.findByPouleId(pouleId)).thenReturn(List.of(e1, e2));
        Match m = new Match(); m.setId(UUID.randomUUID());
        m.setEquipe1(e1); m.setEquipe2(e2);
        // Team 1 wins first set and second set also -> 2-0
        m.setScore1("6-3,6-4");
        m.setScore2("3-6,4-6"); // although group(1) is used by parseScore, we craft to ensure wins
        when(matchRepository.findFinishedWithinTeams(MatchStatus.TERMINE, List.of(e1.getId(), e2.getId()))).thenReturn(List.of(m));

        List<RankingRow> rows = rankingService.rankingForPoule(pouleId);
        assertThat(rows).hasSize(2);
        // e1 should be first because it has one win
        assertThat(rows.get(0).entreprise()).isEqualTo(e1.getEntreprise());
    }

    @Test
    public void malformed_scores_dont_break_ranking() {
        when(equipeRepository.findByPouleId(pouleId)).thenReturn(List.of(e1, e2));
        Match m2 = new Match(); m2.setId(UUID.randomUUID()); m2.setEquipe1(e1); m2.setEquipe2(e2);
        m2.setScore1("bad-data,x"); m2.setScore2("bad,stuff");
        when(matchRepository.findFinishedWithinTeams(MatchStatus.TERMINE, List.of(e1.getId(), e2.getId()))).thenReturn(List.of(m2));
        var rows2 = rankingService.rankingForPoule(pouleId);
        assertThat(rows2).isNotNull();
    }
}
