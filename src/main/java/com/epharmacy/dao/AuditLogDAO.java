package com.epharmacy.dao;

import com.epharmacy.config.DBConnection;
import com.epharmacy.exception.DataAccessException;
import com.epharmacy.model.AuditLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour l'enregistrement et la consultation des journaux d'audit.
 *
 * @author E-PHARMACIE
 * @version 3.0
 */
public class AuditLogDAO {

    private static final Logger logger = LoggerFactory.getLogger(AuditLogDAO.class);

    private static final String SQL_INSERT =
            "INSERT INTO audit_log (id_utilisateur, action, entite, id_entite, details, date_action) " +
            "VALUES (?, ?, ?, ?, ?, ?)";

    private static final String SQL_FIND_ALL =
            "SELECT a.*, u.login AS login_utilisateur " +
            "FROM audit_log a " +
            "LEFT JOIN utilisateur u ON a.id_utilisateur = u.id_utilisateur " +
            "ORDER BY a.date_action DESC LIMIT 100";

    private static final String SQL_FIND_BY_ENTITE =
            "SELECT a.*, u.login AS login_utilisateur " +
            "FROM audit_log a " +
            "LEFT JOIN utilisateur u ON a.id_utilisateur = u.id_utilisateur " +
            "WHERE a.entite = ? AND a.id_entite = ? " +
            "ORDER BY a.date_action DESC";

    /**
     * Enregistre une entrée d'audit dans une connexion existante (transactionnelle).
     */
    public void insert(Connection conn, AuditLog log) {
        if (log == null) return;
        try (PreparedStatement stmt = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {
            if (log.getIdUtilisateur() != null && log.getIdUtilisateur() > 0) {
                stmt.setInt(1, log.getIdUtilisateur());
            } else {
                stmt.setNull(1, Types.INTEGER);
            }
            stmt.setString(2, log.getAction());
            stmt.setString(3, log.getEntite());
            if (log.getIdEntite() != null) {
                stmt.setInt(4, log.getIdEntite());
            } else {
                stmt.setNull(4, Types.INTEGER);
            }
            stmt.setString(5, log.getDetails());
            stmt.setTimestamp(6, Timestamp.valueOf(log.getDateAction() != null ? log.getDateAction() : java.time.LocalDateTime.now()));

            stmt.executeUpdate();
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    log.setIdAudit(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de l'insertion de l'audit log [transactionnel] : {}", e.getMessage(), e);
            throw new DataAccessException("Impossible d'insérer l'audit log", e);
        }
    }

    /**
     * Enregistre une entrée d'audit autonome.
     */
    public void insert(AuditLog log) {
        try (Connection conn = DBConnection.getConnection()) {
            insert(conn, log);
        } catch (SQLException e) {
            logger.error("Erreur lors de l'obtention de la connexion pour audit log : {}", e.getMessage(), e);
            throw new DataAccessException("Erreur de connexion pour audit log", e);
        }
    }

    public List<AuditLog> findAll() {
        List<AuditLog> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_ALL);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la récupération des logs d'audit : {}", e.getMessage(), e);
            throw new DataAccessException("Erreur lors de la récupération des logs d'audit", e);
        }
        return list;
    }

    public List<AuditLog> findByEntite(String entite, int idEntite) {
        List<AuditLog> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_ENTITE)) {
            stmt.setString(1, entite);
            stmt.setInt(2, idEntite);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la recherche des logs pour {} #{} : {}", entite, idEntite, e.getMessage(), e);
            throw new DataAccessException("Erreur lors de la recherche des logs", e);
        }
        return list;
    }

    private AuditLog mapResultSet(ResultSet rs) throws SQLException {
        AuditLog log = new AuditLog();
        log.setIdAudit(rs.getInt("id_audit"));
        int idUser = rs.getInt("id_utilisateur");
        if (!rs.wasNull()) {
            log.setIdUtilisateur(idUser);
        }
        log.setAction(rs.getString("action"));
        log.setEntite(rs.getString("entite"));
        int idEnt = rs.getInt("id_entite");
        if (!rs.wasNull()) {
            log.setIdEntite(idEnt);
        }
        log.setDetails(rs.getString("details"));
        Timestamp ts = rs.getTimestamp("date_action");
        if (ts != null) {
            log.setDateAction(ts.toLocalDateTime());
        }
        log.setLoginUtilisateur(rs.getString("login_utilisateur"));
        return log;
    }
}
