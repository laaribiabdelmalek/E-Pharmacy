package com.epharmacy.service;

import com.epharmacy.config.DBConnection;
import com.epharmacy.dao.AuditLogDAO;
import com.epharmacy.dao.ClientDAO;
import com.epharmacy.dao.PaiementClientDAO;
import com.epharmacy.exception.BusinessException;
import com.epharmacy.exception.DataAccessException;
import com.epharmacy.model.AuditLog;
import com.epharmacy.model.Client;
import com.epharmacy.model.PaiementClient;
import com.epharmacy.util.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Service métier pour la gestion des clients et de leurs règlements / crédits.
 *
 * @author E-PHARMACIE
 * @version 3.0
 */
public class ClientService {

    private static final Logger logger = LoggerFactory.getLogger(ClientService.class);

    private final ClientDAO clientDAO;
    private final PaiementClientDAO paiementDAO;
    private final AuditLogDAO auditLogDAO;

    public ClientService() {
        this(new ClientDAO(), new PaiementClientDAO(), new AuditLogDAO());
    }

    public ClientService(ClientDAO clientDAO, PaiementClientDAO paiementDAO, AuditLogDAO auditLogDAO) {
        this.clientDAO = clientDAO;
        this.paiementDAO = paiementDAO;
        this.auditLogDAO = auditLogDAO;
    }

    public List<Client> getTous() {
        return clientDAO.findAll();
    }

    public Client getParId(int id) {
        return clientDAO.findById(id);
    }

    public List<Client> rechercher(String motCle) {
        return clientDAO.search(motCle);
    }

    public boolean ajouter(Client c) {
        validerClient(c);
        boolean ok = clientDAO.insert(c);
        if (ok) {
            logAudit("AJOUT_CLIENT", c.getIdClient(), "Ajout du client : " + c.getNomComplet());
        }
        return ok;
    }

    public boolean modifier(Client c) {
        validerClient(c);
        boolean ok = clientDAO.update(c);
        if (ok) {
            logAudit("MODIFICATION_CLIENT", c.getIdClient(), "Mise à jour du client : " + c.getNomComplet());
        }
        return ok;
    }

    public boolean supprimer(int id) {
        boolean ok = clientDAO.delete(id);
        if (ok) {
            logAudit("DESACTIVATION_CLIENT", id, "Désactivation logique du client #" + id);
        }
        return ok;
    }

    public List<PaiementClient> getHistoriqueReglements(int idClient) {
        return paiementDAO.findByClient(idClient);
    }

    /**
     * Enregistre un règlement de crédit client dans une transaction atomique.
     *
     * @param idClient      identifiant du client
     * @param montant       montant du paiement
     * @param modePaiement  mode de paiement (ESPECES, CARTE, VIREMENT)
     * @param idUtilisateur agent encaissant le règlement
     * @return true si l'opération a réussi
     */
    public boolean enregistrerReglement(int idClient, BigDecimal montant, String modePaiement, int idUtilisateur) {
        if (montant == null || montant.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Le montant du règlement doit être strictement positif.");
        }

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Client client = clientDAO.findById(conn, idClient, true);
                if (client == null) {
                    throw new BusinessException("Client introuvable #" + idClient);
                }

                if (client.getCreditBD().compareTo(BigDecimal.ZERO) <= 0) {
                    throw new BusinessException("Ce client n'a aucun crédit en cours à régler.");
                }

                if (montant.compareTo(client.getCreditBD()) > 0) {
                    throw new BusinessException(String.format(
                            "Le montant du règlement (%.2f DH) ne peut pas excéder la dette totale en cours (%.2f DH).",
                            montant.doubleValue(), client.getCredit()));
                }

                BigDecimal nouveauCredit = client.getCreditBD().subtract(montant).setScale(2, RoundingMode.HALF_UP);
                clientDAO.updateCredit(conn, idClient, nouveauCredit);

                PaiementClient p = new PaiementClient(idClient, idUtilisateur, montant, modePaiement != null ? modePaiement : "ESPECES");
                paiementDAO.insert(conn, p);

                String details = String.format("Règlement de %.2f DH reçu par agent #%d. Nouveau crédit client: %.2f DH",
                        montant.doubleValue(), idUtilisateur, nouveauCredit.doubleValue());
                auditLogDAO.insert(conn, new AuditLog(idUtilisateur, "REGLEMENT_CREDIT", "CLIENT", idClient, details));

                conn.commit();
                logger.info("Règlement de {} DH enregistré avec succès pour le client #{}", montant, idClient);
                return true;
            } catch (Exception e) {
                conn.rollback();
                logger.error("Erreur lors de l'enregistrement du règlement client #{}: {}", idClient, e.getMessage(), e);
                if (e instanceof BusinessException) throw (BusinessException) e;
                throw new BusinessException("Impossible d'enregistrer le règlement : " + e.getMessage(), e);
            }
        } catch (SQLException e) {
            logger.error("Erreur de connexion SQL pour le règlement client : {}", e.getMessage(), e);
            throw new DataAccessException("Erreur base de données", e);
        }
    }

    public boolean enregistrerReglement(int idClient, double montant, String modePaiement, int idUtilisateur) {
        return enregistrerReglement(idClient, BigDecimal.valueOf(montant), modePaiement, idUtilisateur);
    }

    private void validerClient(Client c) {
        if (c == null) throw new BusinessException("Données client manquantes.");
        if (c.getNom() == null || c.getNom().isBlank()) throw new BusinessException("Le nom du client est obligatoire.");
        if (c.getPrenom() == null || c.getPrenom().isBlank()) throw new BusinessException("Le prénom du client est obligatoire.");
        if (c.getPlafondCredit() == null || c.getPlafondCredit().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Le plafond de crédit ne peut pas être négatif.");
        }
    }

    private void logAudit(String action, int idEntite, String details) {
        Integer userId = null;
        if (SessionManager.getInstance().isConnecte()) {
            userId = SessionManager.getInstance().getUtilisateurConnecte().getIdUtilisateur();
        }
        auditLogDAO.insert(new AuditLog(userId, action, "CLIENT", idEntite, details));
    }
}
