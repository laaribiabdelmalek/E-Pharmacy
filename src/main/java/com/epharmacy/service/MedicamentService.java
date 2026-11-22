package com.epharmacy.service;

import com.epharmacy.dao.AuditLogDAO;
import com.epharmacy.dao.MedicamentDAO;
import com.epharmacy.exception.BusinessException;
import com.epharmacy.model.AuditLog;
import com.epharmacy.model.Medicament;
import com.epharmacy.util.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service métier pour la gestion du catalogue de médicaments.
 *
 * @author E-PHARMACIE
 * @version 3.0
 */
public class MedicamentService {

    private static final Logger logger = LoggerFactory.getLogger(MedicamentService.class);

    private final MedicamentDAO dao;
    private final AuditLogDAO auditLogDAO;

    public MedicamentService() {
        this(new MedicamentDAO(), new AuditLogDAO());
    }

    public MedicamentService(MedicamentDAO dao, AuditLogDAO auditLogDAO) {
        this.dao = dao;
        this.auditLogDAO = auditLogDAO;
    }

    public List<Medicament> getTous() {
        return dao.findAll();
    }

    public List<Medicament> rechercher(String keyword) {
        return dao.search(keyword);
    }

    public List<Medicament> getParStatut(String statut) {
        return dao.findByStatut(statut);
    }

    public List<Medicament> getAlertesStock() {
        return dao.findAlertesStock();
    }

    public Medicament getParId(int id) {
        return dao.findById(id);
    }

    public int getNombreTotal() {
        return dao.count();
    }

    public double getValeurStock() {
        return dao.getTotalValeurStock();
    }

    public boolean ajouter(Medicament m) {
        validerMedicament(m);
        boolean ok = dao.insert(m);
        if (ok) {
            logAudit("AJOUT_MEDICAMENT", m.getIdMedicament(), "Création du médicament : " + m.getNomCommercial());
        }
        return ok;
    }

    public boolean modifier(Medicament m) {
        validerMedicament(m);
        boolean ok = dao.update(m);
        if (ok) {
            logAudit("MODIFICATION_MEDICAMENT", m.getIdMedicament(), "Mise à jour du médicament : " + m.getNomCommercial());
        }
        return ok;
    }

    /**
     * Désactivation logique du médicament.
     */
    public boolean supprimer(int id) {
        boolean ok = dao.delete(id);
        if (ok) {
            logAudit("DESACTIVATION_MEDICAMENT", id, "Désactivation logique du médicament #" + id);
        }
        return ok;
    }

    private void validerMedicament(Medicament m) {
        if (m == null) {
            throw new BusinessException("Les données du médicament ne peuvent être nulles.");
        }
        if (m.getNomCommercial() == null || m.getNomCommercial().isBlank()) {
            throw new BusinessException("Le nom commercial du médicament est obligatoire.");
        }
        if (m.getPrixVentePublicBD() == null || m.getPrixVentePublicBD().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Le prix de vente public ne peut être négatif.");
        }
        if (m.getSeuilMinimal() < 0) {
            throw new BusinessException("Le seuil minimal ne peut être négatif.");
        }
    }

    private void logAudit(String action, int idEntite, String details) {
        Integer userId = null;
        if (SessionManager.getInstance().isConnecte()) {
            userId = SessionManager.getInstance().getUtilisateurConnecte().getIdUtilisateur();
        }
        auditLogDAO.insert(new AuditLog(userId, action, "MEDICAMENT", idEntite, details));
    }
}
