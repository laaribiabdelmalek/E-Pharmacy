package com.epharmacy.dao;

import com.epharmacy.config.DBConnection;
import com.epharmacy.exception.DataAccessException;
import com.epharmacy.model.PaiementClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour les opérations CRUD sur les paiements / règlements clients.
 *
 * @author E-PHARMACIE
 * @version 3.0
 */
public class PaiementClientDAO {

    private static final Logger logger = LoggerFactory.getLogger(PaiementClientDAO.class);

    private static final String SQL_INSERT =
            "INSERT INTO paiement_client (id_client, id_utilisateur, montant, mode_paiement, date_paiement) " +
            "VALUES (?, ?, ?, ?, ?)";

    private static final String SQL_FIND_BY_CLIENT =
            "SELECT p.*, CONCAT(c.prenom, ' ', c.nom) AS nom_client, u.login AS login_utilisateur " +
            "FROM paiement_client p " +
            "JOIN client c ON p.id_client = c.id_client " +
            "JOIN utilisateur u ON p.id_utilisateur = u.id_utilisateur " +
            "WHERE p.id_client = ? " +
            "ORDER BY p.date_paiement DESC";

    private static final String SQL_FIND_ALL =
            "SELECT p.*, CONCAT(c.prenom, ' ', c.nom) AS nom_client, u.login AS login_utilisateur " +
            "FROM paiement_client p " +
            "JOIN client c ON p.id_client = c.id_client " +
            "JOIN utilisateur u ON p.id_utilisateur = u.id_utilisateur " +
            "ORDER BY p.date_paiement DESC";

    /**
     * Insère un règlement au sein d'une connexion transactionnelle.
     */
    public void insert(Connection conn, PaiementClient p) {
        try (PreparedStatement stmt = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, p.getIdClient());
            stmt.setInt(2, p.getIdUtilisateur());
            stmt.setBigDecimal(3, p.getMontant() != null ? p.getMontant() : BigDecimal.ZERO);
            stmt.setString(4, p.getModePaiement());
            stmt.setTimestamp(5, Timestamp.valueOf(p.getDatePaiement() != null ? p.getDatePaiement() : java.time.LocalDateTime.now()));

            stmt.executeUpdate();
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    p.setIdPaiement(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de l'insertion transactionnelle du paiement : {}", e.getMessage(), e);
            throw new DataAccessException("Impossible d'insérer le paiement", e);
        }
    }

    /**
     * Insère un règlement de façon autonome.
     */
    public void insert(PaiementClient p) {
        try (Connection conn = DBConnection.getConnection()) {
            insert(conn, p);
        } catch (SQLException e) {
            logger.error("Erreur de connexion pour insérer le paiement : {}", e.getMessage(), e);
            throw new DataAccessException("Erreur de connexion pour enregistrer le paiement", e);
        }
    }

    public List<PaiementClient> findByClient(int idClient) {
        List<PaiementClient> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_CLIENT)) {
            stmt.setInt(1, idClient);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la récupération des règlements du client #{} : {}", idClient, e.getMessage(), e);
            throw new DataAccessException("Erreur lors de la récupération des règlements", e);
        }
        return list;
    }

    public List<PaiementClient> findAll() {
        List<PaiementClient> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_ALL);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la récupération de tous les règlements : {}", e.getMessage(), e);
            throw new DataAccessException("Erreur lors de la récupération des règlements", e);
        }
        return list;
    }

    private PaiementClient mapResultSet(ResultSet rs) throws SQLException {
        PaiementClient p = new PaiementClient();
        p.setIdPaiement(rs.getInt("id_paiement"));
        p.setIdClient(rs.getInt("id_client"));
        p.setIdUtilisateur(rs.getInt("id_utilisateur"));
        p.setMontant(rs.getBigDecimal("montant"));
        p.setModePaiement(rs.getString("mode_paiement"));
        Timestamp ts = rs.getTimestamp("date_paiement");
        if (ts != null) {
            p.setDatePaiement(ts.toLocalDateTime());
        }
        p.setNomClient(rs.getString("nom_client"));
        p.setNomUtilisateur(rs.getString("login_utilisateur"));
        return p;
    }
}
