package com.epharmacy.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Représente un approvisionnement (commande fournisseur) de la pharmacie.
 * <p>
 * Un approvisionnement est associé à un fournisseur et à l'utilisateur
 * qui l'a créé. Il contient une liste de lignes d'approvisionnement
 * détaillant les médicaments commandés.
 * </p>
 *
 * @author E-PHARMACIE
 * @version 1.0
 */
public class Approvisionnement {

    /** Identifiant unique de l'approvisionnement. */
    private int idApprovisionnement;

    /** Identifiant du fournisseur associé. */
    private int idFournisseur;

    /** Identifiant de l'utilisateur ayant créé l'approvisionnement. */
    private int idUtilisateur;

    /** Date et heure de l'approvisionnement. */
    private LocalDateTime dateApprovisionnement;

    /** Montant total de l'approvisionnement (DH). */
    private double montantTotal;

    /** Statut de l'approvisionnement (EN_COURS, LIVREE, ANNULEE, etc.). */
    private String statut;

    // --- Champs transitoires pour l'affichage ---

    /** Nom du fournisseur (transitoire, utilisé pour l'affichage). */
    private String nomFournisseur;

    /** Nom de l'utilisateur (transitoire, utilisé pour l'affichage). */
    private String nomUtilisateur;

    /** Liste des lignes d'approvisionnement. */
    private List<LigneApprovisionnement> lignes;

    /**
     * Constructeur sans arguments.
     * Initialise la liste des lignes à une liste vide.
     */
    public Approvisionnement() {
        this.lignes = new ArrayList<>();
    }

    /**
     * Constructeur avec tous les arguments persistants.
     *
     * @param idApprovisionnement    identifiant unique
     * @param idFournisseur          identifiant du fournisseur
     * @param idUtilisateur          identifiant de l'utilisateur
     * @param dateApprovisionnement  date et heure de l'approvisionnement
     * @param montantTotal           montant total (DH)
     * @param statut                 statut de l'approvisionnement
     */
    public Approvisionnement(int idApprovisionnement, int idFournisseur, int idUtilisateur,
                             LocalDateTime dateApprovisionnement, double montantTotal,
                             String statut) {
        this.idApprovisionnement = idApprovisionnement;
        this.idFournisseur = idFournisseur;
        this.idUtilisateur = idUtilisateur;
        this.dateApprovisionnement = dateApprovisionnement;
        this.montantTotal = montantTotal;
        this.statut = statut;
        this.lignes = new ArrayList<>();
    }

    /**
     * @return l'identifiant unique de l'approvisionnement
     */
    public int getIdApprovisionnement() {
        return idApprovisionnement;
    }

    /**
     * @param idApprovisionnement l'identifiant unique à définir
     */
    public void setIdApprovisionnement(int idApprovisionnement) {
        this.idApprovisionnement = idApprovisionnement;
    }

    /**
     * @return l'identifiant du fournisseur
     */
    public int getIdFournisseur() {
        return idFournisseur;
    }

    /**
     * @param idFournisseur l'identifiant du fournisseur à définir
     */
    public void setIdFournisseur(int idFournisseur) {
        this.idFournisseur = idFournisseur;
    }

    /**
     * @return l'identifiant de l'utilisateur
     */
    public int getIdUtilisateur() {
        return idUtilisateur;
    }

    /**
     * @param idUtilisateur l'identifiant de l'utilisateur à définir
     */
    public void setIdUtilisateur(int idUtilisateur) {
        this.idUtilisateur = idUtilisateur;
    }

    /**
     * @return la date et heure de l'approvisionnement
     */
    public LocalDateTime getDateApprovisionnement() {
        return dateApprovisionnement;
    }

    /**
     * @param dateApprovisionnement la date et heure à définir
     */
    public void setDateApprovisionnement(LocalDateTime dateApprovisionnement) {
        this.dateApprovisionnement = dateApprovisionnement;
    }

    /**
     * @return le montant total (DH)
     */
    public double getMontantTotal() {
        return montantTotal;
    }

    /**
     * @param montantTotal le montant total à définir (DH)
     */
    public void setMontantTotal(double montantTotal) {
        this.montantTotal = montantTotal;
    }

    /**
     * @return le statut de l'approvisionnement
     */
    public String getStatut() {
        return statut;
    }

    /**
     * @param statut le statut à définir
     */
    public void setStatut(String statut) {
        this.statut = statut;
    }

    /**
     * @return le nom du fournisseur (transitoire)
     */
    public String getNomFournisseur() {
        return nomFournisseur;
    }

    /**
     * @param nomFournisseur le nom du fournisseur à définir
     */
    public void setNomFournisseur(String nomFournisseur) {
        this.nomFournisseur = nomFournisseur;
    }

    /**
     * @return le nom de l'utilisateur (transitoire)
     */
    public String getNomUtilisateur() {
        return nomUtilisateur;
    }

    /**
     * @param nomUtilisateur le nom de l'utilisateur à définir
     */
    public void setNomUtilisateur(String nomUtilisateur) {
        this.nomUtilisateur = nomUtilisateur;
    }

    /**
     * @return la liste des lignes d'approvisionnement
     */
    public List<LigneApprovisionnement> getLignes() {
        return lignes;
    }

    /**
     * @param lignes la liste des lignes d'approvisionnement à définir
     */
    public void setLignes(List<LigneApprovisionnement> lignes) {
        this.lignes = lignes;
    }

    /**
     * Ajoute une ligne d'approvisionnement à la liste.
     *
     * @param ligne la ligne à ajouter
     */
    public void addLigne(LigneApprovisionnement ligne) {
        if (this.lignes == null) {
            this.lignes = new ArrayList<>();
        }
        this.lignes.add(ligne);
    }

    @Override
    public String toString() {
        return "Approvisionnement{" +
                "idApprovisionnement=" + idApprovisionnement +
                ", idFournisseur=" + idFournisseur +
                ", idUtilisateur=" + idUtilisateur +
                ", dateApprovisionnement=" + dateApprovisionnement +
                ", montantTotal=" + montantTotal +
                ", statut='" + statut + '\'' +
                ", nomFournisseur='" + nomFournisseur + '\'' +
                ", nomUtilisateur='" + nomUtilisateur + '\'' +
                ", lignes=" + (lignes != null ? lignes.size() : 0) +
                '}';
    }
}
