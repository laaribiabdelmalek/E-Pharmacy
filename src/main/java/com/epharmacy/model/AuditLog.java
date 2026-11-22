package com.epharmacy.model;

import java.time.LocalDateTime;

/**
 * Entité représentant une entrée d'audit du système.
 *
 * @author E-PHARMACIE
 * @version 3.0
 */
public class AuditLog {

    private int idAudit;
    private Integer idUtilisateur; // Peut être null si action système
    private String action;
    private String entite;
    private Integer idEntite;
    private String details;
    private LocalDateTime dateAction;

    // Champ d'affichage
    private String loginUtilisateur;

    public AuditLog() {
        this.dateAction = LocalDateTime.now();
    }

    public AuditLog(Integer idUtilisateur, String action, String entite, Integer idEntite, String details) {
        this.idUtilisateur = idUtilisateur;
        this.action = action;
        this.entite = entite;
        this.idEntite = idEntite;
        this.details = details;
        this.dateAction = LocalDateTime.now();
    }

    public int getIdAudit() {
        return idAudit;
    }

    public void setIdAudit(int idAudit) {
        this.idAudit = idAudit;
    }

    public Integer getIdUtilisateur() {
        return idUtilisateur;
    }

    public void setIdUtilisateur(Integer idUtilisateur) {
        this.idUtilisateur = idUtilisateur;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getEntite() {
        return entite;
    }

    public void setEntite(String entite) {
        this.entite = entite;
    }

    public Integer getIdEntite() {
        return idEntite;
    }

    public void setIdEntite(Integer idEntite) {
        this.idEntite = idEntite;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public LocalDateTime getDateAction() {
        return dateAction;
    }

    public void setDateAction(LocalDateTime dateAction) {
        this.dateAction = dateAction;
    }

    public String getLoginUtilisateur() {
        return loginUtilisateur;
    }

    public void setLoginUtilisateur(String loginUtilisateur) {
        this.loginUtilisateur = loginUtilisateur;
    }

    @Override
    public String toString() {
        return "AuditLog{" +
                "idAudit=" + idAudit +
                ", idUtilisateur=" + idUtilisateur +
                ", action='" + action + '\'' +
                ", entite='" + entite + '\'' +
                ", idEntite=" + idEntite +
                ", dateAction=" + dateAction +
                '}';
    }
}
