package com.epharmacy.ui.panels;

import com.epharmacy.model.Medicament;
import com.epharmacy.service.MedicamentService;
import com.epharmacy.ui.components.*;
import com.epharmacy.util.ValidationUtils;
import net.miginfocom.swing.MigLayout;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Panneau de gestion du catalogue de Médicaments.
 * @author E-PHARMACIE
 */
public class MedicamentPanel extends JPanel {
    private StyledTable styledTable;
    private JComboBox<String> filtreStatut;
    private JTextField searchField;
    private JPanel formPanel;
    private boolean formVisible = false;
    private Medicament editingMed = null;

    // Form fields
    private JTextField tfNom, tfDci, tfDosage, tfConditionnement, tfLaboratoire, tfConditions, tfCategorie, tfPrix, tfSeuil;
    private JComboBox<String> cbForme, cbType;

    private final MedicamentService service = new MedicamentService();

    public MedicamentPanel() { initUI(); chargerDonnees(null, null); }

    private void initUI() {
        setLayout(new MigLayout("wrap, insets 15 20 15 20, gap 10", "[grow]", "[][][][grow]"));

        // Header
        JPanel header = new JPanel(new MigLayout("insets 0", "[grow][]", "[]"));
        header.setOpaque(false);
        JLabel title = new JLabel("Liste des Médicaments");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        JButton btnAjouter = new JButton("+ Ajouter Médicament");
        btnAjouter.setBackground(new Color(34, 197, 94));
        btnAjouter.setForeground(Color.WHITE);
        btnAjouter.addActionListener(e -> toggleForm(null));
        header.add(title, "grow"); header.add(btnAjouter);
        add(header, "growx");

        // Filters
        JPanel filters = new JPanel(new MigLayout("insets 0, gap 10", "[][grow]", "[]"));
        filters.setOpaque(false);
        filtreStatut = new JComboBox<>(new String[]{"Tous", "COMMERCIALISE", "NON_COMMERCIALISE", "EN_RUPTURE"});
        filtreStatut.addActionListener(e -> appliquerFiltres());
        searchField = new JTextField(20);
        searchField.putClientProperty("JTextField.placeholderText", "Rechercher (nom, DCI, catégorie)...");
        searchField.addActionListener(e -> appliquerFiltres());
        filters.add(new JLabel("Statut:")); filters.add(filtreStatut);
        filters.add(new JLabel("Recherche:")); filters.add(searchField, "growx");
        add(filters, "growx");

        // Inline form (hidden by default)
        formPanel = createFormPanel();
        formPanel.setVisible(false);
        add(formPanel, "growx");

        // Table
        styledTable = new StyledTable(new String[]{"ID", "Nom Commercial", "DCI", "Forme", "Laboratoire", "Catégorie", "Prix (DH)", "Stock", "Seuil", "Statut"});
        styledTable.setColumnWidths(40, 150, 120, 90, 120, 90, 80, 60, 50, 110);

        // Actions on row click
        styledTable.getTable().addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int id = styledTable.getSelectedId();
                    if (id > 0) toggleForm(service.getParId(id));
                }
            }
            @Override public void mousePressed(java.awt.event.MouseEvent e) { showPopup(e); }
            @Override public void mouseReleased(java.awt.event.MouseEvent e) { showPopup(e); }
            private void showPopup(java.awt.event.MouseEvent e) {
                if (e.isPopupTrigger()) {
                    int row = styledTable.getTable().rowAtPoint(e.getPoint());
                    if (row >= 0) {
                        styledTable.getTable().setRowSelectionInterval(row, row);
                        int id = styledTable.getSelectedId();
                        ActionPopupMenu.create(
                            () -> toggleForm(service.getParId(id)),
                            () -> supprimerMedicament(id)
                        ).show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            }
        });
        add(styledTable, "grow, push");
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new MigLayout("wrap 6, insets 10, gap 8", "[right][grow][right][grow][right][grow]", "[][]"));
        panel.setBorder(BorderFactory.createTitledBorder("Formulaire Médicament"));

        tfNom = new JTextField(); tfDci = new JTextField(); tfDosage = new JTextField();
        tfConditionnement = new JTextField(); tfLaboratoire = new JTextField();
        tfConditions = new JTextField(); tfCategorie = new JTextField();
        tfPrix = new JTextField(); tfSeuil = new JTextField("10");
        cbForme = new JComboBox<>(new String[]{"COMPRIME","GELULE","SIROP","INJECTABLE","POMMADE","SUPPOSITOIRE","COLLYRE","SACHET","SOLUTION","AUTRE"});
        cbType = new JComboBox<>(new String[]{"PRINCEPS","GENERIQUE","OTC"});

        panel.add(new JLabel("Nom Commercial *:")); panel.add(tfNom, "growx");
        panel.add(new JLabel("DCI:")); panel.add(tfDci, "growx");
        panel.add(new JLabel("Dosage:")); panel.add(tfDosage, "growx");
        panel.add(new JLabel("Forme:")); panel.add(cbForme, "growx");
        panel.add(new JLabel("Conditionnement:")); panel.add(tfConditionnement, "growx");
        panel.add(new JLabel("Laboratoire:")); panel.add(tfLaboratoire, "growx");
        panel.add(new JLabel("Type:")); panel.add(cbType, "growx");
        panel.add(new JLabel("Conditions Conserv.:")); panel.add(tfConditions, "growx");
        panel.add(new JLabel("Catégorie:")); panel.add(tfCategorie, "growx");
        panel.add(new JLabel("Prix Vente (DH) *:")); panel.add(tfPrix, "growx");
        panel.add(new JLabel("Seuil Minimal:")); panel.add(tfSeuil, "growx");

        JPanel btns = new JPanel(new MigLayout("insets 0, gap 10"));
        JButton btnSave = new JButton("Enregistrer"); btnSave.setBackground(new Color(34, 197, 94)); btnSave.setForeground(Color.WHITE);
        JButton btnCancel = new JButton("Annuler");
        btnSave.addActionListener(e -> sauvegarder());
        btnCancel.addActionListener(e -> { formPanel.setVisible(false); editingMed = null; });
        btns.add(btnSave); btns.add(btnCancel);
        panel.add(btns, "span 6, center");
        return panel;
    }

    private void toggleForm(Medicament m) {
        editingMed = m;
        if (m != null) {
            tfNom.setText(m.getNomCommercial()); tfDci.setText(m.getDci()); tfDosage.setText(m.getDosage());
            cbForme.setSelectedItem(m.getFormePharmaceutique()); tfConditionnement.setText(m.getConditionnement());
            tfLaboratoire.setText(m.getLaboratoire()); cbType.setSelectedItem(m.getTypeMedicament());
            tfConditions.setText(m.getConditionsConservation()); tfCategorie.setText(m.getCategorie());
            tfPrix.setText(String.valueOf(m.getPrixVentePublic())); tfSeuil.setText(String.valueOf(m.getSeuilMinimal()));
        } else {
            tfNom.setText(""); tfDci.setText(""); tfDosage.setText(""); tfConditionnement.setText("");
            tfLaboratoire.setText(""); tfConditions.setText(""); tfCategorie.setText("");
            tfPrix.setText(""); tfSeuil.setText("10");
        }
        formPanel.setVisible(true);
    }

    private void sauvegarder() {
        String nom = tfNom.getText().trim();
        if (nom.isEmpty()) { JOptionPane.showMessageDialog(this, "Le nom commercial est obligatoire."); return; }
        if (!ValidationUtils.isPositiveNumber(tfPrix.getText().trim())) { JOptionPane.showMessageDialog(this, "Prix invalide."); return; }

        Medicament m = editingMed != null ? editingMed : new Medicament();
        m.setNomCommercial(nom); m.setDci(tfDci.getText().trim()); m.setDosage(tfDosage.getText().trim());
        m.setFormePharmaceutique((String)cbForme.getSelectedItem()); m.setConditionnement(tfConditionnement.getText().trim());
        m.setLaboratoire(tfLaboratoire.getText().trim()); m.setTypeMedicament((String)cbType.getSelectedItem());
        m.setConditionsConservation(tfConditions.getText().trim()); m.setCategorie(tfCategorie.getText().trim());
        m.setPrixVentePublic(Double.parseDouble(tfPrix.getText().trim()));
        m.setSeuilMinimal(tfSeuil.getText().isBlank() ? 10 : Integer.parseInt(tfSeuil.getText().trim()));

        boolean ok = editingMed != null ? service.modifier(m) : service.ajouter(m);
        if (ok) {
            JOptionPane.showMessageDialog(this, "Médicament enregistré avec succès !");
            formPanel.setVisible(false); editingMed = null; chargerDonnees(null, null);
        } else {
            JOptionPane.showMessageDialog(this, "Erreur lors de l'enregistrement.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void supprimerMedicament(int id) {
        if (JOptionPane.showConfirmDialog(this, "Supprimer ce médicament ?", "Confirmation", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            if (service.supprimer(id)) { chargerDonnees(null, null); }
            else { JOptionPane.showMessageDialog(this, "Impossible de supprimer (référencé par des lots/ventes).", "Erreur", JOptionPane.ERROR_MESSAGE); }
        }
    }

    private void appliquerFiltres() {
        String statut = filtreStatut.getSelectedIndex() == 0 ? null : (String) filtreStatut.getSelectedItem();
        String keyword = searchField.getText().trim().isEmpty() ? null : searchField.getText().trim();
        chargerDonnees(statut, keyword);
    }

    private void chargerDonnees(String statut, String keyword) {
        List<Medicament> data;
        if (keyword != null && !keyword.isEmpty()) data = service.rechercher(keyword);
        else if (statut != null) data = service.getParStatut(statut);
        else data = service.getTous();

        Object[][] tableData = data.stream().map(m -> new Object[]{
            m.getIdMedicament(), m.getNomCommercial(), m.getDci(),
            m.getFormePharmaceutique(), m.getLaboratoire(), m.getCategorie(),
            String.format("%.2f", m.getPrixVentePublic()), m.getQuantiteDisponible(),
            m.getSeuilMinimal(), m.getStatut()
        }).toArray(Object[][]::new);
        styledTable.setData(tableData);
    }
}
