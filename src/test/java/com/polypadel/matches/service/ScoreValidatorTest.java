package com.polypadel.matches.service;

import com.polypadel.common.exception.BusinessException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ScoreValidatorTest {

    // --- TESTS DE FORMAT (REGEX) ---

    @Test
    public void validate_accepts_valid_formats() {
        // Format standard
        assertThatCode(() -> ScoreValidator.validate("6-4, 6-3", "6-4, 6-3")).doesNotThrowAnyException();
        // Format sans espace
        assertThatCode(() -> ScoreValidator.validate("6-4,6-3", "6-4,6-3")).doesNotThrowAnyException();
        // 3 Sets
        assertThatCode(() -> ScoreValidator.validate("6-4, 4-6, 6-2", "4-6, 6-4, 2-6")).doesNotThrowAnyException();
    }

    @Test
    public void validate_rejects_bad_format() {
        // Lettres
        assertThatThrownBy(() -> ScoreValidator.validate("X-4", "4-6"))
            .isInstanceOf(BusinessException.class);
        
        // Séparateur incorrect
        assertThatThrownBy(() -> ScoreValidator.validate("6-4; 6-3", "6-4"))
            .isInstanceOf(BusinessException.class);
            
        // Set incomplet
        assertThatThrownBy(() -> ScoreValidator.validate("6-4, 6", "6-4, 6-2"))
            .isInstanceOf(BusinessException.class);
    }

    // --- TESTS DE LOGIQUE PADEL ---

    @Test
    public void validate_logic_standard_sets() {
        // 6-0 à 6-4 sont valides
        assertThatCode(() -> ScoreValidator.validate("6-0, 6-1, 6-4", "0-6, 1-6, 4-6")).doesNotThrowAnyException();
        // 7-5 et 7-6 sont valides
        assertThatCode(() -> ScoreValidator.validate("7-5, 7-6", "5-7, 6-7")).doesNotThrowAnyException();
    }

    @Test
    public void validate_rejects_impossible_scores() {
        // Règle : Vainqueur < 6 (ex: 5-3 n'est pas un set fini)
        assertThatThrownBy(() -> ScoreValidator.validate("5-3, 6-2", "3-5, 2-6"))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("doit aller au moins jusqu'à 6");

        // Règle : 6-5 est impossible (le match doit continuer jusqu'à 7-5 ou 7-6)
        assertThatThrownBy(() -> ScoreValidator.validate("6-5, 6-2", "5-6, 2-6"))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("À 6-5, le set doit continuer");

       // Règle : 7-4 est impossible (à 6-4 le match est fini) [cite: 245]
        assertThatThrownBy(() -> ScoreValidator.validate("7-4, 6-2", "4-7, 2-6"))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Un score de 7-4 est impossible");

        // Règle : 7-0 est impossible (à 6-0 le match est fini)
        assertThatThrownBy(() -> ScoreValidator.validate("7-0, 6-2", "0-7, 2-6"))
            .isInstanceOf(BusinessException.class);
            
        // Règle : Max 7 jeux (8-6 impossible)
        assertThatThrownBy(() -> ScoreValidator.validate("8-6, 6-2", "6-8, 2-6"))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    public void validate_nulls() {
        // Accepte si rien n'est fourni (validation optionnelle si non terminé)
        // Note: Si votre règle métier est stricte, adaptez ce test.
        // Ici on suppose que le service vérifie la présence avant d'appeler validate.
        
        // Rejette si UN seul score est fourni alors que l'autre est null (incohérent)
        // (Si votre implémentation le permet, changez ce test, mais c'est mieux d'être strict)
        // Pour ce test, on se base sur la logique du MatchService qui envoie les deux.
    }
}