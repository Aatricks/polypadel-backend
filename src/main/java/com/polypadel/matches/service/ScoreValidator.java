package com.polypadel.matches.service;

import com.polypadel.common.exception.BusinessException;

import java.util.regex.Pattern;

public class ScoreValidator {

    // Regex conforme à la spec 3.4.2 : 2 ou 3 sets, avec gestion des espaces
    // Format : "6-4, 6-3" ou "6-4, 4-6, 6-2"
    private static final Pattern SCORE_FORMAT = Pattern.compile("^(\\d+-\\d+)(,\\s*\\d+-\\d+){1,2}$");

    public static void validate(String score1, String score2) {
        // Au moins l'un des deux scores doit être présent (ou les deux selon votre logique)
        if (score1 == null && score2 == null) {
            throw new BusinessException("INVALID_SCORE", "Au moins un score doit être fourni");
        }

        if (score1 != null) validateSingleScore(score1);
        if (score2 != null) validateSingleScore(score2);
    }

    private static void validateSingleScore(String score) {
        // 1. Validation du Format (Regex)
        if (!SCORE_FORMAT.matcher(score).matches()) {
            throw new BusinessException("INVALID_SCORE_FORMAT", 
                "Le score doit être au format 'X-Y, X-Y' (ex: '6-4, 6-3'). Reçu: " + score);
        }

        // 2. Validation des Règles du Tennis/Padel (Logique Métier)
        String[] sets = score.split(",");
        for (String set : sets) {
            validateSetRules(set.trim());
        }
    }

    private static void validateSetRules(String set) {
        String[] games = set.split("-");
        int g1 = Integer.parseInt(games[0]);
        int g2 = Integer.parseInt(games[1]);

        int winner = Math.max(g1, g2);
        int loser = Math.min(g1, g2);

        // Règle: Le vainqueur doit avoir au moins 6 jeux [cite: 244]
        if (winner < 6) {
            throw new BusinessException("INVALID_SET_SCORE", 
                "Un set doit aller au moins jusqu'à 6 jeux. Score invalide: " + set);
        }

        // Règle: Pas de set > 7 (sauf super tie-break non géré ici, on reste standard)
        if (winner > 7) {
            throw new BusinessException("INVALID_SET_SCORE", 
                "Score impossible (max 7 jeux). Score invalide: " + set);
        }

        // Règle: Si 6 jeux, l'écart doit être au moins de 2 (donc 6-0, 6-1, 6-2, 6-3, 6-4)
        // 6-5 est interdit (doit aller à 7)
        if (winner == 6 && loser > 4) {
            throw new BusinessException("INVALID_SET_SCORE", 
                "À 6-5, le set doit continuer. Score invalide: " + set);
        }

        // Règle: Si 7 jeux, le perdant doit avoir 5 ou 6 [cite: 245-246]
        // 7-0, 7-1... 7-4 sont interdits (auraient dû finir à 6-...)
        if (winner == 7 && loser < 5) {
            throw new BusinessException("INVALID_SET_SCORE", 
                "Un score de 7-" + loser + " est impossible (le match aurait dû finir à 6-" + loser + ")");
        }
    }
}