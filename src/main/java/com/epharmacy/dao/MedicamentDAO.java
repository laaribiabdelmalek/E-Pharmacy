package com.epharmacy.dao;

import com.epharmacy.config.DBConnection;
import com.epharmacy.exception.DataAccessException;
import com.epharmacy.model.Medicament;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour la table {@code medicament}.
 * Utilise try-with-resources et SLF4J.
 *
 * @author E-PHARMACIE
 * @version 3.0
 */
public class MedicamentDAO {

    private static final Logger logger = LoggerFactory.getLogger(MedicamentDAO.class);

    public List<Medicament> findAll() {
        List<Medicament> list = new ArrayList<>();
        String sql = "SELECT * FROM medicament ORDER BY nom_commercial";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la récupération des médicaments : {}", e.getMessage(), e);
            throw new DataAccessException("Erreur lors de la récupération des médicaments", e);
        }
        return list;
    }

    public Medicament findById(int id) {
        try (Connection conn = DBConnection.getConnection()) {
            return findById(conn, id);
        } catch (SQLException e) {
            logger.error("Erreur de connexion pour findById({}) : {}", id, e.getMessage(), e);
            throw new DataAccessException("Erreur de connexion", e);
        }
    }

    public Medicament findById(Connection conn, int id) {
        String sql = "SELECT * FROM medicament WHERE id_medicament = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la recherche du médicament #{} : {}", id, e.getMessage(), e);
            throw new DataAccessException("Erreur lors de la recherche du médicament #" + id, e);
        }
        return null;
    }

    public List<Medicament> findByStatut(String statut) {
        List<Medicament> list = new ArrayList<>();
        String sql = "SELECT * FROM medicament WHERE statut = ? ORDER BY nom_commercial";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, statut);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la recherche par statut ({}) : {}", statut, e.getMessage(), e);
            throw new DataAccessException("Erreur lors de la recherche par statut", e);
        }
        return list;
    }

    public List<Medicament> search(String keyword) {
        List<Medicament> list = new ArrayList<>();
        String sql = "SELECT * FROM medicament WHERE nom_commercial LIKE ? OR dci LIKE ? OR categorie LIKE ? ORDER BY nom_commercial";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String pattern = "%" + keyword + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la recherche de médicaments ('{}') : {}", keyword, e.getMessage(), e);
            throw new DataAccessException("Erreur lors de la recherche de médicaments", e);
        }
        return list;
    }

    public List<Medicament> findAlertesStock() {
        List<Medicament> list = new ArrayList<>();
        String sql = "SELECT * FROM medicament WHERE quantite_disponible <= seuil_minimal AND statut = 'COMMERCIALISE' ORDER BY nom_commercial";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la recherche des alertes stock : {}", e.getMessage(), e);
            throw new DataAccessException("Erreur lors de la recherche des alertes stock", e);
        }
        return list;
    }

    public boolean insert(Medicament m) {
        String sql = "INSERT INTO medicament (nom_commercial, dci, dosage, forme_pharmaceutique, " +
                     "conditionnement, laboratoire, type_medicament, conditions_conservation, " +
                     "categorie, prix_vente_public, seuil_minimal, statut, quantite_disponible, ordonnance_requise) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, m.getNomCommercial());
            ps.setString(2, m.getDci());
            ps.setString(3, m.getDosage());
            ps.setString(4, m.getFormePharmaceutique());
            ps.setString(5, m.getConditionnement());
            ps.setString(6, m.getLaboratoire());
            ps.setString(7, m.getTypeMedicament());
            ps.setString(8, m.getConditionsConservation());
            ps.setString(9, m.getCategorie());
            ps.setBigDecimal(10, m.getPrixVentePublicBD());
            ps.setInt(11, m.getSeuilMinimal());
            ps.setString(12, m.getStatut() != null ? m.getStatut() : "COMMERCIALISE");
            ps.setInt(13, m.getQuantiteDisponible());
            ps.setBoolean(14, m.isOrdonnanceRequise());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) m.setIdMedicament(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de l'insertion du médicament : {}", e.getMessage(), e);
            throw new DataAccessException("Erreur lors de l'insertion du médicament", e);
        }
        return false;
    }

    public boolean update(Medicament m) {
        String sql = "UPDATE medicament SET nom_commercial=?, dci=?, dosage=?, forme_pharmaceutique=?, " +
                     "conditionnement=?, laboratoire=?, type_medicament=?, conditions_conservation=?, " +
                     "categorie=?, prix_vente_public=?, seuil_minimal=?, statut=?, ordonnance_requise=? " +
                     "WHERE id_medicament=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, m.getNomCommercial());
            ps.setString(2, m.getDci());
            ps.setString(3, m.getDosage());
            ps.setString(4, m.getFormePharmaceutique());
            ps.setString(5, m.getConditionnement());
            ps.setString(6, m.getLaboratoire());
            ps.setString(7, m.getTypeMedicament());
            ps.setString(8, m.getConditionsConservation());
            ps.setString(9, m.getCategorie());
            ps.setBigDecimal(10, m.getPrixVentePublicBD());
            ps.setInt(11, m.getSeuilMinimal());
            ps.setString(12, m.getStatut());
            ps.setBoolean(13, m.isOrdonnanceRequise());
            ps.setInt(14, m.getIdMedicament());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Erreur lors de la mise à jour du médicament #{} : {}", m.getIdMedicament(), e.getMessage(), e);
            throw new DataAccessException("Erreur lors de la mise à jour du médicament", e);
        }
    }

    public boolean updateQuantite(int id, int quantite) {
        try (Connection conn = DBConnection.getConnection()) {
            return updateQuantite(conn, id, quantite);
        } catch (SQLException e) {
            logger.error("Erreur de connexion pour updateQuantite : {}", e.getMessage(), e);
            throw new DataAccessException("Erreur de connexion", e);
        }
    }

    public boolean updateQuantite(Connection conn, int id, int quantite) {
        String sql = "UPDATE medicament SET quantite_disponible = ? WHERE id_medicament = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quantite);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Erreur lors de la mise à jour de la quantité (#{}) : {}", id, e.getMessage(), e);
            throw new DataAccessException("Erreur mise à jour quantité", e);
        }
    }

    public boolean updateStatut(int id, String statut) {
        try (Connection conn = DBConnection.getConnection()) {
            return updateStatut(conn, id, statut);
        } catch (SQLException e) {
            logger.error("Erreur de connexion pour updateStatut : {}", e.getMessage(), e);
            throw new DataAccessException("Erreur de connexion", e);
        }
    }

    public boolean updateStatut(Connection conn, int id, String statut) {
        String sql = "UPDATE medicament SET statut = ? WHERE id_medicament = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, statut);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Erreur lors de la mise à jour du statut (#{}) : {}", id, e.getMessage(), e);
            throw new DataAccessException("Erreur mise à jour statut", e);
        }
    }

    /**
     * Désactivation logique du médicament (remplace la suppression physique).
     */
    public boolean delete(int id) {
        return updateStatut(id, "INACTIF");
    }

    public int count() {
        String sql = "SELECT COUNT(*) FROM medicament";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            logger.error("Erreur lors du comptage des médicaments : {}", e.getMessage(), e);
            throw new DataAccessException("Erreur comptage médicaments", e);
        }
        return 0;
    }

    public double getTotalValeurStock() {
        String sql = "SELECT COALESCE(SUM(prix_vente_public * quantite_disponible), 0) FROM medicament";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            logger.error("Erreur lors du calcul de la valeur du stock : {}", e.getMessage(), e);
            throw new DataAccessException("Erreur calcul valeur stock", e);
        }
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
        m.setPrixVentePublic(rs.getBigDecimal("prix_vente_public"));
        m.setSeuilMinimal(rs.getInt("seuil_minimal"));
        m.setStatut(rs.getString("statut"));
        m.setQuantiteDisponible(rs.getInt("quantite_disponible"));
        try {
            m.setOrdonnanceRequise(rs.getBoolean("ordonnance_requise"));
        } catch (SQLException ignored) {
            // Au cas où la colonne n'est pas sélectionnée ou dans un ancien schéma
        }
        return m;
    }
}
