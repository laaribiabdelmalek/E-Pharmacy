package com.epharmacy.dao;

import com.epharmacy.config.DBConnection;
import com.epharmacy.model.Utilisateur;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour la gestion des opérations CRUD sur la table {@code utilisateur}.
 *
 * @author E-PHARMACIE
 * @version 1.0
 */
public class UtilisateurDAO {

    /**
     * Recherche un utilisateur par son login.
     *
     * @param login le login de l'utilisateur
     * @return l'utilisateur trouvé, ou {@code null}
     */
    public Utilisateur findByLogin(String login) {
        String sql = "SELECT * FROM utilisateur WHERE login = ?";
        try {
            Connection conn = DBConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, login);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Récupère tous les utilisateurs, triés par nom.
     *
     * @return la liste des utilisateurs
     */
    public List<Utilisateur> findAll() {
        List<Utilisateur> list = new ArrayList<>();
        String sql = "SELECT * FROM utilisateur ORDER BY nom";
        try {
            Connection conn = DBConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Recherche un utilisateur par son identifiant.
     */
    public Utilisateur findById(int id) {
        String sql = "SELECT * FROM utilisateur WHERE id_utilisateur = ?";
        try {
            Connection conn = DBConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Insère un nouvel utilisateur.
     */
    public boolean insert(Utilisateur u) {
        String sql = "INSERT INTO utilisateur (nom, prenom, login, mot_de_passe_hash, role, statut) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try {
            Connection conn = DBConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, u.getNom());
            ps.setString(2, u.getPrenom());
            ps.setString(3, u.getLogin());
            ps.setString(4, u.getMotDePasseHash());
            ps.setString(5, u.getRole());
            ps.setString(6, u.getStatut() != null ? u.getStatut() : "ACTIF");
            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    u.setIdUtilisateur(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Met à jour un utilisateur existant.
     */
    public boolean update(Utilisateur u) {
        String sql = "UPDATE utilisateur SET nom = ?, prenom = ?, login = ?, " +
                     "role = ?, statut = ? WHERE id_utilisateur = ?";
        try {
            Connection conn = DBConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, u.getNom());
            ps.setString(2, u.getPrenom());
            ps.setString(3, u.getLogin());
            ps.setString(4, u.getRole());
            ps.setString(5, u.getStatut());
            ps.setInt(6, u.getIdUtilisateur());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Met à jour le mot de passe d'un utilisateur.
     */
    public boolean updatePassword(int id, String newHash) {
        String sql = "UPDATE utilisateur SET mot_de_passe_hash = ? WHERE id_utilisateur = ?";
        try {
            Connection conn = DBConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, newHash);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Supprime un utilisateur par son identifiant.
     */
    public boolean delete(int id) {
        String sql = "DELETE FROM utilisateur WHERE id_utilisateur = ?";
        try {
            Connection conn = DBConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Met à jour le statut d'un utilisateur (ACTIF/INACTIF).
     */
    public boolean updateStatut(int id, String statut) {
        String sql = "UPDATE utilisateur SET statut = ? WHERE id_utilisateur = ?";
        try {
            Connection conn = DBConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, statut);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
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
