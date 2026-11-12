package com.epharmacy.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Représente une vente effectuée dans la pharmacie.
 * <p>
 * Une vente est associée à un utilisateur (vendeur) et optionnellement
 * à un client. Elle contient une liste de lignes de vente détaillant
 * les médicaments vendus.
 * </p>
 *
 * @author E-PHARMACIE
 * @version 1.0
 */
public class Vente {

    /** Identifiant unique de la vente. */
    private int idVente;

    /** Identifiant de l'utilisateur ayant effectué la vente. */
    private int idUtilisateur;

    /** Identifiant du client (0 si vente anonyme). */
    private int idClient;

    /** Date et heure de la vente. */
    private LocalDateTime dateVente;

    /** Montant total de la vente (DH). */
    private double montantTotal;

    /** Mode de paiement : ESPECES, CARTE ou CREDIT. */
    private String modePaiement;

    /** Statut de la vente (VALIDEE, ANNULEE, etc.). */
    private String statut;

    // --- Champs transitoires pour l'affichage ---

    /** Nom du client (transitoire, utilisé pour l'affichage). */
    private String nomClient;

    /** Nom de l'utilisateur (transitoire, utilisé pour l'affichage). */
    private String nomUtilisateur;

    /** Liste des lignes de vente. */
    private List<LigneVente> lignes;

    /**
     * Constructeur sans arguments.
     * Initialise la liste des lignes à une liste vide.
     */
    public Vente() {
        this.lignes = new ArrayList<>();
    }

    /**
     * Constructeur avec tous les arguments persistants.
     *
     * @param idVente       identifiant unique
     * @param idUtilisateur identifiant de l'utilisateur vendeur
     * @param idClient      identifiant du client (0 si anonyme)
     * @param dateVente     date et heure de la vente
     * @param montantTotal  montant total (DH)
     * @param modePaiement  mode de paiement (ESPECES/CARTE/CREDIT)
     * @param statut        statut de la vente
     */
    public Vente(int idVente, int idUtilisateur, int idClient, LocalDateTime dateVente,
                 double montantTotal, String modePaiement, String statut) {
        this.idVente = idVente;
        this.idUtilisateur = idUtilisateur;
        this.idClient = idClient;
        this.dateVente = dateVente;
        this.montantTotal = montantTotal;
        this.modePaiement = modePaiement;
        this.statut = statut;
        this.lignes = new ArrayList<>();
    }

    /**
     * @return l'identifiant unique de la vente
     */
    public int getIdVente() {
        return idVente;
    }

    /**
     * @param idVente l'identifiant unique à définir
     */
    public void setIdVente(int idVente) {
        this.idVente = idVente;
    }

    /**
     * @return l'identifiant de l'utilisateur vendeur
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
     * @return l'identifiant du client
     */
    public int getIdClient() {
        return idClient;
    }

    /**
     * @param idClient l'identifiant du client à définir
     */
    public void setIdClient(int idClient) {
        this.idClient = idClient;
    }

    /**
     * @return la date et heure de la vente
     */
    public LocalDateTime getDateVente() {
        return dateVente;
    }

    /**
     * @param dateVente la date et heure à définir
     */
    public void setDateVente(LocalDateTime dateVente) {
        this.dateVente = dateVente;
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
     * @return le mode de paiement
     */
    public String getModePaiement() {
        return modePaiement;
    }

    /**
     * @param modePaiement le mode de paiement à définir (ESPECES/CARTE/CREDIT)
     */
    public void setModePaiement(String modePaiement) {
        this.modePaiement = modePaiement;
    }

    /**
     * @return le statut de la vente
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
     * @return le nom du client (transitoire)
     */
    public String getNomClient() {
        return nomClient;
    }

    /**
     * @param nomClient le nom du client à définir
     */
    public void setNomClient(String nomClient) {
        this.nomClient = nomClient;
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
     * @return la liste des lignes de vente
     */
    public List<LigneVente> getLignes() {
        return lignes;
    }

    /**
     * @param lignes la liste des lignes de vente à définir
     */
    public void setLignes(List<LigneVente> lignes) {
        this.lignes = lignes;
    }

    /**
     * Ajoute une ligne de vente à la liste.
     *
     * @param ligne la ligne de vente à ajouter
     */
    public void addLigne(LigneVente ligne) {
        if (this.lignes == null) {
            this.lignes = new ArrayList<>();
        }
        this.lignes.add(ligne);
    }

    @Override
    public String toString() {
        return "Vente{" +
                "idVente=" + idVente +
                ", idUtilisateur=" + idUtilisateur +
                ", idClient=" + idClient +
                ", dateVente=" + dateVente +
                ", montantTotal=" + montantTotal +
                ", modePaiement='" + modePaiement + '\'' +
                ", statut='" + statut + '\'' +
                ", nomClient='" + nomClient + '\'' +
                ", nomUtilisateur='" + nomUtilisateur + '\'' +
                ", lignes=" + (lignes != null ? lignes.size() : 0) +
                '}';
    }
}
