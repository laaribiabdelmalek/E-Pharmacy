package com.epharmacy.service;

import com.epharmacy.dao.UtilisateurDAO;
import com.epharmacy.model.Utilisateur;
import com.epharmacy.util.PasswordUtils;
import com.epharmacy.util.SessionManager;

/**
 * Service d'authentification et de gestion de session.
 * @author E-PHARMACIE
 */
public class AuthService {
    private final UtilisateurDAO utilisateurDAO = new UtilisateurDAO();

    /**
     * Authentifie un utilisateur par login/mot de passe.
     * @return Utilisateur si authentification réussie, null sinon
     */
    public Utilisateur authentifier(String login, String motDePasse) {
        Utilisateur u = utilisateurDAO.findByLogin(login);
        if (u != null && "ACTIF".equals(u.getStatut())
                && PasswordUtils.checkPassword(motDePasse, u.getMotDePasseHash())) {
            SessionManager.getInstance().ouvrirSession(u);
            return u;
        }
        return null;
    }

    /** Déconnecte l'utilisateur courant. */
    public void deconnecter() {
        SessionManager.getInstance().fermerSession();
    }
}