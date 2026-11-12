package com.epharmacy.model;

/**
 * Représente un fournisseur de médicaments pour la pharmacie.
 * <p>
 * Chaque fournisseur est identifié par son ICE (Identifiant Commun de l'Entreprise)
 * et possède des informations commerciales et de contact.
 * </p>
 *
 * @author E-PHARMACIE
 * @version 1.0
 */
public class Fournisseur {

    /** Identifiant unique du fournisseur. */
    private int idFournisseur;

    /** Identifiant Commun de l'Entreprise. */
    private String ice;

    /** Raison sociale du fournisseur. */
    private String raisonSociale;

    /** Nom commercial du fournisseur. */
    private String nomCommercial;

    /** Numéro de téléphone du fournisseur. */
    private String telephone;

    /** Adresse e-mail du fournisseur. */
    private String email;

    /** Adresse postale du fournisseur. */
    private String adresse;

    /** Ville du fournisseur. */
    private String ville;

    /** Nom du contact principal chez le fournisseur. */
    private String nomContact;

    /** Statut du fournisseur (ACTIF/INACTIF). */
    private String statut;

    /**
     * Constructeur sans arguments.
     */
    public Fournisseur() {
    }

    /**
     * Constructeur avec tous les arguments.
     *
     * @param idFournisseur identifiant unique
     * @param ice           ICE du fournisseur
     * @param raisonSociale raison sociale
     * @param nomCommercial nom commercial
     * @param telephone     numéro de téléphone
     * @param email         adresse e-mail
     * @param adresse       adresse postale
     * @param ville         ville
     * @param nomContact    nom du contact principal
     * @param statut        statut (ACTIF/INACTIF)
     */
    public Fournisseur(int idFournisseur, String ice, String raisonSociale,
                       String nomCommercial, String telephone, String email,
                       String adresse, String ville, String nomContact, String statut) {
        this.idFournisseur = idFournisseur;
        this.ice = ice;
        this.raisonSociale = raisonSociale;
        this.nomCommercial = nomCommercial;
        this.telephone = telephone;
        this.email = email;
        this.adresse = adresse;
        this.ville = ville;
        this.nomContact = nomContact;
        this.statut = statut;
    }

    /**
     * @return l'identifiant unique du fournisseur
     */
    public int getIdFournisseur() {
        return idFournisseur;
    }

    /**
     * @param idFournisseur l'identifiant unique à définir
     */
    public void setIdFournisseur(int idFournisseur) {
        this.idFournisseur = idFournisseur;
    }

    /**
     * @return l'ICE du fournisseur
     */
    public String getIce() {
        return ice;
    }

    /**
     * @param ice l'ICE à définir
     */
    public void setIce(String ice) {
        this.ice = ice;
    }

    /**
     * @return la raison sociale
     */
    public String getRaisonSociale() {
        return raisonSociale;
    }

    /**
     * @param raisonSociale la raison sociale à définir
     */
    public void setRaisonSociale(String raisonSociale) {
        this.raisonSociale = raisonSociale;
    }

    /**
     * @return le nom commercial
     */
    public String getNomCommercial() {
        return nomCommercial;
    }

    /**
     * @param nomCommercial le nom commercial à définir
     */
    public void setNomCommercial(String nomCommercial) {
        this.nomCommercial = nomCommercial;
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
     * @return la ville
     */
    public String getVille() {
        return ville;
    }

    /**
     * @param ville la ville à définir
     */
    public void setVille(String ville) {
        this.ville = ville;
    }

    /**
     * @return le nom du contact principal
     */
    public String getNomContact() {
        return nomContact;
    }

    /**
     * @param nomContact le nom du contact principal à définir
     */
    public void setNomContact(String nomContact) {
        this.nomContact = nomContact;
    }

    /**
     * @return le statut du fournisseur
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

    @Override
    public String toString() {
        return "Fournisseur{" +
                "idFournisseur=" + idFournisseur +
                ", ice='" + ice + '\'' +
                ", raisonSociale='" + raisonSociale + '\'' +
                ", nomCommercial='" + nomCommercial + '\'' +
                ", telephone='" + telephone + '\'' +
                ", email='" + email + '\'' +
                ", adresse='" + adresse + '\'' +
                ", ville='" + ville + '\'' +
                ", nomContact='" + nomContact + '\'' +
                ", statut='" + statut + '\'' +
                '}';
    }
}
