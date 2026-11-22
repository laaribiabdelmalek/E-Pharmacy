package com.epharmacy.ui.panels;

import com.epharmacy.dao.ClientDAO;
import com.epharmacy.dao.MedicamentDAO;
import com.epharmacy.dao.ParametresSocieteDAO;
import com.epharmacy.exception.BusinessException;
import com.epharmacy.model.*;
import com.epharmacy.print.FacturePrintable;
import com.epharmacy.print.PrintPreviewDialog;
import com.epharmacy.print.TicketPrintable;
import com.epharmacy.service.StockService;
import com.epharmacy.service.VenteService;
import com.epharmacy.ui.components.StyledTable;
import com.epharmacy.util.SessionManager;
import com.epharmacy.util.ValidationUtils;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Panneau de gestion des Ventes (Point de Vente POS et Historique).
 * Aligné à 100% sur l'architecture visuelle et le pattern UX d'ApprovisionnementPanel :
 * <ul>
 *     <li>Layout parent rétractable {@code hidemode 3} avec expansion verticale totale du tableau</li>
 *     <li>Toolbar supérieure avec bouton vert "+ Nouvelle Vente", bouton rouge "Annuler la Vente" et réimpressions</li>
 *     <li>Formulaire inline collapsible avec barre de saisie compacte, tableau de panier et pied d'action</li>
 *     <li>Validation FEFO atomique, vérification ordonnance et blocage modification P.U. pour EMPLOYE</li>
 * </ul>
 *
 * @author E-PHARMACIE
 * @version 3.0
 */
public class VentePanel extends JPanel {

    private final VenteService venteService = new VenteService();
    private final StockService stockService = new StockService();
    private final MedicamentDAO medicamentDAO = new MedicamentDAO();
    private final ClientDAO clientDAO = new ClientDAO();
    private final ParametresSocieteDAO parametresDAO = new ParametresSocieteDAO();

    // Toolbar supérieure
    private JButton btnNouvelleVente;
    private JButton btnAnnulerVente;
    private JButton btnPrintTicket;
    private JButton btnPrintFacture;

    // Formulaire Inline Collapsible
    private JPanel formPanel;
    private JComboBox<String> cbClient;
    private List<Client> clients = new ArrayList<>();
    private JComboBox<MedicamentItem> cbMedicament;
    private JTextField tfQte;
    private JTextField tfPrix;
    private JComboBox<String> cbPaiement;
    private DefaultTableModel cartModel;
    private JTable cartTable;
    private JLabel lblTotal;
    private final List<CartItem> cartItems = new ArrayList<>();

    // Table Historique principale
    private StyledTable historyTable;

    public VentePanel() {
        initUI();
        chargerHistorique();
    }

