package com.epharmacy.dao;

import com.epharmacy.config.DBConnection;
import com.epharmacy.exception.DataAccessException;
import com.epharmacy.model.LigneVente;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour la table {@code ligne_vente}.
 * Utilise try-with-resources et SLF4J.
 *
 * @author E-PHARMACIE
 * @version 3.0
 */
public class LigneVenteDAO {

    private static final Logger logger = LoggerFactory.getLogger(LigneVenteDAO.class);

    private static final String SQL_FIND_BY_VENTE =
            "SELECT lv.*, lo.numero_lot, m.nom_commercial AS nom_medicament, m.dosage " +
            "FROM ligne_vente lv " +
            "JOIN lot lo ON lv.id_lot = lo.id_lot " +
            "JOIN ligne_approvisionnement la ON lo.id_ligne_approvisionnement = la.id_ligne_approvisionnement " +
            "JOIN medicament m ON la.id_medicament = m.id_medicament " +
            "WHERE lv.id_vente = ? ORDER BY lv.id_ligne_vente";

    public List<LigneVente> findByVente(int idVente) {
        try (Connection conn = DBConnection.getConnection()) {
            return findByVente(conn, idVente);
        } catch (SQLException e) {
            logger.error("Erreur connexion findByVente #{}: {}", idVente, e.getMessage(), e);
            throw new DataAccessException("Erreur connexion", e);
        }
    }

    public List<LigneVente> findByVente(Connection conn, int idVente) {
        List<LigneVente> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_VENTE)) {
            ps.setInt(1, idVente);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la récupération des lignes de la vente #{}: {}", idVente, e.getMessage(), e);
            throw new DataAccessException("Erreur récupération lignes vente", e);
        }
        return list;
    }

    public boolean insert(LigneVente lv) {
        try (Connection conn = DBConnection.getConnection()) {
            return insert(conn, lv);
        } catch (SQLException e) {
            logger.error("Erreur connexion insert ligne vente : {}", e.getMessage(), e);
            throw new DataAccessException("Erreur connexion", e);
        }
    }

    public boolean insert(Connection conn, LigneVente lv) {
        String sql = "INSERT INTO ligne_vente (id_vente, id_lot, quantite, prix_unitaire_vente, sous_total) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, lv.getIdVente());
            ps.setInt(2, lv.getIdLot());
            ps.setInt(3, lv.getQuantite());
            ps.setBigDecimal(4, lv.getPrixUnitaireVenteBD());
            ps.setBigDecimal(5, lv.getSousTotalBD());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        lv.setIdLigneVente(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de l'insertion de la ligne de vente : {}", e.getMessage(), e);
            throw new DataAccessException("Erreur insertion ligne vente", e);
        }
        return false;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM ligne_vente WHERE id_ligne_vente = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Erreur lors de la suppression de la ligne de vente #{}: {}", id, e.getMessage(), e);
            throw new DataAccessException("Erreur suppression ligne vente", e);
        }
    }

    private LigneVente mapResultSet(ResultSet rs) throws SQLException {
        LigneVente lv = new LigneVente();
        lv.setIdLigneVente(rs.getInt("id_ligne_vente"));
        lv.setIdVente(rs.getInt("id_vente"));
        lv.setIdLot(rs.getInt("id_lot"));
        lv.setQuantite(rs.getInt("quantite"));
        lv.setPrixUnitaireVente(rs.getBigDecimal("prix_unitaire_vente"));
        lv.setSousTotal(rs.getBigDecimal("sous_total"));
        try { lv.setNumeroLot(rs.getString("numero_lot")); } catch (SQLException ignored) {}
        try { lv.setNomMedicament(rs.getString("nom_medicament")); } catch (SQLException ignored) {}
        try { lv.setDosage(rs.getString("dosage")); } catch (SQLException ignored) {}
        return lv;
    }
}
