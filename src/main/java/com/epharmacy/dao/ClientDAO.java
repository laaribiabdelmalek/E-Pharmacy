package com.epharmacy.dao;

import com.epharmacy.config.DBConnection;
import com.epharmacy.model.Client;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour la table {@code client}.
 * @author E-PHARMACIE
 */
public class ClientDAO {

    public List<Client> findAll() {
        List<Client> list = new ArrayList<>();
        String sql = "SELECT * FROM client ORDER BY nom, prenom";
        try {
            Connection conn = DBConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapResultSet(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public Client findById(int id) {
        String sql = "SELECT * FROM client WHERE id_client = ?";
        try {
            Connection conn = DBConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapResultSet(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public List<Client> search(String keyword) {
        List<Client> list = new ArrayList<>();
        String sql = "SELECT * FROM client WHERE nom LIKE ? OR prenom LIKE ? OR telephone LIKE ? ORDER BY nom, prenom";
        try {
            Connection conn = DBConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            String pattern = "%" + keyword + "%";
            ps.setString(1, pattern); ps.setString(2, pattern); ps.setString(3, pattern);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapResultSet(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public boolean insert(Client c) {
        String sql = "INSERT INTO client (nom, prenom, telephone, email, adresse, credit) VALUES (?, ?, ?, ?, ?, ?)";
        try {
            Connection conn = DBConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, c.getNom()); ps.setString(2, c.getPrenom());
            ps.setString(3, c.getTelephone()); ps.setString(4, c.getEmail());
            ps.setString(5, c.getAdresse()); ps.setDouble(6, c.getCredit());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) c.setIdClient(rs.getInt(1));
                return true;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean update(Client c) {
        String sql = "UPDATE client SET nom=?, prenom=?, telephone=?, email=?, adresse=?, credit=? WHERE id_client=?";
        try {
            Connection conn = DBConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, c.getNom()); ps.setString(2, c.getPrenom());
            ps.setString(3, c.getTelephone()); ps.setString(4, c.getEmail());
            ps.setString(5, c.getAdresse()); ps.setDouble(6, c.getCredit());
            ps.setInt(7, c.getIdClient());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM client WHERE id_client = ?";
        try {
            Connection conn = DBConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean updateCredit(int id, double nouveauCredit) {
        String sql = "UPDATE client SET credit = ? WHERE id_client = ?";
        try {
            Connection conn = DBConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setDouble(1, nouveauCredit); ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public int count() {
        String sql = "SELECT COUNT(*) FROM client";
        try {
            Connection conn = DBConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
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
        c.setCredit(rs.getDouble("credit"));
        return c;
    }
}
