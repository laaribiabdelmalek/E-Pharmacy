package com.epharmacy.service;

import com.epharmacy.config.DBConnection;
import com.epharmacy.dao.*;
import com.epharmacy.model.*;
import java.sql.*;
import java.util.List;

/**
 * Service transactionnel pour les approvisionnements.
 * Crée automatiquement les lots et incrémente le stock.
 * @author E-PHARMACIE
 */
public class ApprovisionnementService {
    private final ApprovisionnementDAO approDAO = new ApprovisionnementDAO();
    private final LigneApprovisionnementDAO ligneDAO = new LigneApprovisionnementDAO();
    private final LotDAO lotDAO = new LotDAO();
    private final MedicamentDAO medicamentDAO = new MedicamentDAO();

    /**
     * Enregistre un approvisionnement complet avec création des lots.
     */
    public boolean enregistrerApprovisionnement(Approvisionnement appro,
            List<LigneApprovisionnement> lignes, List<Lot> lots) {
        Connection conn = null;
        try {
            conn = DBConnection.getInstance().getConnection();
            conn.setAutoCommit(false);

            if (!approDAO.insert(appro)) { conn.rollback(); return false; }

            double montantTotal = 0;
            for (int i = 0; i < lignes.size(); i++) {
                LigneApprovisionnement la = lignes.get(i);
                la.setIdApprovisionnement(appro.getIdApprovisionnement());
                if (!ligneDAO.insert(la)) { conn.rollback(); return false; }

                Lot lot = lots.get(i);
                lot.setIdLigneApprovisionnement(la.getIdLigneApprovisionnement());
                lot.setQuantiteInitiale(la.getQuantiteRecue());
                lot.setQuantiteRestante(la.getQuantiteRecue());
                if (!lotDAO.insert(lot)) { conn.rollback(); return false; }

                // Incrémenter le stock du médicament
                Medicament med = medicamentDAO.findById(la.getIdMedicament());
                medicamentDAO.updateQuantite(la.getIdMedicament(), med.getQuantiteDisponible() + la.getQuantiteRecue());

                montantTotal += la.getQuantiteRecue() * la.getPrixAchatUnitaire();
            }

            // Mettre à jour le total et valider
            approDAO.updateMontantTotal(appro.getIdApprovisionnement(), montantTotal);
            approDAO.updateStatut(appro.getIdApprovisionnement(), "VALIDEE");
            appro.setMontantTotal(montantTotal);

            conn.commit();
            return true;
        } catch (Exception e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) try { conn.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    public List<Approvisionnement> getTous() { return approDAO.findAll(); }
    public Approvisionnement getParId(int id) { return approDAO.findById(id); }
    public List<LigneApprovisionnement> getLignes(int idAppro) { return ligneDAO.findByApprovisionnement(idAppro); }
    public int getNombreTotal() { return approDAO.count(); }
}
