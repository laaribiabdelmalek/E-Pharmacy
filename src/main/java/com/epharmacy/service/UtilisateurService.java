package com.epharmacy.service;

import com.epharmacy.dao.AuditLogDAO;
import com.epharmacy.dao.UtilisateurDAO;
import com.epharmacy.exception.BusinessException;
import com.epharmacy.model.AuditLog;
import com.epharmacy.model.Utilisateur;
import com.epharmacy.util.PasswordUtils;
import com.epharmacy.util.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Service de gestion des utilisateurs avec règles de sécurité strictes :
 * complexité des mots de passe, protection du dernier admin et interdiction
 * de rétrogradation de son propre rôle.
 *
 * @author E-PHARMACIE
 * @version 3.0
 */
public class UtilisateurService {

    private static final Logger logger = LoggerFactory.getLogger(UtilisateurService.class);

    // Minimum 8 caractères, au moins 1 majuscule, 1 minuscule, 1 chiffre et 1 caractère spécial
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{8,}$");

    private final UtilisateurDAO utilisateurDAO;
    private final AuditLogDAO auditLogDAO;

    public UtilisateurService() {
        this(new UtilisateurDAO(), new AuditLogDAO());
    }

    public UtilisateurService(UtilisateurDAO utilisateurDAO, AuditLogDAO auditLogDAO) {
        this.utilisateurDAO = utilisateurDAO;
        this.auditLogDAO = auditLogDAO;
    }

    public List<Utilisateur> getTous() {
        return utilisateurDAO.findAll();
    }

    public Utilisateur getParId(int id) {
        return utilisateurDAO.findById(id);
    }

    public Utilisateur getParLogin(String login) {
        return utilisateurDAO.findByLogin(login);
    }

    /**
     * Valide les critères de complexité d'un mot de passe en clair.
     */
    public static boolean validerComplexiteMotDePasse(String password) {
        if (password == null) return false;
        return PASSWORD_PATTERN.matcher(password).matches();
    }

    /**
     * Crée un utilisateur avec validation et hachage BCrypt.
     */
    public boolean ajouter(Utilisateur u, String plainPassword) {
        validerDonneesUtilisateur(u);

        if (!validerComplexiteMotDePasse(plainPassword)) {
            throw new BusinessException("Le mot de passe doit contenir au moins 8 caractères, dont une majuscule, une minuscule, un chiffre et un caractère spécial.");
        }

        if (utilisateurDAO.findByLogin(u.getLogin()) != null) {
            throw new BusinessException("Ce login est déjà utilisé par un autre utilisateur.");
        }

        u.setMotDePasseHash(PasswordUtils.hashPassword(plainPassword));
        if (u.getStatut() == null || u.getStatut().isBlank()) {
            u.setStatut("ACTIF");
        }

        boolean ok = utilisateurDAO.insert(u);
        if (ok) {
            logAudit("AJOUT_UTILISATEUR", u.getIdUtilisateur(), "Création du compte : " + u.getLogin() + " (Rôle: " + u.getRole() + ")");
        }
        return ok;
    }

