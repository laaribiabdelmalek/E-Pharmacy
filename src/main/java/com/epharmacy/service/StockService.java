package com.epharmacy.service;

import com.epharmacy.config.DBConnection;
import com.epharmacy.dao.LotDAO;
import com.epharmacy.dao.MedicamentDAO;
import com.epharmacy.exception.DataAccessException;
import com.epharmacy.exception.InsufficientStockException;
import com.epharmacy.model.LigneVente;
import com.epharmacy.model.Lot;
import com.epharmacy.model.Medicament;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Service de gestion du stock et traçabilité des lots (FEFO).
 *
 * @author E-PHARMACIE
 * @version 3.0
 */
public class StockService {

    private static final Logger logger = LoggerFactory.getLogger(StockService.class);

    private final LotDAO lotDAO;
    private final MedicamentDAO medicamentDAO;

    public StockService() {
        this(new LotDAO(), new MedicamentDAO());
    }

    public StockService(LotDAO lotDAO, MedicamentDAO medicamentDAO) {
        this.lotDAO = lotDAO;
        this.medicamentDAO = medicamentDAO;
    }

    public List<Lot> getTousLesLots() {
        return lotDAO.findAll();
    }

    public List<Lot> getLotsDisponibles(int idMedicament) {
        return lotDAO.findDisponiblesByMedicament(idMedicament);
    }

    public List<Lot> getLotsExpirationProche() {
        return lotDAO.findExpirationProche(90);
    }

    public List<Lot> getLotsExpires() {
        return lotDAO.findExpires();
    }

    /**
     * Prépare les lignes de vente en décrémentant selon la stratégie FEFO (double).
     */
    public List<LigneVente> decrementerStock(int idMedicament, int quantiteDemandee, double prixUnitaireVente) {
        return decrementerStock(idMedicament, quantiteDemandee, BigDecimal.valueOf(prixUnitaireVente));
    }

    /**
     * Prépare les lignes de vente en décrémentant selon la stratégie FEFO avec BigDecimal.
     *
     * @throws InsufficientStockException si stock insuffisant
     */
    public List<LigneVente> decrementerStock(int idMedicament, int quantiteDemandee, BigDecimal prixUnitaireVente) {
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
            if (prixUnitaireVente != null) {
                lv.setSousTotal(prixUnitaireVente.multiply(BigDecimal.valueOf(aPrendre)).setScale(2, RoundingMode.HALF_UP));
            }
            lv.setNomMedicament(lot.getNomMedicament());
            lv.setNumeroLot(lot.getNumeroLot());
            lignes.add(lv);
            restant -= aPrendre;
        }

        if (restant > 0) {
            int disponible = quantiteDemandee - restant;
            throw new InsufficientStockException(idMedicament, quantiteDemandee, disponible);
        }
        return lignes;
    }

    /**
     * Recalcule la quantité totale disponible et ajuste automatiquement le statut
     * du médicament ('EN_RUPTURE' ou 'COMMERCIALISE') au sein d'une transaction.
     */
    public void recalculerStockDisponible(Connection conn, int idMedicament) {
        String sql = "SELECT COALESCE(SUM(l.quantite_restante), 0) " +
                     "FROM lot l " +
                     "JOIN ligne_approvisionnement la ON l.id_ligne_approvisionnement = la.id_ligne_approvisionnement " +
                     "WHERE la.id_medicament = ? AND l.quantite_restante > 0 AND l.date_expiration >= CURDATE()";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idMedicament);
            int quantiteTotal = 0;
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    quantiteTotal = rs.getInt(1);
                }
            }

            medicamentDAO.updateQuantite(conn, idMedicament, quantiteTotal);

            // Ajustement automatique du statut
            Medicament m = medicamentDAO.findById(conn, idMedicament);
            if (m != null) {
                if (quantiteTotal == 0 && "COMMERCIALISE".equalsIgnoreCase(m.getStatut())) {
                    medicamentDAO.updateStatut(conn, idMedicament, "EN_RUPTURE");
                    logger.info("Médicament #{} '{}' passé automatiquement en statut 'EN_RUPTURE'", idMedicament, m.getNomCommercial());
                } else if (quantiteTotal > 0 && "EN_RUPTURE".equalsIgnoreCase(m.getStatut())) {
                    medicamentDAO.updateStatut(conn, idMedicament, "COMMERCIALISE");
                    logger.info("Médicament #{} '{}' repassé en statut 'COMMERCIALISE'", idMedicament, m.getNomCommercial());
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur lors du recalcul du stock pour le médicament #{}: {}", idMedicament, e.getMessage(), e);
            throw new DataAccessException("Erreur recalcul stock", e);
        }
    }

    /**
     * Recalcule la quantité totale disponible de façon autonome.
     */
    public void recalculerStockDisponible(int idMedicament) {
        try (Connection conn = DBConnection.getConnection()) {
            recalculerStockDisponible(conn, idMedicament);
        } catch (SQLException e) {
            logger.error("Erreur connexion pour recalculerStockDisponible(#{}) : {}", idMedicament, e.getMessage(), e);
            throw new DataAccessException("Erreur connexion", e);
        }
    }

    /**
     * Méthode de rétrocompatibilité.
     */
    public void recalculerQuantiteDisponible(int idMedicament) {
        recalculerStockDisponible(idMedicament);
    }
}
