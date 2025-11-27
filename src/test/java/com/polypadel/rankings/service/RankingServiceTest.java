package com.polypadel.rankings.service;

import com.polypadel.domain.entity.Evenement;
import com.polypadel.domain.entity.Equipe;
import com.polypadel.domain.entity.Match;
import com.polypadel.domain.entity.Poule;
import com.polypadel.domain.enums.MatchStatus;
import com.polypadel.equipes.repository.EquipeRepository;
import com.polypadel.matches.repository.MatchRepository;
import com.polypadel.rankings.dto.RankingRow;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

public class RankingServiceTest {

    @Test
    public void parseScore_and_rank_simple() throws Exception {
        EquipeRepository equipeRepository = mock(EquipeRepository.class);
        MatchRepository matchRepository = mock(MatchRepository.class);
        RankingService service = new RankingService(equipeRepository, matchRepository);

        // Test parseScore via reflection (private method)
        Method parse = RankingService.class.getDeclaredMethod("parseScore", String.class, String.class);
        parse.setAccessible(true);
        int[] res = (int[]) parse.invoke(service, "6-4,6-4", "4-6,4-6");
        assertThat(res).containsExactly(2, 0, 12, 8);

        // empty team listing leads to empty ranking
        when(equipeRepository.findAll()).thenReturn(List.of());
        var ranking = service.rankingForPoule(UUID.randomUUID());
        assertThat(ranking).isEmpty();

        // Now build a non-empty scenario with 2 teams and a finished match
        UUID pouleId = UUID.randomUUID();
        Poule poule = new Poule(); poule.setId(pouleId);

        Equipe t1 = new Equipe(); t1.setId(UUID.randomUUID()); t1.setPoule(poule); t1.setEntreprise("EntA");
        Equipe t2 = new Equipe(); t2.setId(UUID.randomUUID()); t2.setPoule(poule); t2.setEntreprise("EntA");

        when(equipeRepository.findAll()).thenReturn(List.of(t1, t2));

        Match m = new Match();
        m.setId(UUID.randomUUID());
        m.setEvenement(new Evenement());
        m.setEquipe1(t1);
        m.setEquipe2(t2);
        m.setPiste(1);
        m.setStartTime(LocalTime.of(10, 0));
        m.setStatut(MatchStatus.TERMINE);
        m.setScore1("6-4,6-4");
        m.setScore2("4-6,4-6");

        when(matchRepository.findFinishedWithinTeams(MatchStatus.TERMINE, List.of(t1.getId(), t2.getId()))).thenReturn(List.of(m));

        List<RankingRow> result = service.rankingForPoule(pouleId);
        assertThat(result).hasSize(2);
        assertThat(result.get(0).teamId()).isEqualTo(t1.getId());
        assertThat(result.get(0).wins()).isEqualTo(1);
        assertThat(result.get(0).setsFor()).isEqualTo(2);
        assertThat(result.get(0).gamesFor()).isEqualTo(12);
    }

    @Test
    public void parseScore_null_and_malformed_tokens() throws Exception {
        EquipeRepository equipeRepository = mock(EquipeRepository.class);
        MatchRepository matchRepository = mock(MatchRepository.class);
        RankingService service = new RankingService(equipeRepository, matchRepository);
        Method parse = RankingService.class.getDeclaredMethod("parseScore", String.class, String.class);
        parse.setAccessible(true);

        int[] resNull = (int[]) parse.invoke(service, null, null);
        assertThat(resNull).containsExactly(0, 0, 0, 0);

        int[] resMalformed = (int[]) parse.invoke(service, "6-4,XX", "4-6,6-3");
        // first token valid -> counts toward totals; second token invalid in first string -> ignored
        assertThat(resMalformed).containsExactly(1, 0, 6, 4);
    }

