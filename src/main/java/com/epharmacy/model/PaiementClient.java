package com.epharmacy.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Représente un paiement ou règlement de crédit effectué par un client.
 *
 * @author E-PHARMACIE
 * @version 3.0
 */
public class PaiementClient {

    private int idPaiement;
    private int idClient;
    private int idUtilisateur;
    private BigDecimal montant;
    private String modePaiement; // ESPECES, CARTE, VIREMENT
    private LocalDateTime datePaiement;

    // Champs de commodité pour l'affichage
    private String nomClient;
    private String nomUtilisateur;

    public PaiementClient() {
        this.montant = BigDecimal.ZERO;
        this.modePaiement = "ESPECES";
        this.datePaiement = LocalDateTime.now();
    }

    public PaiementClient(int idClient, int idUtilisateur, BigDecimal montant, String modePaiement) {
        this.idClient = idClient;
        this.idUtilisateur = idUtilisateur;
        this.montant = montant != null ? montant : BigDecimal.ZERO;
        this.modePaiement = modePaiement != null ? modePaiement : "ESPECES";
        this.datePaiement = LocalDateTime.now();
    }

    public int getIdPaiement() {
        return idPaiement;
    }

    public void setIdPaiement(int idPaiement) {
        this.idPaiement = idPaiement;
    }

    public int getIdClient() {
        return idClient;
    }

    public void setIdClient(int idClient) {
        this.idClient = idClient;
    }

    public int getIdUtilisateur() {
        return idUtilisateur;
    }

    public void setIdUtilisateur(int idUtilisateur) {
        this.idUtilisateur = idUtilisateur;
    }

    public BigDecimal getMontant() {
        return montant;
    }

    public void setMontant(BigDecimal montant) {
        this.montant = montant != null ? montant : BigDecimal.ZERO;
    }

    public double getMontantDouble() {
        return montant != null ? montant.doubleValue() : 0.0;
    }

    public String getModePaiement() {
        return modePaiement;
    }

    public void setModePaiement(String modePaiement) {
        this.modePaiement = modePaiement;
    }

    public LocalDateTime getDatePaiement() {
        return datePaiement;
    }

    public void setDatePaiement(LocalDateTime datePaiement) {
        this.datePaiement = datePaiement;
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

    @Override
    public String toString() {
        return "PaiementClient{" +
                "idPaiement=" + idPaiement +
                ", idClient=" + idClient +
                ", idUtilisateur=" + idUtilisateur +
                ", montant=" + montant +
                ", modePaiement='" + modePaiement + '\'' +
                ", datePaiement=" + datePaiement +
                '}';
    }
}
