package com.epharmacy.ui.panels;

import com.epharmacy.dao.FournisseurDAO;
import com.epharmacy.dao.MedicamentDAO;
import com.epharmacy.dao.ParametresSocieteDAO;
import com.epharmacy.model.*;
import com.epharmacy.print.BonCommandePrintable;
import com.epharmacy.print.PrintPreviewDialog;
import com.epharmacy.service.ApprovisionnementService;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Panneau de gestion des Approvisionnements en 2 étapes avec formulaire rétractable hidemode 3.
 * Étape 1 : Commande en attente (Bon de commande imprimable).
 * Étape 2 : Réception avec quantités ajustables, saisie des lots & péremptions et mise à jour des stocks.
 *
 * @author E-PHARMACIE
 */
public class ApprovisionnementPanel extends JPanel {

    private final ApprovisionnementService service = new ApprovisionnementService();
    private final FournisseurDAO fournisseurDAO = new FournisseurDAO();
    private final MedicamentDAO medicamentDAO = new MedicamentDAO();
    private final ParametresSocieteDAO parametresDAO = new ParametresSocieteDAO();

    // Composants principaux
    private StyledTable styledTable;
    private JPanel formPanel;
    private JButton btnReception;
    private JButton btnPrintBC;
    private JButton btnAnnulerCmd;

    // Formulaire Étape 1 (Nouvelle commande)
    private JComboBox<FournisseurItem> cbFournisseur;
    private JComboBox<MedicamentItem> cbMedicament;
    private JTextField tfQteCmd;
    private JTextField tfPrixAchat;
    private DefaultTableModel orderLinesModel;
    private JTable orderLinesTable;
    private JLabel lblTotalEstime;
    private final List<LigneApprovisionnement> tempOrderLines = new ArrayList<>();

    public ApprovisionnementPanel() {
        initUI();
        chargerDonnees();
    }