    private void initUI() {
        setLayout(new MigLayout("fill, insets 20, hidemode 3", "[grow, fill, 0::]", "[]12[]12[grow, fill]"));

        // ==========================================
        // 1. TOOLBAR SUPÉRIEURE
        // ==========================================
        JPanel header = new JPanel(new MigLayout("insets 0, fillx", "[grow][right]", "[]"));
        header.setOpaque(false);

        JLabel title = new JLabel("Ventes");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        header.add(title, "grow");

        JPanel actionButtons = new JPanel(new MigLayout("insets 0, gap 8", "[][][][]", "[]"));
        actionButtons.setOpaque(false);

        btnNouvelleVente = new JButton("+ Nouvelle Vente");
        btnNouvelleVente.setBackground(new Color(40, 167, 69));
        btnNouvelleVente.setForeground(Color.WHITE);
        btnNouvelleVente.setFont(btnNouvelleVente.getFont().deriveFont(Font.BOLD, 12f));
        btnNouvelleVente.setMargin(new Insets(4, 10, 4, 10));
        btnNouvelleVente.addActionListener(e -> ouvrirFormulaireNouvelleVente());
        actionButtons.add(btnNouvelleVente);

        btnAnnulerVente = new JButton("Annuler la Vente");
        btnAnnulerVente.setBackground(new Color(220, 53, 69));
        btnAnnulerVente.setForeground(Color.WHITE);
        btnAnnulerVente.setFont(btnAnnulerVente.getFont().deriveFont(Font.BOLD, 12f));
        btnAnnulerVente.setMargin(new Insets(4, 10, 4, 10));
        btnAnnulerVente.setEnabled(false);
        btnAnnulerVente.addActionListener(e -> annulerVenteSelectionnee());
        actionButtons.add(btnAnnulerVente);

        btnPrintTicket = new JButton("Imprimer Ticket");
        btnPrintTicket.setMargin(new Insets(4, 10, 4, 10));
        btnPrintTicket.setEnabled(false);
        btnPrintTicket.addActionListener(e -> imprimerTicketHistorique());
        actionButtons.add(btnPrintTicket);

        btnPrintFacture = new JButton("Imprimer Facture");
        btnPrintFacture.setMargin(new Insets(4, 10, 4, 10));
        btnPrintFacture.setEnabled(false);
        btnPrintFacture.addActionListener(e -> imprimerFactureHistorique());
        actionButtons.add(btnPrintFacture);

        header.add(actionButtons, "right");
        add(header, "growx, wrap");

        // ==========================================
        // 2. FORMULAIRE INLINE COLLAPSIBLE (hidemode 3)
        // ==========================================
        formPanel = new JPanel(new MigLayout("fillx, insets 10", "[grow, fill, 0::]", "[]8[]10[]10[]"));
        formPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(40, 167, 69), 1),
                "Nouvelle Vente",
                TitledBorder.LEFT, TitledBorder.TOP,
                getFont().deriveFont(Font.BOLD, 13f),
                new Color(40, 167, 69)
        ));
        formPanel.setVisible(false);

        // Ligne 1 : Client, Médicament, Paiement
        JPanel row1 = new JPanel(new MigLayout("insets 0, fillx", "[]8[grow, fill, 180::]15[]8[grow, fill, 220::]15[]8[130!]", "[]"));
        row1.setOpaque(false);

        cbClient = new JComboBox<>();
        row1.add(new JLabel("Client :"));
        row1.add(cbClient, "growx");

        cbMedicament = new JComboBox<>();
        row1.add(new JLabel("Médicament :"));
        row1.add(cbMedicament, "growx");

        cbPaiement = new JComboBox<>(new String[]{"ESPECES", "CARTE", "CREDIT"});
        row1.add(new JLabel("Paiement :"));
        row1.add(cbPaiement, "w 130!");

        formPanel.add(row1, "growx, wrap");

        // Ligne 2 : Qté, P.U., Bouton Ajouter
        JPanel row2 = new JPanel(new MigLayout("insets 0, fillx", "[]8[80!]15[]8[100!]push[]", "[]"));
        row2.setOpaque(false);

        tfQte = new JTextField("1", 5);
        row2.add(new JLabel("Qté :"));
        row2.add(tfQte, "w 80!");

        tfPrix = new JTextField(6);
        // Protection rôle : Si EMPLOYE, interdiction de modifier le prix unitaire
        boolean isEmploye = SessionManager.getInstance().isConnecte()
                && "EMPLOYE".equalsIgnoreCase(SessionManager.getInstance().getUtilisateurConnecte().getRole());
        tfPrix.setEditable(!isEmploye);
        if (isEmploye) {
            tfPrix.setToolTipText("Modification du prix réservée aux pharmaciens et administrateurs");
        }

        row2.add(new JLabel("P.U. (DH) :"));
        row2.add(tfPrix, "w 100!");

        JButton btnAdd = new JButton("+ Ajouter");
        btnAdd.setBackground(new Color(0, 123, 255)); // #007BFF
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setFont(btnAdd.getFont().deriveFont(Font.BOLD, 12f));
        btnAdd.setMargin(new Insets(4, 15, 4, 15));
        btnAdd.addActionListener(e -> ajouterAuPanier());
        row2.add(btnAdd, "right");

        formPanel.add(row2, "growx, wrap");

        // Ligne 3 : Tableau des articles du panier
        cartModel = new DefaultTableModel(
                new String[]{"Médicament", "ID Méd.", "Qté", "P.U. Vente (DH)", "Sous-Total (DH)"}, 0
        ) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        cartTable = new JTable(cartModel);
        cartTable.setRowHeight(26);
        JScrollPane scrollCart = new JScrollPane(cartTable);
        formPanel.add(scrollCart, "growx, h 130:150:200, wrap");

        // Ligne 4 : Pied du formulaire avec Total et Actions
        JPanel formBottom = new JPanel(new MigLayout("insets 0, fillx", "[]15[grow]push[]8[]", "[]"));
        formBottom.setOpaque(false);

        JButton btnRemove = new JButton("Retirer la ligne");
        btnRemove.setMargin(new Insets(4, 10, 4, 10));
        btnRemove.addActionListener(e -> retirerDuPanier());
        formBottom.add(btnRemove);

        lblTotal = new JLabel("Total : 0.00 DH");
        lblTotal.setFont(lblTotal.getFont().deriveFont(Font.BOLD, 14f));
        lblTotal.setForeground(new Color(24, 76, 120));
        formBottom.add(lblTotal, "growx");

        JButton btnValider = new JButton("Valider la Vente");
        btnValider.setBackground(new Color(40, 167, 69));
        btnValider.setForeground(Color.WHITE);
        btnValider.setFont(btnValider.getFont().deriveFont(Font.BOLD, 12f));
        btnValider.setMargin(new Insets(4, 12, 4, 12));
        btnValider.addActionListener(e -> validerVente());
        formBottom.add(btnValider);

        JButton btnAnnuler = new JButton("Annuler");
        btnAnnuler.setMargin(new Insets(4, 12, 4, 12));
        btnAnnuler.addActionListener(e -> fermerFormulaire());
        formBottom.add(btnAnnuler);

        formPanel.add(formBottom, "growx");

        add(formPanel, "growx, wrap");

        // ==========================================
        // 3. TABLE PRINCIPALE DE L'HISTORIQUE DES VENTES
        // ==========================================
        historyTable = new StyledTable(new String[]{"ID", "Date", "Client", "Montant (DH)", "Paiement", "Statut"});
        historyTable.getTable().getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    mettreAJourBoutonsActions();
                }
            }
        });
        add(historyTable, "grow, push");
    }

    private void chargerClientsEtMedicaments() {
        cbClient.removeAllItems();
        cbClient.addItem("-- Vente comptoir --");
        clients = clientDAO.findAll();
        for (Client c : clients) {
            cbClient.addItem(c.getPrenom() + " " + c.getNom() + " (ID:" + c.getIdClient() + ")");
        }

        cbMedicament.removeAllItems();
        List<Medicament> meds = medicamentDAO.findAll();
        for (Medicament m : meds) {
            if (m.getQuantiteDisponible() > 0 && "COMMERCIALISE".equalsIgnoreCase(m.getStatut())) {
                cbMedicament.addItem(new MedicamentItem(m));
            }
        }

        cbMedicament.addActionListener(e -> {
            MedicamentItem sel = (MedicamentItem) cbMedicament.getSelectedItem();
            if (sel != null && sel.medicament != null) {
                tfPrix.setText(String.format(java.util.Locale.US, "%.2f", sel.medicament.getPrixVentePublic()));
            }
        });

        if (cbMedicament.getItemCount() > 0) {
            cbMedicament.setSelectedIndex(0);
            MedicamentItem sel = (MedicamentItem) cbMedicament.getSelectedItem();
            if (sel != null && sel.medicament != null) {
                tfPrix.setText(String.format(java.util.Locale.US, "%.2f", sel.medicament.getPrixVentePublic()));
            }
        }
    }

    private void ouvrirFormulaireNouvelleVente() {
        chargerClientsEtMedicaments();
        cartItems.clear();
        cartModel.setRowCount(0);
        tfQte.setText("1");
        cbPaiement.setSelectedIndex(0);
        majTotal();

        boolean isEmploye = SessionManager.getInstance().isConnecte()
                && "EMPLOYE".equalsIgnoreCase(SessionManager.getInstance().getUtilisateurConnecte().getRole());
        tfPrix.setEditable(!isEmploye);

        formPanel.setVisible(true);
        revalidate();
        repaint();
        cbMedicament.requestFocusInWindow();
    }

    private void fermerFormulaire() {
        formPanel.setVisible(false);
        cartItems.clear();
        cartModel.setRowCount(0);
        majTotal();
        historyTable.getTable().clearSelection();
        mettreAJourBoutonsActions();
        revalidate();
        repaint();
    }

    private void ajouterAuPanier() {
        MedicamentItem selMed = (MedicamentItem) cbMedicament.getSelectedItem();
        if (selMed == null || selMed.medicament == null) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un médicament.", "Attention", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Règle pharmacie : Médicament nécessitant ordonnance
        if (selMed.medicament.isOrdonnanceRequise()) {
            int rep = JOptionPane.showConfirmDialog(
                    this,
                    "Ce médicament (" + selMed.medicament.getNomCommercial() + ") est soumis à prescription médicale obligatoire.\n" +
                    "Avez-vous vérifié la validité de l'ordonnance médicale du client ?",
                    "Prescription Médicale Obligatoire",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );
            if (rep != JOptionPane.YES_OPTION) {
                return;
            }
        }

        String qteStr = tfQte.getText().trim();
        String prixStr = tfPrix.getText().trim().replace(',', '.');

        if (!ValidationUtils.isPositiveInteger(qteStr)) {
            JOptionPane.showMessageDialog(this, "La quantité doit être un entier strictement positif.", "Erreur de saisie", JOptionPane.ERROR_MESSAGE);
            tfQte.requestFocus();
            return;
        }
        if (!ValidationUtils.isPositiveNumber(prixStr)) {
            JOptionPane.showMessageDialog(this, "Le prix unitaire doit être un nombre positif.", "Erreur de saisie", JOptionPane.ERROR_MESSAGE);
            tfPrix.requestFocus();
            return;
        }

        int qte = Integer.parseInt(qteStr);
        double pu = Double.parseDouble(prixStr);

        int existingQty = cartItems.stream()
                .filter(item -> item.idMedicament == selMed.medicament.getIdMedicament())
                .mapToInt(item -> item.quantite)
                .sum();

        if (existingQty + qte > selMed.medicament.getQuantiteDisponible()) {
            JOptionPane.showMessageDialog(this,
                    "Stock insuffisant pour " + selMed.medicament.getNomCommercial() + " !\n"
                            + "Disponible : " + selMed.medicament.getQuantiteDisponible() + " unité(s)\n"
                            + "Déjà dans le panier : " + existingQty + " unité(s)",
                    "Avertissement Stock", JOptionPane.WARNING_MESSAGE);
            return;
        }

        CartItem existing = null;
        for (CartItem it : cartItems) {
            if (it.idMedicament == selMed.medicament.getIdMedicament()) {
                existing = it;
                break;
            }
        }

        if (existing != null) {
            existing.quantite += qte;
            existing.prixUnitaireVente = pu;
        } else {
            CartItem it = new CartItem();
            it.idMedicament = selMed.medicament.getIdMedicament();
            it.nomMedicament = selMed.medicament.getNomCommercial();
            it.dosage = selMed.medicament.getDosage();
            it.quantite = qte;
            it.prixUnitaireVente = pu;
            cartItems.add(it);
        }

        rafraichirTableauPanier();
        majTotal();
        tfQte.setText("1");
    }

    private void retirerDuPanier() {
        int row = cartTable.getSelectedRow();
        if (row >= 0 && row < cartItems.size()) {
            cartItems.remove(row);
            rafraichirTableauPanier();
            majTotal();
        } else {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un article du panier à retirer.", "Sélection requise", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void rafraichirTableauPanier() {
        cartModel.setRowCount(0);
        for (CartItem it : cartItems) {
            String medLabel = it.nomMedicament + (it.dosage != null && !it.dosage.isBlank() ? " (" + it.dosage + ")" : "");
            cartModel.addRow(new Object[]{
                    medLabel,
                    it.idMedicament,
                    it.quantite,
                    String.format(java.util.Locale.US, "%.2f", it.prixUnitaireVente),
                    String.format(java.util.Locale.US, "%.2f", it.getSousTotal())
            });
        }
    }

    private void majTotal() {
        double total = cartItems.stream().mapToDouble(CartItem::getSousTotal).sum();
        lblTotal.setText("Total : " + String.format(java.util.Locale.US, "%.2f", total) + " DH");
    }

    private void validerVente() {
        if (cartItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Le panier est vide ! Veuillez ajouter au moins un médicament.", "Attention", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String modePaiement = (String) cbPaiement.getSelectedItem();
        int clientIdx = cbClient.getSelectedIndex();
        Client selectedClient = null;

        if (clientIdx > 0 && clientIdx <= clients.size()) {
            selectedClient = clients.get(clientIdx - 1);
        }

        if ("CREDIT".equals(modePaiement) && (selectedClient == null || selectedClient.getIdClient() <= 0)) {
            JOptionPane.showMessageDialog(this,
                    "Le paiement à crédit nécessite la sélection d'un client enregistré !",
                    "Validation Client", JOptionPane.WARNING_MESSAGE);
            cbClient.requestFocus();
            return;
        }

        List<LigneVente> allLignesVente = new ArrayList<>();
        try {
            for (CartItem item : cartItems) {
                List<LigneVente> fefoLignes = stockService.decrementerStock(item.idMedicament, item.quantite, item.prixUnitaireVente);
                for (LigneVente lv : fefoLignes) {
                    lv.setDosage(item.dosage);
                    allLignesVente.add(lv);
                }
            }
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erreur de Stock", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Utilisateur currentUser = SessionManager.getInstance().getUtilisateurConnecte();
        Vente vente = new Vente();
        vente.setIdUtilisateur(currentUser != null ? currentUser.getIdUtilisateur() : 1);
        vente.setDateVente(LocalDateTime.now());
        vente.setModePaiement(modePaiement);
        vente.setStatut("VALIDEE");

        if (selectedClient != null) {
            vente.setIdClient(selectedClient.getIdClient());
            vente.setNomClient(selectedClient.getPrenom() + " " + selectedClient.getNom());
        } else {
            vente.setNomClient("Comptoir");
        }

        String vendeurNom = currentUser != null ? currentUser.getPrenom() + " " + currentUser.getNom() : "Pharmacien";
        vente.setNomUtilisateur(vendeurNom);

        try {
            boolean success = venteService.enregistrerVente(vente, allLignesVente);
            if (success) {
                fermerFormulaire();
                chargerHistorique();
                proposerImpression(vente, allLignesVente, selectedClient);
            }
        } catch (BusinessException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Règle de Gestion", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erreur lors de l'enregistrement : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void annulerVenteSelectionnee() {
        int idVente = historyTable.getSelectedId();
        if (idVente <= 0) return;

        Vente v = venteService.getVenteParId(idVente);
        if (v == null || !"VALIDEE".equalsIgnoreCase(v.getStatut())) {
            JOptionPane.showMessageDialog(this, "Seule une vente au statut VALIDEE peut être annulée.", "Action non autorisée", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String motif = JOptionPane.showInputDialog(
                this,
                "Motif de l'annulation de la vente #" + idVente + " :",
                "Annulation de Vente",
                JOptionPane.QUESTION_MESSAGE
        );

        if (motif != null && !motif.isBlank()) {
            try {
                int userId = SessionManager.getInstance().isConnecte()
                        ? SessionManager.getInstance().getUtilisateurConnecte().getIdUtilisateur() : 1;
                boolean ok = venteService.annulerVente(idVente, userId, motif.trim());
                if (ok) {
                    JOptionPane.showMessageDialog(this, "La vente #" + idVente + " a été annulée avec succès.\nLes stocks ont été réintégrés.", "Annulation Confirmée", JOptionPane.INFORMATION_MESSAGE);
                    chargerHistorique();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erreur lors de l'annulation : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void proposerImpression(Vente vente, List<LigneVente> lignes, Client client) {
        Window parent = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(parent, "Confirmation de Vente", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new MigLayout("fill, insets 20", "[grow, fill]", "[]12[]18[]"));
        dialog.setMinimumSize(new Dimension(560, 220));
        dialog.setSize(580, 235);
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(this);

        // Header / Info de confirmation
        JPanel headerPanel = new JPanel(new MigLayout("insets 0", "[grow]", "[]4[]"));
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Vente enregistrée avec succès !");
        lblTitle.setFont(lblTitle.getFont().deriveFont(Font.BOLD, 17f));
        lblTitle.setForeground(new Color(40, 167, 69));
        headerPanel.add(lblTitle, "wrap");

        JLabel lblMontant = new JLabel(String.format(java.util.Locale.US, "Montant total réglé : %.2f DH", vente.getMontantTotal()));
        lblMontant.setFont(lblMontant.getFont().deriveFont(Font.BOLD, 14f));
        headerPanel.add(lblMontant);

        dialog.add(headerPanel, "wrap");

        JLabel lblPrompt = new JLabel("Souhaitez-vous imprimer un document justificatif pour cette vente ?");
        lblPrompt.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        dialog.add(lblPrompt, "wrap");

        // Boutons d'action espacés avec Insets(8, 16, 8, 16)
        JPanel btnPanel = new JPanel(new MigLayout("insets 0, fillx", "[grow]push[]10[]10[]", "[]"));
        btnPanel.setOpaque(false);

        JButton btnTicket = new JButton("Imprimer Ticket de Caisse");
        btnTicket.setBackground(new Color(23, 162, 184)); // Cyan/Info
        btnTicket.setForeground(Color.WHITE);
        btnTicket.setFont(btnTicket.getFont().deriveFont(Font.BOLD, 12f));
        btnTicket.setMargin(new Insets(8, 16, 8, 16));
        btnTicket.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnTicket.addActionListener(e -> {
            dialog.dispose();
            imprimerTicket(vente, lignes);
        });

        JButton btnFacture = new JButton("Imprimer Facture (A4)");
        btnFacture.setBackground(new Color(40, 167, 69)); // Vert/Succès
        btnFacture.setForeground(Color.WHITE);
        btnFacture.setFont(btnFacture.getFont().deriveFont(Font.BOLD, 12f));
        btnFacture.setMargin(new Insets(8, 16, 8, 16));
        btnFacture.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnFacture.addActionListener(e -> {
            dialog.dispose();
            imprimerFacture(vente, lignes, client);
        });

        JButton btnFermer = new JButton("Fermer");
        btnFermer.setFont(btnFermer.getFont().deriveFont(Font.PLAIN, 12f));
        btnFermer.setMargin(new Insets(8, 16, 8, 16));
        btnFermer.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnFermer.addActionListener(e -> dialog.dispose());

        btnPanel.add(btnTicket);
        btnPanel.add(btnFacture);
        btnPanel.add(btnFermer);

        dialog.add(btnPanel, "growx");
        dialog.setVisible(true);
    }

    private void imprimerTicket(Vente vente, List<LigneVente> lignes) {
        ParametresSociete params = parametresDAO.find();
        String nomVendeur = vente.getNomUtilisateur();
        if (nomVendeur == null || nomVendeur.isBlank()) {
            Utilisateur u = SessionManager.getInstance().getUtilisateurConnecte();
            nomVendeur = (u != null) ? u.getPrenom() + " " + u.getNom() : "Pharmacien";
        }
        TicketPrintable printable = new TicketPrintable(vente, lignes, params, nomVendeur);
        Window parent = SwingUtilities.getWindowAncestor(this);
        PrintPreviewDialog dialog = new PrintPreviewDialog(parent, "Ticket de Caisse (80mm)", printable, PrintPreviewDialog.createTicketPageFormat());
        dialog.setVisible(true);
    }

    private void imprimerFacture(Vente vente, List<LigneVente> lignes, Client client) {
        ParametresSociete params = parametresDAO.find();
        if (client == null && vente.getIdClient() > 0) {
            client = clientDAO.findById(vente.getIdClient());
        }
        FacturePrintable printable = new FacturePrintable(vente, lignes, params, client);
        Window parent = SwingUtilities.getWindowAncestor(this);
        PrintPreviewDialog dialog = new PrintPreviewDialog(parent, "Facture Officielle (A4)", printable, PrintPreviewDialog.createA4PageFormat());
        dialog.setVisible(true);
    }

    private void imprimerTicketHistorique() {
        int idVente = historyTable.getSelectedId();
        if (idVente <= 0) return;
        Vente v = venteService.getVenteParId(idVente);
        if (v == null) return;
        List<LigneVente> lv = venteService.getLignesVente(idVente);
        imprimerTicket(v, lv);
    }

    private void imprimerFactureHistorique() {
        int idVente = historyTable.getSelectedId();
        if (idVente <= 0) return;
        Vente v = venteService.getVenteParId(idVente);
        if (v == null) return;
        List<LigneVente> lv = venteService.getLignesVente(idVente);
        Client c = (v.getIdClient() > 0) ? clientDAO.findById(v.getIdClient()) : null;
        imprimerFacture(v, lv, c);
    }

    private void mettreAJourBoutonsActions() {
        int idVente = historyTable.getSelectedId();
        boolean hasSelection = idVente > 0;
        btnPrintTicket.setEnabled(hasSelection);
        btnPrintFacture.setEnabled(hasSelection);

        boolean canCancel = false;
        if (hasSelection) {
            Vente v = venteService.getVenteParId(idVente);
            canCancel = (v != null && "VALIDEE".equalsIgnoreCase(v.getStatut()));
        }
        btnAnnulerVente.setEnabled(canCancel);
    }

    public void chargerDonnees() {
        chargerHistorique();
    }

    public void chargerHistorique() {
        List<Vente> ventes = venteService.getToutesLesVentes();
        historyTable.setData(ventes.stream().map(v -> new Object[]{
                v.getIdVente(),
                v.getDateVente() != null ? ValidationUtils.formatDateTime(v.getDateVente()) : "",
                v.getNomClient() != null ? v.getNomClient() : "Comptoir",
                String.format(java.util.Locale.US, "%.2f", v.getMontantTotal()),
                v.getModePaiement(), v.getStatut()
        }).toArray(Object[][]::new));
        mettreAJourBoutonsActions();
    }

    private static class CartItem {
        int idMedicament;
        String nomMedicament;
        String dosage;
        int quantite;
        double prixUnitaireVente;

        double getSousTotal() {
            return quantite * prixUnitaireVente;
        }
    }

    private static class MedicamentItem {
        final Medicament medicament;

        MedicamentItem(Medicament m) {
            this.medicament = m;
        }

        @Override
        public String toString() {
            String dos = (medicament.getDosage() != null && !medicament.getDosage().isBlank())
                    ? " (" + medicament.getDosage() + ")" : "";
            String rx = medicament.isOrdonnanceRequise() ? " [Ordonnance]" : "";
            return medicament.getNomCommercial() + dos + rx + " [Stock: " + medicament.getQuantiteDisponible() + "]";
        }
    }
}
