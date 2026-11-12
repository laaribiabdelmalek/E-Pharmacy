package com.epharmacy.ui.panels;

import com.epharmacy.dao.UtilisateurDAO;
import com.epharmacy.model.Utilisateur;
import com.epharmacy.ui.components.*;
import com.epharmacy.util.*;
import net.miginfocom.swing.MigLayout;
import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Panneau d'administration des Utilisateurs (Admin uniquement).
 * @author E-PHARMACIE
 */
public class UtilisateurPanel extends JPanel {
    private StyledTable styledTable;
    private final UtilisateurDAO dao = new UtilisateurDAO();

    public UtilisateurPanel() {
        initUI();
        chargerDonnees();
    }

    private void initUI() {
        setLayout(new MigLayout("wrap, insets 15 20 15 20, gap 10", "[grow]", "[][grow]"));

        JPanel header = new JPanel(new MigLayout("insets 0", "[grow][]", "[]"));
        header.setOpaque(false);
        JLabel title = new JLabel("Gestion des Utilisateurs");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        JButton btnAjouter = new JButton("+ Ajouter Utilisateur");
        btnAjouter.setBackground(new Color(34, 197, 94));
        btnAjouter.setForeground(Color.WHITE);
        btnAjouter.addActionListener(e -> ouvrirFormulaire(null));
        header.add(title, "grow");
        header.add(btnAjouter);
        add(header, "growx");

        styledTable = new StyledTable(new String[]{"ID", "Nom", "Prénom", "Login", "Rôle", "Statut"});
        styledTable.getTable().addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mousePressed(java.awt.event.MouseEvent e) { popup(e); }
            @Override public void mouseReleased(java.awt.event.MouseEvent e) { popup(e); }
            private void popup(java.awt.event.MouseEvent e) {
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

    private void ouvrirFormulaire(Utilisateur u) {
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
        FormDialog dialog = new FormDialog(frame, u == null ? "Ajouter Utilisateur" : "Modifier Utilisateur", 450, 400);

        JTextField tfNom = new JTextField(u != null ? u.getNom() : "");
        JTextField tfPrenom = new JTextField(u != null ? u.getPrenom() : "");
        JTextField tfLogin = new JTextField(u != null ? u.getLogin() : "");
        JPasswordField tfPassword = new JPasswordField();
        JComboBox<String> cbRole = new JComboBox<>(new String[]{"ADMIN", "PHARMACIEN", "EMPLOYE"});
        JComboBox<String> cbStatut = new JComboBox<>(new String[]{"ACTIF", "INACTIF"});
        if (u != null) {
            cbRole.setSelectedItem(u.getRole());
            cbStatut.setSelectedItem(u.getStatut());
        }

        dialog.addFormField("Nom *", tfNom);
        dialog.addFormField("Prénom *", tfPrenom);
        dialog.addFormField("Login *", tfLogin);
        dialog.addFormField(u == null ? "Mot de passe *" : "Nouveau mot de passe", tfPassword);
        dialog.addFormField("Rôle", cbRole);
        dialog.addFormField("Statut", cbStatut);

        dialog.setOkAction(() -> {
            if (tfNom.getText().isBlank() || tfPrenom.getText().isBlank() || tfLogin.getText().isBlank()) {
                dialog.showError("Nom, prénom et login sont obligatoires.");
                return;
            }
            if (u == null && tfPassword.getPassword().length == 0) {
                dialog.showError("Le mot de passe est obligatoire pour un nouvel utilisateur.");
                return;
            }

            Utilisateur user = u != null ? u : new Utilisateur();
            user.setNom(tfNom.getText().trim());
            user.setPrenom(tfPrenom.getText().trim());
            user.setLogin(tfLogin.getText().trim());
            user.setRole((String) cbRole.getSelectedItem());
            user.setStatut((String) cbStatut.getSelectedItem());

            boolean ok;
            if (u == null) {
                user.setMotDePasseHash(PasswordUtils.hashPassword(new String(tfPassword.getPassword())));
                ok = dao.insert(user);
            } else {
                ok = dao.update(user);
                if (ok && tfPassword.getPassword().length > 0) {
                    dao.updatePassword(user.getIdUtilisateur(), PasswordUtils.hashPassword(new String(tfPassword.getPassword())));
                }
            }

            if (ok) {
                dialog.dispose();
                chargerDonnees();
            } else {
                dialog.showError("Erreur d'enregistrement.");
            }
        });
        dialog.setVisible(true);
    }

    private void supprimer(int id) {
        int currentUserId = SessionManager.getInstance().getUtilisateurConnecte().getIdUtilisateur();
        if (id == currentUserId) {
            JOptionPane.showMessageDialog(this, "Vous ne pouvez pas supprimer votre propre compte !");
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Supprimer cet utilisateur ?", "Confirmation", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            if (!dao.delete(id)) {
                JOptionPane.showMessageDialog(this, "Impossible de supprimer.", "Erreur", JOptionPane.ERROR_MESSAGE);
            }
            chargerDonnees();
        }
    }

    private void chargerDonnees() {
        List<Utilisateur> data = dao.findAll();
        styledTable.setData(data.stream().map(u -> new Object[]{
            u.getIdUtilisateur(), u.getNom(), u.getPrenom(), u.getLogin(), u.getRole(), u.getStatut()
        }).toArray(Object[][]::new));
    }
}
