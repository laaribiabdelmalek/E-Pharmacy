package com.epharmacy.ui.panels;

import com.epharmacy.dao.FournisseurDAO;
import com.epharmacy.model.Fournisseur;
import com.epharmacy.ui.components.*;
import net.miginfocom.swing.MigLayout;
import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Panneau de gestion des Fournisseurs.
 * @author E-PHARMACIE
 */
public class FournisseurPanel extends JPanel {
    private StyledTable styledTable;
    private JTextField searchField;
    private final FournisseurDAO dao = new FournisseurDAO();

    public FournisseurPanel() { initUI(); chargerDonnees(null); }

    private void initUI() {
        setLayout(new MigLayout("wrap, insets 15 20 15 20, gap 10", "[grow]", "[][grow]"));
        JPanel header = new JPanel(new MigLayout("insets 0", "[][grow][]", "[]"));
        header.setOpaque(false);
        JLabel title = new JLabel("Liste des Fournisseurs");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        searchField = new JTextField(20);
        searchField.putClientProperty("JTextField.placeholderText", "Rechercher...");
        searchField.addActionListener(e -> chargerDonnees(searchField.getText().trim()));
        JButton btnAjouter = new JButton("+ Ajouter Fournisseur");
        btnAjouter.setBackground(new Color(34, 197, 94)); btnAjouter.setForeground(Color.WHITE);
        btnAjouter.addActionListener(e -> ouvrirFormulaire(null));
        header.add(title); header.add(searchField, "growx"); header.add(btnAjouter);
        add(header, "growx");

        styledTable = new StyledTable(new String[]{"ID", "ICE", "Raison Sociale", "Nom Commercial", "Téléphone", "Ville", "Contact", "Statut"});
        styledTable.getTable().addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mousePressed(java.awt.event.MouseEvent e) { popup(e); }
            @Override public void mouseReleased(java.awt.event.MouseEvent e) { popup(e); }
            private void popup(java.awt.event.MouseEvent e) {
                if (e.isPopupTrigger()) {
                    int row = styledTable.getTable().rowAtPoint(e.getPoint());
                    if (row >= 0) {
                        styledTable.getTable().setRowSelectionInterval(row, row);
                        int id = styledTable.getSelectedId();
                        ActionPopupMenu.create(() -> ouvrirFormulaire(dao.findById(id)), () -> supprimer(id))
                            .show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            }
        });
        add(styledTable, "grow, push");
    }

    private void ouvrirFormulaire(Fournisseur f) {
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
        FormDialog dialog = new FormDialog(frame, f == null ? "Ajouter Fournisseur" : "Modifier Fournisseur", 550, 500);
        JTextField tfIce = new JTextField(f != null ? f.getIce() : "");
        JTextField tfRaison = new JTextField(f != null ? f.getRaisonSociale() : "");
        JTextField tfNomCom = new JTextField(f != null ? f.getNomCommercial() : "");
        JTextField tfTel = new JTextField(f != null ? f.getTelephone() : "");
        JTextField tfEmail = new JTextField(f != null ? f.getEmail() : "");
        JTextField tfAdresse = new JTextField(f != null ? f.getAdresse() : "");
        JTextField tfVille = new JTextField(f != null ? f.getVille() : "");
        JTextField tfContact = new JTextField(f != null ? f.getNomContact() : "");
        JComboBox<String> cbStatut = new JComboBox<>(new String[]{"ACTIF", "INACTIF"});
        if (f != null) cbStatut.setSelectedItem(f.getStatut());

        dialog.addFormField("ICE", tfIce); dialog.addFormField("Raison Sociale *", tfRaison);
        dialog.addFormField("Nom Commercial", tfNomCom); dialog.addFormField("Téléphone", tfTel);
        dialog.addFormField("Email", tfEmail); dialog.addFormField("Adresse", tfAdresse);
        dialog.addFormField("Ville", tfVille); dialog.addFormField("Contact", tfContact);
        dialog.addFormField("Statut", cbStatut);
        dialog.setOkAction(() -> {
            if (tfRaison.getText().isBlank()) { dialog.showError("Raison sociale obligatoire."); return; }
            Fournisseur fn = f != null ? f : new Fournisseur();
            fn.setIce(tfIce.getText().trim()); fn.setRaisonSociale(tfRaison.getText().trim());
            fn.setNomCommercial(tfNomCom.getText().trim()); fn.setTelephone(tfTel.getText().trim());
            fn.setEmail(tfEmail.getText().trim()); fn.setAdresse(tfAdresse.getText().trim());
            fn.setVille(tfVille.getText().trim()); fn.setNomContact(tfContact.getText().trim());
            fn.setStatut((String) cbStatut.getSelectedItem());
            boolean ok = f != null ? dao.update(fn) : dao.insert(fn);
            if (ok) { dialog.dispose(); chargerDonnees(null); } else dialog.showError("Erreur d'enregistrement.");
        });
        dialog.setVisible(true);
    }

    private void supprimer(int id) {
        if (JOptionPane.showConfirmDialog(this, "Supprimer ce fournisseur ?", "Confirmation", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            if (!dao.delete(id)) JOptionPane.showMessageDialog(this, "Impossible de supprimer.", "Erreur", JOptionPane.ERROR_MESSAGE);
            chargerDonnees(null);
        }
    }

    private void chargerDonnees(String keyword) {
        List<Fournisseur> data = keyword != null && !keyword.isEmpty() ? dao.search(keyword) : dao.findAll();
        styledTable.setData(data.stream().map(f -> new Object[]{
            f.getIdFournisseur(), f.getIce(), f.getRaisonSociale(), f.getNomCommercial(),
            f.getTelephone(), f.getVille(), f.getNomContact(), f.getStatut()
        }).toArray(Object[][]::new));
    }
}
