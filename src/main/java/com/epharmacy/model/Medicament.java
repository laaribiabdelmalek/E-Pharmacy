package com.epharmacy.model;

import java.math.BigDecimal;

/**
 * Représente un médicament dans le catalogue de la pharmacie.
 * <p>
 * Contient les informations pharmaceutiques (DCI, dosage, forme, laboratoire),
 * les informations commerciales (prix de vente public), de prescription (ordonnance requise)
 * et de gestion de stock (seuil minimal, quantité disponible).
 * </p>
 *
 * @author E-PHARMACIE
 * @version 3.0
 */
public class Medicament {

    private int idMedicament;
    private String nomCommercial;
    private String dci;
    private String dosage;
    private String formePharmaceutique;
    private String conditionnement;
    private String laboratoire;
    private String typeMedicament;
    private String conditionsConservation;
    private String categorie;
    private BigDecimal prixVentePublic = BigDecimal.ZERO;
    private int seuilMinimal;
    private String statut;
    private int quantiteDisponible;
    private boolean ordonnanceRequise = false;

    public Medicament() {
        this.prixVentePublic = BigDecimal.ZERO;
        this.ordonnanceRequise = false;
    }

    public Medicament(int idMedicament, String nomCommercial, String dci, String dosage,
                      String formePharmaceutique, String conditionnement, String laboratoire,
                      String typeMedicament, String conditionsConservation, String categorie,
                      double prixVentePublic, int seuilMinimal, String statut,
                      int quantiteDisponible) {
        this.idMedicament = idMedicament;
        this.nomCommercial = nomCommercial;
        this.dci = dci;
        this.dosage = dosage;
        this.formePharmaceutique = formePharmaceutique;
        this.conditionnement = conditionnement;
        this.laboratoire = laboratoire;
        this.typeMedicament = typeMedicament;
        this.conditionsConservation = conditionsConservation;
        this.categorie = categorie;
        this.prixVentePublic = BigDecimal.valueOf(prixVentePublic);
        this.seuilMinimal = seuilMinimal;
        this.statut = statut;
        this.quantiteDisponible = quantiteDisponible;
        this.ordonnanceRequise = false;
    }

    public Medicament(int idMedicament, String nomCommercial, String dci, String dosage,
                      String formePharmaceutique, String conditionnement, String laboratoire,
                      String typeMedicament, String conditionsConservation, String categorie,
                      BigDecimal prixVentePublic, int seuilMinimal, String statut,
                      int quantiteDisponible, boolean ordonnanceRequise) {
        this.idMedicament = idMedicament;
        this.nomCommercial = nomCommercial;
        this.dci = dci;
        this.dosage = dosage;
        this.formePharmaceutique = formePharmaceutique;
        this.conditionnement = conditionnement;
        this.laboratoire = laboratoire;
        this.typeMedicament = typeMedicament;
        this.conditionsConservation = conditionsConservation;
        this.categorie = categorie;
        this.prixVentePublic = prixVentePublic != null ? prixVentePublic : BigDecimal.ZERO;
        this.seuilMinimal = seuilMinimal;
        this.statut = statut;
        this.quantiteDisponible = quantiteDisponible;
        this.ordonnanceRequise = ordonnanceRequise;
    }

    public int getIdMedicament() {
        return idMedicament;
    }

    public void setIdMedicament(int idMedicament) {
        this.idMedicament = idMedicament;
    }

    public String getNomCommercial() {
        return nomCommercial;
    }

    public void setNomCommercial(String nomCommercial) {
        this.nomCommercial = nomCommercial;
    }

    public String getDci() {
        return dci;
    }

    public void setDci(String dci) {
        this.dci = dci;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public String getFormePharmaceutique() {
        return formePharmaceutique;
    }

    public void setFormePharmaceutique(String formePharmaceutique) {
        this.formePharmaceutique = formePharmaceutique;
    }

    public String getConditionnement() {
        return conditionnement;
    }

    public void setConditionnement(String conditionnement) {
        this.conditionnement = conditionnement;
    }

    public String getLaboratoire() {
        return laboratoire;
    }

    public void setLaboratoire(String laboratoire) {
        this.laboratoire = laboratoire;
    }

    public String getTypeMedicament() {
        return typeMedicament;
    }

    public void setTypeMedicament(String typeMedicament) {
        this.typeMedicament = typeMedicament;
    }

    public String getConditionsConservation() {
        return conditionsConservation;
    }

    public void setConditionsConservation(String conditionsConservation) {
        this.conditionsConservation = conditionsConservation;
    }

    public String getCategorie() {
        return categorie;
    }

    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }

    public double getPrixVentePublic() {
        return prixVentePublic != null ? prixVentePublic.doubleValue() : 0.0;
    }

    public void setPrixVentePublic(double prixVentePublic) {
        this.prixVentePublic = BigDecimal.valueOf(prixVentePublic);
    }

    public BigDecimal getPrixVentePublicBD() {
        return prixVentePublic != null ? prixVentePublic : BigDecimal.ZERO;
    }

    public void setPrixVentePublic(BigDecimal prixVentePublic) {
        this.prixVentePublic = prixVentePublic != null ? prixVentePublic : BigDecimal.ZERO;
    }

    public int getSeuilMinimal() {
        return seuilMinimal;
    }

    public void setSeuilMinimal(int seuilMinimal) {
        this.seuilMinimal = seuilMinimal;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public int getQuantiteDisponible() {
        return quantiteDisponible;
    }

    public void setQuantiteDisponible(int quantiteDisponible) {
        this.quantiteDisponible = quantiteDisponible;
    }

    public boolean isOrdonnanceRequise() {
        return ordonnanceRequise;
    }

    public void setOrdonnanceRequise(boolean ordonnanceRequise) {
        this.ordonnanceRequise = ordonnanceRequise;
    }

    @Override
    public String toString() {
        return "Medicament{" +
                "idMedicament=" + idMedicament +
                ", nomCommercial='" + nomCommercial + '\'' +
                ", dci='" + dci + '\'' +
                ", dosage='" + dosage + '\'' +
                ", formePharmaceutique='" + formePharmaceutique + '\'' +
                ", conditionnement='" + conditionnement + '\'' +
                ", laboratoire='" + laboratoire + '\'' +
                ", typeMedicament='" + typeMedicament + '\'' +
                ", conditionsConservation='" + conditionsConservation + '\'' +
                ", categorie='" + categorie + '\'' +
                ", prixVentePublic=" + prixVentePublic +
                ", seuilMinimal=" + seuilMinimal +
                ", statut='" + statut + '\'' +
                ", quantiteDisponible=" + quantiteDisponible +
                ", ordonnanceRequise=" + ordonnanceRequise +
                '}';
    }
}
