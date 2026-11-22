package com.epharmacy.model;

import java.math.BigDecimal;
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
 * @version 3.0
 */
public class Approvisionnement {

    private int idApprovisionnement;
    private int idFournisseur;
    private int idUtilisateur;
    private LocalDateTime dateApprovisionnement;
    private BigDecimal montantTotal = BigDecimal.ZERO;
    private String statut;

    // --- Champs transitoires pour l'affichage ---
    private String nomFournisseur;
    private String nomUtilisateur;
    private List<LigneApprovisionnement> lignes;

    public Approvisionnement() {
        this.lignes = new ArrayList<>();
        this.montantTotal = BigDecimal.ZERO;
    }

    public Approvisionnement(int idApprovisionnement, int idFournisseur, int idUtilisateur,
                             LocalDateTime dateApprovisionnement, double montantTotal,
                             String statut) {
        this.idApprovisionnement = idApprovisionnement;
        this.idFournisseur = idFournisseur;
        this.idUtilisateur = idUtilisateur;
        this.dateApprovisionnement = dateApprovisionnement;
        this.montantTotal = BigDecimal.valueOf(montantTotal);
        this.statut = statut;
        this.lignes = new ArrayList<>();
    }

    public Approvisionnement(int idApprovisionnement, int idFournisseur, int idUtilisateur,
                             LocalDateTime dateApprovisionnement, BigDecimal montantTotal,
                             String statut) {
        this.idApprovisionnement = idApprovisionnement;
        this.idFournisseur = idFournisseur;
        this.idUtilisateur = idUtilisateur;
        this.dateApprovisionnement = dateApprovisionnement;
        this.montantTotal = montantTotal != null ? montantTotal : BigDecimal.ZERO;
        this.statut = statut;
        this.lignes = new ArrayList<>();
    }

    public int getIdApprovisionnement() {
        return idApprovisionnement;
    }

    public void setIdApprovisionnement(int idApprovisionnement) {
        this.idApprovisionnement = idApprovisionnement;
    }

    public int getIdFournisseur() {
        return idFournisseur;
    }

    public void setIdFournisseur(int idFournisseur) {
        this.idFournisseur = idFournisseur;
    }

    public int getIdUtilisateur() {
        return idUtilisateur;
    }

    public void setIdUtilisateur(int idUtilisateur) {
        this.idUtilisateur = idUtilisateur;
    }

    public LocalDateTime getDateApprovisionnement() {
        return dateApprovisionnement;
    }

    public void setDateApprovisionnement(LocalDateTime dateApprovisionnement) {
        this.dateApprovisionnement = dateApprovisionnement;
    }

    public double getMontantTotal() {
        return montantTotal != null ? montantTotal.doubleValue() : 0.0;
    }

    public void setMontantTotal(double montantTotal) {
        this.montantTotal = BigDecimal.valueOf(montantTotal);
    }

    public BigDecimal getMontantTotalBD() {
        return montantTotal != null ? montantTotal : BigDecimal.ZERO;
    }

    public void setMontantTotal(BigDecimal montantTotal) {
        this.montantTotal = montantTotal != null ? montantTotal : BigDecimal.ZERO;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getNomFournisseur() {
        return nomFournisseur;
    }

    public void setNomFournisseur(String nomFournisseur) {
        this.nomFournisseur = nomFournisseur;
    }

    public String getNomUtilisateur() {
        return nomUtilisateur;
    }

    public void setNomUtilisateur(String nomUtilisateur) {
        this.nomUtilisateur = nomUtilisateur;
    }

    public List<LigneApprovisionnement> getLignes() {
        return lignes;
    }

    public void setLignes(List<LigneApprovisionnement> lignes) {
        this.lignes = lignes;
    }

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
