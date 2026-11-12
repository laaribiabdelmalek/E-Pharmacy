package com.epharmacy.service;

import com.epharmacy.config.DBConnection;
import com.epharmacy.dao.*;
import com.epharmacy.model.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

/**
 * Service transactionnel pour les ventes (POS).
 * Gère le commit/rollback JDBC.
 * @author E-PHARMACIE
 */
public class VenteService {
    private final VenteDAO venteDAO = new VenteDAO();
    private final LigneVenteDAO ligneVenteDAO = new LigneVenteDAO();
    private final LotDAO lotDAO = new LotDAO();
    private final MedicamentDAO medicamentDAO = new MedicamentDAO();
    private final ClientDAO clientDAO = new ClientDAO();

    /**
     * Enregistre une vente complète avec gestion transactionnelle.
     */
    public boolean enregistrerVente(Vente vente, List<LigneVente> lignes) {
        Connection conn = null;
        try {
            conn = DBConnection.getInstance().getConnection();
            conn.setAutoCommit(false);

            // 1. Insérer la vente
            if (!venteDAO.insert(vente)) { conn.rollback(); return false; }

            double montantTotal = 0;
            Set<Integer> medIds = new HashSet<>();

            // 2. Traiter chaque ligne
            for (LigneVente lv : lignes) {
                lv.setIdVente(vente.getIdVente());
                if (!ligneVenteDAO.insert(lv)) { conn.rollback(); return false; }

                // Décrémenter le lot
                Lot lot = lotDAO.findById(lv.getIdLot());
                int newQty = lot.getQuantiteRestante() - lv.getQuantite();
                if (newQty < 0) { conn.rollback(); throw new RuntimeException("Stock insuffisant lot " + lot.getNumeroLot()); }
                lotDAO.updateQuantiteRestante(lv.getIdLot(), newQty);

                montantTotal += lv.getSousTotal();

                // Trouver l'ID médicament via le lot
                PreparedStatement ps = conn.prepareStatement(
                    "SELECT la.id_medicament FROM lot l JOIN ligne_approvisionnement la " +
                    "ON l.id_ligne_approvisionnement = la.id_ligne_approvisionnement WHERE l.id_lot = ?");
                ps.setInt(1, lv.getIdLot());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) medIds.add(rs.getInt("id_medicament"));
                rs.close(); ps.close();
            }

            // 3. Mettre à jour le montant total
            vente.setMontantTotal(montantTotal);
            PreparedStatement psUp = conn.prepareStatement("UPDATE vente SET montant_total = ? WHERE id_vente = ?");
            psUp.setDouble(1, montantTotal); psUp.setInt(2, vente.getIdVente());
            psUp.executeUpdate(); psUp.close();

            // 4. Recalculer les stocks médicaments
            for (int medId : medIds) {
                PreparedStatement psQty = conn.prepareStatement(
                    "SELECT COALESCE(SUM(l.quantite_restante), 0) FROM lot l " +
                    "JOIN ligne_approvisionnement la ON l.id_ligne_approvisionnement = la.id_ligne_approvisionnement " +
                    "WHERE la.id_medicament = ? AND l.date_expiration >= CURDATE()");
                psQty.setInt(1, medId);
                ResultSet rsQty = psQty.executeQuery();
                if (rsQty.next()) medicamentDAO.updateQuantite(medId, rsQty.getInt(1));
                rsQty.close(); psQty.close();
            }

            // 5. Gérer le paiement à crédit
            if ("CREDIT".equals(vente.getModePaiement()) && vente.getIdClient() > 0) {
                Client client = clientDAO.findById(vente.getIdClient());
                clientDAO.updateCredit(vente.getIdClient(), client.getCredit() + montantTotal);
            }

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

    public List<Vente> getToutesLesVentes() { return venteDAO.findAll(); }
    public Vente getVenteParId(int id) { return venteDAO.findById(id); }
    public List<LigneVente> getLignesVente(int idVente) { return ligneVenteDAO.findByVente(idVente); }
    public boolean annulerVente(int id) { return venteDAO.updateStatut(id, "ANNULEE"); }
    public int getNombreVentes() { return venteDAO.count(); }
    public double getChiffreAffaires() { return venteDAO.getChiffreAffaires(); }
    public double getChiffreAffairesJour() { return venteDAO.getChiffreAffairesJour(LocalDate.now()); }
}
