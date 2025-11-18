package com.polypadel.rankings.service;

import com.polypadel.domain.entity.Equipe;
import com.polypadel.domain.entity.Match;
import com.polypadel.domain.enums.MatchStatus;
import com.polypadel.equipes.repository.EquipeRepository;
import com.polypadel.matches.repository.MatchRepository;
import com.polypadel.rankings.dto.RankingRow;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class RankingService {

    private final EquipeRepository equipeRepository;
    private final MatchRepository matchRepository;

    public RankingService(EquipeRepository equipeRepository, MatchRepository matchRepository) {
        this.equipeRepository = equipeRepository;
        this.matchRepository = matchRepository;
    }

    @Transactional(readOnly = true)
    public List<RankingRow> rankingForPoule(UUID pouleId) {
        List<Equipe> teams = equipeRepository.findAll().stream()
                .filter(e -> e.getPoule() != null && pouleId.equals(e.getPoule().getId()))
                .toList();
        if (teams.isEmpty()) return List.of();
        List<UUID> teamIds = teams.stream().map(Equipe::getId).toList();
        // Fetch finished matches where both teams belong to this poule
        List<Match> matches = matchRepository.findFinishedWithinTeams(MatchStatus.TERMINE, teamIds);

        Map<UUID, RankingRow> map = new HashMap<>();
        for (Equipe e : teams) {
            RankingRow r = new RankingRow();
            r.teamId = e.getId();
            r.entreprise = e.getEntreprise();
            map.put(e.getId(), r);
        }

        // Head-to-head matrix: wins[a][b] = number of wins of team a over team b
        Map<UUID, Map<UUID, Integer>> h2h = new HashMap<>();

        for (Match m : matches) {
            RankingRow r1 = map.get(m.getEquipe1().getId());
            RankingRow r2 = map.get(m.getEquipe2().getId());
            r1.played++; r2.played++;
            // parse scores
            int[] sets = setsWon(m.getScore1(), m.getScore2());
            r1.setsFor += sets[0];
            r1.setsAgainst += sets[1];
            r2.setsFor += sets[1];
            r2.setsAgainst += sets[0];
            int[] games = gamesTotals(m.getScore1(), m.getScore2());
            r1.gamesFor += games[0];
            r1.gamesAgainst += games[1];
            r2.gamesFor += games[1];
            r2.gamesAgainst += games[0];
            // decide winner by sets
            if (sets[0] > sets[1]) {
                r1.wins++; r2.losses++;
                h2h.computeIfAbsent(r1.teamId, k -> new HashMap<>())
                        .merge(r2.teamId, 1, Integer::sum);
            }
            else if (sets[1] > sets[0]) {
                r2.wins++; r1.losses++;
                h2h.computeIfAbsent(r2.teamId, k -> new HashMap<>())
                        .merge(r1.teamId, 1, Integer::sum);
            }
        }

        List<RankingRow> rows = new ArrayList<>(map.values());
        // Custom comparator: points desc -> head-to-head (when exactly two-team tie) -> set diff desc -> game diff desc
        rows.sort((a, b) -> {
            int c = Integer.compare(b.points(), a.points());
            if (c != 0) return c;

            // If exactly two teams tied on points (within this compare scope), try head-to-head
            int aOverB = h2h.getOrDefault(a.teamId, Map.of()).getOrDefault(b.teamId, 0);
            int bOverA = h2h.getOrDefault(b.teamId, Map.of()).getOrDefault(a.teamId, 0);
            int h2hDiff = Integer.compare(aOverB, bOverA);
            if (h2hDiff != 0) return -h2hDiff; // winner (more wins) should come first

            c = Integer.compare(b.setDiff(), a.setDiff());
            if (c != 0) return c;
            return Integer.compare(b.gameDiff(), a.gameDiff());
        });
        return rows;
    }

    private static final Pattern SET_RE = Pattern.compile("(\\d{1,2})-(\\d{1,2})");

    private int[] setsWon(String s1, String s2) {
        int a = 0, b = 0;
        if (s1 == null || s2 == null) return new int[]{0, 0};
        String[] A = s1.split(",");
        String[] B = s2.split(",");
        int len = Math.min(A.length, B.length);
        for (int i = 0; i < len; i++) {
            Matcher m1 = SET_RE.matcher(A[i].trim());
            Matcher m2 = SET_RE.matcher(B[i].trim());
            if (m1.matches() && m2.matches()) {
                int a1 = Integer.parseInt(m1.group(1));
                int a2 = Integer.parseInt(m2.group(1));
                if (a1 > a2) a++; else if (a2 > a1) b++;
            }
        }
        return new int[]{a, b};
    }

    private int[] gamesTotals(String s1, String s2) {
        int g1 = 0, g2 = 0;
        if (s1 == null || s2 == null) return new int[]{0, 0};
        String[] A = s1.split(",");
        String[] B = s2.split(",");
        int len = Math.min(A.length, B.length);
        for (int i = 0; i < len; i++) {
            Matcher m1 = SET_RE.matcher(A[i].trim());
            Matcher m2 = SET_RE.matcher(B[i].trim());
            if (m1.matches() && m2.matches()) {
                g1 += Integer.parseInt(m1.group(1));
                g2 += Integer.parseInt(m2.group(1));
            }
        }
        return new int[]{g1, g2};
    }
}
