package com.epharmacy.dao;

import com.epharmacy.config.DBConnection;
import com.epharmacy.exception.DataAccessException;
import com.epharmacy.model.ParametresSociete;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

/**
 * DAO pour la table {@code parametres_societe}.
 * Utilise try-with-resources et SLF4J.
 *
 * @author E-PHARMACIE
 * @version 3.0
 */
public class ParametresSocieteDAO {

    private static final Logger logger = LoggerFactory.getLogger(ParametresSocieteDAO.class);

    public ParametresSociete find() {
        String sql = "SELECT * FROM parametres_societe WHERE id = 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return mapResultSet(rs);
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la récupération des paramètres de l'entreprise : {}", e.getMessage(), e);
            throw new DataAccessException("Erreur récupération paramètres société", e);
        }
        return getDefault();
    }

    public boolean save(ParametresSociete p) {
        String sql = "INSERT INTO parametres_societe (id, nom_pharmacie, ice, identifiant_fiscal, rc, patente, " +
                     "telephone, email, adresse, ville, message_ticket, theme_app) " +
                     "VALUES (1, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE nom_pharmacie=VALUES(nom_pharmacie), ice=VALUES(ice), " +
                     "identifiant_fiscal=VALUES(identifiant_fiscal), rc=VALUES(rc), patente=VALUES(patente), " +
                     "telephone=VALUES(telephone), email=VALUES(email), adresse=VALUES(adresse), " +
                     "ville=VALUES(ville), message_ticket=VALUES(message_ticket), theme_app=VALUES(theme_app)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getNomPharmacie());
            ps.setString(2, p.getIce());
            ps.setString(3, p.getIdentifiantFiscal());
            ps.setString(4, p.getRc());
            ps.setString(5, p.getPatente());
            ps.setString(6, p.getTelephone());
            ps.setString(7, p.getEmail());
            ps.setString(8, p.getAdresse());
            ps.setString(9, p.getVille());
            ps.setString(10, p.getMessageTicket());
            ps.setString(11, p.getThemeApp());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Erreur lors de l'enregistrement des paramètres : {}", e.getMessage(), e);
            throw new DataAccessException("Erreur enregistrement paramètres société", e);
        }
    }

    public boolean updateTheme(String theme) {
        String sql = "UPDATE parametres_societe SET theme_app = ? WHERE id = 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, theme);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Erreur lors de la mise à jour du thème ({}) : {}", theme, e.getMessage(), e);
            throw new DataAccessException("Erreur mise à jour thème", e);
        }
    }

    public String getTheme() {
        String sql = "SELECT theme_app FROM parametres_societe WHERE id = 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getString("theme_app");
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la récupération du thème : {}", e.getMessage(), e);
            throw new DataAccessException("Erreur récupération thème", e);
        }
        return "CLAIR";
    }

    private ParametresSociete mapResultSet(ResultSet rs) throws SQLException {
        ParametresSociete p = new ParametresSociete();
        p.setId(rs.getInt("id"));
        p.setNomPharmacie(rs.getString("nom_pharmacie"));
        p.setIce(rs.getString("ice"));
        p.setIdentifiantFiscal(rs.getString("identifiant_fiscal"));
        p.setRc(rs.getString("rc"));
        p.setPatente(rs.getString("patente"));
        p.setTelephone(rs.getString("telephone"));
        p.setEmail(rs.getString("email"));
        p.setAdresse(rs.getString("adresse"));
        p.setVille(rs.getString("ville"));
        p.setMessageTicket(rs.getString("message_ticket"));
        p.setThemeApp(rs.getString("theme_app"));
        return p;
    }

    private ParametresSociete getDefault() {
        ParametresSociete p = new ParametresSociete();
        p.setNomPharmacie("Pharmacie E-Pharmacie");
        p.setIce(""); p.setIdentifiantFiscal(""); p.setRc(""); p.setPatente("");
        p.setTelephone(""); p.setEmail(""); p.setAdresse(""); p.setVille("");
        p.setMessageTicket("Merci de votre visite.");
        p.setThemeApp("CLAIR");
        return p;
    }
}
