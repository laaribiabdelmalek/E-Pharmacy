package com.epharmacy.service;

import com.epharmacy.config.DBConnection;
import com.epharmacy.dao.*;
import com.epharmacy.exception.BusinessException;
import com.epharmacy.exception.DataAccessException;
import com.epharmacy.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * Service transactionnel pour les approvisionnements en 2 étapes :
 * 1) Création de la commande fournisseur (statut 'En attente').
 * 2) Réception et validation de livraison avec support des réceptions partielles,
 *    génération automatique de numéros de lots et recalcul du stock.
 *
 * @author E-PHARMACIE
 * @version 3.0
 */
public class ApprovisionnementService {

    private static final Logger logger = LoggerFactory.getLogger(ApprovisionnementService.class);

    private final ApprovisionnementDAO approDAO;
    private final LigneApprovisionnementDAO ligneDAO;
    private final LotDAO lotDAO;
    private final MedicamentDAO medicamentDAO;
    private final AuditLogDAO auditLogDAO;
    private final StockService stockService;

    public ApprovisionnementService() {
        this.approDAO = new ApprovisionnementDAO();
        this.ligneDAO = new LigneApprovisionnementDAO();
        this.lotDAO = new LotDAO();
        this.medicamentDAO = new MedicamentDAO();
        this.auditLogDAO = new AuditLogDAO();
        this.stockService = new StockService(this.lotDAO, this.medicamentDAO);
    }

    public ApprovisionnementService(ApprovisionnementDAO approDAO, LigneApprovisionnementDAO ligneDAO,
                                    LotDAO lotDAO, MedicamentDAO medicamentDAO,
                                    AuditLogDAO auditLogDAO, StockService stockService) {
        this.approDAO = approDAO;
        this.ligneDAO = ligneDAO;
        this.lotDAO = lotDAO;
        this.medicamentDAO = medicamentDAO;
        this.auditLogDAO = auditLogDAO;
        this.stockService = stockService;
    }

