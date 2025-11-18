package com.polypadel.matches;

import com.polypadel.common.exception.BusinessException;
import com.polypadel.matches.service.ScoreValidator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ScoreValidatorTest {

    @Test
    void validScoresPass() {
        assertDoesNotThrow(() -> ScoreValidator.validate("6-4,7-6", "4-6,6-7"));
        assertDoesNotThrow(() -> ScoreValidator.validate(null, null));
        assertDoesNotThrow(() -> ScoreValidator.validate("10-8", "8-10"));
    }

    @Test
    void invalidScoresFail() {
        assertThrows(BusinessException.class, () -> ScoreValidator.validate("6-4", null));
        assertThrows(BusinessException.class, () -> ScoreValidator.validate("6-4,7-6x", "4-6,6-7"));
        assertThrows(BusinessException.class, () -> ScoreValidator.validate("", ""));
        assertThrows(BusinessException.class, () -> ScoreValidator.validate("6-4,", "4-6,"));
    }
}
