package com.polypadel.service;

import com.polypadel.dto.*;
import com.polypadel.model.*;
import com.polypadel.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

@Service
public class ResultsService {
    private final MatchRepository matchRepository;
    private final PlayerRepository playerRepository;
    private final TeamRepository teamRepository;

    public ResultsService(MatchRepository matchRepository, PlayerRepository playerRepository, 
                         TeamRepository teamRepository) {
        this.matchRepository = matchRepository;
        this.playerRepository = playerRepository;
        this.teamRepository = teamRepository;
    }

    public MyResultsResponse getMyResults(User user) {
        Player player = playerRepository.findByUserId(user.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profil joueur non trouvé"));

        List<Team> myTeams = teamRepository.findByPlayerId(player.getId());
        if (myTeams.isEmpty()) {
            return new MyResultsResponse(List.of(), new MyResultsResponse.Statistics(0, 0, 0, 0.0));
        }

        List<MyResultResponse> results = new ArrayList<>();
        int wins = 0, losses = 0;

        for (Team myTeam : myTeams) {
            List<Match> completedMatches = matchRepository.findCompletedByTeamId(myTeam.getId());
            
            for (Match match : completedMatches) {
                boolean isTeam1 = match.getTeam1().getId().equals(myTeam.getId());
                Team opponentTeam = isTeam1 ? match.getTeam2() : match.getTeam1();
                String myScore = isTeam1 ? match.getScoreTeam1() : match.getScoreTeam2();
                String opponentScore = isTeam1 ? match.getScoreTeam2() : match.getScoreTeam1();

                boolean won = determineWinner(myScore, opponentScore);
                if (won) wins++; else losses++;

                List<String> opponentPlayers = new ArrayList<>();
                if (opponentTeam.getPlayer1() != null) {
                    opponentPlayers.add(opponentTeam.getPlayer1().getFirstName() + " " + 
                                       opponentTeam.getPlayer1().getLastName());
                }
                if (opponentTeam.getPlayer2() != null) {
                    opponentPlayers.add(opponentTeam.getPlayer2().getFirstName() + " " + 
                                       opponentTeam.getPlayer2().getLastName());
                }

                results.add(new MyResultResponse(
                    match.getId(),
                    match.getEvent().getEventDate(),
                    new MyResultResponse.Opponents(opponentTeam.getCompany(), opponentPlayers),
                    myScore,
                    won ? "VICTOIRE" : "DEFAITE",
                    match.getCourtNumber()
                ));
            }
        }

        int total = wins + losses;
        double winRate = total > 0 ? (double) wins / total * 100 : 0.0;

        return new MyResultsResponse(results, new MyResultsResponse.Statistics(total, wins, losses, winRate));
    }

    private boolean determineWinner(String myScore, String opponentScore) {
        if (myScore == null || opponentScore == null) return false;
        String[] mySets = myScore.split(",\\s*");
        String[] oppSets = opponentScore.split(",\\s*");
        int myWins = 0, oppWins = 0;
        for (int i = 0; i < mySets.length && i < oppSets.length; i++) {
            int myGames = Integer.parseInt(mySets[i].split("-")[0].trim());
            int oppGames = Integer.parseInt(oppSets[i].split("-")[0].trim());
            if (myGames > oppGames) myWins++;
            else oppWins++;
        }
        return myWins > oppWins;
    }
}
