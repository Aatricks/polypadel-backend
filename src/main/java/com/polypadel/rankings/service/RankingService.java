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
        // Load teams for the poule first; if none we can short‑circuit
        List<Equipe> teams = equipeRepository.findAll().stream()
                .filter(e -> e.getPoule() != null && pouleId.equals(e.getPoule().getId()))
                .toList();
        if (teams.isEmpty()) return List.of();

        List<UUID> teamIds = teams.stream().map(Equipe::getId).toList();
        List<Match> matches = matchRepository.findFinishedWithinTeams(MatchStatus.TERMINE, teamIds);

        Map<UUID, RankingRow> rowByTeam = new HashMap<>();
        for (Equipe e : teams) {
            RankingRow r = new RankingRow();
            r.teamId = e.getId();
            r.entreprise = e.getEntreprise();
            rowByTeam.put(e.getId(), r);
        }

        // Track head‑to‑head wins: h2h[a][b] = wins of a over b
        Map<UUID, Map<UUID, Integer>> h2h = new HashMap<>();

        for (Match m : matches) {
            RankingRow r1 = rowByTeam.get(m.getEquipe1().getId());
            RankingRow r2 = rowByTeam.get(m.getEquipe2().getId());
            if (r1 == null || r2 == null) continue; // defensive, should not happen
            r1.played++; r2.played++;

            int[] parsed = parseScore(m.getScore1(), m.getScore2());
            int sets1 = parsed[0], sets2 = parsed[1], games1 = parsed[2], games2 = parsed[3];

            r1.setsFor += sets1; r1.setsAgainst += sets2;
            r2.setsFor += sets2; r2.setsAgainst += sets1;
            r1.gamesFor += games1; r1.gamesAgainst += games2;
            r2.gamesFor += games2; r2.gamesAgainst += games1;

            if (sets1 > sets2) {
                r1.wins++; r2.losses++;
                h2h.computeIfAbsent(r1.teamId, k -> new HashMap<>())
                        .merge(r2.teamId, 1, (oldV, inc) -> oldV + inc);
            } else if (sets2 > sets1) {
                r2.wins++; r1.losses++;
                h2h.computeIfAbsent(r2.teamId, k -> new HashMap<>())
                        .merge(r1.teamId, 1, (oldV, inc) -> oldV + inc);
            }
        }

        List<RankingRow> rows = new ArrayList<>(rowByTeam.values());
        rows.sort((a, b) -> {
            int cmp = Integer.compare(b.points(), a.points());
            if (cmp != 0) return cmp;
            int aOverB = h2h.getOrDefault(a.teamId, Map.of()).getOrDefault(b.teamId, 0);
            int bOverA = h2h.getOrDefault(b.teamId, Map.of()).getOrDefault(a.teamId, 0);
            int h2hDiff = Integer.compare(aOverB, bOverA);
            if (h2hDiff != 0) return -h2hDiff; // more direct wins comes first
            cmp = Integer.compare(b.setDiff(), a.setDiff());
            if (cmp != 0) return cmp;
            return Integer.compare(b.gameDiff(), a.gameDiff());
        });
        return rows;
    }

    private static final Pattern SET_RE = Pattern.compile("(\\d{1,2})-(\\d{1,2})");

    /**
     * Parse score strings and return aggregated stats: [setsTeam1, setsTeam2, gamesTeam1, gamesTeam2].
     * Invalid set tokens are ignored silently; differing lengths are truncated to the shorter list.
     */
    private int[] parseScore(String s1, String s2) {
        int sets1 = 0, sets2 = 0, games1 = 0, games2 = 0;
        if (s1 == null || s2 == null) return new int[]{0,0,0,0};
        String[] A = s1.split(",");
        String[] B = s2.split(",");
        int len = Math.min(A.length, B.length);
        for (int i = 0; i < len; i++) {
            Matcher m1 = SET_RE.matcher(A[i].trim());
            Matcher m2 = SET_RE.matcher(B[i].trim());
            if (!m1.matches() || !m2.matches()) continue; // skip malformed tokens
            int gA1 = Integer.parseInt(m1.group(1));
            int gB1 = Integer.parseInt(m2.group(1));
            // decide set winner
            if (gA1 > gB1) sets1++; else if (gB1 > gA1) sets2++;
            games1 += gA1; games2 += gB1;
        }
        return new int[]{sets1, sets2, games1, games2};
    }
}
