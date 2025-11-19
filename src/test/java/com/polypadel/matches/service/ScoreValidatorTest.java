package com.polypadel.matches.service;

import com.polypadel.common.exception.BusinessException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

public class ScoreValidatorTest {

    @Test
    public void validate_accepts_null_null() {
        ScoreValidator.validate(null, null);
    }

    @Test
    public void validate_rejects_one_null() {
        assertThatThrownBy(() -> ScoreValidator.validate("6-4", null)).isInstanceOf(BusinessException.class);
    }

    @Test
    public void validate_rejects_bad_format() {
        assertThatThrownBy(() -> ScoreValidator.validate("X-4", "4-6")).isInstanceOf(BusinessException.class);
    }

    @Test
    public void validate_accepts_good() {
        ScoreValidator.validate("6-4,7-6", "4-6,6-7");
    }
}
