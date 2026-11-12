package com.epharmacy.model;

/**
 * Représente un médicament dans le catalogue de la pharmacie.
 * <p>
 * Contient les informations pharmaceutiques (DCI, dosage, forme, laboratoire),
 * les informations commerciales (prix de vente public) et les informations
 * de gestion de stock (seuil minimal, quantité disponible).
 * </p>
 *
 * @author E-PHARMACIE
 * @version 1.0
 */
public class Medicament {

    /** Identifiant unique du médicament. */
    private int idMedicament;

    /** Nom commercial du médicament. */
    private String nomCommercial;

    /** Dénomination Commune Internationale (DCI). */
    private String dci;

    /** Dosage du médicament (ex: 500mg, 1g). */
    private String dosage;

    /** Forme pharmaceutique (comprimé, gélule, sirop, etc.). */
    private String formePharmaceutique;

    /** Conditionnement (boîte de 20, flacon 150ml, etc.). */
    private String conditionnement;

    /** Laboratoire fabricant. */
    private String laboratoire;

    /** Type de médicament (GENERIQUE, PRINCEPS, etc.). */
    private String typeMedicament;

    /** Conditions de conservation (température ambiante, réfrigéré, etc.). */
    private String conditionsConservation;

    /** Catégorie du médicament (ANTALGIQUE, ANTIBIOTIQUE, etc.). */
    private String categorie;

    /** Prix de vente public en DH. */
    private double prixVentePublic;

    /** Seuil minimal de stock déclenchant une alerte. */
    private int seuilMinimal;

    /** Statut du médicament (ACTIF/INACTIF). */
    private String statut;

    /** Quantité disponible en stock (calculée à partir des lots). */
    private int quantiteDisponible;

    /**
     * Constructeur sans arguments.
     */
    public Medicament() {
    }

    /**
     * Constructeur avec tous les arguments.
     *
     * @param idMedicament           identifiant unique
     * @param nomCommercial          nom commercial
     * @param dci                    dénomination commune internationale
     * @param dosage                 dosage
     * @param formePharmaceutique    forme pharmaceutique
     * @param conditionnement        conditionnement
     * @param laboratoire            laboratoire fabricant
     * @param typeMedicament         type de médicament
     * @param conditionsConservation conditions de conservation
     * @param categorie              catégorie
     * @param prixVentePublic        prix de vente public (DH)
     * @param seuilMinimal           seuil minimal de stock
     * @param statut                 statut (ACTIF/INACTIF)
     * @param quantiteDisponible     quantité disponible en stock
     */
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
        this.prixVentePublic = prixVentePublic;
        this.seuilMinimal = seuilMinimal;
        this.statut = statut;
        this.quantiteDisponible = quantiteDisponible;
    }

    /**
     * @return l'identifiant unique du médicament
     */
    public int getIdMedicament() {
        return idMedicament;
    }

    /**
     * @param idMedicament l'identifiant unique à définir
     */
    public void setIdMedicament(int idMedicament) {
        this.idMedicament = idMedicament;
    }

    /**
     * @return le nom commercial du médicament
     */
    public String getNomCommercial() {
        return nomCommercial;
    }

    /**
     * @param nomCommercial le nom commercial à définir
     */
    public void setNomCommercial(String nomCommercial) {
        this.nomCommercial = nomCommercial;
    }

    /**
     * @return la dénomination commune internationale (DCI)
     */
    public String getDci() {
        return dci;
    }

    /**
     * @param dci la DCI à définir
     */
    public void setDci(String dci) {
        this.dci = dci;
    }

    /**
     * @return le dosage du médicament
     */
    public String getDosage() {
        return dosage;
    }

    /**
     * @param dosage le dosage à définir
     */
    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    /**
     * @return la forme pharmaceutique
     */
    public String getFormePharmaceutique() {
        return formePharmaceutique;
    }

    /**
     * @param formePharmaceutique la forme pharmaceutique à définir
     */
    public void setFormePharmaceutique(String formePharmaceutique) {
        this.formePharmaceutique = formePharmaceutique;
    }

    /**
     * @return le conditionnement
     */
    public String getConditionnement() {
        return conditionnement;
    }

    /**
     * @param conditionnement le conditionnement à définir
     */
    public void setConditionnement(String conditionnement) {
        this.conditionnement = conditionnement;
    }

    /**
     * @return le laboratoire fabricant
     */
    public String getLaboratoire() {
        return laboratoire;
    }

    /**
     * @param laboratoire le laboratoire fabricant à définir
     */
    public void setLaboratoire(String laboratoire) {
        this.laboratoire = laboratoire;
    }

    /**
     * @return le type de médicament
     */
    public String getTypeMedicament() {
        return typeMedicament;
    }

    /**
     * @param typeMedicament le type de médicament à définir
     */
    public void setTypeMedicament(String typeMedicament) {
        this.typeMedicament = typeMedicament;
    }

    /**
     * @return les conditions de conservation
     */
    public String getConditionsConservation() {
        return conditionsConservation;
    }

    /**
     * @param conditionsConservation les conditions de conservation à définir
     */
    public void setConditionsConservation(String conditionsConservation) {
        this.conditionsConservation = conditionsConservation;
    }

    /**
     * @return la catégorie du médicament
     */
    public String getCategorie() {
        return categorie;
    }

    /**
     * @param categorie la catégorie à définir
     */
    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }

    /**
     * @return le prix de vente public (DH)
     */
    public double getPrixVentePublic() {
        return prixVentePublic;
    }

    /**
     * @param prixVentePublic le prix de vente public à définir (DH)
     */
    public void setPrixVentePublic(double prixVentePublic) {
        this.prixVentePublic = prixVentePublic;
    }

    /**
     * @return le seuil minimal de stock
     */
    public int getSeuilMinimal() {
        return seuilMinimal;
    }

    /**
     * @param seuilMinimal le seuil minimal à définir
     */
    public void setSeuilMinimal(int seuilMinimal) {
        this.seuilMinimal = seuilMinimal;
    }

    /**
     * @return le statut du médicament
     */
    public String getStatut() {
        return statut;
    }

    /**
     * @param statut le statut à définir (ACTIF/INACTIF)
     */
    public void setStatut(String statut) {
        this.statut = statut;
    }

    /**
     * @return la quantité disponible en stock
     */
    public int getQuantiteDisponible() {
        return quantiteDisponible;
    }

    /**
     * @param quantiteDisponible la quantité disponible à définir
     */
    public void setQuantiteDisponible(int quantiteDisponible) {
        this.quantiteDisponible = quantiteDisponible;
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
                '}';
    }
}
