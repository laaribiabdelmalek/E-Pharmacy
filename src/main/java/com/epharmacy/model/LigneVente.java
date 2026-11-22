package com.epharmacy.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Représente une ligne d'une vente (détail d'un médicament vendu).
 * <p>
 * Chaque ligne est associée à un lot spécifique pour assurer la traçabilité
 * et la gestion FEFO (First Expired, First Out).
 * </p>
 *
 * @author E-PHARMACIE
 * @version 3.0
 */
public class LigneVente {

    private int idLigneVente;
    private int idVente;
    private int idLot;
    private int quantite;
    private BigDecimal prixUnitaireVente = BigDecimal.ZERO;
    private BigDecimal sousTotal = BigDecimal.ZERO;

    // --- Champs transitoires pour l'affichage ---
    private String nomMedicament;
    private String numeroLot;
    private String dosage;

    public LigneVente() {
        this.prixUnitaireVente = BigDecimal.ZERO;
        this.sousTotal = BigDecimal.ZERO;
    }

    public LigneVente(int idLigneVente, int idVente, int idLot, int quantite,
                      double prixUnitaireVente, double sousTotal) {
        this.idLigneVente = idLigneVente;
        this.idVente = idVente;
        this.idLot = idLot;
        this.quantite = quantite;
        this.prixUnitaireVente = BigDecimal.valueOf(prixUnitaireVente);
        this.sousTotal = BigDecimal.valueOf(sousTotal);
    }

    public LigneVente(int idLigneVente, int idVente, int idLot, int quantite,
                      BigDecimal prixUnitaireVente, BigDecimal sousTotal) {
        this.idLigneVente = idLigneVente;
        this.idVente = idVente;
        this.idLot = idLot;
        this.quantite = quantite;
        this.prixUnitaireVente = prixUnitaireVente != null ? prixUnitaireVente : BigDecimal.ZERO;
        this.sousTotal = sousTotal != null ? sousTotal : BigDecimal.ZERO;
    }

    public int getIdLigneVente() {
        return idLigneVente;
    }

    public void setIdLigneVente(int idLigneVente) {
        this.idLigneVente = idLigneVente;
    }

    public int getIdVente() {
        return idVente;
    }

    public void setIdVente(int idVente) {
        this.idVente = idVente;
    }

    public int getIdLot() {
        return idLot;
    }

    public void setIdLot(int idLot) {
        this.idLot = idLot;
    }

    public int getQuantite() {
        return quantite;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }

    public double getPrixUnitaireVente() {
        return prixUnitaireVente != null ? prixUnitaireVente.doubleValue() : 0.0;
    }

    public void setPrixUnitaireVente(double prixUnitaireVente) {
        this.prixUnitaireVente = BigDecimal.valueOf(prixUnitaireVente);
    }

    public BigDecimal getPrixUnitaireVenteBD() {
        return prixUnitaireVente != null ? prixUnitaireVente : BigDecimal.ZERO;
    }

    public void setPrixUnitaireVente(BigDecimal prixUnitaireVente) {
        this.prixUnitaireVente = prixUnitaireVente != null ? prixUnitaireVente : BigDecimal.ZERO;
    }

    public double getSousTotal() {
        return sousTotal != null ? sousTotal.doubleValue() : 0.0;
    }

    public void setSousTotal(double sousTotal) {
        this.sousTotal = BigDecimal.valueOf(sousTotal);
    }

    public BigDecimal getSousTotalBD() {
        return sousTotal != null ? sousTotal : BigDecimal.ZERO;
    }

    public void setSousTotal(BigDecimal sousTotal) {
        this.sousTotal = sousTotal != null ? sousTotal : BigDecimal.ZERO;
    }

    public String getNomMedicament() {
        return nomMedicament;
    }

    public void setNomMedicament(String nomMedicament) {
        this.nomMedicament = nomMedicament;
    }

    public String getNumeroLot() {
        return numeroLot;
    }

    public void setNumeroLot(String numeroLot) {
        this.numeroLot = numeroLot;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    /**
     * Recalcule le sous-total à partir de la quantité et du prix unitaire.
     */
    public void calculerSousTotal() {
        if (this.prixUnitaireVente != null) {
            this.sousTotal = this.prixUnitaireVente.multiply(BigDecimal.valueOf(this.quantite))
                    .setScale(2, RoundingMode.HALF_UP);
        } else {
            this.sousTotal = BigDecimal.ZERO;
        }
    }

    @Override
    public String toString() {
        return "LigneVente{" +
                "idLigneVente=" + idLigneVente +
                ", idVente=" + idVente +
                ", idLot=" + idLot +
                ", quantite=" + quantite +
                ", prixUnitaireVente=" + prixUnitaireVente +
                ", sousTotal=" + sousTotal +
                ", nomMedicament='" + nomMedicament + '\'' +
                ", numeroLot='" + numeroLot + '\'' +
                '}';
    }
}
