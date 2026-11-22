package com.epharmacy.dao;

import com.epharmacy.config.DBConnection;
import com.epharmacy.exception.DataAccessException;
import com.epharmacy.model.Lot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour la table {@code lot}.
 * Utilise try-with-resources et SLF4J.
 *
 * @author E-PHARMACIE
 * @version 3.0
 */
public class LotDAO {

    private static final Logger logger = LoggerFactory.getLogger(LotDAO.class);

    private static final String SELECT_WITH_JOIN =
            "SELECT l.*, m.nom_commercial AS nom_medicament, la.id_medicament " +
            "FROM lot l " +
            "JOIN ligne_approvisionnement la ON l.id_ligne_approvisionnement = la.id_ligne_approvisionnement " +
            "JOIN medicament m ON la.id_medicament = m.id_medicament";

    public List<Lot> findAll() {
        List<Lot> list = new ArrayList<>();
        String sql = SELECT_WITH_JOIN + " ORDER BY l.date_expiration ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la récupération des lots : {}", e.getMessage(), e);
            throw new DataAccessException("Erreur récupération lots", e);
        }
        return list;
    }

    public List<Lot> findByMedicament(int idMedicament) {
        List<Lot> list = new ArrayList<>();
        String sql = SELECT_WITH_JOIN + " WHERE la.id_medicament = ? ORDER BY l.date_expiration ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idMedicament);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la recherche des lots pour médicament #{} : {}", idMedicament, e.getMessage(), e);
            throw new DataAccessException("Erreur recherche lots", e);
        }
        return list;
    }

    /**
     * Lots disponibles triés FEFO (premier expiré, premier sorti).
     */
    public List<Lot> findDisponiblesByMedicament(int idMedicament) {
        try (Connection conn = DBConnection.getConnection()) {
            return findDisponiblesByMedicament(conn, idMedicament, false);
        } catch (SQLException e) {
            logger.error("Erreur connexion pour findDisponiblesByMedicament({}) : {}", idMedicament, e.getMessage(), e);
            throw new DataAccessException("Erreur connexion", e);
        }
    }

    /**
     * Lots disponibles triés FEFO au sein d'une transaction avec verrouillage optionnel FOR UPDATE.
     */
    public List<Lot> findDisponiblesByMedicament(Connection conn, int idMedicament, boolean forUpdate) {
        List<Lot> list = new ArrayList<>();
        String sql = SELECT_WITH_JOIN + " WHERE la.id_medicament = ? AND l.quantite_restante > 0 " +
                     "AND l.date_expiration >= CURDATE() ORDER BY l.date_expiration ASC" +
                     (forUpdate ? " FOR UPDATE" : "");
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idMedicament);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la recherche transactionnelle FEFO (médicament #{}) : {}", idMedicament, e.getMessage(), e);
            throw new DataAccessException("Erreur recherche FEFO", e);
        }
        return list;
    }

    public List<Lot> findExpirationProche(int joursAvant) {
        List<Lot> list = new ArrayList<>();
        String sql = SELECT_WITH_JOIN +
                     " WHERE l.date_expiration <= DATE_ADD(CURDATE(), INTERVAL ? DAY) " +
                     "AND l.date_expiration >= CURDATE() AND l.quantite_restante > 0 " +
                     "ORDER BY l.date_expiration ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, joursAvant);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la recherche expiration proche ({}j) : {}", joursAvant, e.getMessage(), e);
            throw new DataAccessException("Erreur expiration proche", e);
        }
        return list;
    }

    public List<Lot> findExpires() {
        List<Lot> list = new ArrayList<>();
        String sql = SELECT_WITH_JOIN + " WHERE l.date_expiration < CURDATE() AND l.quantite_restante > 0 " +
                     "ORDER BY l.date_expiration ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la recherche des lots expirés : {}", e.getMessage(), e);
            throw new DataAccessException("Erreur lots expirés", e);
        }
        return list;
    }

    public Lot findById(int id) {
        try (Connection conn = DBConnection.getConnection()) {
            return findById(conn, id);
        } catch (SQLException e) {
            logger.error("Erreur connexion findById lot #{} : {}", id, e.getMessage(), e);
            throw new DataAccessException("Erreur connexion", e);
        }
    }

    public Lot findById(Connection conn, int id) {
        String sql = SELECT_WITH_JOIN + " WHERE l.id_lot = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur recherche lot #{} : {}", id, e.getMessage(), e);
            throw new DataAccessException("Erreur recherche lot #" + id, e);
        }
        return null;
    }

    public boolean insert(Lot lot) {
        try (Connection conn = DBConnection.getConnection()) {
            return insert(conn, lot);
        } catch (SQLException e) {
            logger.error("Erreur connexion insert lot : {}", e.getMessage(), e);
            throw new DataAccessException("Erreur connexion", e);
        }
    }

    public boolean insert(Connection conn, Lot lot) {
        String sql = "INSERT INTO lot (id_ligne_approvisionnement, numero_lot, date_expiration, " +
                     "quantite_initiale, quantite_restante) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, lot.getIdLigneApprovisionnement());
            ps.setString(2, lot.getNumeroLot());
            ps.setDate(3, Date.valueOf(lot.getDateExpiration()));
            ps.setInt(4, lot.getQuantiteInitiale());
            ps.setInt(5, lot.getQuantiteRestante());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) lot.setIdLot(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de l'insertion du lot : {}", e.getMessage(), e);
            throw new DataAccessException("Erreur insertion lot", e);
        }
        return false;
    }

    public boolean updateQuantiteRestante(int id, int quantiteRestante) {
        try (Connection conn = DBConnection.getConnection()) {
            return updateQuantiteRestante(conn, id, quantiteRestante);
        } catch (SQLException e) {
            logger.error("Erreur connexion updateQuantiteRestante lot #{} : {}", id, e.getMessage(), e);
            throw new DataAccessException("Erreur connexion", e);
        }
    }

    public boolean updateQuantiteRestante(Connection conn, int id, int quantiteRestante) {
        String sql = "UPDATE lot SET quantite_restante = ? WHERE id_lot = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quantiteRestante);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Erreur lors de la mise à jour de la quantité restante du lot #{} : {}", id, e.getMessage(), e);
            throw new DataAccessException("Erreur mise à jour quantité lot", e);
        }
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM lot WHERE id_lot = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Erreur lors de la suppression du lot #{} : {}", id, e.getMessage(), e);
            throw new DataAccessException("Erreur suppression lot", e);
        }
    }

    private Lot mapResultSet(ResultSet rs) throws SQLException {
        Lot lot = new Lot();
        lot.setIdLot(rs.getInt("id_lot"));
        lot.setIdLigneApprovisionnement(rs.getInt("id_ligne_approvisionnement"));
        lot.setNumeroLot(rs.getString("numero_lot"));
        Date dateExp = rs.getDate("date_expiration");
        if (dateExp != null) lot.setDateExpiration(dateExp.toLocalDate());
        lot.setQuantiteInitiale(rs.getInt("quantite_initiale"));
        lot.setQuantiteRestante(rs.getInt("quantite_restante"));
        lot.setNomMedicament(rs.getString("nom_medicament"));
        lot.setIdMedicament(rs.getInt("id_medicament"));
        return lot;
    }
}
