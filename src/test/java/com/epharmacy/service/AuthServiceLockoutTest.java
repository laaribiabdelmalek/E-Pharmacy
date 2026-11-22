package com.epharmacy.service;

import com.epharmacy.dao.AuditLogDAO;
import com.epharmacy.dao.UtilisateurDAO;
import com.epharmacy.exception.UnauthorizedException;
import com.epharmacy.model.AuditLog;
import com.epharmacy.model.Utilisateur;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tests du verrouillage par force brute dans AuthService")
public class AuthServiceLockoutTest {

    // Dummy DAO pour tester la logique de lockout sans connexion MySQL active
    private static class MockUtilisateurDAO extends UtilisateurDAO {
        @Override
        public Utilisateur findByLogin(String login) {
            if ("target_user".equalsIgnoreCase(login)) {
                Utilisateur u = new Utilisateur();
                u.setIdUtilisateur(99);
                u.setLogin("target_user");
                // Hash BCrypt pour mot de passe "ValidPassword1!"
                u.setMotDePasseHash("$2a$10$N.yBqW8wDqGqQh2lH8w8/.F0b68Vb4x008qXFv5x1.39j/3L1.a6m");
                u.setRole("EMPLOYE");
                u.setStatut("ACTIF");
                return u;
            }
            return null;
        }
    }

    private static class MockAuditDAO extends AuditLogDAO {
        @Override
        public void insert(AuditLog log) {
            // no-op pour les tests unitaires
        }

        @Override
        public void insert(Connection conn, AuditLog log) {
            // no-op
        }
    }

    @Test
    @DisplayName("5 échecs consécutifs déclenchent le verrouillage temporaire du compte")
    void testLockoutApres5Echecs() {
        AuthService authService = new AuthService(new MockUtilisateurDAO(), new MockAuditDAO());
        String login = "target_user";

        // 4 échecs
        for (int i = 0; i < 4; i++) {
            assertNull(authService.authentifier(login, "WrongPassword"),
                    "L'authentification avec mauvais mot de passe doit retourner null");
            assertFalse(authService.isLocked(login), "Le compte ne doit pas encore être verrouillé");
        }

        // 5ème échec
        assertNull(authService.authentifier(login, "WrongPassword"));
        assertTrue(authService.isLocked(login), "Le compte doit être verrouillé après 5 échecs");
        assertTrue(authService.getRemainingLockSeconds(login) > 0, "Il doit rester du temps de verrouillage");

        // Toute tentative subséquente doit lever UnauthorizedException
        assertThrows(UnauthorizedException.class, () -> authService.authentifier(login, "ValidPassword1!"));
    }
}
