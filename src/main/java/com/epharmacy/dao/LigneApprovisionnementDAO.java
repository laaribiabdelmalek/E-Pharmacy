package com.epharmacy.dao;

import com.epharmacy.config.DBConnection;
import com.epharmacy.model.LigneApprovisionnement;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour la table {@code ligne_approvisionnement}.
 * @author E-PHARMACIE
 */
public class LigneApprovisionnementDAO {

    public List<LigneApprovisionnement> findByApprovisionnement(int idApprovisionnement) {
        List<LigneApprovisionnement> list = new ArrayList<>();
        String sql = "SELECT la.*, m.nom_commercial AS nom_medicament " +
                     "FROM ligne_approvisionnement la " +
                     "JOIN medicament m ON la.id_medicament = m.id_medicament " +
                     "WHERE la.id_approvisionnement = ? ORDER BY la.id_ligne_approvisionnement";
        try {
            Connection conn = DBConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, idApprovisionnement);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapResultSet(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public boolean insert(LigneApprovisionnement la) {
        String sql = "INSERT INTO ligne_approvisionnement (id_approvisionnement, id_medicament, " +
                     "date_reception, quantite_commandee, quantite_recue, prix_achat_unitaire) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try {
            Connection conn = DBConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, la.getIdApprovisionnement());
            ps.setInt(2, la.getIdMedicament());
            if (la.getDateReception() != null) {
                ps.setDate(3, Date.valueOf(la.getDateReception()));
            } else {
                ps.setNull(3, Types.DATE);
            }
            ps.setInt(4, la.getQuantiteCommandee());
            ps.setInt(5, la.getQuantiteRecue());
            ps.setDouble(6, la.getPrixAchatUnitaire());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) la.setIdLigneApprovisionnement(rs.getInt(1));
                return true;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM ligne_approvisionnement WHERE id_ligne_approvisionnement = ?";
        try {
            Connection conn = DBConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    private LigneApprovisionnement mapResultSet(ResultSet rs) throws SQLException {
        LigneApprovisionnement la = new LigneApprovisionnement();
        la.setIdLigneApprovisionnement(rs.getInt("id_ligne_approvisionnement"));
        la.setIdApprovisionnement(rs.getInt("id_approvisionnement"));
        la.setIdMedicament(rs.getInt("id_medicament"));
        Date d = rs.getDate("date_reception");
        if (d != null) la.setDateReception(d.toLocalDate());
        la.setQuantiteCommandee(rs.getInt("quantite_commandee"));
        la.setQuantiteRecue(rs.getInt("quantite_recue"));
        la.setPrixAchatUnitaire(rs.getDouble("prix_achat_unitaire"));
        try { la.setNomMedicament(rs.getString("nom_medicament")); } catch (SQLException ignored) {}
        return la;
    }
}
