package com.epharmacy.dao;

import com.epharmacy.config.DBConnection;
import com.epharmacy.model.LigneVente;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour la table {@code ligne_vente}.
 * @author E-PHARMACIE
 */
public class LigneVenteDAO {

    public List<LigneVente> findByVente(int idVente) {
        List<LigneVente> list = new ArrayList<>();
        String sql = "SELECT lv.*, lo.numero_lot, m.nom_commercial AS nom_medicament " +
                     "FROM ligne_vente lv " +
                     "JOIN lot lo ON lv.id_lot = lo.id_lot " +
                     "JOIN ligne_approvisionnement la ON lo.id_ligne_approvisionnement = la.id_ligne_approvisionnement " +
                     "JOIN medicament m ON la.id_medicament = m.id_medicament " +
                     "WHERE lv.id_vente = ? ORDER BY lv.id_ligne_vente";
        try {
            Connection conn = DBConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, idVente);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapResultSet(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public boolean insert(LigneVente lv) {
        String sql = "INSERT INTO ligne_vente (id_vente, id_lot, quantite, prix_unitaire_vente, sous_total) VALUES (?, ?, ?, ?, ?)";
        try {
            Connection conn = DBConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, lv.getIdVente());
            ps.setInt(2, lv.getIdLot());
            ps.setInt(3, lv.getQuantite());
            ps.setDouble(4, lv.getPrixUnitaireVente());
            ps.setDouble(5, lv.getSousTotal());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) lv.setIdLigneVente(rs.getInt(1));
                return true;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM ligne_vente WHERE id_ligne_vente = ?";
        try {
            Connection conn = DBConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    private LigneVente mapResultSet(ResultSet rs) throws SQLException {
        LigneVente lv = new LigneVente();
        lv.setIdLigneVente(rs.getInt("id_ligne_vente"));
        lv.setIdVente(rs.getInt("id_vente"));
        lv.setIdLot(rs.getInt("id_lot"));
        lv.setQuantite(rs.getInt("quantite"));
        lv.setPrixUnitaireVente(rs.getDouble("prix_unitaire_vente"));
        lv.setSousTotal(rs.getDouble("sous_total"));
        try { lv.setNumeroLot(rs.getString("numero_lot")); } catch (SQLException ignored) {}
        try { lv.setNomMedicament(rs.getString("nom_medicament")); } catch (SQLException ignored) {}
        return lv;
    }
}
