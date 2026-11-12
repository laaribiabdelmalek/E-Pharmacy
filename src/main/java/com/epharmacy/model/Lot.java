package com.epharmacy.model;

import java.time.LocalDate;

/**
 * Représente un lot de médicaments reçu lors d'un approvisionnement.
 * <p>
 * Chaque lot est identifié par un numéro de lot unique, possède une date
 * d'expiration et un suivi des quantités (initiale et restante) pour
 * la gestion FEFO (First Expired, First Out).
 * </p>
 *
 * @author E-PHARMACIE
 * @version 1.0
 */
public class Lot {

    /** Identifiant unique du lot. */
    private int idLot;

    /** Identifiant de la ligne d'approvisionnement associée. */
    private int idLigneApprovisionnement;

    /** Numéro de lot du fabricant. */
    private String numeroLot;

    /** Date d'expiration du lot. */
    private LocalDate dateExpiration;

    /** Quantité initiale reçue dans ce lot. */
    private int quantiteInitiale;

    /** Quantité restante disponible dans ce lot. */
    private int quantiteRestante;

    // --- Champs transitoires pour l'affichage et la commodité ---

    /** Nom du médicament (transitoire, utilisé pour l'affichage). */
    private String nomMedicament;

    /** Identifiant du médicament (transitoire, utilisé pour la commodité). */
    private int idMedicament;

    /**
     * Constructeur sans arguments.
     */
    public Lot() {
    }

    /**
     * Constructeur avec tous les arguments persistants.
     *
     * @param idLot                    identifiant unique du lot
     * @param idLigneApprovisionnement identifiant de la ligne d'approvisionnement
     * @param numeroLot                numéro de lot du fabricant
     * @param dateExpiration           date d'expiration
     * @param quantiteInitiale         quantité initiale reçue
     * @param quantiteRestante         quantité restante disponible
     */
    public Lot(int idLot, int idLigneApprovisionnement, String numeroLot,
               LocalDate dateExpiration, int quantiteInitiale, int quantiteRestante) {
        this.idLot = idLot;
        this.idLigneApprovisionnement = idLigneApprovisionnement;
        this.numeroLot = numeroLot;
        this.dateExpiration = dateExpiration;
        this.quantiteInitiale = quantiteInitiale;
        this.quantiteRestante = quantiteRestante;
    }

    /**
     * @return l'identifiant unique du lot
     */
    public int getIdLot() {
        return idLot;
    }

    /**
     * @param idLot l'identifiant unique à définir
     */
    public void setIdLot(int idLot) {
        this.idLot = idLot;
    }

    /**
     * @return l'identifiant de la ligne d'approvisionnement
     */
    public int getIdLigneApprovisionnement() {
        return idLigneApprovisionnement;
    }

    /**
     * @param idLigneApprovisionnement l'identifiant de la ligne à définir
     */
    public void setIdLigneApprovisionnement(int idLigneApprovisionnement) {
        this.idLigneApprovisionnement = idLigneApprovisionnement;
    }

    /**
     * @return le numéro de lot du fabricant
     */
    public String getNumeroLot() {
        return numeroLot;
    }

    /**
     * @param numeroLot le numéro de lot à définir
     */
    public void setNumeroLot(String numeroLot) {
        this.numeroLot = numeroLot;
    }

    /**
     * @return la date d'expiration
     */
    public LocalDate getDateExpiration() {
        return dateExpiration;
    }

    /**
     * @param dateExpiration la date d'expiration à définir
     */
    public void setDateExpiration(LocalDate dateExpiration) {
        this.dateExpiration = dateExpiration;
    }

    /**
     * @return la quantité initiale reçue
     */
    public int getQuantiteInitiale() {
        return quantiteInitiale;
    }

    /**
     * @param quantiteInitiale la quantité initiale à définir
     */
    public void setQuantiteInitiale(int quantiteInitiale) {
        this.quantiteInitiale = quantiteInitiale;
    }

    /**
     * @return la quantité restante disponible
     */
    public int getQuantiteRestante() {
        return quantiteRestante;
    }

    /**
     * @param quantiteRestante la quantité restante à définir
     */
    public void setQuantiteRestante(int quantiteRestante) {
        this.quantiteRestante = quantiteRestante;
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
     * @return l'identifiant du médicament (transitoire)
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
     * Vérifie si le lot est expiré à la date du jour.
     *
     * @return {@code true} si le lot est expiré, {@code false} sinon
     */
    public boolean isExpire() {
        return dateExpiration != null && dateExpiration.isBefore(LocalDate.now());
    }

    /**
     * Vérifie si le lot est proche de l'expiration (dans les 90 jours).
     *
     * @return {@code true} si le lot expire dans les 90 prochains jours
     */
    public boolean isProcheExpiration() {
        if (dateExpiration == null) {
            return false;
        }
        LocalDate dans90Jours = LocalDate.now().plusDays(90);
        return dateExpiration.isBefore(dans90Jours) && !isExpire();
    }

    @Override
    public String toString() {
        return "Lot{" +
                "idLot=" + idLot +
                ", idLigneApprovisionnement=" + idLigneApprovisionnement +
                ", numeroLot='" + numeroLot + '\'' +
                ", dateExpiration=" + dateExpiration +
                ", quantiteInitiale=" + quantiteInitiale +
                ", quantiteRestante=" + quantiteRestante +
                ", nomMedicament='" + nomMedicament + '\'' +
                ", idMedicament=" + idMedicament +
                '}';
    }
}
