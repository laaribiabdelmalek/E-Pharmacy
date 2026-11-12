package com.epharmacy.model;

import java.time.LocalDate;

/**
 * Représente un client de la pharmacie.
 * <p>
 * Un client peut avoir un crédit (solde à régler) et est identifié
 * par ses coordonnées personnelles.
 * </p>
 *
 * @author E-PHARMACIE
 * @version 1.0
 */
public class Client {

    /** Identifiant unique du client. */
    private int idClient;

    /** Nom de famille du client. */
    private String nom;

    /** Prénom du client. */
    private String prenom;

    /** Numéro de téléphone du client. */
    private String telephone;

    /** Adresse e-mail du client. */
    private String email;

    /** Adresse postale du client. */
    private String adresse;

    /** Date d'inscription du client. */
    private LocalDate dateInscription;

    /** Montant du crédit en cours (en DH). */
    private double credit;

    /**
     * Constructeur sans arguments.
     */
    public Client() {
    }

    /**
     * Constructeur avec tous les arguments.
     *
     * @param idClient         identifiant unique
     * @param nom              nom de famille
     * @param prenom           prénom
     * @param telephone        numéro de téléphone
     * @param email            adresse e-mail
     * @param adresse          adresse postale
     * @param dateInscription  date d'inscription
     * @param credit           montant du crédit (DH)
     */
    public Client(int idClient, String nom, String prenom, String telephone,
                  String email, String adresse, LocalDate dateInscription, double credit) {
        this.idClient = idClient;
        this.nom = nom;
        this.prenom = prenom;
        this.telephone = telephone;
        this.email = email;
        this.adresse = adresse;
        this.dateInscription = dateInscription;
        this.credit = credit;
    }

    /**
     * @return l'identifiant unique du client
     */
    public int getIdClient() {
        return idClient;
    }

    /**
     * @param idClient l'identifiant unique à définir
     */
    public void setIdClient(int idClient) {
        this.idClient = idClient;
    }

    /**
     * @return le nom de famille du client
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
     * @return le prénom du client
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
     * @return le numéro de téléphone
     */
    public String getTelephone() {
        return telephone;
    }

    /**
     * @param telephone le numéro de téléphone à définir
     */
    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    /**
     * @return l'adresse e-mail
     */
    public String getEmail() {
        return email;
    }

    /**
     * @param email l'adresse e-mail à définir
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * @return l'adresse postale
     */
    public String getAdresse() {
        return adresse;
    }

    /**
     * @param adresse l'adresse postale à définir
     */
    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    /**
     * @return la date d'inscription
     */
    public LocalDate getDateInscription() {
        return dateInscription;
    }

    /**
     * @param dateInscription la date d'inscription à définir
     */
    public void setDateInscription(LocalDate dateInscription) {
        this.dateInscription = dateInscription;
    }

    /**
     * @return le montant du crédit (DH)
     */
    public double getCredit() {
        return credit;
    }

    /**
     * @param credit le montant du crédit à définir (DH)
     */
    public void setCredit(double credit) {
        this.credit = credit;
    }

    /**
     * Retourne le nom complet du client (prénom + nom).
     *
     * @return le nom complet
     */
    public String getNomComplet() {
        return (prenom != null ? prenom : "") + " " + (nom != null ? nom : "");
    }

    @Override
    public String toString() {
        return "Client{" +
                "idClient=" + idClient +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", telephone='" + telephone + '\'' +
                ", email='" + email + '\'' +
                ", adresse='" + adresse + '\'' +
                ", dateInscription=" + dateInscription +
                ", credit=" + credit +
                '}';
    }
}
