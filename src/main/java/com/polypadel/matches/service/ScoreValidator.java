package com.polypadel.matches.service;

import com.polypadel.common.exception.BusinessException;

import java.util.regex.Pattern;

public class ScoreValidator {
    // Simple pattern like 6-4 or 7-6, sets separated by commas
    private static final Pattern SET_PATTERN = Pattern.compile("^\\d{1,2}-\\d{1,2}(,\\d{1,2}-\\d{1,2})*$");

    public static void validate(String score1, String score2) {
        if (score1 == null && score2 == null) return; // nothing to validate
        if (score1 == null || score2 == null) {
            throw new BusinessException("INVALID_SCORE", "Both scores must be provided");
        }
        if (!SET_PATTERN.matcher(score1).matches() || !SET_PATTERN.matcher(score2).matches()) {
            throw new BusinessException("INVALID_SCORE", "Scores must be in format '6-4,7-5'");
        }
    }
}
