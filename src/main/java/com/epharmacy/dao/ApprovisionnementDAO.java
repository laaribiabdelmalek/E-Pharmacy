package com.epharmacy.dao;

import com.epharmacy.config.DBConnection;
import com.epharmacy.model.Approvisionnement;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour la table {@code approvisionnement}.
 * @author E-PHARMACIE
 */
public class ApprovisionnementDAO {

    private static final String SELECT_WITH_JOINS =
            "SELECT a.*, f.raison_sociale AS nom_fournisseur, " +
            "CONCAT(u.prenom, ' ', u.nom) AS nom_utilisateur " +
            "FROM approvisionnement a " +
            "LEFT JOIN fournisseur f ON a.id_fournisseur = f.id_fournisseur " +
            "LEFT JOIN utilisateur u ON a.id_utilisateur = u.id_utilisateur";

    public List<Approvisionnement> findAll() {
        List<Approvisionnement> list = new ArrayList<>();
        String sql = SELECT_WITH_JOINS + " ORDER BY a.date_approvisionnement DESC";
        try {
            Connection conn = DBConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapResultSet(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public Approvisionnement findById(int id) {
        String sql = SELECT_WITH_JOINS + " WHERE a.id_approvisionnement = ?";
        try {
            Connection conn = DBConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapResultSet(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public boolean insert(Approvisionnement a) {
        String sql = "INSERT INTO approvisionnement (id_fournisseur, id_utilisateur, date_approvisionnement, montant_total, statut) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try {
            Connection conn = DBConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, a.getIdFournisseur());
            ps.setInt(2, a.getIdUtilisateur());
            ps.setTimestamp(3, Timestamp.valueOf(a.getDateApprovisionnement()));
            ps.setDouble(4, a.getMontantTotal());
            ps.setString(5, a.getStatut() != null ? a.getStatut() : "EN_COURS");
            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) a.setIdApprovisionnement(rs.getInt(1));
                return true;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean updateStatut(int id, String statut) {
        String sql = "UPDATE approvisionnement SET statut = ? WHERE id_approvisionnement = ?";
        try {
            Connection conn = DBConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, statut); ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean updateMontantTotal(int id, double montant) {
        String sql = "UPDATE approvisionnement SET montant_total = ? WHERE id_approvisionnement = ?";
        try {
            Connection conn = DBConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setDouble(1, montant); ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public int count() {
        String sql = "SELECT COUNT(*) FROM approvisionnement";
        try {
            Connection conn = DBConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    private Approvisionnement mapResultSet(ResultSet rs) throws SQLException {
        Approvisionnement a = new Approvisionnement();
        a.setIdApprovisionnement(rs.getInt("id_approvisionnement"));
        a.setIdFournisseur(rs.getInt("id_fournisseur"));
        a.setIdUtilisateur(rs.getInt("id_utilisateur"));
        Timestamp ts = rs.getTimestamp("date_approvisionnement");
        if (ts != null) a.setDateApprovisionnement(ts.toLocalDateTime());
        a.setMontantTotal(rs.getDouble("montant_total"));
        a.setStatut(rs.getString("statut"));
        try { a.setNomFournisseur(rs.getString("nom_fournisseur")); } catch (SQLException ignored) {}
        try { a.setNomUtilisateur(rs.getString("nom_utilisateur")); } catch (SQLException ignored) {}
        return a;
    }
}
