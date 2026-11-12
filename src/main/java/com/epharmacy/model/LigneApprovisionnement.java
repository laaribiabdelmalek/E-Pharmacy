package com.epharmacy.model;

import java.time.LocalDate;

/**
 * Représente une ligne d'un approvisionnement (détail d'un médicament commandé).
 * <p>
 * Chaque ligne correspond à un médicament spécifique avec sa quantité commandée,
 * sa quantité reçue et son prix d'achat unitaire.
 * </p>
 *
 * @author E-PHARMACIE
 * @version 1.0
 */
public class LigneApprovisionnement {

    /** Identifiant unique de la ligne d'approvisionnement. */
    private int idLigneApprovisionnement;

    /** Identifiant de l'approvisionnement parent. */
    private int idApprovisionnement;

    /** Identifiant du médicament concerné. */
    private int idMedicament;

    /** Date de réception de la livraison. */
    private LocalDate dateReception;

    /** Quantité commandée. */
    private int quantiteCommandee;

    /** Quantité effectivement reçue. */
    private int quantiteRecue;

    /** Prix d'achat unitaire (DH). */
    private double prixAchatUnitaire;

    // --- Champ transitoire pour l'affichage ---

    /** Nom du médicament (transitoire, utilisé pour l'affichage). */
    private String nomMedicament;

    /**
     * Constructeur sans arguments.
     */
    public LigneApprovisionnement() {
    }

    /**
     * Constructeur avec tous les arguments persistants.
     *
     * @param idLigneApprovisionnement identifiant unique de la ligne
     * @param idApprovisionnement      identifiant de l'approvisionnement parent
     * @param idMedicament             identifiant du médicament
     * @param dateReception            date de réception
     * @param quantiteCommandee        quantité commandée
     * @param quantiteRecue            quantité reçue
     * @param prixAchatUnitaire        prix d'achat unitaire (DH)
     */
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
        this.prixAchatUnitaire = prixAchatUnitaire;
    }

    /**
     * @return l'identifiant unique de la ligne d'approvisionnement
     */
    public int getIdLigneApprovisionnement() {
        return idLigneApprovisionnement;
    }

    /**
     * @param idLigneApprovisionnement l'identifiant unique à définir
     */
    public void setIdLigneApprovisionnement(int idLigneApprovisionnement) {
        this.idLigneApprovisionnement = idLigneApprovisionnement;
    }

    /**
     * @return l'identifiant de l'approvisionnement parent
     */
    public int getIdApprovisionnement() {
        return idApprovisionnement;
    }

    /**
     * @param idApprovisionnement l'identifiant de l'approvisionnement parent à définir
     */
    public void setIdApprovisionnement(int idApprovisionnement) {
        this.idApprovisionnement = idApprovisionnement;
    }

    /**
     * @return l'identifiant du médicament
     */
    public int getIdMedicament() {
        return idMedicament;
    }

    /**
     * @param idMedicament l'identifiant du médicament à définir
     */
    public void setIdMedicament(int idMedicament) {
        this.idMedicament = idMedicament;
    }

    /**
     * @return la date de réception
     */
    public LocalDate getDateReception() {
        return dateReception;
    }

    /**
     * @param dateReception la date de réception à définir
     */
    public void setDateReception(LocalDate dateReception) {
        this.dateReception = dateReception;
    }

    /**
     * @return la quantité commandée
     */
    public int getQuantiteCommandee() {
        return quantiteCommandee;
    }

    /**
     * @param quantiteCommandee la quantité commandée à définir
     */
    public void setQuantiteCommandee(int quantiteCommandee) {
        this.quantiteCommandee = quantiteCommandee;
    }

    /**
     * @return la quantité effectivement reçue
     */
    public int getQuantiteRecue() {
        return quantiteRecue;
    }

    /**
     * @param quantiteRecue la quantité reçue à définir
     */
    public void setQuantiteRecue(int quantiteRecue) {
        this.quantiteRecue = quantiteRecue;
    }

    /**
     * @return le prix d'achat unitaire (DH)
     */
    public double getPrixAchatUnitaire() {
        return prixAchatUnitaire;
    }

    /**
     * @param prixAchatUnitaire le prix d'achat unitaire à définir (DH)
     */
    public void setPrixAchatUnitaire(double prixAchatUnitaire) {
        this.prixAchatUnitaire = prixAchatUnitaire;
    }

    /**
     * @return le nom du médicament (transitoire)
     */
    public String getNomMedicament() {
        return nomMedicament;
    }

    /**
     * @param nomMedicament le nom du médicament à définir
     */
    public void setNomMedicament(String nomMedicament) {
        this.nomMedicament = nomMedicament;
    }

    /**
     * Calcule le sous-total de cette ligne (quantité reçue × prix unitaire).
     *
     * @return le sous-total (DH)
     */
    public double getSousTotal() {
        return quantiteRecue * prixAchatUnitaire;
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
