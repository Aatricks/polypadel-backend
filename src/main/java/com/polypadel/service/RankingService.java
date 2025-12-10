package com.polypadel.service;

import com.polypadel.dto.RankingRow;
import com.polypadel.model.*;
import com.polypadel.repository.*;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class RankingService {
    private final MatchRepository matchRepository;
    private final TeamRepository teamRepository;

    public RankingService(MatchRepository matchRepository, TeamRepository teamRepository) {
        this.matchRepository = matchRepository;
        this.teamRepository = teamRepository;
    }

    public List<RankingRow> getRankings() {
        List<Match> completedMatches = matchRepository.findByStatus(MatchStatus.TERMINE);
        Map<String, Stats> companyStats = new HashMap<>();

        for (Match match : completedMatches) {
            String company1 = match.getTeam1().getCompany();
            String company2 = match.getTeam2().getCompany();
            companyStats.putIfAbsent(company1, new Stats());
            companyStats.putIfAbsent(company2, new Stats());

            Stats s1 = companyStats.get(company1);
            Stats s2 = companyStats.get(company2);
            s1.matchesPlayed++;
            s2.matchesPlayed++;

            int[] sets = parseScore(match.getScoreTeam1(), match.getScoreTeam2());
            s1.setsWon += sets[0];
            s1.setsLost += sets[1];
            s2.setsWon += sets[1];
            s2.setsLost += sets[0];

            if (sets[0] > sets[1]) {
                s1.wins++;
                s1.points += 3;
                s2.losses++;
            } else {
                s2.wins++;
                s2.points += 3;
                s1.losses++;
            }
        }

        List<Map.Entry<String, Stats>> sorted = new ArrayList<>(companyStats.entrySet());
        sorted.sort((a, b) -> {
            int cmp = Integer.compare(b.getValue().points, a.getValue().points);
            if (cmp != 0) return cmp;
            cmp = Integer.compare(b.getValue().wins, a.getValue().wins);
            if (cmp != 0) return cmp;
            int diff1 = a.getValue().setsWon - a.getValue().setsLost;
            int diff2 = b.getValue().setsWon - b.getValue().setsLost;
            cmp = Integer.compare(diff2, diff1);
            if (cmp != 0) return cmp;
            return a.getKey().compareTo(b.getKey());
        });

        List<RankingRow> result = new ArrayList<>();
        int pos = 1;
        for (Map.Entry<String, Stats> entry : sorted) {
            Stats s = entry.getValue();
            result.add(new RankingRow(pos++, entry.getKey(), s.matchesPlayed, s.wins, s.losses, s.points, s.setsWon, s.setsLost));
        }
        return result;
    }

    private int[] parseScore(String score1, String score2) {
        if (score1 == null || score2 == null) return new int[]{0, 0};
        String[] sets1 = score1.split(",\\s*");
        String[] sets2 = score2.split(",\\s*");
        int won1 = 0, won2 = 0;
        for (int i = 0; i < sets1.length; i++) {
            String[] parts1 = sets1[i].split("-");
            String[] parts2 = sets2[i].split("-");
            int g1 = Integer.parseInt(parts1[0].trim());
            int g2 = Integer.parseInt(parts2[0].trim());
            if (g1 > g2) won1++;
            else won2++;
        }
        return new int[]{won1, won2};
    }

    private static class Stats {
        int matchesPlayed, wins, losses, points, setsWon, setsLost;
    }
}
