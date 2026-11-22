package com.epharmacy.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tests des règles métier UtilisateurService")
public class UtilisateurServiceTest {

    @Test
    @DisplayName("Test mot de passe valide standard")
    void testMotDePasseValide() {
        assertTrue(UtilisateurService.validerComplexiteMotDePasse("Admin@1234"));
        assertTrue(UtilisateurService.validerComplexiteMotDePasse("P@ssw0rd2026"));
        assertTrue(UtilisateurService.validerComplexiteMotDePasse("Complexe#1"));
    }

    @Test
    @DisplayName("Test mot de passe faible rejeté")
    void testMotDePasseFaible() {
        assertFalse(UtilisateurService.validerComplexiteMotDePasse("court1!"));
        assertFalse(UtilisateurService.validerComplexiteMotDePasse("sansmajuscule1!"));
        assertFalse(UtilisateurService.validerComplexiteMotDePasse("SANSMINUSCULE1!"));
        assertFalse(UtilisateurService.validerComplexiteMotDePasse("SansChiffre!"));
        assertFalse(UtilisateurService.validerComplexiteMotDePasse("SansSpecial123"));
        assertFalse(UtilisateurService.validerComplexiteMotDePasse(""));
        assertFalse(UtilisateurService.validerComplexiteMotDePasse("   "));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "Admin@1234",
            "P@ssw0rd2026",
            "Complexe#1",
            "Super!Secr3t"
    })
    @DisplayName("Validation de complexité : mots de passe valides acceptés (paramétré)")
    void testParametriseValide(String password) {
        assertTrue(UtilisateurService.validerComplexiteMotDePasse(password),
                "Le mot de passe '" + password + "' devrait être valide");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "court1!",
            "sansmajuscule1!",
            "SANSMINUSCULE1!",
            "SansChiffre!",
            "SansSpecial123",
            "",
            "       "
    })
    @DisplayName("Validation de complexité : mots de passe faibles rejetés (paramétré)")
    void testParametriseInvalide(String password) {
        assertFalse(UtilisateurService.validerComplexiteMotDePasse(password),
                "Le mot de passe faible '" + password + "' devrait être rejeté");
    }
}