    private void initUI() {
        setLayout(new MigLayout("fill, insets 20, hidemode 3", "[grow, fill, 0::]", "[]12[]12[grow, fill]"));

        // ==========================================
        // 1. TOOLBAR SUPÉRIEURE
        // ==========================================
        JPanel header = new JPanel(new MigLayout("insets 0, fillx", "[grow][right]", "[]"));
        header.setOpaque(false);

        JLabel title = new JLabel("Approvisionnements");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        header.add(title, "grow");

        JPanel actionButtons = new JPanel(new MigLayout("insets 0, gap 8", "[][][][]", "[]"));
        actionButtons.setOpaque(false);

        JButton btnNouveau = new JButton("+ Nouvel Approvisionnement");
        btnNouveau.setBackground(new Color(40, 167, 69));
        btnNouveau.setForeground(Color.WHITE);
        btnNouveau.setFont(btnNouveau.getFont().deriveFont(Font.BOLD, 12f));
        btnNouveau.setMargin(new Insets(4, 10, 4, 10));
        btnNouveau.addActionListener(e -> ouvrirFormulaireNouvelleCommande());
        actionButtons.add(btnNouveau);

        btnReception = new JButton("Réceptionner la livraison");
        btnReception.setMargin(new Insets(4, 10, 4, 10));
        btnReception.setEnabled(false);
        btnReception.addActionListener(e -> ouvrirDialogueReception());
        actionButtons.add(btnReception);

        btnPrintBC = new JButton("Imprimer Bon de Commande");
        btnPrintBC.setMargin(new Insets(4, 10, 4, 10));
        btnPrintBC.setEnabled(false);
        btnPrintBC.addActionListener(e -> imprimerBonCommandeSelectionne());
        actionButtons.add(btnPrintBC);

        btnAnnulerCmd = new JButton("Annuler la commande");
        btnAnnulerCmd.setForeground(Color.RED);
        btnAnnulerCmd.setMargin(new Insets(4, 10, 4, 10));
        btnAnnulerCmd.setEnabled(false);
        btnAnnulerCmd.addActionListener(e -> annulerCommandeSelectionnee());
        actionButtons.add(btnAnnulerCmd);

        header.add(actionButtons, "right");
        add(header, "growx, wrap");

        // ==========================================
        // 2. FORMULAIRE INLINE COLLAPSIBLE (hidemode 3)
        // ==========================================
        formPanel = new JPanel(new MigLayout("fillx, insets 10", "[grow, fill, 0::]", "[]8[]10[]"));
        formPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(40, 167, 69), 1),
                "Étape 1 : Nouveau Bon de Commande Fournisseur",
                TitledBorder.LEFT, TitledBorder.TOP,
                getFont().deriveFont(Font.BOLD, 13f),
                new Color(40, 167, 69)
        ));
        formPanel.setVisible(false);

        // Sélection Fournisseur & Ajout de Médicament
        JPanel formTop = new JPanel(new MigLayout("insets 0, fillx", "[]8[grow, fill, 180::]15[]8[grow, fill, 200::]15[]8[70!]15[]8[80!]push[]", "[]"));
        formTop.setOpaque(false);

        cbFournisseur = new JComboBox<>();
        formTop.add(new JLabel("Fournisseur * :"));
        formTop.add(cbFournisseur, "growx");

        cbMedicament = new JComboBox<>();
        formTop.add(new JLabel("Médicament :"));
        formTop.add(cbMedicament, "growx");

        tfQteCmd = new JTextField("100", 5);
        formTop.add(new JLabel("Qté :"));
        formTop.add(tfQteCmd, "w 70!");

        tfPrixAchat = new JTextField(6);
        formTop.add(new JLabel("P.U Achat (DH) :"));
        formTop.add(tfPrixAchat, "w 80!");

        JButton btnAddLine = new JButton("+ Ajouter");
        btnAddLine.setBackground(new Color(24, 76, 120));
        btnAddLine.setForeground(Color.WHITE);
        btnAddLine.setFont(btnAddLine.getFont().deriveFont(Font.BOLD, 12f));
        btnAddLine.setMargin(new Insets(4, 12, 4, 12));
        btnAddLine.addActionListener(e -> ajouterLigneCommande());
        formTop.add(btnAddLine, "right");

        formPanel.add(formTop, "growx, wrap");

        // Tableau des articles commandés
        orderLinesModel = new DefaultTableModel(
                new String[]{"Médicament", "ID Méd.", "Qté Commandée", "P.U. Achat (DH)", "Sous-Total (DH)"}, 0
        ) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        orderLinesTable = new JTable(orderLinesModel);
        orderLinesTable.setRowHeight(26);
        JScrollPane scrollOrder = new JScrollPane(orderLinesTable);
        formPanel.add(scrollOrder, "growx, h 130:150:200, wrap");

        // Pied du formulaire avec Total et Boutons d'action
        JPanel formBottom = new JPanel(new MigLayout("insets 0, fillx", "[]15[grow]push[]8[]", "[]"));
        formBottom.setOpaque(false);

        JButton btnSupprLigne = new JButton("Retirer la ligne");
        btnSupprLigne.setMargin(new Insets(4, 10, 4, 10));
        btnSupprLigne.addActionListener(e -> retirerLigneCommande());
        formBottom.add(btnSupprLigne);

        lblTotalEstime = new JLabel("Total Estimé : 0.00 DH");
        lblTotalEstime.setFont(lblTotalEstime.getFont().deriveFont(Font.BOLD, 14f));
        lblTotalEstime.setForeground(new Color(24, 76, 120));
        formBottom.add(lblTotalEstime, "growx");

        JButton btnSaveOrder = new JButton("Enregistrer la Commande (En attente)");
        btnSaveOrder.setBackground(new Color(40, 167, 69));
        btnSaveOrder.setForeground(Color.WHITE);
        btnSaveOrder.setFont(btnSaveOrder.getFont().deriveFont(Font.BOLD, 12f));
        btnSaveOrder.setMargin(new Insets(4, 12, 4, 12));
        btnSaveOrder.addActionListener(e -> validerNouvelleCommande());
        formBottom.add(btnSaveOrder);

        JButton btnCancelOrder = new JButton("Annuler");
        btnCancelOrder.setMargin(new Insets(4, 12, 4, 12));
        btnCancelOrder.addActionListener(e -> fermerFormulaire());
        formBottom.add(btnCancelOrder);

        formPanel.add(formBottom, "growx");

        add(formPanel, "growx, wrap");

        // ==========================================
        // 3. TABLE DES APPROVISIONNEMENTS
        // ==========================================
        styledTable = new StyledTable(new String[]{"ID", "Date", "Fournisseur", "Utilisateur", "Montant (DH)", "Statut"});
        styledTable.getTable().getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    mettreAJourBoutonsActions();
                }
            }
        });
        add(styledTable, "grow, push");
    }

    private void chargerFournisseursEtMedicaments() {
        cbFournisseur.removeAllItems();
        List<Fournisseur> fournisseurs = fournisseurDAO.findActifs();
        for (Fournisseur f : fournisseurs) {
            cbFournisseur.addItem(new FournisseurItem(f));
        }

        cbMedicament.removeAllItems();
        List<Medicament> meds = medicamentDAO.findAll();
        for (Medicament m : meds) {
            cbMedicament.addItem(new MedicamentItem(m));
        }

        cbMedicament.addActionListener(e -> {
            MedicamentItem sel = (MedicamentItem) cbMedicament.getSelectedItem();
            if (sel != null && (tfPrixAchat.getText() == null || tfPrixAchat.getText().isBlank())) {
                double prixEstime = sel.medicament.getPrixVentePublic() * 0.70; // Estimation de base
                tfPrixAchat.setText(String.format("%.2f", prixEstime).replace(',', '.'));
            }
        });
    }

    private void ouvrirFormulaireNouvelleCommande() {
        chargerFournisseursEtMedicaments();
        tempOrderLines.clear();
        orderLinesModel.setRowCount(0);
        tfQteCmd.setText("100");
        tfPrixAchat.setText("");
        majTotalEstime();

        formPanel.setVisible(true);
        revalidate();
        repaint();
    }

    private void fermerFormulaire() {
        formPanel.setVisible(false);
        tempOrderLines.clear();
        orderLinesModel.setRowCount(0);
        revalidate();
        repaint();
    }

    private void ajouterLigneCommande() {
        MedicamentItem selMed = (MedicamentItem) cbMedicament.getSelectedItem();
        if (selMed == null) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un médicament.", "Attention", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String qteStr = tfQteCmd.getText().trim();
        String prixStr = tfPrixAchat.getText().trim().replace(',', '.');

        if (!ValidationUtils.isPositiveInteger(qteStr)) {
            JOptionPane.showMessageDialog(this, "La quantité commandée doit être un entier positif.", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!ValidationUtils.isPositiveNumber(prixStr)) {
            JOptionPane.showMessageDialog(this, "Le prix d'achat doit être un nombre positif.", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int qte = Integer.parseInt(qteStr);
        double pu = Double.parseDouble(prixStr);
        double st = qte * pu;

        LigneApprovisionnement la = new LigneApprovisionnement();
        la.setIdMedicament(selMed.medicament.getIdMedicament());
        la.setNomMedicament(selMed.medicament.getNomCommercial());
        la.setQuantiteCommandee(qte);
        la.setPrixAchatUnitaire(pu);
        tempOrderLines.add(la);

        orderLinesModel.addRow(new Object[]{
                selMed.medicament.getNomCommercial(),
                selMed.medicament.getIdMedicament(),
                qte,
                String.format("%.2f", pu),
                String.format("%.2f", st)
        });

        majTotalEstime();
        tfPrixAchat.setText("");
    }

    private void retirerLigneCommande() {
        int row = orderLinesTable.getSelectedRow();
        if (row >= 0 && row < tempOrderLines.size()) {
            tempOrderLines.remove(row);
            orderLinesModel.removeRow(row);
            majTotalEstime();
        }
    }

    private void majTotalEstime() {
        double total = tempOrderLines.stream().mapToDouble(l -> l.getQuantiteCommandee() * l.getPrixAchatUnitaire()).sum();
        lblTotalEstime.setText("Total Estimé : " + String.format("%.2f", total) + " DH");
    }

    private void validerNouvelleCommande() {
        FournisseurItem selFour = (FournisseurItem) cbFournisseur.getSelectedItem();
        if (selFour == null) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un fournisseur.", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (tempOrderLines.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Veuillez ajouter au moins un médicament à la commande.", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Approvisionnement appro = new Approvisionnement();
        appro.setIdFournisseur(selFour.fournisseur.getIdFournisseur());
        appro.setNomFournisseur(selFour.fournisseur.getRaisonSociale());
        Utilisateur user = SessionManager.getInstance().getUtilisateurConnecte();
        appro.setIdUtilisateur(user != null ? user.getIdUtilisateur() : 1);
        appro.setDateApprovisionnement(LocalDateTime.now());
        appro.setStatut("En attente");

        boolean ok = service.creerCommande(appro, new ArrayList<>(tempOrderLines));
        if (ok) {
            JOptionPane.showMessageDialog(this, "Bon de commande N° " + appro.getIdApprovisionnement() + " créé avec succès (Statut: En attente) !", "Succès", JOptionPane.INFORMATION_MESSAGE);

            int printChoice = JOptionPane.showConfirmDialog(
                    this,
                    "Souhaitez-vous imprimer le Bon de Commande maintenant ?",
                    "Impression Bon de Commande",
                    JOptionPane.YES_NO_OPTION
            );
            if (printChoice == JOptionPane.YES_OPTION) {
                imprimerBonCommande(appro, tempOrderLines, selFour.fournisseur);
            }

            fermerFormulaire();
            chargerDonnees();
        } else {
            JOptionPane.showMessageDialog(this, "Erreur lors de la création de la commande.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void mettreAJourBoutonsActions() {
        int idAppro = styledTable.getSelectedId();
        if (idAppro <= 0) {
            btnReception.setEnabled(false);
            btnPrintBC.setEnabled(false);
            btnAnnulerCmd.setEnabled(false);
            return;
        }

        Approvisionnement a = service.getParId(idAppro);
        if (a != null) {
            boolean isEnAttente = "En attente".equalsIgnoreCase(a.getStatut());
            btnReception.setEnabled(isEnAttente);
            btnAnnulerCmd.setEnabled(isEnAttente);
            btnPrintBC.setEnabled(true);
        } else {
            btnReception.setEnabled(false);
            btnPrintBC.setEnabled(false);
            btnAnnulerCmd.setEnabled(false);
        }
    }

    private void imprimerBonCommandeSelectionne() {
        int idAppro = styledTable.getSelectedId();
        if (idAppro <= 0) return;
        Approvisionnement a = service.getParId(idAppro);
        if (a == null) return;
        List<LigneApprovisionnement> lignes = service.getLignes(idAppro);
        Fournisseur f = fournisseurDAO.findById(a.getIdFournisseur());
        imprimerBonCommande(a, lignes, f);
    }

    private void imprimerBonCommande(Approvisionnement appro, List<LigneApprovisionnement> lignes, Fournisseur f) {
        ParametresSociete params = parametresDAO.find();
        BonCommandePrintable printable = new BonCommandePrintable(appro, lignes, params, f);
        Window parent = SwingUtilities.getWindowAncestor(this);
        PrintPreviewDialog dialog = new PrintPreviewDialog(parent, "Bon de Commande Fournisseur (A4)", printable, PrintPreviewDialog.createA4PageFormat());
        dialog.setVisible(true);
    }

    private void annulerCommandeSelectionnee() {
        int idAppro = styledTable.getSelectedId();
        if (idAppro <= 0) return;

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Êtes-vous sûr de vouloir annuler la commande N° " + idAppro + " ?",
                "Confirmation d'annulation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            boolean ok = service.annulerCommande(idAppro);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Commande N° " + idAppro + " annulée.", "Confirmation", JOptionPane.INFORMATION_MESSAGE);
                chargerDonnees();
            } else {
                JOptionPane.showMessageDialog(this, "Impossible d'annuler cette commande.", "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ==========================================
    // ÉTAPE 2 : RÉCEPTION DE LA LIVRAISON
    // ==========================================
    private void ouvrirDialogueReception() {
        int idAppro = styledTable.getSelectedId();
        if (idAppro <= 0) return;

        Approvisionnement a = service.getParId(idAppro);
        if (a == null || !"En attente".equalsIgnoreCase(a.getStatut())) {
            JOptionPane.showMessageDialog(this, "Seules les commandes 'En attente' peuvent être réceptionnées.", "Attention", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<LigneApprovisionnement> lignesOriginales = service.getLignes(idAppro);
        if (lignesOriginales == null || lignesOriginales.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Cette commande ne contient aucun article à réceptionner.",
                    "Commande vide", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(parentWindow, "Réception de Livraison — Commande N° " + a.getIdApprovisionnement(), Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(950, 520);
        dialog.setMinimumSize(new Dimension(880, 460));
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new MigLayout("fill, insets 16", "[grow, fill]", "[]10[grow, fill]12[]"));

        // Header info
        JPanel dlgHeader = new JPanel(new MigLayout("insets 0", "[grow][]", "[]"));
        dlgHeader.setOpaque(false);
        JLabel lblInfo = new JLabel("Fournisseur : " + a.getNomFournisseur() + "  |  Date Commande : " + ValidationUtils.formatDateTime(a.getDateApprovisionnement()));
        lblInfo.setFont(lblInfo.getFont().deriveFont(Font.BOLD, 13f));
        dlgHeader.add(lblInfo, "grow");
        dialog.add(dlgHeader, "growx, wrap");

        // Table editable
        String[] cols = new String[]{"Médicament", "ID Méd.", "Qté Cmd", "Qté Reçue *", "P.U Achat *", "N° Lot *", "Date Exp. * (YYYY-MM-DD)", "Sous-Total"};
        DefaultTableModel recepModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                // Qté Reçue, P.U Achat, N° Lot, Date Exp sont éditables
                return col == 3 || col == 4 || col == 5 || col == 6;
            }
        };

        LocalDate defExp = LocalDate.now().plusYears(2);
        for (LigneApprovisionnement la : lignesOriginales) {
            int qteCmd = la.getQuantiteCommandee();
            double pu = la.getPrixAchatUnitaire();
            double st = qteCmd * pu;
            recepModel.addRow(new Object[]{
                    la.getNomMedicament() != null ? la.getNomMedicament() : ("Médicament #" + la.getIdMedicament()),
                    la.getIdMedicament(),
                    qteCmd,
                    qteCmd, // par défaut égale à commandée
                    String.format(java.util.Locale.US, "%.2f", pu),
                    "LOT-" + (System.currentTimeMillis() % 100000),
                    defExp.toString(),
                    String.format(java.util.Locale.US, "%.2f", st)
            });
        }

        JTable recepTable = new JTable(recepModel);
        recepTable.setRowHeight(28);
        recepTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        recepTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        if (recepTable.getColumnModel().getColumnCount() >= 8) {
            recepTable.getColumnModel().getColumn(0).setPreferredWidth(180); // Médicament
            recepTable.getColumnModel().getColumn(1).setPreferredWidth(55);  // ID Méd.
            recepTable.getColumnModel().getColumn(2).setPreferredWidth(75);  // Qté Cmd
            recepTable.getColumnModel().getColumn(3).setPreferredWidth(85);  // Qté Reçue *
            recepTable.getColumnModel().getColumn(4).setPreferredWidth(95);  // P.U Achat *
            recepTable.getColumnModel().getColumn(5).setPreferredWidth(110); // N° Lot *
            recepTable.getColumnModel().getColumn(6).setPreferredWidth(160); // Date Exp. * (YYYY-MM-DD)
            recepTable.getColumnModel().getColumn(7).setPreferredWidth(95);  // Sous-Total
        }

        JLabel lblTotalRecep = new JLabel();
        lblTotalRecep.setFont(lblTotalRecep.getFont().deriveFont(Font.BOLD, 15f));
        lblTotalRecep.setForeground(new Color(40, 167, 69));

        final boolean[] isUpdating = {false};
        Runnable recalcTotal = () -> {
            if (isUpdating[0]) return;
            isUpdating[0] = true;
            try {
                double total = 0;
                for (int r = 0; r < recepModel.getRowCount(); r++) {
                    try {
                        Object qObj = recepModel.getValueAt(r, 3);
                        Object pObj = recepModel.getValueAt(r, 4);
                        if (qObj != null && pObj != null) {
                            int q = Integer.parseInt(qObj.toString().trim());
                            double p = Double.parseDouble(pObj.toString().trim().replace(',', '.'));
                            double st = q * p;
                            recepModel.setValueAt(String.format(java.util.Locale.US, "%.2f", st), r, 7);
                            total += st;
                        }
                    } catch (Exception ignored) {}
                }
                lblTotalRecep.setText("Montant Total Réception : " + String.format(java.util.Locale.US, "%.2f", total) + " DH");
            } finally {
                isUpdating[0] = false;
            }
        };

        recepModel.addTableModelListener(e -> {
            if (isUpdating[0]) return;
            if (e.getColumn() == 3 || e.getColumn() == 4) {
                SwingUtilities.invokeLater(recalcTotal);
            }
        });
        recalcTotal.run();

        dialog.add(new JScrollPane(recepTable), "grow, push, wrap");

        // Footer buttons
        JPanel dlgFooter = new JPanel(new MigLayout("insets 0, fillx", "[grow]push[]10[]", "[]"));
        dlgFooter.setOpaque(false);
        dlgFooter.add(lblTotalRecep);

        JButton btnValider = new JButton("Valider la Réception & Mettre à jour les stocks");
        btnValider.setBackground(new Color(40, 167, 69));
        btnValider.setForeground(Color.WHITE);
        btnValider.setFont(btnValider.getFont().deriveFont(Font.BOLD, 12f));
        btnValider.setMargin(new Insets(6, 16, 6, 16));

        btnValider.addActionListener(ev -> {
            if (recepTable.isEditing() && recepTable.getCellEditor() != null) {
                recepTable.getCellEditor().stopCellEditing();
            }

            List<LigneApprovisionnement> lignesRecues = new ArrayList<>();
            List<Lot> lots = new ArrayList<>();
            LocalDate today = LocalDate.now();

            for (int r = 0; r < recepModel.getRowCount(); r++) {
                int idMed = (int) recepModel.getValueAt(r, 1);
                int qteCmd = (int) recepModel.getValueAt(r, 2);

                String qteRecueStr = recepModel.getValueAt(r, 3).toString().trim();
                String puStr = recepModel.getValueAt(r, 4).toString().trim().replace(',', '.');
                String numLot = recepModel.getValueAt(r, 5).toString().trim();
                String dateExpStr = recepModel.getValueAt(r, 6).toString().trim();

                if (!ValidationUtils.isPositiveInteger(qteRecueStr) && !qteRecueStr.equals("0")) {
                    JOptionPane.showMessageDialog(dialog, "Ligne " + (r + 1) + " : Quantité reçue invalide.", "Erreur de validation", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (!ValidationUtils.isPositiveNumber(puStr)) {
                    JOptionPane.showMessageDialog(dialog, "Ligne " + (r + 1) + " : Prix unitaire invalide.", "Erreur de validation", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                int qteRecue = Integer.parseInt(qteRecueStr);
                double pu = Double.parseDouble(puStr);

                if (qteRecue > 0) {
                    if (numLot.isBlank()) {
                        JOptionPane.showMessageDialog(dialog, "Ligne " + (r + 1) + " : Le N° de lot est obligatoire pour les articles reçus.", "Validation", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    LocalDate expDate;
                    try {
                        expDate = LocalDate.parse(dateExpStr);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(dialog, "Ligne " + (r + 1) + " : Format de date d'expiration invalide (YYYY-MM-DD).", "Validation", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    if (!expDate.isAfter(today)) {
                        JOptionPane.showMessageDialog(dialog, "Ligne " + (r + 1) + " : La date d'expiration doit être strictement supérieure à aujourd'hui.", "Validation", JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    Lot lot = new Lot();
                    lot.setNumeroLot(numLot);
                    lot.setDateExpiration(expDate);
                    lots.add(lot);
                } else {
                    Lot emptyLot = new Lot();
                    emptyLot.setNumeroLot("NON_RECU");
                    emptyLot.setDateExpiration(today.plusDays(1));
                    lots.add(emptyLot);
                }

                LigneApprovisionnement la = new LigneApprovisionnement();
                la.setIdMedicament(idMed);
                la.setQuantiteCommandee(qteCmd);
                la.setQuantiteRecue(qteRecue);
                la.setPrixAchatUnitaire(pu);
                lignesRecues.add(la);
            }

            boolean ok = service.validerReception(a.getIdApprovisionnement(), lignesRecues, lots);
            if (ok) {
                JOptionPane.showMessageDialog(dialog, "Réception validée avec succès ! Les stocks et lots ont été mis à jour.", "Succès", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                chargerDonnees();
            } else {
                JOptionPane.showMessageDialog(dialog, "Erreur lors de la validation de la réception.", "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton btnFermer = new JButton("Fermer");
        btnFermer.setMargin(new Insets(6, 14, 6, 14));
        btnFermer.addActionListener(ev -> dialog.dispose());

        dlgFooter.add(btnValider);
        dlgFooter.add(btnFermer);
        dialog.add(dlgFooter, "growx");

        dialog.setVisible(true);
    }

    public void chargerDonnees() {
        List<Approvisionnement> data = service.getTous();
        styledTable.setData(data.stream().map(a -> new Object[]{
                a.getIdApprovisionnement(),
                a.getDateApprovisionnement() != null ? ValidationUtils.formatDateTime(a.getDateApprovisionnement()) : "",
                a.getNomFournisseur(),
                a.getNomUtilisateur(),
                String.format("%.2f", a.getMontantTotal()),
                a.getStatut()
        }).toArray(Object[][]::new));
        mettreAJourBoutonsActions();
    }

    // Classes utilitaires d'affichage ComboBox
    private static class FournisseurItem {
        final Fournisseur fournisseur;

        FournisseurItem(Fournisseur f) {
            this.fournisseur = f;
        }

        @Override
        public String toString() {
            return fournisseur.getRaisonSociale() + " (ID:" + fournisseur.getIdFournisseur() + ")";
        }
    }

    private static class MedicamentItem {
        final Medicament medicament;

        MedicamentItem(Medicament m) {
            this.medicament = m;
        }

        @Override
        public String toString() {
            return medicament.getNomCommercial() + " (" + (medicament.getDosage() != null ? medicament.getDosage() : "") + ")";
        }
    }
}
