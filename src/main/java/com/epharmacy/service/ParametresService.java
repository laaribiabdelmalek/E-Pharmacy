package com.epharmacy.service;

import com.epharmacy.dao.AuditLogDAO;
import com.epharmacy.dao.ParametresSocieteDAO;
import com.epharmacy.exception.BusinessException;
import com.epharmacy.model.AuditLog;
import com.epharmacy.model.ParametresSociete;
import com.epharmacy.util.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service pour la gestion des paramètres globaux de la société et du thème.
 *
 * @author E-PHARMACIE
 * @version 3.0
 */
public class ParametresService {

    private static final Logger logger = LoggerFactory.getLogger(ParametresService.class);

    private final ParametresSocieteDAO dao;
    private final AuditLogDAO auditLogDAO;

    public ParametresService() {
        this(new ParametresSocieteDAO(), new AuditLogDAO());
    }

    public ParametresService(ParametresSocieteDAO dao, AuditLogDAO auditLogDAO) {
        this.dao = dao;
        this.auditLogDAO = auditLogDAO;
    }

    public ParametresSociete getParametres() {
        return dao.find();
    }

    public boolean sauvegarder(ParametresSociete p) {
        if (p == null) {
            throw new BusinessException("Paramètres invalides.");
        }
        if (p.getNomPharmacie() == null || p.getNomPharmacie().isBlank()) {
            throw new BusinessException("La dénomination de la pharmacie est obligatoire.");
        }

        boolean ok = dao.save(p);
        if (ok) {
            Integer userId = null;
            if (SessionManager.getInstance().isConnecte()) {
                userId = SessionManager.getInstance().getUtilisateurConnecte().getIdUtilisateur();
            }
            auditLogDAO.insert(new AuditLog(userId, "MODIFICATION_PARAMETRES", "PARAMETRES", 1, "Mise à jour des paramètres société"));
            logger.info("Paramètres société mis à jour avec succès.");
        }
        return ok;
    }

    public boolean changerTheme(String theme) {
        return dao.updateTheme(theme);
    }

    public String getThemeActuel() {
        return dao.getTheme();
    }
}
