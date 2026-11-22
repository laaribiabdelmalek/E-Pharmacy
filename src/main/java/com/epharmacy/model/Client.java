package com.epharmacy.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Représente un client de la pharmacie.
 * <p>
 * Un client peut avoir un crédit (solde à régler), un plafond de crédit et un statut (ACTIF/INACTIF).
 * Les montants monétaires utilisent {@link BigDecimal} avec accesseurs compatibles {@code double}.
 * </p>
 *
 * @author E-PHARMACIE
 * @version 3.0
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
    private BigDecimal credit = BigDecimal.ZERO;

    /** Plafond maximal de crédit autorisé (en DH). Par défaut 1000.00 DH. */
    private BigDecimal plafondCredit = new BigDecimal("1000.00");

    /** Statut du client (ACTIF, INACTIF). */
    private String statut = "ACTIF";

    /**
     * Constructeur sans arguments.
     */
    public Client() {
        this.credit = BigDecimal.ZERO;
        this.plafondCredit = new BigDecimal("1000.00");
        this.statut = "ACTIF";
    }

    /**
     * Constructeur avec arguments de base.
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
        this.credit = BigDecimal.valueOf(credit);
        this.plafondCredit = new BigDecimal("1000.00");
        this.statut = "ACTIF";
    }

    /**
     * Constructeur complet version 3.0 avec BigDecimal.
     */
    public Client(int idClient, String nom, String prenom, String telephone,
                  String email, String adresse, LocalDate dateInscription,
                  BigDecimal credit, BigDecimal plafondCredit, String statut) {
        this.idClient = idClient;
        this.nom = nom;
        this.prenom = prenom;
        this.telephone = telephone;
        this.email = email;
        this.adresse = adresse;
        this.dateInscription = dateInscription;
        this.credit = credit != null ? credit : BigDecimal.ZERO;
        this.plafondCredit = plafondCredit != null ? plafondCredit : new BigDecimal("1000.00");
        this.statut = statut != null ? statut : "ACTIF";
    }

    public int getIdClient() {
        return idClient;
    }

    public void setIdClient(int idClient) {
        this.idClient = idClient;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public LocalDate getDateInscription() {
        return dateInscription;
    }

    public void setDateInscription(LocalDate dateInscription) {
        this.dateInscription = dateInscription;
    }

    /**
     * @return le montant du crédit (DH) en double pour compatibilité
     */
    public double getCredit() {
        return credit != null ? credit.doubleValue() : 0.0;
    }

    public void setCredit(double credit) {
        this.credit = BigDecimal.valueOf(credit);
    }

    public BigDecimal getCreditBD() {
        return credit != null ? credit : BigDecimal.ZERO;
    }

    public void setCredit(BigDecimal credit) {
        this.credit = credit != null ? credit : BigDecimal.ZERO;
    }

    public BigDecimal getPlafondCredit() {
        return plafondCredit != null ? plafondCredit : new BigDecimal("1000.00");
    }

    public double getPlafondCreditDouble() {
        return plafondCredit != null ? plafondCredit.doubleValue() : 1000.00;
    }

    public void setPlafondCredit(BigDecimal plafondCredit) {
        this.plafondCredit = plafondCredit != null ? plafondCredit : new BigDecimal("1000.00");
    }

    public void setPlafondCredit(double plafondCredit) {
        this.plafondCredit = BigDecimal.valueOf(plafondCredit);
    }

    public String getStatut() {
        return statut != null ? statut : "ACTIF";
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public boolean isActif() {
        return "ACTIF".equalsIgnoreCase(statut);
    }

    /**
     * Retourne le nom complet du client (prénom + nom).
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
                ", plafondCredit=" + plafondCredit +
                ", statut='" + statut + '\'' +
                '}';
    }
}
