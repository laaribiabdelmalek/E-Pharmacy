package com.epharmacy.service;

import com.epharmacy.config.DBConnection;
import com.epharmacy.dao.*;
import com.epharmacy.exception.BusinessException;
import com.epharmacy.exception.DataAccessException;
import com.epharmacy.exception.InsufficientStockException;
import com.epharmacy.model.*;
import com.epharmacy.util.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

/**
 * Service transactionnel pour les ventes (POS).
 * Assure l'atomicité, la traçabilité FEFO, le contrôle des plafonds de crédit
 * et la gestion complète de l'annulation de vente.
 *
 * @author E-PHARMACIE
 * @version 3.0
 */
public class VenteService {

    private static final Logger logger = LoggerFactory.getLogger(VenteService.class);

    private final VenteDAO venteDAO;
    private final LigneVenteDAO ligneVenteDAO;
    private final LotDAO lotDAO;
    private final MedicamentDAO medicamentDAO;
    private final ClientDAO clientDAO;
    private final AuditLogDAO auditLogDAO;
    private final StockService stockService;

    public VenteService() {
        this.venteDAO = new VenteDAO();
        this.ligneVenteDAO = new LigneVenteDAO();
        this.lotDAO = new LotDAO();
        this.medicamentDAO = new MedicamentDAO();
        this.clientDAO = new ClientDAO();
        this.auditLogDAO = new AuditLogDAO();
        this.stockService = new StockService(this.lotDAO, this.medicamentDAO);
    }

    public VenteService(VenteDAO venteDAO, LigneVenteDAO ligneVenteDAO, LotDAO lotDAO,
                        MedicamentDAO medicamentDAO, ClientDAO clientDAO,
                        AuditLogDAO auditLogDAO, StockService stockService) {
        this.venteDAO = venteDAO;
        this.ligneVenteDAO = ligneVenteDAO;
        this.lotDAO = lotDAO;
        this.medicamentDAO = medicamentDAO;
        this.clientDAO = clientDAO;
        this.auditLogDAO = auditLogDAO;
        this.stockService = stockService;
    }

