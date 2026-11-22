package com.epharmacy.dao;

import com.epharmacy.config.DBConnection;
import com.epharmacy.exception.DataAccessException;
import com.epharmacy.model.Approvisionnement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour la table {@code approvisionnement}.
 * Utilise try-with-resources et SLF4J.
 *
 * @author E-PHARMACIE
 * @version 3.0
 */
public class ApprovisionnementDAO {

    private static final Logger logger = LoggerFactory.getLogger(ApprovisionnementDAO.class);

    private static final String SELECT_WITH_JOINS =
            "SELECT a.*, f.raison_sociale AS nom_fournisseur, " +
            "CONCAT(u.prenom, ' ', u.nom) AS nom_utilisateur " +
            "FROM approvisionnement a " +
            "LEFT JOIN fournisseur f ON a.id_fournisseur = f.id_fournisseur " +
            "LEFT JOIN utilisateur u ON a.id_utilisateur = u.id_utilisateur";

    public List<Approvisionnement> findAll() {
        List<Approvisionnement> list = new ArrayList<>();
        String sql = SELECT_WITH_JOINS + " ORDER BY a.date_approvisionnement DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la récupération des approvisionnements : {}", e.getMessage(), e);
            throw new DataAccessException("Erreur récupération approvisionnements", e);
        }
        return list;
    }

    public Approvisionnement findById(int id) {
        try (Connection conn = DBConnection.getConnection()) {
            return findById(conn, id);
        } catch (SQLException e) {
            logger.error("Erreur de connexion pour findById approvisionnement #{}: {}", id, e.getMessage(), e);
            throw new DataAccessException("Erreur connexion", e);
        }
    }

    public Approvisionnement findById(Connection conn, int id) {
        String sql = SELECT_WITH_JOINS + " WHERE a.id_approvisionnement = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la recherche de l'approvisionnement #{}: {}", id, e.getMessage(), e);
            throw new DataAccessException("Erreur recherche approvisionnement #" + id, e);
        }
        return null;
    }

    public boolean insert(Approvisionnement a) {
        try (Connection conn = DBConnection.getConnection()) {
            return insert(conn, a);
        } catch (SQLException e) {
            logger.error("Erreur de connexion insert approvisionnement : {}", e.getMessage(), e);
            throw new DataAccessException("Erreur connexion", e);
        }
    }

    public boolean insert(Connection conn, Approvisionnement a) {
        String sql = "INSERT INTO approvisionnement (id_fournisseur, id_utilisateur, date_approvisionnement, montant_total, statut) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, a.getIdFournisseur());
            ps.setInt(2, a.getIdUtilisateur());
            ps.setTimestamp(3, Timestamp.valueOf(a.getDateApprovisionnement() != null ? a.getDateApprovisionnement() : java.time.LocalDateTime.now()));
            ps.setBigDecimal(4, a.getMontantTotalBD());
            ps.setString(5, a.getStatut() != null ? a.getStatut() : "En attente");
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        a.setIdApprovisionnement(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de l'insertion de l'approvisionnement : {}", e.getMessage(), e);
            throw new DataAccessException("Erreur insertion approvisionnement", e);
        }
        return false;
    }

    public boolean update(Approvisionnement a) {
        String sql = "UPDATE approvisionnement SET id_fournisseur = ?, montant_total = ?, statut = ? WHERE id_approvisionnement = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, a.getIdFournisseur());
            ps.setBigDecimal(2, a.getMontantTotalBD());
            ps.setString(3, a.getStatut());
            ps.setInt(4, a.getIdApprovisionnement());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Erreur lors de la mise à jour de l'approvisionnement #{}: {}", a.getIdApprovisionnement(), e.getMessage(), e);
            throw new DataAccessException("Erreur mise à jour approvisionnement", e);
        }
    }

    public boolean updateStatut(int id, String statut) {
        try (Connection conn = DBConnection.getConnection()) {
            return updateStatut(conn, id, statut);
        } catch (SQLException e) {
            logger.error("Erreur de connexion updateStatut approvisionnement : {}", e.getMessage(), e);
            throw new DataAccessException("Erreur connexion", e);
        }
    }

    public boolean updateStatut(Connection conn, int id, String statut) {
        String sql = "UPDATE approvisionnement SET statut = ? WHERE id_approvisionnement = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, statut);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Erreur lors de la mise à jour du statut de l'approvisionnement #{}: {}", id, e.getMessage(), e);
            throw new DataAccessException("Erreur mise à jour statut approvisionnement", e);
        }
    }

    public boolean updateMontantTotal(int id, double montant) {
        return updateMontantTotal(id, BigDecimal.valueOf(montant));
    }

    public boolean updateMontantTotal(int id, BigDecimal montant) {
        try (Connection conn = DBConnection.getConnection()) {
            return updateMontantTotal(conn, id, montant);
        } catch (SQLException e) {
            logger.error("Erreur de connexion updateMontantTotal approvisionnement : {}", e.getMessage(), e);
            throw new DataAccessException("Erreur connexion", e);
        }
    }

    public boolean updateMontantTotal(Connection conn, int id, BigDecimal montant) {
        String sql = "UPDATE approvisionnement SET montant_total = ? WHERE id_approvisionnement = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, montant != null ? montant : BigDecimal.ZERO);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Erreur lors de la mise à jour du montant total #{}: {}", id, e.getMessage(), e);
            throw new DataAccessException("Erreur mise à jour montant total", e);
        }
    }

    public int count() {
        String sql = "SELECT COUNT(*) FROM approvisionnement";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            logger.error("Erreur lors du comptage des approvisionnements : {}", e.getMessage(), e);
            throw new DataAccessException("Erreur comptage approvisionnements", e);
        }
        return 0;
    }

    private Approvisionnement mapResultSet(ResultSet rs) throws SQLException {
        Approvisionnement a = new Approvisionnement();
        a.setIdApprovisionnement(rs.getInt("id_approvisionnement"));
        a.setIdFournisseur(rs.getInt("id_fournisseur"));
        a.setIdUtilisateur(rs.getInt("id_utilisateur"));
        Timestamp ts = rs.getTimestamp("date_approvisionnement");
        if (ts != null) a.setDateApprovisionnement(ts.toLocalDateTime());
        a.setMontantTotal(rs.getBigDecimal("montant_total"));
        a.setStatut(rs.getString("statut"));
        try { a.setNomFournisseur(rs.getString("nom_fournisseur")); } catch (SQLException ignored) {}
        try { a.setNomUtilisateur(rs.getString("nom_utilisateur")); } catch (SQLException ignored) {}
        return a;
    }
}
