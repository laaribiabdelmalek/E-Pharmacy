package com.epharmacy.ui.panels;

import com.epharmacy.dao.FournisseurDAO;
import com.epharmacy.model.Fournisseur;
import com.epharmacy.ui.components.*;
import net.miginfocom.swing.MigLayout;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Panneau de gestion des Fournisseurs — formulaire inline collapsible.
 * @author E-PHARMACIE
 */
public class FournisseurPanel extends JPanel {
    private StyledTable styledTable;
    private JTextField searchField;
    private JComboBox<String> filtreStatut;
    private JPanel formPanel;
    private TitledBorder formBorder;
    private Integer editingId = null;

    private JTextField tfRaisonSociale, tfNomCommercial, tfIce, tfTelephone, tfEmail, tfVille, tfAdresse, tfNomContact;
    private JComboBox<String> cbStatut;

    private final FournisseurDAO dao = new FournisseurDAO();

    public FournisseurPanel() {
        initUI();
        chargerDonnees(null, null);
    }

    private void initUI() {
        setLayout(new MigLayout("fill, insets 20, hidemode 3", "[grow, fill, 0::]", "[]12[]12[grow, fill]"));

        // === Header ===
        JPanel header = new JPanel(new MigLayout("insets 0", "[][grow, center][][right]", "[]"));
        header.setOpaque(false);
        JLabel title = new JLabel("Liste des Fournisseurs");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));

        searchField = new JTextField(18);
        searchField.putClientProperty("JTextField.placeholderText", "Rechercher (raison sociale, ICE, ville)...");
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { appliquerFiltres(); }
            public void removeUpdate(DocumentEvent e) { appliquerFiltres(); }
            public void changedUpdate(DocumentEvent e) { appliquerFiltres(); }
        });

        filtreStatut = new JComboBox<>(new String[]{"Tous", "ACTIF", "INACTIF"});
        filtreStatut.addActionListener(e -> appliquerFiltres());

        JButton btnAjouter = new JButton("+ Ajouter Fournisseur");
        btnAjouter.setBackground(new Color(40, 167, 69));
        btnAjouter.setForeground(Color.WHITE);
        btnAjouter.addActionListener(e -> ouvrirFormulaire(null));

        header.add(title);
        header.add(searchField, "growx");
        header.add(filtreStatut);
        header.add(btnAjouter);
        add(header, "growx, wrap");

        // === Inline Form ===
        formPanel = createFormPanel();
        formPanel.setVisible(false);
        add(formPanel, "growx, wrap");

        // === Table ===
        styledTable = new StyledTable(new String[]{"ID", "ICE", "Raison Sociale", "Nom Commercial", "Téléphone", "Ville", "Contact", "Statut"});

        styledTable.getTable().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int id = styledTable.getSelectedId();
                    if (id > 0) ouvrirFormulaire(dao.findById(id));
                }
            }

            @Override public void mousePressed(MouseEvent e) { showPopup(e); }
            @Override public void mouseReleased(MouseEvent e) { showPopup(e); }

            private void showPopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    int row = styledTable.getTable().rowAtPoint(e.getPoint());
                    if (row >= 0) {
                        styledTable.getTable().setRowSelectionInterval(row, row);
                        int id = styledTable.getSelectedId();
                        ActionPopupMenu.create(
                            () -> ouvrirFormulaire(dao.findById(id)),
                            () -> supprimer(id)
                        ).show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            }
        });
        add(styledTable, "grow, push");
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new MigLayout("wrap 6, insets 10, gap 8", "[right][grow][right][grow][right][grow]", "[][][]"));
        formBorder = BorderFactory.createTitledBorder("Nouveau Fournisseur");
        panel.setBorder(formBorder);

        tfRaisonSociale = new JTextField();
        tfNomCommercial = new JTextField();
        tfIce = new JTextField();
        tfTelephone = new JTextField();
        tfEmail = new JTextField();
        tfVille = new JTextField();
        tfAdresse = new JTextField();
        tfNomContact = new JTextField();
        cbStatut = new JComboBox<>(new String[]{"ACTIF", "INACTIF"});

        panel.add(new JLabel("Raison Sociale *:")); panel.add(tfRaisonSociale, "growx");
        panel.add(new JLabel("Nom Commercial:")); panel.add(tfNomCommercial, "growx");
        panel.add(new JLabel("ICE:")); panel.add(tfIce, "growx");

        panel.add(new JLabel("Téléphone:")); panel.add(tfTelephone, "growx");
        panel.add(new JLabel("Email:")); panel.add(tfEmail, "growx");
        panel.add(new JLabel("Ville:")); panel.add(tfVille, "growx");

        panel.add(new JLabel("Adresse:")); panel.add(tfAdresse, "growx, span 3");
        panel.add(new JLabel("Nom Contact:")); panel.add(tfNomContact, "growx");

        panel.add(new JLabel("Statut:")); panel.add(cbStatut, "growx");

        JPanel btns = new JPanel(new MigLayout("insets 0, gap 10"));
        JButton btnSave = new JButton("Enregistrer");
        btnSave.setBackground(new Color(40, 167, 69));
        btnSave.setForeground(Color.WHITE);
        JButton btnCancel = new JButton("Annuler");

        btnSave.addActionListener(e -> sauvegarder());
        btnCancel.addActionListener(e -> fermerFormulaire());

        btns.add(btnSave);
        btns.add(btnCancel);
        panel.add(btns, "span 6, center");

        return panel;
    }

    private void ouvrirFormulaire(Fournisseur f) {
        if (f != null) {
            editingId = f.getIdFournisseur();
            formBorder.setTitle("Modifier Fournisseur — ID " + f.getIdFournisseur());
            tfRaisonSociale.setText(f.getRaisonSociale());
            tfNomCommercial.setText(f.getNomCommercial() != null ? f.getNomCommercial() : "");
            tfIce.setText(f.getIce() != null ? f.getIce() : "");
            tfTelephone.setText(f.getTelephone() != null ? f.getTelephone() : "");
            tfEmail.setText(f.getEmail() != null ? f.getEmail() : "");
            tfVille.setText(f.getVille() != null ? f.getVille() : "");
            tfAdresse.setText(f.getAdresse() != null ? f.getAdresse() : "");
            tfNomContact.setText(f.getNomContact() != null ? f.getNomContact() : "");
            cbStatut.setSelectedItem(f.getStatut());
        } else {
            editingId = null;
            formBorder.setTitle("Nouveau Fournisseur");
            tfRaisonSociale.setText(""); tfNomCommercial.setText(""); tfIce.setText("");
            tfTelephone.setText(""); tfEmail.setText(""); tfVille.setText("");
            tfAdresse.setText(""); tfNomContact.setText("");
            cbStatut.setSelectedIndex(0);
        }
        formPanel.setVisible(true);
        revalidate();
        repaint();
        tfRaisonSociale.requestFocusInWindow();
    }

    private void fermerFormulaire() {
        editingId = null;
        formPanel.setVisible(false);
        styledTable.getTable().clearSelection();
        revalidate();
        repaint();
    }

    private void sauvegarder() {
        String raison = tfRaisonSociale.getText().trim();
        if (raison.isEmpty()) {
            JOptionPane.showMessageDialog(this, "La raison sociale est obligatoire.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Fournisseur f;
        if (editingId != null) {
            f = dao.findById(editingId);
            if (f == null) { JOptionPane.showMessageDialog(this, "Fournisseur introuvable.", "Erreur", JOptionPane.ERROR_MESSAGE); return; }
        } else {
            f = new Fournisseur();
        }
        f.setRaisonSociale(raison);
        f.setNomCommercial(tfNomCommercial.getText().trim());
        f.setIce(tfIce.getText().trim());
        f.setTelephone(tfTelephone.getText().trim());
        f.setEmail(tfEmail.getText().trim());
        f.setVille(tfVille.getText().trim());
        f.setAdresse(tfAdresse.getText().trim());
        f.setNomContact(tfNomContact.getText().trim());
        f.setStatut((String) cbStatut.getSelectedItem());

        boolean ok = editingId != null ? dao.update(f) : dao.insert(f);
        if (ok) {
            JOptionPane.showMessageDialog(this, "Fournisseur enregistré avec succès !");
            fermerFormulaire();
            chargerDonnees(null, null);
        } else {
            JOptionPane.showMessageDialog(this, "Erreur lors de l'enregistrement.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void supprimer(int id) {
        int choix = JOptionPane.showConfirmDialog(this,
                "Êtes-vous sûr de vouloir supprimer ce fournisseur ?",
                "Confirmation de suppression", JOptionPane.YES_NO_OPTION);
        if (choix == JOptionPane.YES_OPTION) {
            if (!dao.delete(id)) {
                JOptionPane.showMessageDialog(this,
                        "Impossible de supprimer ce fournisseur car il est lié à des commandes existantes.",
                        "Suppression impossible", JOptionPane.ERROR_MESSAGE);
            }
            chargerDonnees(null, null);
        }
    }

    private void appliquerFiltres() {
        String keyword = searchField.getText().trim();
        String statut = filtreStatut.getSelectedIndex() == 0 ? null : (String) filtreStatut.getSelectedItem();
        chargerDonnees(keyword.isEmpty() ? null : keyword, statut);
    }

    public void chargerDonnees() {
        appliquerFiltres();
    }

    private void chargerDonnees(String keyword, String statut) {
        List<Fournisseur> data;
        if (keyword != null && !keyword.isEmpty()) {
            data = dao.search(keyword);
        } else {
            data = dao.findAll();
        }
        if (statut != null) {
            data = data.stream().filter(f -> statut.equals(f.getStatut())).toList();
        }
        styledTable.setData(data.stream().map(f -> new Object[]{
            f.getIdFournisseur(), f.getIce(), f.getRaisonSociale(), f.getNomCommercial(),
            f.getTelephone(), f.getVille(), f.getNomContact(), f.getStatut()
        }).toArray(Object[][]::new));
    }
}
