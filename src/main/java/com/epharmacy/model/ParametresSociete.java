package com.epharmacy.model;

/**
 * Représente les paramètres de la pharmacie (table parametres_societe).
 * @author E-PHARMACIE
 */
public class ParametresSociete {
    private int id;
    private String nomPharmacie;
    private String ice;
    private String identifiantFiscal;
    private String rc;
    private String patente;
    private String telephone;
    private String email;
    private String adresse;
    private String ville;
    private String messageTicket;
    private String themeApp;

    public ParametresSociete() { this.id = 1; }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNomPharmacie() { return nomPharmacie; }
    public void setNomPharmacie(String nomPharmacie) { this.nomPharmacie = nomPharmacie; }

    public String getIce() { return ice; }
    public void setIce(String ice) { this.ice = ice; }

    public String getIdentifiantFiscal() { return identifiantFiscal; }
    public void setIdentifiantFiscal(String identifiantFiscal) { this.identifiantFiscal = identifiantFiscal; }

    public String getRc() { return rc; }
    public void setRc(String rc) { this.rc = rc; }

    public String getPatente() { return patente; }
    public void setPatente(String patente) { this.patente = patente; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public String getVille() { return ville; }
    public void setVille(String ville) { this.ville = ville; }

    public String getMessageTicket() { return messageTicket; }
    public void setMessageTicket(String messageTicket) { this.messageTicket = messageTicket; }

    public String getThemeApp() { return themeApp; }
    public void setThemeApp(String themeApp) { this.themeApp = themeApp; }
}
