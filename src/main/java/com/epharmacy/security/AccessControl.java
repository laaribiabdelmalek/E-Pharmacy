package com.epharmacy.security;

import com.epharmacy.exception.UnauthorizedException;
import com.epharmacy.model.Utilisateur;
import com.epharmacy.util.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contrôle d'accès basé sur les rôles (RBAC).
 * <p>
 * Vérifie que l'utilisateur connecté possède l'un des rôles autorisés
 * avant d'exécuter une opération sensible.
 * </p>
 *
 * @author E-PHARMACIE
 */
public final class AccessControl {

    private static final Logger logger = LoggerFactory.getLogger(AccessControl.class);

    private AccessControl() {
        throw new UnsupportedOperationException("Classe utilitaire non instanciable");
    }

    /**
     * Vérifie que l'utilisateur connecté possède l'un des rôles spécifiés.
     *
     * @param allowedRoles les rôles autorisés (ex: "ADMIN", "PHARMACIEN")
     * @throws UnauthorizedException si aucun utilisateur n'est connecté ou si le rôle ne correspond pas
     */
    public static void require(String... allowedRoles) {
        Utilisateur user = SessionManager.getInstance().getUtilisateurConnecte();
        if (user == null) {
            logger.warn("Tentative d'accès sans session active.");
            throw new UnauthorizedException("Accès refusé : aucune session active.");
        }

        String userRole = user.getRole();
        for (String role : allowedRoles) {
            if (role.equalsIgnoreCase(userRole)) {
                return;
            }
        }

        logger.warn("Accès refusé pour l'utilisateur {} (rôle: {}). Rôles requis: {}",
                user.getLogin(), userRole, String.join(", ", allowedRoles));
        throw new UnauthorizedException("Accès refusé : droits insuffisants.");
    }

    /**
     * Retourne l'identifiant de l'utilisateur connecté.
     *
     * @return l'ID utilisateur
     * @throws UnauthorizedException si aucun utilisateur n'est connecté
     */
    public static int getCurrentUserId() {
        Utilisateur user = SessionManager.getInstance().getUtilisateurConnecte();
        if (user == null) {
            throw new UnauthorizedException("Accès refusé : aucune session active.");
        }
        return user.getIdUtilisateur();
    }

    /**
     * Retourne l'utilisateur connecté.
     *
     * @return l'utilisateur courant
     * @throws UnauthorizedException si aucun utilisateur n'est connecté
     */
    public static Utilisateur getCurrentUser() {
        Utilisateur user = SessionManager.getInstance().getUtilisateurConnecte();
        if (user == null) {
            throw new UnauthorizedException("Accès refusé : aucune session active.");
        }
        return user;
    }

    /**
     * Vérifie si l'utilisateur connecté possède l'un des rôles spécifiés (sans lever d'exception).
     *
     * @param roles les rôles à vérifier
     * @return {@code true} si l'utilisateur possède l'un des rôles
     */
    public static boolean hasRole(String... roles) {
        Utilisateur user = SessionManager.getInstance().getUtilisateurConnecte();
        if (user == null) return false;
        String userRole = user.getRole();
        for (String role : roles) {
            if (role.equalsIgnoreCase(userRole)) return true;
        }
        return false;
    }
}
