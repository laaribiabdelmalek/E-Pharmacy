package com.epharmacy.service;

import com.epharmacy.dao.LotDAO;
import com.epharmacy.dao.MedicamentDAO;
import com.epharmacy.model.Lot;
import com.epharmacy.model.LigneVente;
import java.util.ArrayList;
import java.util.List;

/**
 * Service de gestion du stock et traçabilité des lots (FEFO).
 * @author E-PHARMACIE
 */
public class StockService {
    private final LotDAO lotDAO = new LotDAO();
    private final MedicamentDAO medicamentDAO = new MedicamentDAO();

    public List<Lot> getTousLesLots() { return lotDAO.findAll(); }
    public List<Lot> getLotsDisponibles(int idMedicament) { return lotDAO.findDisponiblesByMedicament(idMedicament); }
    public List<Lot> getLotsExpirationProche() { return lotDAO.findExpirationProche(90); }
    public List<Lot> getLotsExpires() { return lotDAO.findExpires(); }

    /**
     * Prépare les lignes de vente en décrémentant selon la stratégie FEFO.
     * @throws RuntimeException si stock insuffisant
     */
    public List<LigneVente> decrementerStock(int idMedicament, int quantiteDemandee, double prixUnitaireVente) {
        List<Lot> lots = lotDAO.findDisponiblesByMedicament(idMedicament);
        List<LigneVente> lignes = new ArrayList<>();
        int restant = quantiteDemandee;

        for (Lot lot : lots) {
            if (restant <= 0) break;
            int aPrendre = Math.min(restant, lot.getQuantiteRestante());

            LigneVente lv = new LigneVente();
            lv.setIdLot(lot.getIdLot());
            lv.setQuantite(aPrendre);
            lv.setPrixUnitaireVente(prixUnitaireVente);
            lv.setSousTotal(aPrendre * prixUnitaireVente);
            lv.setNomMedicament(lot.getNomMedicament());
            lv.setNumeroLot(lot.getNumeroLot());
            lignes.add(lv);
            restant -= aPrendre;
        }

        if (restant > 0) {
            throw new RuntimeException("Stock insuffisant. Demandé: " + quantiteDemandee
                    + ", Disponible: " + (quantiteDemandee - restant));
        }
        return lignes;
    }

    /** Recalcule la quantité disponible d'un médicament depuis ses lots. */
    public void recalculerQuantiteDisponible(int idMedicament) {
        List<Lot> lots = lotDAO.findDisponiblesByMedicament(idMedicament);
        int total = lots.stream().mapToInt(Lot::getQuantiteRestante).sum();
        medicamentDAO.updateQuantite(idMedicament, total);
    }
}