    /**
     * Met à jour les informations d'un utilisateur avec garde-fous de sécurité.
     */
    public boolean modifier(Utilisateur u) {
        validerDonneesUtilisateur(u);

        Utilisateur existant = utilisateurDAO.findById(u.getIdUtilisateur());
        if (existant == null) {
            throw new BusinessException("Utilisateur introuvable #" + u.getIdUtilisateur());
        }

        // Vérifier unicité login si modifié
        Utilisateur autre = utilisateurDAO.findByLogin(u.getLogin());
        if (autre != null && autre.getIdUtilisateur() != u.getIdUtilisateur()) {
            throw new BusinessException("Ce login est déjà utilisé par un autre compte.");
        }

        Utilisateur current = SessionManager.getInstance().getUtilisateurConnecte();

        // Règle 1 : Un administrateur connecté ne peut pas rétrograder son propre rôle
        if (current != null && current.getIdUtilisateur() == u.getIdUtilisateur()) {
            if ("ADMIN".equalsIgnoreCase(existant.getRole()) && !"ADMIN".equalsIgnoreCase(u.getRole())) {
                throw new BusinessException("Vous ne pouvez pas rétrograder votre propre rôle d'administrateur.");
            }
        }

        // Règle 2 : Protection du dernier administrateur actif
        if ("ADMIN".equalsIgnoreCase(existant.getRole()) && (!"ADMIN".equalsIgnoreCase(u.getRole()) || "INACTIF".equalsIgnoreCase(u.getStatut()))) {
            if (utilisateurDAO.countActiveAdmins() <= 1) {
                throw new BusinessException("Opération impossible : il doit toujours rester au moins un administrateur actif dans le système.");
            }
        }

        boolean ok = utilisateurDAO.update(u);
        if (ok) {
            logAudit("MODIFICATION_UTILISATEUR", u.getIdUtilisateur(), "Mise à jour du profil de " + u.getLogin());
        }
        return ok;
    }

    /**
     * Modifie le mot de passe d'un utilisateur.
     */
    public boolean changerMotDePasse(int idUtilisateur, String plainNewPassword) {
        if (!validerComplexiteMotDePasse(plainNewPassword)) {
            throw new BusinessException("Le mot de passe doit comporter au moins 8 caractères, une majuscule, une minuscule, un chiffre et un caractère spécial.");
        }
        String hash = PasswordUtils.hashPassword(plainNewPassword);
        boolean ok = utilisateurDAO.updatePassword(idUtilisateur, hash);
        if (ok) {
            logAudit("CHANGEMENT_MDP", idUtilisateur, "Mise à jour du mot de passe pour l'utilisateur #" + idUtilisateur);
        }
        return ok;
    }

    /**
     * Désactivation logique d'un utilisateur avec protection du dernier administrateur.
     */
    public boolean desactiver(int idUtilisateur) {
        Utilisateur existant = utilisateurDAO.findById(idUtilisateur);
        if (existant == null) {
            throw new BusinessException("Utilisateur introuvable #" + idUtilisateur);
        }

        Utilisateur current = SessionManager.getInstance().getUtilisateurConnecte();
        if (current != null && current.getIdUtilisateur() == idUtilisateur) {
            throw new BusinessException("Vous ne pouvez pas désactiver votre propre compte actuellement connecté.");
        }

        if ("ADMIN".equalsIgnoreCase(existant.getRole()) && utilisateurDAO.countActiveAdmins() <= 1) {
            throw new BusinessException("Impossible de désactiver le seul administrateur actif du système.");
        }

        boolean ok = utilisateurDAO.delete(idUtilisateur);
        if (ok) {
            logAudit("DESACTIVATION_UTILISATEUR", idUtilisateur, "Désactivation du compte " + existant.getLogin());
        }
        return ok;
    }

    private void validerDonneesUtilisateur(Utilisateur u) {
        if (u == null) {
            throw new BusinessException("Données utilisateur manquantes.");
        }
        if (u.getNom() == null || u.getNom().isBlank()) {
            throw new BusinessException("Le nom est obligatoire.");
        }
        if (u.getPrenom() == null || u.getPrenom().isBlank()) {
            throw new BusinessException("Le prénom est obligatoire.");
        }
        if (u.getLogin() == null || u.getLogin().isBlank()) {
            throw new BusinessException("Le login est obligatoire.");
        }
        if (u.getRole() == null || u.getRole().isBlank()) {
            throw new BusinessException("Le rôle est obligatoire.");
        }
    }

    private void logAudit(String action, int idEntite, String details) {
        Integer userId = null;
        if (SessionManager.getInstance().isConnecte()) {
            userId = SessionManager.getInstance().getUtilisateurConnecte().getIdUtilisateur();
        }
        auditLogDAO.insert(new AuditLog(userId, action, "UTILISATEUR", idEntite, details));
    }
}
