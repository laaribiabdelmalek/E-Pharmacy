package com.epharmacy.dao;

import com.epharmacy.config.DBConnection;
import com.epharmacy.exception.DataAccessException;
import com.epharmacy.model.LigneApprovisionnement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour la table {@code ligne_approvisionnement}.
 * Utilise try-with-resources et SLF4J.
 *
 * @author E-PHARMACIE
 * @version 3.0
 */
public class LigneApprovisionnementDAO {

    private static final Logger logger = LoggerFactory.getLogger(LigneApprovisionnementDAO.class);

    private static final String SQL_FIND_BY_APPROV =
            "SELECT la.*, m.nom_commercial AS nom_medicament " +
            "FROM ligne_approvisionnement la " +
            "JOIN medicament m ON la.id_medicament = m.id_medicament " +
            "WHERE la.id_approvisionnement = ? ORDER BY la.id_ligne_approvisionnement";

    public List<LigneApprovisionnement> findByApprovisionnement(int idApprovisionnement) {
        try (Connection conn = DBConnection.getConnection()) {
            return findByApprovisionnement(conn, idApprovisionnement);
        } catch (SQLException e) {
            logger.error("Erreur de connexion findByApprovisionnement #{}: {}", idApprovisionnement, e.getMessage(), e);
            throw new DataAccessException("Erreur connexion", e);
        }
    }

    public List<LigneApprovisionnement> findByApprovisionnement(Connection conn, int idApprovisionnement) {
        List<LigneApprovisionnement> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_APPROV)) {
            ps.setInt(1, idApprovisionnement);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la récupération des lignes d'approvisionnement #{}: {}", idApprovisionnement, e.getMessage(), e);
            throw new DataAccessException("Erreur récupération lignes approvisionnement", e);
        }
        return list;
    }

    public boolean insert(LigneApprovisionnement la) {
        try (Connection conn = DBConnection.getConnection()) {
            return insert(conn, la);
        } catch (SQLException e) {
            logger.error("Erreur connexion insert ligne approvisionnement : {}", e.getMessage(), e);
            throw new DataAccessException("Erreur connexion", e);
        }
    }

    public boolean insert(Connection conn, LigneApprovisionnement la) {
        String sql = "INSERT INTO ligne_approvisionnement (id_approvisionnement, id_medicament, " +
                     "date_reception, quantite_commandee, quantite_recue, prix_achat_unitaire) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, la.getIdApprovisionnement());
            ps.setInt(2, la.getIdMedicament());
            if (la.getDateReception() != null) {
                ps.setDate(3, Date.valueOf(la.getDateReception()));
            } else {
                ps.setNull(3, Types.DATE);
            }
            ps.setInt(4, la.getQuantiteCommandee());
            ps.setInt(5, la.getQuantiteRecue());
            ps.setBigDecimal(6, la.getPrixAchatUnitaireBD());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        la.setIdLigneApprovisionnement(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de l'insertion de la ligne d'approvisionnement : {}", e.getMessage(), e);
            throw new DataAccessException("Erreur insertion ligne approvisionnement", e);
        }
        return false;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM ligne_approvisionnement WHERE id_ligne_approvisionnement = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Erreur lors de la suppression de la ligne d'approvisionnement #{}: {}", id, e.getMessage(), e);
            throw new DataAccessException("Erreur suppression ligne approvisionnement", e);
        }
    }

    public boolean update(LigneApprovisionnement la) {
        try (Connection conn = DBConnection.getConnection()) {
            return update(conn, la);
        } catch (SQLException e) {
            logger.error("Erreur connexion update ligne approvisionnement : {}", e.getMessage(), e);
            throw new DataAccessException("Erreur connexion", e);
        }
    }

    public boolean update(Connection conn, LigneApprovisionnement la) {
        String sql = "UPDATE ligne_approvisionnement SET id_medicament = ?, date_reception = ?, " +
                     "quantite_commandee = ?, quantite_recue = ?, prix_achat_unitaire = ? " +
                     "WHERE id_ligne_approvisionnement = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, la.getIdMedicament());
            if (la.getDateReception() != null) {
                ps.setDate(2, Date.valueOf(la.getDateReception()));
            } else {
                ps.setNull(2, Types.DATE);
            }
            ps.setInt(3, la.getQuantiteCommandee());
            ps.setInt(4, la.getQuantiteRecue());
            ps.setBigDecimal(5, la.getPrixAchatUnitaireBD());
            ps.setInt(6, la.getIdLigneApprovisionnement());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Erreur lors de la mise à jour de la ligne d'approvisionnement #{}: {}", la.getIdLigneApprovisionnement(), e.getMessage(), e);
            throw new DataAccessException("Erreur mise à jour ligne approvisionnement", e);
        }
    }

    public boolean deleteByApprovisionnement(int idApprovisionnement) {
        String sql = "DELETE FROM ligne_approvisionnement WHERE id_approvisionnement = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idApprovisionnement);
            return ps.executeUpdate() >= 0;
        } catch (SQLException e) {
            logger.error("Erreur suppression lignes pour approvisionnement #{}: {}", idApprovisionnement, e.getMessage(), e);
            throw new DataAccessException("Erreur suppression lignes approvisionnement", e);
        }
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
        la.setPrixAchatUnitaire(rs.getBigDecimal("prix_achat_unitaire"));
        try { la.setNomMedicament(rs.getString("nom_medicament")); } catch (SQLException ignored) {}
        return la;
    }
}
