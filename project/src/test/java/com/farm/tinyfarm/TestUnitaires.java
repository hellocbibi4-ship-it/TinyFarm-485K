package com.farm.tinyfarm.unit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.farm.tinyfarm.outils.Utilitaires;

class TestUtilitaires {

    @Test
    void nomValideNeLeveRien() {
        assertDoesNotThrow(() -> Utilitaires.validationNom("abc"));
        assertDoesNotThrow(() -> Utilitaires.validationNom("Ferme_01"));
        assertDoesNotThrow(() -> Utilitaires.validationNom("a-b_c-12"));
        assertDoesNotThrow(() -> Utilitaires.validationNom("1234567890123456"));
    }

    @Test
    void nomNullEstRejete() {
        assertThrows(IllegalArgumentException.class, () -> Utilitaires.validationNom(null));
    }

    @Test
    void nomTropCourtEstRejete() {
        assertThrows(IllegalArgumentException.class, () -> Utilitaires.validationNom(""));
        assertThrows(IllegalArgumentException.class, () -> Utilitaires.validationNom("ab"));
    }

    @Test
    void nomTropLongEstRejete() {
        assertThrows(IllegalArgumentException.class, () -> Utilitaires.validationNom("12345678901234567"));
    }

    @Test
    void nomAvecCaracteresInterditsEstRejete() {
        assertThrows(IllegalArgumentException.class, () -> Utilitaires.validationNom("ferme 01"));
        assertThrows(IllegalArgumentException.class, () -> Utilitaires.validationNom("ferme!"));
        assertThrows(IllegalArgumentException.class, () -> Utilitaires.validationNom("ferme@test"));
        assertThrows(IllegalArgumentException.class, () -> Utilitaires.validationNom("ferme.fr"));
    }
}