package com.epharmacy.service;

import com.epharmacy.dao.AuditLogDAO;
import com.epharmacy.dao.UtilisateurDAO;
import com.epharmacy.exception.UnauthorizedException;
import com.epharmacy.model.AuditLog;
import com.epharmacy.model.Utilisateur;
import com.epharmacy.util.PasswordUtils;
import com.epharmacy.util.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service d'authentification et de gestion de session avec protection contre les attaques par force brute.
 *
 * @author E-PHARMACIE
 * @version 3.0
 */
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCKOUT_DURATION_SECONDS = 60;

    private static class AttemptInfo {
        int attempts;
        Instant lockUntil;

        AttemptInfo(int attempts, Instant lockUntil) {
            this.attempts = attempts;
            this.lockUntil = lockUntil;
        }
    }

    private static final Map<String, AttemptInfo> failedAttempts = new ConcurrentHashMap<>();

    private final UtilisateurDAO utilisateurDAO;
    private final AuditLogDAO auditLogDAO;

    public AuthService() {
        this(new UtilisateurDAO(), new AuditLogDAO());
    }

    public AuthService(UtilisateurDAO utilisateurDAO, AuditLogDAO auditLogDAO) {
        this.utilisateurDAO = utilisateurDAO;
        this.auditLogDAO = auditLogDAO;
    }

    /**
     * Vérifie si un compte est actuellement verrouillé temporairement.
     */
    public boolean isLocked(String login) {
        if (login == null) return false;
        AttemptInfo info = failedAttempts.get(login.toLowerCase().trim());
        if (info != null && info.lockUntil != null) {
            if (Instant.now().isBefore(info.lockUntil)) {
                return true;
            } else {
                failedAttempts.remove(login.toLowerCase().trim());
            }
        }
        return false;
    }

    /**
     * Retourne le temps restant de verrouillage en secondes.
     */
    public long getRemainingLockSeconds(String login) {
        if (login == null) return 0;
        AttemptInfo info = failedAttempts.get(login.toLowerCase().trim());
        if (info != null && info.lockUntil != null) {
            long remaining = info.lockUntil.getEpochSecond() - Instant.now().getEpochSecond();
            return Math.max(0, remaining);
        }
        return 0;
    }

    /**
     * Authentifie un utilisateur avec vérification du mot de passe hashé et gestion du verrouillage.
     */
    public Utilisateur authentifier(String login, String motDePasse) {
        if (login == null || login.isBlank()) {
            return null;
        }
        String key = login.toLowerCase().trim();

        if (isLocked(key)) {
            long seconds = getRemainingLockSeconds(key);
            logger.warn("Tentative de connexion sur compte verrouillé : {} (attente: {}s)", key, seconds);
            throw new UnauthorizedException("Compte temporairement verrouillé suite à de trop nombreuses tentatives. Réessayez dans " + seconds + " secondes.");
        }

        Utilisateur u = utilisateurDAO.findByLogin(login);
        if (u == null) {
            recordFailedAttempt(key, null);
            return null;
        }

        if (!"ACTIF".equalsIgnoreCase(u.getStatut())) {
            logger.warn("Tentative de connexion sur un compte inactif : {}", login);
            auditLogDAO.insert(new AuditLog(u.getIdUtilisateur(), "CONNEXION_REFUSEE", "UTILISATEUR", u.getIdUtilisateur(), "Compte inactif"));
            throw new UnauthorizedException("Ce compte est inactif. Veuillez contacter votre administrateur.");
        }

        if (PasswordUtils.checkPassword(motDePasse, u.getMotDePasseHash())) {
            failedAttempts.remove(key);
            SessionManager.getInstance().ouvrirSession(u);
            logger.info("Connexion réussie pour l'utilisateur '{}' (Rôle: {})", u.getLogin(), u.getRole());
            auditLogDAO.insert(new AuditLog(u.getIdUtilisateur(), "CONNEXION_REUSSIE", "UTILISATEUR", u.getIdUtilisateur(), "Connexion établie"));
            return u;
        } else {
            recordFailedAttempt(key, u.getIdUtilisateur());
            return null;
        }
    }

    private void recordFailedAttempt(String login, Integer userId) {
        failedAttempts.compute(login, (k, info) -> {
            if (info == null) {
                return new AttemptInfo(1, null);
            }
            int newAttempts = info.attempts + 1;
            Instant lockUntil = null;
            if (newAttempts >= MAX_ATTEMPTS) {
                lockUntil = Instant.now().plusSeconds(LOCKOUT_DURATION_SECONDS);
                logger.warn("Compte '{}' verrouillé pour {} secondes ({} échecs consécutifs)", login, LOCKOUT_DURATION_SECONDS, newAttempts);
            }
            return new AttemptInfo(newAttempts, lockUntil);
        });

        auditLogDAO.insert(new AuditLog(userId, "CONNEXION_ECHOUEE", "UTILISATEUR", userId, "Échec mot de passe pour '" + login + "'"));
    }

    /** Déconnecte l'utilisateur courant. */
    public void deconnecter() {
        Utilisateur current = SessionManager.getInstance().getUtilisateurConnecte();
        if (current != null) {
            auditLogDAO.insert(new AuditLog(current.getIdUtilisateur(), "DECONNEXION", "UTILISATEUR", current.getIdUtilisateur(), "Déconnexion utilisateur"));
            logger.info("Déconnexion de l'utilisateur '{}'", current.getLogin());
        }
        SessionManager.getInstance().fermerSession();
    }
}