    @Test
    public void ranking_h2h_setDiff_and_gameDiff_tiebreakers() {
        EquipeRepository equipeRepository = mock(EquipeRepository.class);
        MatchRepository matchRepository = mock(MatchRepository.class);
        RankingService service = new RankingService(equipeRepository, matchRepository);

        UUID pouleId = UUID.randomUUID();
        Poule p = new Poule(); p.setId(pouleId);

        // Tiebreak by H2H (t2 should beat t1 head-to-head)
        Equipe t1 = new Equipe(); t1.setId(UUID.randomUUID()); t1.setPoule(p); t1.setEntreprise("Ent");
        Equipe t2 = new Equipe(); t2.setId(UUID.randomUUID()); t2.setPoule(p); t2.setEntreprise("Ent");
        Equipe t3 = new Equipe(); t3.setId(UUID.randomUUID()); t3.setPoule(p); t3.setEntreprise("Ent");
        Equipe t4 = new Equipe(); t4.setId(UUID.randomUUID()); t4.setPoule(p); t4.setEntreprise("Ent");

        when(equipeRepository.findAll()).thenReturn(List.of(t1, t2, t3, t4));

        Match m13 = new Match(); m13.setEquipe1(t1); m13.setEquipe2(t3); m13.setScore1("6-4,6-4"); m13.setScore2("4-6,4-6"); m13.setStatut(MatchStatus.TERMINE);
        Match m14 = new Match(); m14.setEquipe1(t1); m14.setEquipe2(t4); m14.setScore1("6-4,6-4"); m14.setScore2("4-6,4-6"); m14.setStatut(MatchStatus.TERMINE);
        Match m23 = new Match(); m23.setEquipe1(t2); m23.setEquipe2(t3); m23.setScore1("6-4,6-4"); m23.setScore2("4-6,4-6"); m23.setStatut(MatchStatus.TERMINE);
        Match m21 = new Match(); m21.setEquipe1(t2); m21.setEquipe2(t1); m21.setScore1("6-4,6-4"); m21.setScore2("4-6,4-6"); m21.setStatut(MatchStatus.TERMINE);

        when(matchRepository.findFinishedWithinTeams(eq(MatchStatus.TERMINE), any())).thenReturn(List.of(m13, m14, m23, m21));

        var ranking = service.rankingForPoule(pouleId);
        // t1 and t2 have 2 wins each, but t2 beat t1 head-to-head -> t2 should be ahead
        assertThat(ranking.get(0).teamId()).isEqualTo(t2.getId());

        // Now test setDiff tiebreaker
        Equipe a = new Equipe(); a.setId(UUID.randomUUID()); a.setPoule(p); a.setEntreprise("Ent");
        Equipe b = new Equipe(); b.setId(UUID.randomUUID()); b.setPoule(p); b.setEntreprise("Ent");
        Equipe c = new Equipe(); c.setId(UUID.randomUUID()); c.setPoule(p); c.setEntreprise("Ent");
        when(equipeRepository.findAll()).thenReturn(List.of(a, b, c));
        Match mA = new Match(); mA.setEquipe1(a); mA.setEquipe2(c); mA.setScore1("6-4,6-4"); mA.setScore2("4-6,4-6"); mA.setStatut(MatchStatus.TERMINE);
        Match mB = new Match(); mB.setEquipe1(b); mB.setEquipe2(c); mB.setScore1("6-4"); mB.setScore2("4-6"); mB.setStatut(MatchStatus.TERMINE);
        when(matchRepository.findFinishedWithinTeams(eq(MatchStatus.TERMINE), any())).thenReturn(List.of(mA, mB));
        var rankingSet = service.rankingForPoule(pouleId);
        // a and b have 1 win each, but a has higher setDiff -> a ahead
        assertThat(rankingSet.get(0).teamId()).isEqualTo(a.getId());

        // Now test gameDiff tiebreaker (equal sets, different games)
        Equipe g1 = new Equipe(); g1.setId(UUID.randomUUID()); g1.setPoule(p); g1.setEntreprise("Ent");
        Equipe g2 = new Equipe(); g2.setId(UUID.randomUUID()); g2.setPoule(p); g2.setEntreprise("Ent");
        Equipe g3 = new Equipe(); g3.setId(UUID.randomUUID()); g3.setPoule(p); g3.setEntreprise("Ent");
        when(equipeRepository.findAll()).thenReturn(List.of(g1, g2, g3));
        Match mg1 = new Match(); mg1.setEquipe1(g1); mg1.setEquipe2(g3); mg1.setScore1("6-0,1-6,6-4"); mg1.setScore2("0-6,6-1,4-6"); mg1.setStatut(MatchStatus.TERMINE);
        Match mg2 = new Match(); mg2.setEquipe1(g2); mg2.setEquipe2(g3); mg2.setScore1("6-4,4-6,6-4"); mg2.setScore2("4-6,6-4,4-6"); mg2.setStatut(MatchStatus.TERMINE);
        when(matchRepository.findFinishedWithinTeams(eq(MatchStatus.TERMINE), any())).thenReturn(List.of(mg1, mg2));
        var rankingGame = service.rankingForPoule(pouleId);
        // g1 and g2 both have 1 win and equal setDiff; g1 has higher gamesFor -> g1 above g2
        assertThat(rankingGame.get(0).teamId()).isEqualTo(g1.getId());
    }

