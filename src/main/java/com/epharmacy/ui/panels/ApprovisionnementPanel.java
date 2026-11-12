package com.epharmacy.ui.panels;

import com.epharmacy.dao.FournisseurDAO;
import com.epharmacy.dao.MedicamentDAO;
import com.epharmacy.model.*;
import com.epharmacy.service.ApprovisionnementService;
import com.epharmacy.ui.components.*;
import com.epharmacy.util.*;
import net.miginfocom.swing.MigLayout;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Panneau de gestion des Approvisionnements.
 * @author E-PHARMACIE
 */
public class ApprovisionnementPanel extends JPanel {
    private StyledTable styledTable;
    private final ApprovisionnementService service = new ApprovisionnementService();
    private final FournisseurDAO fournisseurDAO = new FournisseurDAO();
    private final MedicamentDAO medicamentDAO = new MedicamentDAO();

    public ApprovisionnementPanel() {
        initUI();
        chargerDonnees();
    }

    private void initUI() {
        setLayout(new MigLayout("wrap, insets 15 20 15 20, gap 10", "[grow]", "[][grow]"));

        JPanel header = new JPanel(new MigLayout("insets 0", "[grow][]", "[]"));
        header.setOpaque(false);
        JLabel title = new JLabel("Approvisionnements");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        JButton btnNouveau = new JButton("+ Nouvel Approvisionnement");
        btnNouveau.setBackground(new Color(34, 197, 94));
        btnNouveau.setForeground(Color.WHITE);
        btnNouveau.addActionListener(e -> ouvrirFormulaire());
        header.add(title, "grow");
        header.add(btnNouveau);
        add(header, "growx");

        styledTable = new StyledTable(new String[]{"ID", "Date", "Fournisseur", "Utilisateur", "Montant (DH)", "Statut"});
        add(styledTable, "grow, push");
    }

