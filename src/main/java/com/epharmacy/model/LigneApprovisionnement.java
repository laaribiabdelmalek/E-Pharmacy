package com.epharmacy.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Représente une ligne d'un approvisionnement (détail d'un médicament commandé).
 * <p>
 * Chaque ligne correspond à un médicament spécifique avec sa quantité commandée,
 * sa quantité reçue et son prix d'achat unitaire.
 * </p>
 *
 * @author E-PHARMACIE
 * @version 3.0
 */
public class LigneApprovisionnement {

    private int idLigneApprovisionnement;
    private int idApprovisionnement;
    private int idMedicament;
    private LocalDate dateReception;
    private int quantiteCommandee;
    private int quantiteRecue;
    private BigDecimal prixAchatUnitaire = BigDecimal.ZERO;

    // --- Champ transitoire pour l'affichage ---
    private String nomMedicament;

    public LigneApprovisionnement() {
        this.prixAchatUnitaire = BigDecimal.ZERO;
    }

    public LigneApprovisionnement(int idLigneApprovisionnement, int idApprovisionnement,
                                  int idMedicament, LocalDate dateReception,
                                  int quantiteCommandee, int quantiteRecue,
                                  double prixAchatUnitaire) {
        this.idLigneApprovisionnement = idLigneApprovisionnement;
        this.idApprovisionnement = idApprovisionnement;
        this.idMedicament = idMedicament;
        this.dateReception = dateReception;
        this.quantiteCommandee = quantiteCommandee;
        this.quantiteRecue = quantiteRecue;
        this.prixAchatUnitaire = BigDecimal.valueOf(prixAchatUnitaire);
    }

    public LigneApprovisionnement(int idLigneApprovisionnement, int idApprovisionnement,
                                  int idMedicament, LocalDate dateReception,
                                  int quantiteCommandee, int quantiteRecue,
                                  BigDecimal prixAchatUnitaire) {
        this.idLigneApprovisionnement = idLigneApprovisionnement;
        this.idApprovisionnement = idApprovisionnement;
        this.idMedicament = idMedicament;
        this.dateReception = dateReception;
        this.quantiteCommandee = quantiteCommandee;
        this.quantiteRecue = quantiteRecue;
        this.prixAchatUnitaire = prixAchatUnitaire != null ? prixAchatUnitaire : BigDecimal.ZERO;
    }

    public int getIdLigneApprovisionnement() {
        return idLigneApprovisionnement;
    }

    public void setIdLigneApprovisionnement(int idLigneApprovisionnement) {
        this.idLigneApprovisionnement = idLigneApprovisionnement;
    }

    public int getIdApprovisionnement() {
        return idApprovisionnement;
    }

    public void setIdApprovisionnement(int idApprovisionnement) {
        this.idApprovisionnement = idApprovisionnement;
    }

    public int getIdMedicament() {
        return idMedicament;
    }

    public void setIdMedicament(int idMedicament) {
        this.idMedicament = idMedicament;
    }

    public LocalDate getDateReception() {
        return dateReception;
    }

    public void setDateReception(LocalDate dateReception) {
        this.dateReception = dateReception;
    }

    public int getQuantiteCommandee() {
        return quantiteCommandee;
    }

    public void setQuantiteCommandee(int quantiteCommandee) {
        this.quantiteCommandee = quantiteCommandee;
    }

    public int getQuantiteRecue() {
        return quantiteRecue;
    }

    public void setQuantiteRecue(int quantiteRecue) {
        this.quantiteRecue = quantiteRecue;
    }

    public double getPrixAchatUnitaire() {
        return prixAchatUnitaire != null ? prixAchatUnitaire.doubleValue() : 0.0;
    }

    public void setPrixAchatUnitaire(double prixAchatUnitaire) {
        this.prixAchatUnitaire = BigDecimal.valueOf(prixAchatUnitaire);
    }

    public BigDecimal getPrixAchatUnitaireBD() {
        return prixAchatUnitaire != null ? prixAchatUnitaire : BigDecimal.ZERO;
    }

    public void setPrixAchatUnitaire(BigDecimal prixAchatUnitaire) {
        this.prixAchatUnitaire = prixAchatUnitaire != null ? prixAchatUnitaire : BigDecimal.ZERO;
    }

    public String getNomMedicament() {
        return nomMedicament;
    }

    public void setNomMedicament(String nomMedicament) {
        this.nomMedicament = nomMedicament;
    }

    @Override
    public String toString() {
        return "LigneApprovisionnement{" +
                "idLigneApprovisionnement=" + idLigneApprovisionnement +
                ", idApprovisionnement=" + idApprovisionnement +
                ", idMedicament=" + idMedicament +
                ", dateReception=" + dateReception +
                ", quantiteCommandee=" + quantiteCommandee +
                ", quantiteRecue=" + quantiteRecue +
                ", prixAchatUnitaire=" + prixAchatUnitaire +
                ", nomMedicament='" + nomMedicament + '\'' +
                '}';
    }
}
