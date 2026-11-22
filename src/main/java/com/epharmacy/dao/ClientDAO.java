package com.epharmacy.dao;

import com.epharmacy.config.DBConnection;
import com.epharmacy.exception.DataAccessException;
import com.epharmacy.model.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour la table {@code client}.
 * Utilise try-with-resources et SLF4J.
 *
 * @author E-PHARMACIE
 * @version 3.0
 */
public class ClientDAO {

    private static final Logger logger = LoggerFactory.getLogger(ClientDAO.class);

    public List<Client> findAll() {
        List<Client> list = new ArrayList<>();
        String sql = "SELECT * FROM client ORDER BY nom, prenom";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la récupération des clients : {}", e.getMessage(), e);
            throw new DataAccessException("Erreur récupération clients", e);
        }
        return list;
    }

    public Client findById(int id) {
        try (Connection conn = DBConnection.getConnection()) {
            return findById(conn, id);
        } catch (SQLException e) {
            logger.error("Erreur de connexion pour findById({}) : {}", id, e.getMessage(), e);
            throw new DataAccessException("Erreur connexion", e);
        }
    }

    public Client findById(Connection conn, int id) {
        return findById(conn, id, false);
    }

    public Client findById(Connection conn, int id, boolean forUpdate) {
        String sql = "SELECT * FROM client WHERE id_client = ?" + (forUpdate ? " FOR UPDATE" : "");
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la recherche du client #{}: {}", id, e.getMessage(), e);
            throw new DataAccessException("Erreur recherche client #" + id, e);
        }
        return null;
    }

    public List<Client> search(String keyword) {
        List<Client> list = new ArrayList<>();
        String sql = "SELECT * FROM client WHERE nom LIKE ? OR prenom LIKE ? OR telephone LIKE ? ORDER BY nom, prenom";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String pattern = "%" + keyword + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la recherche de clients ('{}') : {}", keyword, e.getMessage(), e);
            throw new DataAccessException("Erreur recherche clients", e);
        }
        return list;
    }

    public boolean insert(Client c) {
        String sql = "INSERT INTO client (nom, prenom, telephone, email, adresse, credit, plafond_credit, statut) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getNom());
            ps.setString(2, c.getPrenom());
            ps.setString(3, c.getTelephone());
            ps.setString(4, c.getEmail());
            ps.setString(5, c.getAdresse());
            ps.setBigDecimal(6, c.getCreditBD());
            ps.setBigDecimal(7, c.getPlafondCredit());
            ps.setString(8, c.getStatut() != null ? c.getStatut() : "ACTIF");
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) c.setIdClient(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de l'insertion du client : {}", e.getMessage(), e);
            throw new DataAccessException("Erreur insertion client", e);
        }
        return false;
    }

    public boolean update(Client c) {
        String sql = "UPDATE client SET nom=?, prenom=?, telephone=?, email=?, adresse=?, credit=?, plafond_credit=?, statut=? " +
                     "WHERE id_client=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getNom());
            ps.setString(2, c.getPrenom());
            ps.setString(3, c.getTelephone());
            ps.setString(4, c.getEmail());
            ps.setString(5, c.getAdresse());
            ps.setBigDecimal(6, c.getCreditBD());
            ps.setBigDecimal(7, c.getPlafondCredit());
            ps.setString(8, c.getStatut());
            ps.setInt(9, c.getIdClient());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Erreur lors de la mise à jour du client #{}: {}", c.getIdClient(), e.getMessage(), e);
            throw new DataAccessException("Erreur mise à jour client", e);
        }
    }

    /**
     * Désactivation logique du client (remplace la suppression physique).
     */
    public boolean delete(int id) {
        String sql = "UPDATE client SET statut = 'INACTIF' WHERE id_client = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Erreur lors de la désactivation logique du client #{}: {}", id, e.getMessage(), e);
            throw new DataAccessException("Erreur désactivation client", e);
        }
    }

    public boolean updateCredit(int id, double nouveauCredit) {
        return updateCredit(id, BigDecimal.valueOf(nouveauCredit));
    }

    public boolean updateCredit(int id, BigDecimal nouveauCredit) {
        try (Connection conn = DBConnection.getConnection()) {
            return updateCredit(conn, id, nouveauCredit);
        } catch (SQLException e) {
            logger.error("Erreur de connexion pour updateCredit : {}", e.getMessage(), e);
            throw new DataAccessException("Erreur connexion", e);
        }
    }

    public boolean updateCredit(Connection conn, int id, BigDecimal nouveauCredit) {
        String sql = "UPDATE client SET credit = ? WHERE id_client = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, nouveauCredit != null ? nouveauCredit : BigDecimal.ZERO);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Erreur lors de la mise à jour du crédit du client #{}: {}", id, e.getMessage(), e);
            throw new DataAccessException("Erreur mise à jour crédit client", e);
        }
    }

    public int count() {
        String sql = "SELECT COUNT(*) FROM client";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            logger.error("Erreur lors du comptage des clients : {}", e.getMessage(), e);
            throw new DataAccessException("Erreur comptage clients", e);
        }
        return 0;
    }

    private Client mapResultSet(ResultSet rs) throws SQLException {
        Client c = new Client();
        c.setIdClient(rs.getInt("id_client"));
        c.setNom(rs.getString("nom"));
        c.setPrenom(rs.getString("prenom"));
        c.setTelephone(rs.getString("telephone"));
        c.setEmail(rs.getString("email"));
        c.setAdresse(rs.getString("adresse"));
        Date d = rs.getDate("date_inscription");
        if (d != null) c.setDateInscription(d.toLocalDate());
        c.setCredit(rs.getBigDecimal("credit"));
        try {
            c.setPlafondCredit(rs.getBigDecimal("plafond_credit"));
            c.setStatut(rs.getString("statut"));
        } catch (SQLException ignored) {
            // Tolérance si les colonnes ne sont pas présentes
        }
        return c;
    }
}