    /**
     * Enregistre une vente complète dans une transaction atomique.
     *
     * @param vente  l'entête de la vente
     * @param lignes le détail des lignes de vente
     * @return true si la transaction est validée
     * @throws BusinessException si une règle métier est enfreinte
     */
    public boolean enregistrerVente(Vente vente, List<LigneVente> lignes) {
        if (vente == null || lignes == null || lignes.isEmpty()) {
            throw new BusinessException("Impossible d'enregistrer une vente vide.");
        }

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Calcul du montant total et vérification des règles de prescription
                BigDecimal totalCalculé = BigDecimal.ZERO;
                Set<Integer> medIds = new HashSet<>();
                boolean containsOrdonnance = false;

                for (LigneVente lv : lignes) {
                    if (lv.getSousTotalBD() == null || lv.getSousTotalBD().compareTo(BigDecimal.ZERO) <= 0) {
                        lv.calculerSousTotal();
                    }
                    totalCalculé = totalCalculé.add(lv.getSousTotalBD());

                    // Recherche de l'ID médicament lié au lot
                    String sqlMed = "SELECT la.id_medicament FROM lot l " +
                                    "JOIN ligne_approvisionnement la ON l.id_ligne_approvisionnement = la.id_ligne_approvisionnement " +
                                    "WHERE l.id_lot = ?";
                    try (PreparedStatement ps = conn.prepareStatement(sqlMed)) {
                        ps.setInt(1, lv.getIdLot());
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                int medId = rs.getInt("id_medicament");
                                medIds.add(medId);
                                Medicament med = medicamentDAO.findById(conn, medId);
                                if (med != null && med.isOrdonnanceRequise()) {
                                    containsOrdonnance = true;
                                }
                            }
                        }
                    }
                }
                vente.setMontantTotal(totalCalculé.setScale(2, RoundingMode.HALF_UP));

                // 2. Vérification de la solvabilité si paiement à CREDIT
                if ("CREDIT".equalsIgnoreCase(vente.getModePaiement())) {
                    if (vente.getIdClient() <= 0) {
                        throw new BusinessException("Une vente à crédit nécessite obligatoirement un client sélectionné.");
                    }
                    Client client = clientDAO.findById(conn, vente.getIdClient(), true);
                    if (client == null) {
                        throw new BusinessException("Client introuvable.");
                    }
                    if (!client.isActif()) {
                        throw new BusinessException("Impossible d'accorder un crédit à un client inactif.");
                    }
                    BigDecimal nouveauCredit = client.getCreditBD().add(vente.getMontantTotalBD());
                    if (nouveauCredit.compareTo(client.getPlafondCredit()) > 0) {
                        throw new BusinessException(String.format(
                                "Plafond de crédit dépassé ! Nouveau solde: %.2f DH > Plafond: %.2f DH",
                                nouveauCredit.doubleValue(), client.getPlafondCreditDouble()));
                    }
                    clientDAO.updateCredit(conn, client.getIdClient(), nouveauCredit);
                }

                // 3. Insérer la vente
                vente.setStatut("VALIDEE");
                venteDAO.insert(conn, vente);

                // 4. Traiter chaque ligne et décrémenter les lots avec verrou
                for (LigneVente lv : lignes) {
                    lv.setIdVente(vente.getIdVente());
                    ligneVenteDAO.insert(conn, lv);

                    Lot lot = lotDAO.findById(conn, lv.getIdLot());
                    if (lot == null) {
                        throw new BusinessException("Lot introuvable pour la ligne : ID " + lv.getIdLot());
                    }
                    int newQty = lot.getQuantiteRestante() - lv.getQuantite();
                    if (newQty < 0) {
                        throw new InsufficientStockException(lot.getIdMedicament(), lv.getQuantite(), lot.getQuantiteRestante());
                    }
                    lotDAO.updateQuantiteRestante(conn, lv.getIdLot(), newQty);
                }

                // 5. Recalculer les stocks médicaments et mettre à jour leurs statuts
                for (int medId : medIds) {
                    stockService.recalculerStockDisponible(conn, medId);
                }

                // 6. Journal d'audit
                String details = String.format("Vente #%d enregistrée. Montant: %.2f DH, Mode: %s%s",
                        vente.getIdVente(), vente.getMontantTotal(), vente.getModePaiement(),
                        containsOrdonnance ? " [Médicaments sous ordonnance inclus]" : "");
                auditLogDAO.insert(conn, new AuditLog(vente.getIdUtilisateur(), "VENTE_EFFECTUEE", "VENTE", vente.getIdVente(), details));

                conn.commit();
                logger.info("Vente #{} validée avec succès pour un montant de {} DH", vente.getIdVente(), vente.getMontantTotal());
                return true;
            } catch (Exception e) {
                conn.rollback();
                logger.error("Échec et rollback de la transaction de vente : {}", e.getMessage(), e);
                if (e instanceof BusinessException) {
                    throw (BusinessException) e;
                }
                throw new BusinessException("Erreur lors de l'enregistrement de la vente : " + e.getMessage(), e);
            }
        } catch (SQLException e) {
            logger.error("Erreur de connexion SQL lors de la vente : {}", e.getMessage(), e);
            throw new DataAccessException("Erreur de connexion base de données", e);
        }
    }

    /**
     * Annule une vente validée, restitue les stocks dans les lots d'origine,
     * réajuste le crédit du client si la vente était à crédit, et trace l'opération.
     *
     * @param idVente       identifiant de la vente
     * @param idUtilisateur utilisateur initiant l'annulation
     * @param motif         motif d'annulation
     * @return true si l'annulation est validée
     */
    public boolean annulerVente(int idVente, int idUtilisateur, String motif) {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Vente v = venteDAO.findById(conn, idVente);
                if (v == null) {
                    throw new BusinessException("Vente introuvable #" + idVente);
                }
                if (!"VALIDEE".equalsIgnoreCase(v.getStatut())) {
                    throw new BusinessException("Seule une vente au statut VALIDEE peut être annulée (statut actuel: " + v.getStatut() + ").");
                }

                // 1. Mettre à jour le statut de la vente
                venteDAO.updateStatut(conn, idVente, "ANNULEE");

                // 2. Restituer les quantités vendues dans les lots
                List<LigneVente> lignes = ligneVenteDAO.findByVente(conn, idVente);
                Set<Integer> medIds = new HashSet<>();

                for (LigneVente lv : lignes) {
                    Lot lot = lotDAO.findById(conn, lv.getIdLot());
                    if (lot != null) {
                        int restoredQty = lot.getQuantiteRestante() + lv.getQuantite();
                        lotDAO.updateQuantiteRestante(conn, lot.getIdLot(), restoredQty);
                        medIds.add(lot.getIdMedicament());
                    }
                }

                // 3. Recalculer les stocks des médicaments concernés
                for (int medId : medIds) {
                    stockService.recalculerStockDisponible(conn, medId);
                }

                // 4. Si la vente était à crédit, défalquer le montant du compte client
                if ("CREDIT".equalsIgnoreCase(v.getModePaiement()) && v.getIdClient() > 0) {
                    Client client = clientDAO.findById(conn, v.getIdClient(), true);
                    if (client != null) {
                        BigDecimal creditApres = client.getCreditBD().subtract(v.getMontantTotalBD()).max(BigDecimal.ZERO);
                        clientDAO.updateCredit(conn, client.getIdClient(), creditApres);
                        logger.info("Crédit du client #{} réajusté suite à annulation de la vente #{}: {} -> {}",
                                client.getIdClient(), idVente, client.getCreditBD(), creditApres);
                    }
                }

                // 5. Journal d'audit
                String details = String.format("Vente #%d annulée par user #%d. Motif: %s", idVente, idUtilisateur, motif);
                auditLogDAO.insert(conn, new AuditLog(idUtilisateur, "ANNULATION_VENTE", "VENTE", idVente, details));

                conn.commit();
                logger.info("Vente #{} annulée avec succès.", idVente);
                return true;
            } catch (Exception e) {
                conn.rollback();
                logger.error("Erreur lors de l'annulation de la vente #{}: {}", idVente, e.getMessage(), e);
                if (e instanceof BusinessException) {
                    throw (BusinessException) e;
                }
                throw new BusinessException("Impossible d'annuler la vente : " + e.getMessage(), e);
            }
        } catch (SQLException e) {
            logger.error("Erreur SQL lors de l'annulation de vente : {}", e.getMessage(), e);
            throw new DataAccessException("Erreur connexion base de données", e);
        }
    }

    /**
     * Rétrocompatibilité : annule une vente avec l'utilisateur connecté en cours.
     */
    public boolean annulerVente(int id) {
        int userId = 1;
        if (SessionManager.getInstance().isConnecte()) {
            userId = SessionManager.getInstance().getUtilisateurConnecte().getIdUtilisateur();
        }
        return annulerVente(id, userId, "Annulation standard");
    }

    public List<Vente> getToutesLesVentes() {
        return venteDAO.findAll();
    }

    public Vente getVenteParId(int id) {
        return venteDAO.findById(id);
    }

    public List<LigneVente> getLignesVente(int idVente) {
        return ligneVenteDAO.findByVente(idVente);
    }

    public int getNombreVentes() {
        return venteDAO.count();
    }

    public double getChiffreAffaires() {
        return venteDAO.getChiffreAffaires();
    }

    public double getChiffreAffairesJour() {
        return venteDAO.getChiffreAffairesJour(LocalDate.now());
    }
}
