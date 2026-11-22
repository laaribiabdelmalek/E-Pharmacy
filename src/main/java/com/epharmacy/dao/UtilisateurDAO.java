package com.epharmacy.dao;

import com.epharmacy.config.DBConnection;
import com.epharmacy.exception.DataAccessException;
import com.epharmacy.model.Utilisateur;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour la gestion des opérations CRUD sur la table {@code utilisateur}.
 * Utilise try-with-resources et SLF4J.
 *
 * @author E-PHARMACIE
 * @version 3.0
 */
public class UtilisateurDAO {

    private static final Logger logger = LoggerFactory.getLogger(UtilisateurDAO.class);

    public Utilisateur findByLogin(String login) {
        String sql = "SELECT * FROM utilisateur WHERE login = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, login);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la recherche par login ('{}') : {}", login, e.getMessage(), e);
            throw new DataAccessException("Erreur recherche utilisateur", e);
        }
        return null;
    }

    public List<Utilisateur> findAll() {
        List<Utilisateur> list = new ArrayList<>();
        String sql = "SELECT * FROM utilisateur ORDER BY nom";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la récupération des utilisateurs : {}", e.getMessage(), e);
            throw new DataAccessException("Erreur récupération utilisateurs", e);
        }
        return list;
    }

    public Utilisateur findById(int id) {
        String sql = "SELECT * FROM utilisateur WHERE id_utilisateur = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur recherche utilisateur #{}: {}", id, e.getMessage(), e);
            throw new DataAccessException("Erreur recherche utilisateur #" + id, e);
        }
        return null;
    }

    public boolean insert(Utilisateur u) {
        String sql = "INSERT INTO utilisateur (nom, prenom, login, mot_de_passe_hash, role, statut) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, u.getNom());
            ps.setString(2, u.getPrenom());
            ps.setString(3, u.getLogin());
            ps.setString(4, u.getMotDePasseHash());
            ps.setString(5, u.getRole());
            ps.setString(6, u.getStatut() != null ? u.getStatut() : "ACTIF");
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        u.setIdUtilisateur(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de l'insertion de l'utilisateur : {}", e.getMessage(), e);
            throw new DataAccessException("Erreur insertion utilisateur", e);
        }
        return false;
    }

    public boolean update(Utilisateur u) {
        String sql = "UPDATE utilisateur SET nom = ?, prenom = ?, login = ?, " +
                     "role = ?, statut = ? WHERE id_utilisateur = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, u.getNom());
            ps.setString(2, u.getPrenom());
            ps.setString(3, u.getLogin());
            ps.setString(4, u.getRole());
            ps.setString(5, u.getStatut());
            ps.setInt(6, u.getIdUtilisateur());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Erreur lors de la mise à jour de l'utilisateur #{}: {}", u.getIdUtilisateur(), e.getMessage(), e);
            throw new DataAccessException("Erreur mise à jour utilisateur", e);
        }
    }

    public boolean updatePassword(int id, String newHash) {
        String sql = "UPDATE utilisateur SET mot_de_passe_hash = ? WHERE id_utilisateur = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newHash);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Erreur lors de la mise à jour du mot de passe (#{}: {}", id, e.getMessage(), e);
            throw new DataAccessException("Erreur mise à jour mot de passe", e);
        }
    }

    /**
     * Désactivation logique de l'utilisateur (remplace la suppression physique).
     */
    public boolean delete(int id) {
        return updateStatut(id, "INACTIF");
    }

    public boolean updateStatut(int id, String statut) {
        String sql = "UPDATE utilisateur SET statut = ? WHERE id_utilisateur = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, statut);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Erreur lors de la mise à jour du statut (#{}: {}", id, e.getMessage(), e);
            throw new DataAccessException("Erreur mise à jour statut utilisateur", e);
        }
    }

    /**
     * Compte le nombre d'administrateurs actuellement actifs.
     */
    public int countActiveAdmins() {
        String sql = "SELECT COUNT(*) FROM utilisateur WHERE role = 'ADMIN' AND statut = 'ACTIF'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.error("Erreur lors du comptage des admins actifs : {}", e.getMessage(), e);
            throw new DataAccessException("Erreur comptage administrateurs", e);
        }
        return 0;
    }

    private Utilisateur mapResultSet(ResultSet rs) throws SQLException {
        Utilisateur u = new Utilisateur();
        u.setIdUtilisateur(rs.getInt("id_utilisateur"));
        u.setNom(rs.getString("nom"));
        u.setPrenom(rs.getString("prenom"));
        u.setLogin(rs.getString("login"));
        u.setMotDePasseHash(rs.getString("mot_de_passe_hash"));
        u.setRole(rs.getString("role"));
        u.setStatut(rs.getString("statut"));
        Timestamp ts = rs.getTimestamp("date_creation");
        if (ts != null) {
            u.setDateCreation(ts.toLocalDateTime());
        }
        return u;
    }
}
