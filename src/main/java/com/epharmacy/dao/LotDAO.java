package com.epharmacy.dao;

import com.epharmacy.config.DBConnection;
import com.epharmacy.model.Lot;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour la table {@code lot}.
 *
 * @author E-PHARMACIE
 * @version 1.0
 */
public class LotDAO {

    private static final String SELECT_WITH_JOIN =
            "SELECT l.*, m.nom_commercial AS nom_medicament, la.id_medicament " +
            "FROM lot l " +
            "JOIN ligne_approvisionnement la ON l.id_ligne_approvisionnement = la.id_ligne_approvisionnement " +
            "JOIN medicament m ON la.id_medicament = m.id_medicament";

    public List<Lot> findAll() {
        List<Lot> list = new ArrayList<>();
        String sql = SELECT_WITH_JOIN + " ORDER BY l.date_expiration ASC";
        try {
            Connection conn = DBConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) { list.add(mapResultSet(rs)); }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Lot> findByMedicament(int idMedicament) {
        List<Lot> list = new ArrayList<>();
        String sql = SELECT_WITH_JOIN + " WHERE la.id_medicament = ? ORDER BY l.date_expiration ASC";
        try {
            Connection conn = DBConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, idMedicament);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) { list.add(mapResultSet(rs)); }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    /**
     * Lots disponibles triés FEFO (premier expiré, premier sorti).
     */
    public List<Lot> findDisponiblesByMedicament(int idMedicament) {
        List<Lot> list = new ArrayList<>();
        String sql = SELECT_WITH_JOIN + " WHERE la.id_medicament = ? AND l.quantite_restante > 0 " +
                     "AND l.date_expiration >= CURDATE() ORDER BY l.date_expiration ASC";
        try {
            Connection conn = DBConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, idMedicament);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) { list.add(mapResultSet(rs)); }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Lot> findExpirationProche(int joursAvant) {
        List<Lot> list = new ArrayList<>();
        String sql = SELECT_WITH_JOIN +
                     " WHERE l.date_expiration <= DATE_ADD(CURDATE(), INTERVAL ? DAY) " +
                     "AND l.date_expiration >= CURDATE() AND l.quantite_restante > 0 " +
                     "ORDER BY l.date_expiration ASC";
        try {
            Connection conn = DBConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, joursAvant);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) { list.add(mapResultSet(rs)); }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Lot> findExpires() {
        List<Lot> list = new ArrayList<>();
        String sql = SELECT_WITH_JOIN + " WHERE l.date_expiration < CURDATE() AND l.quantite_restante > 0 " +
                     "ORDER BY l.date_expiration ASC";
        try {
            Connection conn = DBConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) { list.add(mapResultSet(rs)); }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public Lot findById(int id) {
        String sql = SELECT_WITH_JOIN + " WHERE l.id_lot = ?";
        try {
            Connection conn = DBConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapResultSet(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public boolean insert(Lot lot) {
        String sql = "INSERT INTO lot (id_ligne_approvisionnement, numero_lot, date_expiration, " +
                     "quantite_initiale, quantite_restante) VALUES (?, ?, ?, ?, ?)";
        try {
            Connection conn = DBConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, lot.getIdLigneApprovisionnement());
            ps.setString(2, lot.getNumeroLot());
            ps.setDate(3, Date.valueOf(lot.getDateExpiration()));
            ps.setInt(4, lot.getQuantiteInitiale());
            ps.setInt(5, lot.getQuantiteRestante());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) lot.setIdLot(rs.getInt(1));
                return true;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean updateQuantiteRestante(int id, int quantiteRestante) {
        String sql = "UPDATE lot SET quantite_restante = ? WHERE id_lot = ?";
        try {
            Connection conn = DBConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, quantiteRestante);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM lot WHERE id_lot = ?";
        try {
            Connection conn = DBConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    private Lot mapResultSet(ResultSet rs) throws SQLException {
        Lot lot = new Lot();
        lot.setIdLot(rs.getInt("id_lot"));
        lot.setIdLigneApprovisionnement(rs.getInt("id_ligne_approvisionnement"));
        lot.setNumeroLot(rs.getString("numero_lot"));
        Date dateExp = rs.getDate("date_expiration");
        if (dateExp != null) lot.setDateExpiration(dateExp.toLocalDate());
        lot.setQuantiteInitiale(rs.getInt("quantite_initiale"));
        lot.setQuantiteRestante(rs.getInt("quantite_restante"));
        lot.setNomMedicament(rs.getString("nom_medicament"));
        lot.setIdMedicament(rs.getInt("id_medicament"));
        return lot;
    }
}
