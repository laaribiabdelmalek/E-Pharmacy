package com.epharmacy.dao;

import com.epharmacy.config.DBConnection;
import com.epharmacy.model.Medicament;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour la table {@code medicament}.
 *
 * @author E-PHARMACIE
 * @version 1.0
 */
public class MedicamentDAO {

    public List<Medicament> findAll() {
        List<Medicament> list = new ArrayList<>();
        String sql = "SELECT * FROM medicament ORDER BY nom_commercial";
        try {
            Connection conn = DBConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) { list.add(mapResultSet(rs)); }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public Medicament findById(int id) {
        String sql = "SELECT * FROM medicament WHERE id_medicament = ?";
        try {
            Connection conn = DBConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapResultSet(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public List<Medicament> findByStatut(String statut) {
        List<Medicament> list = new ArrayList<>();
        String sql = "SELECT * FROM medicament WHERE statut = ? ORDER BY nom_commercial";
        try {
            Connection conn = DBConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, statut);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) { list.add(mapResultSet(rs)); }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Medicament> search(String keyword) {
        List<Medicament> list = new ArrayList<>();
        String sql = "SELECT * FROM medicament WHERE nom_commercial LIKE ? OR dci LIKE ? OR categorie LIKE ? ORDER BY nom_commercial";
        try {
            Connection conn = DBConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            String pattern = "%" + keyword + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) { list.add(mapResultSet(rs)); }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Medicament> findAlertesStock() {
        List<Medicament> list = new ArrayList<>();
        String sql = "SELECT * FROM medicament WHERE quantite_disponible <= seuil_minimal AND statut = 'COMMERCIALISE' ORDER BY nom_commercial";
        try {
            Connection conn = DBConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) { list.add(mapResultSet(rs)); }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public boolean insert(Medicament m) {
        String sql = "INSERT INTO medicament (nom_commercial, dci, dosage, forme_pharmaceutique, " +
                     "conditionnement, laboratoire, type_medicament, conditions_conservation, " +
                     "categorie, prix_vente_public, seuil_minimal, statut, quantite_disponible) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            Connection conn = DBConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, m.getNomCommercial());
            ps.setString(2, m.getDci());
            ps.setString(3, m.getDosage());
            ps.setString(4, m.getFormePharmaceutique());
            ps.setString(5, m.getConditionnement());
            ps.setString(6, m.getLaboratoire());
            ps.setString(7, m.getTypeMedicament());
            ps.setString(8, m.getConditionsConservation());
            ps.setString(9, m.getCategorie());
            ps.setDouble(10, m.getPrixVentePublic());
            ps.setInt(11, m.getSeuilMinimal());
            ps.setString(12, m.getStatut() != null ? m.getStatut() : "COMMERCIALISE");
            ps.setInt(13, m.getQuantiteDisponible());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) m.setIdMedicament(rs.getInt(1));
                return true;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean update(Medicament m) {
        String sql = "UPDATE medicament SET nom_commercial=?, dci=?, dosage=?, forme_pharmaceutique=?, " +
                     "conditionnement=?, laboratoire=?, type_medicament=?, conditions_conservation=?, " +
                     "categorie=?, prix_vente_public=?, seuil_minimal=?, statut=? WHERE id_medicament=?";
        try {
            Connection conn = DBConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, m.getNomCommercial());
            ps.setString(2, m.getDci());
            ps.setString(3, m.getDosage());
            ps.setString(4, m.getFormePharmaceutique());
            ps.setString(5, m.getConditionnement());
            ps.setString(6, m.getLaboratoire());
            ps.setString(7, m.getTypeMedicament());
            ps.setString(8, m.getConditionsConservation());
            ps.setString(9, m.getCategorie());
            ps.setDouble(10, m.getPrixVentePublic());
            ps.setInt(11, m.getSeuilMinimal());
            ps.setString(12, m.getStatut());
            ps.setInt(13, m.getIdMedicament());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean updateQuantite(int id, int quantite) {
        String sql = "UPDATE medicament SET quantite_disponible = ? WHERE id_medicament = ?";
        try {
            Connection conn = DBConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, quantite);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM medicament WHERE id_medicament = ?";
        try {
            Connection conn = DBConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public int count() {
        String sql = "SELECT COUNT(*) FROM medicament";
        try {
            Connection conn = DBConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public double getTotalValeurStock() {
        String sql = "SELECT COALESCE(SUM(prix_vente_public * quantite_disponible), 0) FROM medicament";
        try {
            Connection conn = DBConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0.0;
    }

    private Medicament mapResultSet(ResultSet rs) throws SQLException {
        Medicament m = new Medicament();
        m.setIdMedicament(rs.getInt("id_medicament"));
        m.setNomCommercial(rs.getString("nom_commercial"));
        m.setDci(rs.getString("dci"));
        m.setDosage(rs.getString("dosage"));
        m.setFormePharmaceutique(rs.getString("forme_pharmaceutique"));
        m.setConditionnement(rs.getString("conditionnement"));
        m.setLaboratoire(rs.getString("laboratoire"));
        m.setTypeMedicament(rs.getString("type_medicament"));
        m.setConditionsConservation(rs.getString("conditions_conservation"));
        m.setCategorie(rs.getString("categorie"));
        m.setPrixVentePublic(rs.getDouble("prix_vente_public"));
        m.setSeuilMinimal(rs.getInt("seuil_minimal"));
        m.setStatut(rs.getString("statut"));
        m.setQuantiteDisponible(rs.getInt("quantite_disponible"));
        return m;
    }
}