    private void ouvrirFormulaire() {
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(frame, "Nouvel Approvisionnement", true);
        dialog.setSize(800, 600);
        dialog.setLocationRelativeTo(frame);
        dialog.setLayout(new MigLayout("wrap, insets 15, gap 10", "[grow]", "[][][][grow][]"));

        // Fournisseur selection
        List<Fournisseur> fournisseurs = fournisseurDAO.findActifs();
        JComboBox<String> cbFournisseur = new JComboBox<>();
        for (Fournisseur f : fournisseurs) {
            cbFournisseur.addItem(f.getRaisonSociale() + " (ID:" + f.getIdFournisseur() + ")");
        }
        JPanel topPanel = new JPanel(new MigLayout("insets 0", "[][grow]", "[]"));
        topPanel.add(new JLabel("Fournisseur *:"));
        topPanel.add(cbFournisseur, "growx");
        dialog.add(topPanel, "growx");

        // Lines table
        DefaultTableModel linesModel = new DefaultTableModel(
            new String[]{"Médicament", "ID Méd.", "Qté Commandée", "Qté Reçue", "Prix Achat (DH)", "N° Lot", "Date Expiration"}, 0
        ) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable linesTable = new JTable(linesModel);
        linesTable.setRowHeight(30);
        dialog.add(new JScrollPane(linesTable), "grow, push");

        // Add line button
        JButton btnAddLine = new JButton("+ Ajouter Ligne");
        btnAddLine.addActionListener(e -> {
            JDialog lineDialog = new JDialog(dialog, "Ajouter Ligne", true);
            lineDialog.setSize(500, 400);
            lineDialog.setLocationRelativeTo(dialog);
            lineDialog.setLayout(new MigLayout("wrap 2, insets 15, gap 10", "[right][grow]", ""));

            List<Medicament> meds = medicamentDAO.findAll();
            JComboBox<String> cbMed = new JComboBox<>();
            for (Medicament m : meds) cbMed.addItem(m.getNomCommercial() + " (ID:" + m.getIdMedicament() + ")");
            JTextField tfQteCmd = new JTextField("100");
            JTextField tfQteRecu = new JTextField("100");
            JTextField tfPrix = new JTextField();
            JTextField tfNumLot = new JTextField();
            JTextField tfDateExp = new JTextField("2027-12-31");

            lineDialog.add(new JLabel("Médicament *:")); lineDialog.add(cbMed, "growx");
            lineDialog.add(new JLabel("Qté Commandée *:")); lineDialog.add(tfQteCmd, "growx");
            lineDialog.add(new JLabel("Qté Reçue *:")); lineDialog.add(tfQteRecu, "growx");
            lineDialog.add(new JLabel("Prix Achat (DH) *:")); lineDialog.add(tfPrix, "growx");
            lineDialog.add(new JLabel("N° Lot *:")); lineDialog.add(tfNumLot, "growx");
            lineDialog.add(new JLabel("Date Expiration * (YYYY-MM-DD):")); lineDialog.add(tfDateExp, "growx");

            JButton btnOk = new JButton("Ajouter");
            btnOk.setBackground(new Color(34, 197, 94));
            btnOk.setForeground(Color.WHITE);
            btnOk.addActionListener(ev -> {
                if (cbMed.getSelectedIndex() < 0 || tfNumLot.getText().isBlank() || tfPrix.getText().isBlank()) {
                    JOptionPane.showMessageDialog(lineDialog, "Tous les champs * sont obligatoires.");
                    return;
                }
                try {
                    Medicament selMed = meds.get(cbMed.getSelectedIndex());
                    linesModel.addRow(new Object[]{
                        selMed.getNomCommercial(), selMed.getIdMedicament(),
                        Integer.parseInt(tfQteCmd.getText().trim()),
                        Integer.parseInt(tfQteRecu.getText().trim()),
                        Double.parseDouble(tfPrix.getText().trim()),
                        tfNumLot.getText().trim(), tfDateExp.getText().trim()
                    });
                    lineDialog.dispose();
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(lineDialog, "Valeurs numériques invalides.");
                }
            });
            lineDialog.add(btnOk, "span 2, center");
            lineDialog.setVisible(true);
        });
        dialog.add(btnAddLine, "left");

        // Save button
        JPanel btnPanel = new JPanel(new MigLayout("insets 0, gap 10", "push[][]push"));
        JButton btnSave = new JButton("Enregistrer");
        btnSave.setBackground(new Color(34, 197, 94));
        btnSave.setForeground(Color.WHITE);
        btnSave.addActionListener(e -> {
            if (fournisseurs.isEmpty() || cbFournisseur.getSelectedIndex() < 0) {
                JOptionPane.showMessageDialog(dialog, "Sélectionnez un fournisseur.");
                return;
            }
            if (linesModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(dialog, "Ajoutez au moins une ligne.");
                return;
            }

            Approvisionnement appro = new Approvisionnement();
            appro.setIdFournisseur(fournisseurs.get(cbFournisseur.getSelectedIndex()).getIdFournisseur());
            appro.setIdUtilisateur(SessionManager.getInstance().getUtilisateurConnecte().getIdUtilisateur());
            appro.setDateApprovisionnement(LocalDateTime.now());
            appro.setStatut("EN_COURS");

            List<LigneApprovisionnement> lignes = new ArrayList<>();
            List<Lot> lots = new ArrayList<>();

            for (int i = 0; i < linesModel.getRowCount(); i++) {
                LigneApprovisionnement la = new LigneApprovisionnement();
                la.setIdMedicament((int) linesModel.getValueAt(i, 1));
                la.setQuantiteCommandee((int) linesModel.getValueAt(i, 2));
                la.setQuantiteRecue((int) linesModel.getValueAt(i, 3));
                la.setPrixAchatUnitaire((double) linesModel.getValueAt(i, 4));
                la.setDateReception(LocalDate.now());
                lignes.add(la);

                Lot lot = new Lot();
                lot.setNumeroLot((String) linesModel.getValueAt(i, 5));
                lot.setDateExpiration(LocalDate.parse((String) linesModel.getValueAt(i, 6)));
                lots.add(lot);
            }

            boolean ok = service.enregistrerApprovisionnement(appro, lignes, lots);
            if (ok) {
                JOptionPane.showMessageDialog(dialog, "Approvisionnement enregistré !");
                dialog.dispose();
                chargerDonnees();
            } else {
                JOptionPane.showMessageDialog(dialog, "Erreur d'enregistrement.", "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });
        JButton btnCancel = new JButton("Annuler");
        btnCancel.addActionListener(e -> dialog.dispose());
        btnPanel.add(btnSave);
        btnPanel.add(btnCancel);
        dialog.add(btnPanel, "growx");
        dialog.setVisible(true);
    }

    private void chargerDonnees() {
        List<Approvisionnement> data = service.getTous();
        styledTable.setData(data.stream().map(a -> new Object[]{
            a.getIdApprovisionnement(),
            a.getDateApprovisionnement() != null ? ValidationUtils.formatDateTime(a.getDateApprovisionnement()) : "",
            a.getNomFournisseur(), a.getNomUtilisateur(),
            String.format("%.2f", a.getMontantTotal()), a.getStatut()
        }).toArray(Object[][]::new));
    }
}
