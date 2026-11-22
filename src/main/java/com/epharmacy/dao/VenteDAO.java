package com.epharmacy.dao;

import com.epharmacy.config.DBConnection;
import com.epharmacy.exception.DataAccessException;
import com.epharmacy.model.Vente;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour la table {@code vente}.
 * Utilise try-with-resources et SLF4J.
 *
 * @author E-PHARMACIE
 * @version 3.0
 */
public class VenteDAO {

    private static final Logger logger = LoggerFactory.getLogger(VenteDAO.class);

    private static final String SELECT_WITH_JOINS =
            "SELECT v.*, " +
            "CONCAT(u.prenom, ' ', u.nom) AS nom_utilisateur, " +
            "CONCAT(c.prenom, ' ', c.nom) AS nom_client " +
            "FROM vente v " +
            "LEFT JOIN utilisateur u ON v.id_utilisateur = u.id_utilisateur " +
            "LEFT JOIN client c ON v.id_client = c.id_client";

    public List<Vente> findAll() {
        List<Vente> list = new ArrayList<>();
        String sql = SELECT_WITH_JOINS + " ORDER BY v.date_vente DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la récupération des ventes : {}", e.getMessage(), e);
            throw new DataAccessException("Erreur récupération ventes", e);
        }
        return list;
    }

    public Vente findById(int id) {
        try (Connection conn = DBConnection.getConnection()) {
            return findById(conn, id);
        } catch (SQLException e) {
            logger.error("Erreur de connexion pour findById vente #{}: {}", id, e.getMessage(), e);
            throw new DataAccessException("Erreur connexion", e);
        }
    }

    public Vente findById(Connection conn, int id) {
        String sql = SELECT_WITH_JOINS + " WHERE v.id_vente = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la recherche de la vente #{}: {}", id, e.getMessage(), e);
            throw new DataAccessException("Erreur recherche vente #" + id, e);
        }
        return null;
    }

    public List<Vente> findByDate(LocalDate date) {
        List<Vente> list = new ArrayList<>();
        String sql = SELECT_WITH_JOINS + " WHERE DATE(v.date_vente) = ? ORDER BY v.date_vente DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la recherche des ventes par date ({}): {}", date, e.getMessage(), e);
            throw new DataAccessException("Erreur recherche ventes par date", e);
        }
        return list;
    }

    public boolean insert(Vente v) {
        try (Connection conn = DBConnection.getConnection()) {
            return insert(conn, v);
        } catch (SQLException e) {
            logger.error("Erreur connexion insert vente : {}", e.getMessage(), e);
            throw new DataAccessException("Erreur connexion", e);
        }
    }

    public boolean insert(Connection conn, Vente v) {
        String sql = "INSERT INTO vente (id_utilisateur, id_client, date_vente, montant_total, mode_paiement, statut) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, v.getIdUtilisateur());
            if (v.getIdClient() > 0) {
                ps.setInt(2, v.getIdClient());
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            ps.setTimestamp(3, Timestamp.valueOf(v.getDateVente() != null ? v.getDateVente() : java.time.LocalDateTime.now()));
            ps.setBigDecimal(4, v.getMontantTotalBD());
            ps.setString(5, v.getModePaiement());
            ps.setString(6, v.getStatut() != null ? v.getStatut() : "VALIDEE");
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        v.setIdVente(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de l'insertion de la vente : {}", e.getMessage(), e);
            throw new DataAccessException("Erreur insertion vente", e);
        }
        return false;
    }

    public boolean updateStatut(int id, String statut) {
        try (Connection conn = DBConnection.getConnection()) {
            return updateStatut(conn, id, statut);
        } catch (SQLException e) {
            logger.error("Erreur connexion updateStatut vente : {}", e.getMessage(), e);
            throw new DataAccessException("Erreur connexion", e);
        }
    }

    public boolean updateStatut(Connection conn, int id, String statut) {
        String sql = "UPDATE vente SET statut = ? WHERE id_vente = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, statut);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Erreur lors de la mise à jour du statut de la vente #{}: {}", id, e.getMessage(), e);
            throw new DataAccessException("Erreur mise à jour statut vente", e);
        }
    }

    public int count() {
        String sql = "SELECT COUNT(*) FROM vente";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            logger.error("Erreur lors du comptage des ventes : {}", e.getMessage(), e);
            throw new DataAccessException("Erreur comptage ventes", e);
        }
        return 0;
    }

    public double getChiffreAffaires() {
        String sql = "SELECT COALESCE(SUM(montant_total), 0) FROM vente WHERE statut = 'VALIDEE'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            logger.error("Erreur lors du calcul du CA global : {}", e.getMessage(), e);
            throw new DataAccessException("Erreur calcul CA", e);
        }
        return 0.0;
    }

    public double getChiffreAffairesJour(LocalDate date) {
        String sql = "SELECT COALESCE(SUM(montant_total), 0) FROM vente WHERE statut = 'VALIDEE' AND DATE(date_vente) = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble(1);
            }
        } catch (SQLException e) {
            logger.error("Erreur lors du calcul du CA journalier ({}) : {}", date, e.getMessage(), e);
            throw new DataAccessException("Erreur calcul CA journalier", e);
        }
        return 0.0;
    }

    private Vente mapResultSet(ResultSet rs) throws SQLException {
        Vente v = new Vente();
        v.setIdVente(rs.getInt("id_vente"));
        v.setIdUtilisateur(rs.getInt("id_utilisateur"));
        v.setIdClient(rs.getInt("id_client"));
        Timestamp ts = rs.getTimestamp("date_vente");
        if (ts != null) v.setDateVente(ts.toLocalDateTime());
        v.setMontantTotal(rs.getBigDecimal("montant_total"));
        v.setModePaiement(rs.getString("mode_paiement"));
        v.setStatut(rs.getString("statut"));
        try { v.setNomUtilisateur(rs.getString("nom_utilisateur")); } catch (SQLException ignored) {}
        try { v.setNomClient(rs.getString("nom_client")); } catch (SQLException ignored) {}
        return v;
    }
}
