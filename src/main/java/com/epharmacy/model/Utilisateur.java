package com.epharmacy.model;

import java.time.LocalDateTime;

/**
 * Représente un utilisateur du système E-PHARMACIE.
 * <p>
 * Un utilisateur peut avoir l'un des rôles suivants : ADMIN, PHARMACIEN ou EMPLOYE.
 * Son statut peut être ACTIF ou INACTIF.
 * </p>
 *
 * @author E-PHARMACIE
 * @version 1.0
 */
public class Utilisateur {

    /** Identifiant unique de l'utilisateur. */
    private int idUtilisateur;

    /** Nom de famille de l'utilisateur. */
    private String nom;

    /** Prénom de l'utilisateur. */
    private String prenom;

    /** Identifiant de connexion (login). */
    private String login;

    /** Hash du mot de passe de l'utilisateur. */
    private String motDePasseHash;

    /** Rôle de l'utilisateur : ADMIN, PHARMACIEN ou EMPLOYE. */
    private String role;

    /** Statut du compte : ACTIF ou INACTIF. */
    private String statut;

    /** Date et heure de création du compte. */
    private LocalDateTime dateCreation;

    /**
     * Constructeur sans arguments.
     */
    public Utilisateur() {
    }

    /**
     * Constructeur avec tous les arguments.
     *
     * @param idUtilisateur   identifiant unique
     * @param nom             nom de famille
     * @param prenom          prénom
     * @param login           identifiant de connexion
     * @param motDePasseHash  hash du mot de passe
     * @param role            rôle (ADMIN/PHARMACIEN/EMPLOYE)
     * @param statut          statut (ACTIF/INACTIF)
     * @param dateCreation    date et heure de création
     */
    public Utilisateur(int idUtilisateur, String nom, String prenom, String login,
                       String motDePasseHash, String role, String statut,
                       LocalDateTime dateCreation) {
        this.idUtilisateur = idUtilisateur;
        this.nom = nom;
        this.prenom = prenom;
        this.login = login;
        this.motDePasseHash = motDePasseHash;
        this.role = role;
        this.statut = statut;
        this.dateCreation = dateCreation;
    }

    /**
     * @return l'identifiant unique de l'utilisateur
     */
    public int getIdUtilisateur() {
        return idUtilisateur;
    }

    /**
     * @param idUtilisateur l'identifiant unique à définir
     */
    public void setIdUtilisateur(int idUtilisateur) {
        this.idUtilisateur = idUtilisateur;
    }

    /**
     * @return le nom de famille de l'utilisateur
     */
    public String getNom() {
        return nom;
    }

    /**
     * @param nom le nom de famille à définir
     */
    public void setNom(String nom) {
        this.nom = nom;
    }

    /**
     * @return le prénom de l'utilisateur
     */
    public String getPrenom() {
        return prenom;
    }

    /**
     * @param prenom le prénom à définir
     */
    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    /**
     * @return l'identifiant de connexion
     */
    public String getLogin() {
        return login;
    }

    /**
     * @param login l'identifiant de connexion à définir
     */
    public void setLogin(String login) {
        this.login = login;
    }

    /**
     * @return le hash du mot de passe
     */
    public String getMotDePasseHash() {
        return motDePasseHash;
    }

    /**
     * @param motDePasseHash le hash du mot de passe à définir
     */
    public void setMotDePasseHash(String motDePasseHash) {
        this.motDePasseHash = motDePasseHash;
    }

    /**
     * @return le rôle de l'utilisateur
     */
    public String getRole() {
        return role;
    }

    /**
     * @param role le rôle à définir (ADMIN/PHARMACIEN/EMPLOYE)
     */
    public void setRole(String role) {
        this.role = role;
    }

    /**
     * @return le statut du compte
     */
    public String getStatut() {
        return statut;
    }

    /**
     * @param statut le statut à définir (ACTIF/INACTIF)
     */
    public void setStatut(String statut) {
        this.statut = statut;
    }

    /**
     * @return la date et heure de création du compte
     */
    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    /**
     * @param dateCreation la date et heure de création à définir
     */
    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    /**
     * Retourne le nom complet de l'utilisateur (prénom + nom).
     *
     * @return le nom complet
     */
    public String getNomComplet() {
        return (prenom != null ? prenom : "") + " " + (nom != null ? nom : "");
    }

    @Override
    public String toString() {
        return "Utilisateur{" +
                "idUtilisateur=" + idUtilisateur +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", login='" + login + '\'' +
                ", role='" + role + '\'' +
                ", statut='" + statut + '\'' +
                ", dateCreation=" + dateCreation +
                '}';
    }
}
