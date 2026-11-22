package com.epharmacy.security;

import com.epharmacy.exception.UnauthorizedException;
import com.epharmacy.model.Utilisateur;
import com.epharmacy.util.SessionManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tests du contrôle d'accès RBAC (AccessControl)")
public class AccessControlTest {

    @BeforeEach
    void setUp() {
        SessionManager.getInstance().fermerSession();
    }

    @AfterEach
    void tearDown() {
        SessionManager.getInstance().fermerSession();
    }

    @Test
    @DisplayName("require() lève UnauthorizedException si aucun utilisateur n'est connecté")
    void testRequireNonConnecte() {
        assertThrows(UnauthorizedException.class, () -> AccessControl.require("ADMIN"));
    }

    @Test
    @DisplayName("require() réussit si l'utilisateur possède l'un des rôles requis")
    void testRequireSucces() {
        Utilisateur admin = new Utilisateur();
        admin.setIdUtilisateur(1);
        admin.setLogin("admin");
        admin.setRole("ADMIN");
        SessionManager.getInstance().ouvrirSession(admin);

        assertDoesNotThrow(() -> AccessControl.require("ADMIN", "PHARMACIEN"));
    }

    @Test
    @DisplayName("require() lève UnauthorizedException si l'utilisateur n'a pas le rôle autorisé")
    void testRequireEchecRole() {
        Utilisateur emp = new Utilisateur();
        emp.setIdUtilisateur(2);
        emp.setLogin("emp");
        emp.setRole("EMPLOYE");
        SessionManager.getInstance().ouvrirSession(emp);

        assertThrows(UnauthorizedException.class, () -> AccessControl.require("ADMIN"));
    }

    @Test
    @DisplayName("hasRole() retourne true uniquement pour les rôles possédés")
    void testHasRole() {
        Utilisateur pharma = new Utilisateur();
        pharma.setIdUtilisateur(3);
        pharma.setLogin("pharma");
        pharma.setRole("PHARMACIEN");
        SessionManager.getInstance().ouvrirSession(pharma);

        assertTrue(AccessControl.hasRole("PHARMACIEN"));
        assertFalse(AccessControl.hasRole("ADMIN"));
    }
}