    /**
     * Étape 1 : Créer une commande d'approvisionnement en attente.
     */
    public boolean creerCommande(Approvisionnement appro, List<LigneApprovisionnement> lignes) {
        if (appro == null || lignes == null || lignes.isEmpty()) {
            throw new BusinessException("Impossible de créer une commande sans articles.");
        }

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                appro.setStatut("En attente");
                if (appro.getDateApprovisionnement() == null) {
                    appro.setDateApprovisionnement(LocalDateTime.now());
                }

                BigDecimal montantEstime = BigDecimal.ZERO;
                for (LigneApprovisionnement la : lignes) {
                    BigDecimal sub = la.getPrixAchatUnitaireBD().multiply(BigDecimal.valueOf(la.getQuantiteCommandee()));
                    montantEstime = montantEstime.add(sub);
                }
                appro.setMontantTotal(montantEstime.setScale(2, RoundingMode.HALF_UP));

                if (!approDAO.insert(conn, appro)) {
                    conn.rollback();
                    return false;
                }

                for (LigneApprovisionnement la : lignes) {
                    la.setIdApprovisionnement(appro.getIdApprovisionnement());
                    la.setQuantiteRecue(0);
                    la.setDateReception(null);
                    if (!ligneDAO.insert(conn, la)) {
                        conn.rollback();
                        return false;
                    }
                }

                String details = String.format("Commande #%d créée pour le fournisseur #%d (Montant estimé: %.2f DH)",
                        appro.getIdApprovisionnement(), appro.getIdFournisseur(), appro.getMontantTotal());
                auditLogDAO.insert(conn, new AuditLog(appro.getIdUtilisateur(), "COMMANDE_CREEE", "APPROVISIONNEMENT", appro.getIdApprovisionnement(), details));

                conn.commit();
                logger.info("Commande #{} créée avec succès.", appro.getIdApprovisionnement());
                return true;
            } catch (Exception e) {
                conn.rollback();
                logger.error("Erreur lors de la création de commande appro : {}", e.getMessage(), e);
                if (e instanceof BusinessException) throw (BusinessException) e;
                throw new BusinessException("Impossible de créer la commande d'approvisionnement", e);
            }
        } catch (SQLException e) {
            logger.error("Erreur de connexion SQL pour créer la commande : {}", e.getMessage(), e);
            throw new DataAccessException("Erreur de connexion base de données", e);
        }
    }

    /**
     * Étape 2 : Réceptionner et valider la livraison.
     * Gère la réception totale ('Reçu') ou partielle ('Partiellement reçu').
     */
    public boolean validerReception(int idAppro, List<LigneApprovisionnement> lignesRecues, List<Lot> lots) {
        if (lignesRecues == null || lignesRecues.isEmpty() || lots == null || lots.size() != lignesRecues.size()) {
            throw new BusinessException("Données de réception incomplètes.");
        }

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Approvisionnement appro = approDAO.findById(conn, idAppro);
                if (appro == null) {
                    throw new BusinessException("Approvisionnement introuvable #" + idAppro);
                }

                // Supprimer les lignes provisoires initiales pour réinsérer les lignes finales reçues
                ligneDAO.deleteByApprovisionnement(idAppro);

                BigDecimal montantFinal = BigDecimal.ZERO;
                LocalDate today = LocalDate.now();
                int totalCommandé = 0;
                int totalReçu = 0;

                for (int i = 0; i < lignesRecues.size(); i++) {
                    LigneApprovisionnement la = lignesRecues.get(i);
                    Lot lot = lots.get(i);

                    la.setIdApprovisionnement(idAppro);
                    la.setDateReception(today);
                    ligneDAO.insert(conn, la);

                    int qteRecue = la.getQuantiteRecue();
                    totalCommandé += la.getQuantiteCommandee();
                    totalReçu += qteRecue;

                    BigDecimal ligneTotal = la.getPrixAchatUnitaireBD().multiply(BigDecimal.valueOf(qteRecue));
                    montantFinal = montantFinal.add(ligneTotal);

                    if (qteRecue > 0) {
                        lot.setIdLigneApprovisionnement(la.getIdLigneApprovisionnement());
                        lot.setQuantiteInitiale(qteRecue);
                        lot.setQuantiteRestante(qteRecue);

                        // Si le numéro de lot est absent, générer un identifiant unique traçable
                        if (lot.getNumeroLot() == null || lot.getNumeroLot().isBlank()) {
                            String codeDate = today.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                            String randomSuffix = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
                            lot.setNumeroLot("LOT-" + codeDate + "-" + randomSuffix);
                        }

                        if (lot.getDateExpiration() == null) {
                            lot.setDateExpiration(today.plusYears(2));
                        }

                        lotDAO.insert(conn, lot);

                        // Recalculer le stock du médicament via la méthode standard
                        stockService.recalculerStockDisponible(conn, la.getIdMedicament());
                    }
                }

                // Déterminer le statut final
                String nouveauStatut = "Reçu";
                if (totalReçu < totalCommandé && totalReçu > 0) {
                    nouveauStatut = "Partiellement reçu";
                }

                approDAO.updateMontantTotal(conn, idAppro, montantFinal.setScale(2, RoundingMode.HALF_UP));
                approDAO.updateStatut(conn, idAppro, nouveauStatut);

                String details = String.format("Réception validée pour commande #%d. Statut: %s. Reçu: %d/%d unités.",
                        idAppro, nouveauStatut, totalReçu, totalCommandé);
                auditLogDAO.insert(conn, new AuditLog(appro.getIdUtilisateur(), "LIVRAISON_VALIDEE", "APPROVISIONNEMENT", idAppro, details));

                conn.commit();
                logger.info("Réception appro #{} validée avec statut '{}'", idAppro, nouveauStatut);
                return true;
            } catch (Exception e) {
                conn.rollback();
                logger.error("Erreur lors de la validation de la réception appro #{}: {}", idAppro, e.getMessage(), e);
                if (e instanceof BusinessException) throw (BusinessException) e;
                throw new BusinessException("Impossible de valider la réception : " + e.getMessage(), e);
            }
        } catch (SQLException e) {
            logger.error("Erreur connexion SQL lors de la réception : {}", e.getMessage(), e);
            throw new DataAccessException("Erreur base de données", e);
        }
    }

    public boolean annulerCommande(int idAppro) {
        Approvisionnement a = approDAO.findById(idAppro);
        if (a != null && "En attente".equalsIgnoreCase(a.getStatut())) {
            boolean ok = approDAO.updateStatut(idAppro, "Annulé");
            if (ok) {
                auditLogDAO.insert(new AuditLog(a.getIdUtilisateur(), "COMMANDE_ANNULEE", "APPROVISIONNEMENT", idAppro, "Commande annulée"));
            }
            return ok;
        }
        return false;
    }

    public boolean enregistrerApprovisionnement(Approvisionnement appro,
                                                List<LigneApprovisionnement> lignes,
                                                List<Lot> lots) {
        if (!creerCommande(appro, lignes)) {
            return false;
        }
        return validerReception(appro.getIdApprovisionnement(), lignes, lots);
    }

    public List<Approvisionnement> getTous() {
        return approDAO.findAll();
    }

    public Approvisionnement getParId(int id) {
        return approDAO.findById(id);
    }

    public List<LigneApprovisionnement> getLignes(int idAppro) {
        return ligneDAO.findByApprovisionnement(idAppro);
    }

    public int getNombreTotal() {
        return approDAO.count();
    }
}
