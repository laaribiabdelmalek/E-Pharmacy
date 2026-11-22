package com.epharmacy.dao;

import com.epharmacy.config.DBConnection;
import com.epharmacy.exception.DataAccessException;
import com.epharmacy.model.Fournisseur;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour la table {@code fournisseur}.
 * Utilise try-with-resources et SLF4J.
 *
 * @author E-PHARMACIE
 * @version 3.0
 */
public class FournisseurDAO {

    private static final Logger logger = LoggerFactory.getLogger(FournisseurDAO.class);

    public List<Fournisseur> findAll() {
        List<Fournisseur> list = new ArrayList<>();
        String sql = "SELECT * FROM fournisseur ORDER BY raison_sociale";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la récupération des fournisseurs : {}", e.getMessage(), e);
            throw new DataAccessException("Erreur récupération fournisseurs", e);
        }
        return list;
    }

    public List<Fournisseur> findActifs() {
        List<Fournisseur> list = new ArrayList<>();
        String sql = "SELECT * FROM fournisseur WHERE statut = 'ACTIF' ORDER BY raison_sociale";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la récupération des fournisseurs actifs : {}", e.getMessage(), e);
            throw new DataAccessException("Erreur récupération fournisseurs actifs", e);
        }
        return list;
    }

    public Fournisseur findById(int id) {
        String sql = "SELECT * FROM fournisseur WHERE id_fournisseur = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur recherche fournisseur #{}: {}", id, e.getMessage(), e);
            throw new DataAccessException("Erreur recherche fournisseur #" + id, e);
        }
        return null;
    }

    public List<Fournisseur> search(String keyword) {
        List<Fournisseur> list = new ArrayList<>();
        String sql = "SELECT * FROM fournisseur WHERE raison_sociale LIKE ? OR nom_commercial LIKE ? OR ice LIKE ? OR ville LIKE ? ORDER BY raison_sociale";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String pattern = "%" + keyword + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            ps.setString(4, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la recherche des fournisseurs ('{}') : {}", keyword, e.getMessage(), e);
            throw new DataAccessException("Erreur recherche fournisseurs", e);
        }
        return list;
    }

    public boolean insert(Fournisseur f) {
        String sql = "INSERT INTO fournisseur (ice, raison_sociale, nom_commercial, telephone, email, adresse, ville, nom_contact, statut) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, f.getIce());
            ps.setString(2, f.getRaisonSociale());
            ps.setString(3, f.getNomCommercial());
            ps.setString(4, f.getTelephone());
            ps.setString(5, f.getEmail());
            ps.setString(6, f.getAdresse());
            ps.setString(7, f.getVille());
            ps.setString(8, f.getNomContact());
            ps.setString(9, f.getStatut() != null ? f.getStatut() : "ACTIF");
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        f.setIdFournisseur(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de l'insertion du fournisseur : {}", e.getMessage(), e);
            throw new DataAccessException("Erreur insertion fournisseur", e);
        }
        return false;
    }

    public boolean update(Fournisseur f) {
        String sql = "UPDATE fournisseur SET ice=?, raison_sociale=?, nom_commercial=?, telephone=?, email=?, adresse=?, ville=?, nom_contact=?, statut=? WHERE id_fournisseur=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, f.getIce());
            ps.setString(2, f.getRaisonSociale());
            ps.setString(3, f.getNomCommercial());
            ps.setString(4, f.getTelephone());
            ps.setString(5, f.getEmail());
            ps.setString(6, f.getAdresse());
            ps.setString(7, f.getVille());
            ps.setString(8, f.getNomContact());
            ps.setString(9, f.getStatut());
            ps.setInt(10, f.getIdFournisseur());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Erreur lors de la mise à jour du fournisseur #{}: {}", f.getIdFournisseur(), e.getMessage(), e);
            throw new DataAccessException("Erreur mise à jour fournisseur", e);
        }
    }

    /**
     * Désactivation logique du fournisseur.
     */
    public boolean delete(int id) {
        String sql = "UPDATE fournisseur SET statut = 'INACTIF' WHERE id_fournisseur = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Erreur lors de la désactivation logique du fournisseur #{}: {}", id, e.getMessage(), e);
            throw new DataAccessException("Erreur désactivation fournisseur", e);
        }
    }

    public int count() {
        String sql = "SELECT COUNT(*) FROM fournisseur";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            logger.error("Erreur lors du comptage des fournisseurs : {}", e.getMessage(), e);
            throw new DataAccessException("Erreur comptage fournisseurs", e);
        }
        return 0;
    }

    private Fournisseur mapResultSet(ResultSet rs) throws SQLException {
        Fournisseur f = new Fournisseur();
        f.setIdFournisseur(rs.getInt("id_fournisseur"));
        f.setIce(rs.getString("ice"));
        f.setRaisonSociale(rs.getString("raison_sociale"));
        f.setNomCommercial(rs.getString("nom_commercial"));
        f.setTelephone(rs.getString("telephone"));
        f.setEmail(rs.getString("email"));
        f.setAdresse(rs.getString("adresse"));
        f.setVille(rs.getString("ville"));
        f.setNomContact(rs.getString("nom_contact"));
        f.setStatut(rs.getString("statut"));
        return f;
    }
}
