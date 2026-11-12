package com.epharmacy.ui.panels;

import com.epharmacy.dao.ClientDAO;
import com.epharmacy.model.Client;
import com.epharmacy.ui.components.*;
import com.epharmacy.util.ValidationUtils;
import net.miginfocom.swing.MigLayout;
import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Panneau de gestion des Clients.
 * @author E-PHARMACIE
 */
public class ClientPanel extends JPanel {
    private StyledTable styledTable;
    private JTextField searchField;
    private final ClientDAO dao = new ClientDAO();

    public ClientPanel() { initUI(); chargerDonnees(null); }

    private void initUI() {
        setLayout(new MigLayout("wrap, insets 15 20 15 20, gap 10", "[grow]", "[][grow]"));

        JPanel header = new JPanel(new MigLayout("insets 0", "[][grow][]", "[]"));
        header.setOpaque(false);
        JLabel title = new JLabel("Liste des Clients");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        searchField = new JTextField(20);
        searchField.putClientProperty("JTextField.placeholderText", "Rechercher...");
        searchField.addActionListener(e -> chargerDonnees(searchField.getText().trim()));
        JButton btnAjouter = new JButton("+ Ajouter Client");
        btnAjouter.setBackground(new Color(34, 197, 94)); btnAjouter.setForeground(Color.WHITE);
        btnAjouter.addActionListener(e -> ouvrirFormulaire(null));
        header.add(title); header.add(searchField, "growx"); header.add(btnAjouter);
        add(header, "growx");

        styledTable = new StyledTable(new String[]{"ID", "Nom", "Prénom", "Téléphone", "Email", "Adresse", "Crédit (DH)"});
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

    private void ouvrirFormulaire(Client c) {
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
        FormDialog dialog = new FormDialog(frame, c == null ? "Ajouter Client" : "Modifier Client", 500, 400);
        JTextField tfNom = new JTextField(c != null ? c.getNom() : "");
        JTextField tfPrenom = new JTextField(c != null ? c.getPrenom() : "");
        JTextField tfTel = new JTextField(c != null ? c.getTelephone() : "");
        JTextField tfEmail = new JTextField(c != null ? c.getEmail() : "");
        JTextField tfAdresse = new JTextField(c != null ? c.getAdresse() : "");
        dialog.addFormField("Nom *", tfNom); dialog.addFormField("Prénom *", tfPrenom);
        dialog.addFormField("Téléphone", tfTel); dialog.addFormField("Email", tfEmail);
        dialog.addFormField("Adresse", tfAdresse);
        dialog.setOkAction(() -> {
            if (tfNom.getText().isBlank() || tfPrenom.getText().isBlank()) { dialog.showError("Nom et prénom obligatoires."); return; }
            Client cl = c != null ? c : new Client();
            cl.setNom(tfNom.getText().trim()); cl.setPrenom(tfPrenom.getText().trim());
            cl.setTelephone(tfTel.getText().trim()); cl.setEmail(tfEmail.getText().trim());
            cl.setAdresse(tfAdresse.getText().trim());
            boolean ok = c != null ? dao.update(cl) : dao.insert(cl);
            if (ok) { dialog.dispose(); chargerDonnees(null); } else dialog.showError("Erreur d'enregistrement.");
        });
        dialog.setVisible(true);
    }

    private void supprimer(int id) {
        if (JOptionPane.showConfirmDialog(this, "Supprimer ce client ?", "Confirmation", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            if (!dao.delete(id)) JOptionPane.showMessageDialog(this, "Impossible de supprimer.", "Erreur", JOptionPane.ERROR_MESSAGE);
            chargerDonnees(null);
        }
    }

    private void chargerDonnees(String keyword) {
        List<Client> data = keyword != null && !keyword.isEmpty() ? dao.search(keyword) : dao.findAll();
        styledTable.setData(data.stream().map(c -> new Object[]{
            c.getIdClient(), c.getNom(), c.getPrenom(), c.getTelephone(), c.getEmail(),
            c.getAdresse(), String.format("%.2f", c.getCredit())
        }).toArray(Object[][]::new));
    }
}
