package com.epharmacy.model;

/**
 * Représente une ligne d'une vente (détail d'un médicament vendu).
 * <p>
 * Chaque ligne est associée à un lot spécifique pour assurer la traçabilité
 * et la gestion FEFO (First Expired, First Out).
 * </p>
 *
 * @author E-PHARMACIE
 * @version 1.0
 */
public class LigneVente {

    /** Identifiant unique de la ligne de vente. */
    private int idLigneVente;

    /** Identifiant de la vente parente. */
    private int idVente;

    /** Identifiant du lot utilisé pour cette vente. */
    private int idLot;

    /** Quantité vendue. */
    private int quantite;

    /** Prix unitaire de vente (DH). */
    private double prixUnitaireVente;

    /** Sous-total de la ligne (quantité × prix unitaire, en DH). */
    private double sousTotal;

    // --- Champs transitoires pour l'affichage ---

    /** Nom du médicament (transitoire, utilisé pour l'affichage). */
    private String nomMedicament;

    /** Numéro de lot (transitoire, utilisé pour l'affichage). */
    private String numeroLot;

    /**
     * Constructeur sans arguments.
     */
    public LigneVente() {
    }

    /**
     * Constructeur avec tous les arguments persistants.
     *
     * @param idLigneVente     identifiant unique de la ligne
     * @param idVente          identifiant de la vente parente
     * @param idLot            identifiant du lot
     * @param quantite         quantité vendue
     * @param prixUnitaireVente prix unitaire de vente (DH)
     * @param sousTotal        sous-total (DH)
     */
    public LigneVente(int idLigneVente, int idVente, int idLot, int quantite,
                      double prixUnitaireVente, double sousTotal) {
        this.idLigneVente = idLigneVente;
        this.idVente = idVente;
        this.idLot = idLot;
        this.quantite = quantite;
        this.prixUnitaireVente = prixUnitaireVente;
        this.sousTotal = sousTotal;
    }

    /**
     * @return l'identifiant unique de la ligne de vente
     */
    public int getIdLigneVente() {
        return idLigneVente;
    }

    /**
     * @param idLigneVente l'identifiant unique à définir
     */
    public void setIdLigneVente(int idLigneVente) {
        this.idLigneVente = idLigneVente;
    }

    /**
     * @return l'identifiant de la vente parente
     */
    public int getIdVente() {
        return idVente;
    }

    /**
     * @param idVente l'identifiant de la vente à définir
     */
    public void setIdVente(int idVente) {
        this.idVente = idVente;
    }

    /**
     * @return l'identifiant du lot
     */
    public int getIdLot() {
        return idLot;
    }

    /**
     * @param idLot l'identifiant du lot à définir
     */
    public void setIdLot(int idLot) {
        this.idLot = idLot;
    }

    /**
     * @return la quantité vendue
     */
    public int getQuantite() {
        return quantite;
    }

    /**
     * @param quantite la quantité à définir
     */
    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }

    /**
     * @return le prix unitaire de vente (DH)
     */
    public double getPrixUnitaireVente() {
        return prixUnitaireVente;
    }

    /**
     * @param prixUnitaireVente le prix unitaire de vente à définir (DH)
     */
    public void setPrixUnitaireVente(double prixUnitaireVente) {
        this.prixUnitaireVente = prixUnitaireVente;
    }

    /**
     * @return le sous-total de la ligne (DH)
     */
    public double getSousTotal() {
        return sousTotal;
    }

    /**
     * @param sousTotal le sous-total à définir (DH)
     */
    public void setSousTotal(double sousTotal) {
        this.sousTotal = sousTotal;
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
     * @return le numéro de lot (transitoire)
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
     * Recalcule le sous-total à partir de la quantité et du prix unitaire.
     */
    public void calculerSousTotal() {
        this.sousTotal = this.quantite * this.prixUnitaireVente;
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
