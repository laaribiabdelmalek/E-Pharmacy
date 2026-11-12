package com.epharmacy.dao;

import com.epharmacy.config.DBConnection;
import com.epharmacy.model.Vente;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour la table {@code vente}.
 *
 * @author E-PHARMACIE
 * @version 1.0
 */
public class VenteDAO {

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
        try {
            Connection conn = DBConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) { list.add(mapResultSet(rs)); }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public Vente findById(int id) {
        String sql = SELECT_WITH_JOINS + " WHERE v.id_vente = ?";
        try {
            Connection conn = DBConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapResultSet(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public List<Vente> findByDate(LocalDate date) {
        List<Vente> list = new ArrayList<>();
        String sql = SELECT_WITH_JOINS + " WHERE DATE(v.date_vente) = ? ORDER BY v.date_vente DESC";
        try {
            Connection conn = DBConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setDate(1, Date.valueOf(date));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) { list.add(mapResultSet(rs)); }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public boolean insert(Vente v) {
        String sql = "INSERT INTO vente (id_utilisateur, id_client, date_vente, montant_total, mode_paiement, statut) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try {
            Connection conn = DBConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, v.getIdUtilisateur());
            if (v.getIdClient() > 0) {
                ps.setInt(2, v.getIdClient());
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            ps.setTimestamp(3, Timestamp.valueOf(v.getDateVente()));
            ps.setDouble(4, v.getMontantTotal());
            ps.setString(5, v.getModePaiement());
            ps.setString(6, v.getStatut() != null ? v.getStatut() : "VALIDEE");
            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) v.setIdVente(rs.getInt(1));
                return true;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean updateStatut(int id, String statut) {
        String sql = "UPDATE vente SET statut = ? WHERE id_vente = ?";
        try {
            Connection conn = DBConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, statut);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public int count() {
        String sql = "SELECT COUNT(*) FROM vente";
        try {
            Connection conn = DBConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public double getChiffreAffaires() {
        String sql = "SELECT COALESCE(SUM(montant_total), 0) FROM vente WHERE statut = 'VALIDEE'";
        try {
            Connection conn = DBConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0.0;
    }

    public double getChiffreAffairesJour(LocalDate date) {
        String sql = "SELECT COALESCE(SUM(montant_total), 0) FROM vente WHERE statut = 'VALIDEE' AND DATE(date_vente) = ?";
        try {
            Connection conn = DBConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setDate(1, Date.valueOf(date));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0.0;
    }

    private Vente mapResultSet(ResultSet rs) throws SQLException {
        Vente v = new Vente();
        v.setIdVente(rs.getInt("id_vente"));
        v.setIdUtilisateur(rs.getInt("id_utilisateur"));
        v.setIdClient(rs.getInt("id_client"));
        Timestamp ts = rs.getTimestamp("date_vente");
        if (ts != null) v.setDateVente(ts.toLocalDateTime());
        v.setMontantTotal(rs.getDouble("montant_total"));
        v.setModePaiement(rs.getString("mode_paiement"));
        v.setStatut(rs.getString("statut"));
        try { v.setNomUtilisateur(rs.getString("nom_utilisateur")); } catch (SQLException ignored) {}
        try { v.setNomClient(rs.getString("nom_client")); } catch (SQLException ignored) {}
        return v;
    }
}