    @Test
    public void ranking_ignores_matches_with_outside_team() {
        EquipeRepository equipeRepository = mock(EquipeRepository.class);
        MatchRepository matchRepository = mock(MatchRepository.class);
        RankingService service = new RankingService(equipeRepository, matchRepository);

        UUID pouleId = UUID.randomUUID();
        Poule p = new Poule(); p.setId(pouleId);
        Equipe t1 = new Equipe(); t1.setId(UUID.randomUUID()); t1.setPoule(p);
        Equipe t2 = new Equipe(); t2.setId(UUID.randomUUID()); t2.setPoule(p);
        Equipe outside = new Equipe(); outside.setId(UUID.randomUUID()); // not in poule

        when(equipeRepository.findAll()).thenReturn(List.of(t1, t2));
        Match m = new Match(); m.setEquipe1(t1); m.setEquipe2(outside); m.setScore1("6-4"); m.setScore2("4-6"); m.setStatut(MatchStatus.TERMINE);
        when(matchRepository.findFinishedWithinTeams(eq(MatchStatus.TERMINE), any())).thenReturn(List.of(m));

        var ranking = service.rankingForPoule(pouleId);
        // match should be ignored so both teams have 0 wins
        assertThat(ranking.get(0).wins()).isEqualTo(0);
        assertThat(ranking.get(1).wins()).isEqualTo(0);
    }

    @Test
    public void ranking_h2h_merge_lambda_invoked_on_multiple_wins() {
        EquipeRepository equipeRepository = mock(EquipeRepository.class);
        MatchRepository matchRepository = mock(MatchRepository.class);
        RankingService service = new RankingService(equipeRepository, matchRepository);

        UUID pouleId = UUID.randomUUID();
        Poule p = new Poule(); p.setId(pouleId);

        Equipe t1 = new Equipe(); t1.setId(UUID.randomUUID()); t1.setPoule(p);
        Equipe t2 = new Equipe(); t2.setId(UUID.randomUUID()); t2.setPoule(p);
        when(equipeRepository.findAll()).thenReturn(List.of(t1, t2));

        Match m1 = new Match(); m1.setEquipe1(t2); m1.setEquipe2(t1); m1.setScore1("6-4,6-4"); m1.setScore2("4-6,4-6"); m1.setStatut(MatchStatus.TERMINE);
        Match m2 = new Match(); m2.setEquipe1(t2); m2.setEquipe2(t1); m2.setScore1("6-4,6-4"); m2.setScore2("4-6,4-6"); m2.setStatut(MatchStatus.TERMINE);
        when(matchRepository.findFinishedWithinTeams(eq(MatchStatus.TERMINE), any())).thenReturn(List.of(m1, m2));

        var ranking = service.rankingForPoule(pouleId);
        assertThat(ranking.get(0).teamId()).isEqualTo(t2.getId());
        assertThat(ranking.get(0).wins()).isEqualTo(2);
    }
}
