package com.epharmacy.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Représente une vente effectuée dans la pharmacie.
 * <p>
 * Une vente est associée à un utilisateur (vendeur) et optionnellement
 * à un client. Elle contient une liste de lignes de vente détaillant
 * les médicaments vendus. Les montants monétaires utilisent {@link BigDecimal}.
 * </p>
 *
 * @author E-PHARMACIE
 * @version 3.0
 */
public class Vente {

    private int idVente;
    private int idUtilisateur;
    private int idClient;
    private LocalDateTime dateVente;
    private BigDecimal montantTotal = BigDecimal.ZERO;
    private String modePaiement;
    private String statut;

    // --- Champs transitoires pour l'affichage ---
    private String nomClient;
    private String nomUtilisateur;
    private List<LigneVente> lignes;

    public Vente() {
        this.lignes = new ArrayList<>();
        this.montantTotal = BigDecimal.ZERO;
    }

    public Vente(int idVente, int idUtilisateur, int idClient, LocalDateTime dateVente,
                 double montantTotal, String modePaiement, String statut) {
        this.idVente = idVente;
        this.idUtilisateur = idUtilisateur;
        this.idClient = idClient;
        this.dateVente = dateVente;
        this.montantTotal = BigDecimal.valueOf(montantTotal);
        this.modePaiement = modePaiement;
        this.statut = statut;
        this.lignes = new ArrayList<>();
    }

    public Vente(int idVente, int idUtilisateur, int idClient, LocalDateTime dateVente,
                 BigDecimal montantTotal, String modePaiement, String statut) {
        this.idVente = idVente;
        this.idUtilisateur = idUtilisateur;
        this.idClient = idClient;
        this.dateVente = dateVente;
        this.montantTotal = montantTotal != null ? montantTotal : BigDecimal.ZERO;
        this.modePaiement = modePaiement;
        this.statut = statut;
        this.lignes = new ArrayList<>();
    }

    public int getIdVente() {
        return idVente;
    }

    public void setIdVente(int idVente) {
        this.idVente = idVente;
    }

    public int getIdUtilisateur() {
        return idUtilisateur;
    }

    public void setIdUtilisateur(int idUtilisateur) {
        this.idUtilisateur = idUtilisateur;
    }

    public int getIdClient() {
        return idClient;
    }

    public void setIdClient(int idClient) {
        this.idClient = idClient;
    }

    public LocalDateTime getDateVente() {
        return dateVente;
    }

    public void setDateVente(LocalDateTime dateVente) {
        this.dateVente = dateVente;
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

    public String getModePaiement() {
        return modePaiement;
    }

    public void setModePaiement(String modePaiement) {
        this.modePaiement = modePaiement;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getNomClient() {
        return nomClient;
    }

    public void setNomClient(String nomClient) {
        this.nomClient = nomClient;
    }

    public String getNomUtilisateur() {
        return nomUtilisateur;
    }

    public void setNomUtilisateur(String nomUtilisateur) {
        this.nomUtilisateur = nomUtilisateur;
    }

    public List<LigneVente> getLignes() {
        return lignes;
    }

    public void setLignes(List<LigneVente> lignes) {
        this.lignes = lignes;
    }

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
