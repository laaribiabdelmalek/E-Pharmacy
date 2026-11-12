package com.epharmacy.util;

import com.epharmacy.model.Utilisateur;

/**
 * Gère la session de l'utilisateur connecté (Singleton).
 * <p>
 * Cette classe maintient une référence vers l'utilisateur actuellement
 * authentifié et fournit des méthodes utilitaires pour vérifier
 * son rôle et son état de connexion.
 * </p>
 *
 * @author E-PHARMACIE
 * @version 1.0
 */
public class SessionManager {

    /** Instance unique du gestionnaire de session */
    private static SessionManager instance;

    /** Utilisateur actuellement connecté */
    private Utilisateur utilisateurConnecte;

    /**
     * Constructeur privé pour empêcher l'instanciation externe.
     */
    private SessionManager() {}

    /**
     * Retourne l'instance unique du gestionnaire de session.
     *
     * @return l'instance singleton de {@link SessionManager}
     */
    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    /**
     * Ouvre une session pour l'utilisateur spécifié.
     *
     * @param utilisateur l'utilisateur qui vient de se connecter
     */
    public void ouvrirSession(Utilisateur utilisateur) {
        this.utilisateurConnecte = utilisateur;
    }

    /**
     * Ferme la session en cours en supprimant la référence
     * vers l'utilisateur connecté.
     */
    public void fermerSession() {
        this.utilisateurConnecte = null;
    }

    /**
     * Retourne l'utilisateur actuellement connecté.
     *
     * @return l'utilisateur connecté, ou {@code null} si aucune session n'est ouverte
     */
    public Utilisateur getUtilisateurConnecte() {
        return utilisateurConnecte;
    }

    /**
     * Vérifie si un utilisateur est actuellement connecté.
     *
     * @return {@code true} si un utilisateur est connecté, {@code false} sinon
     */
    public boolean isConnecte() {
        return utilisateurConnecte != null;
    }

    /**
     * Vérifie si l'utilisateur connecté possède le rôle ADMIN.
     *
     * @return {@code true} si l'utilisateur est un administrateur, {@code false} sinon
     */
    public boolean isAdmin() {
        return isConnecte() && "ADMIN".equals(utilisateurConnecte.getRole());
    }

    /**
     * Vérifie si l'utilisateur connecté possède le rôle PHARMACIEN.
     *
     * @return {@code true} si l'utilisateur est un pharmacien, {@code false} sinon
     */
    public boolean isPharmacien() {
        return isConnecte() && "PHARMACIEN".equals(utilisateurConnecte.getRole());
    }
}
