package com.epharmacy.ui.panels;

import com.epharmacy.exception.BusinessException;
import com.epharmacy.model.Utilisateur;
import com.epharmacy.service.UtilisateurService;
import com.epharmacy.ui.components.ActionPopupMenu;
import com.epharmacy.ui.components.StyledTable;
import com.epharmacy.util.SessionManager;
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
 * Panneau de gestion des utilisateurs avec application des règles de sécurité :
 * validation de la complexité des mots de passe, protection contre l'auto-rétrogradation
 * et maintien obligatoire d'au moins un administrateur actif.
 *
 * @author E-PHARMACIE
 * @version 3.0
 */
public class UtilisateurPanel extends JPanel {

    private final UtilisateurService service = new UtilisateurService();
    private StyledTable styledTable;
    private JTextField searchField;
    private JPanel formPanel;
    private TitledBorder formBorder;
    private Integer editingId = null;

    // Form fields
    private JTextField tfNom;
    private JTextField tfPrenom;
    private JTextField tfLogin;
    private JPasswordField tfPassword;
    private JComboBox<String> cbRole;
    private JComboBox<String> cbStatut;
    private JLabel lblPassword;

    public UtilisateurPanel() {
        initUI();
        chargerDonnees(null);
    }

    private void initUI() {
        setLayout(new MigLayout("fill, insets 20, hidemode 3", "[grow, fill, 0::]", "[]12[]12[grow, fill]"));

        // === Header Bar ===
        JPanel header = new JPanel(new MigLayout("insets 0", "[grow][250!][]", "[]"));
        JLabel title = new JLabel("Utilisateurs");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));

        searchField = new JTextField();
        searchField.putClientProperty("JTextField.placeholderText", "Rechercher (nom, prénom, login)...");
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { appliquerRecherche(); }
            public void removeUpdate(DocumentEvent e) { appliquerRecherche(); }
            public void changedUpdate(DocumentEvent e) { appliquerRecherche(); }
        });

        JButton btnAjouter = new JButton("+ Ajouter Utilisateur");
        btnAjouter.setBackground(new Color(40, 167, 69));
        btnAjouter.setForeground(Color.WHITE);
        btnAjouter.addActionListener(e -> ouvrirFormulaire(null));

        header.add(title);
        header.add(searchField, "growx");
        header.add(btnAjouter);
        add(header, "growx, wrap");

        // === Inline Form ===
        formPanel = createFormPanel();
        formPanel.setVisible(false);
        add(formPanel, "growx, wrap");

        // === Table ===
        styledTable = new StyledTable(new String[]{"ID", "Nom", "Prénom", "Login", "Rôle", "Statut"});

        styledTable.getTable().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int id = styledTable.getSelectedId();
                    if (id > 0) ouvrirFormulaire(service.getParId(id));
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
                            () -> ouvrirFormulaire(service.getParId(id)),
                            () -> supprimer(id)
                        ).show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            }
        });
        add(styledTable, "grow, push");
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new MigLayout("wrap 6, insets 12, gap 10", "[right][grow][right][grow][right][grow]", "[][][]"));
        formBorder = BorderFactory.createTitledBorder("Nouvel Utilisateur");
        panel.setBorder(formBorder);

        tfNom = new JTextField();
        tfPrenom = new JTextField();
        tfLogin = new JTextField();
        tfPassword = new JPasswordField();
        cbRole = new JComboBox<>(new String[]{"ADMIN", "PHARMACIEN", "EMPLOYE"});
        cbStatut = new JComboBox<>(new String[]{"ACTIF", "INACTIF"});
        lblPassword = new JLabel("Mot de passe *:");

        panel.add(new JLabel("Nom *:")); panel.add(tfNom, "growx");
        panel.add(new JLabel("Prénom *:")); panel.add(tfPrenom, "growx");
        panel.add(new JLabel("Login *:")); panel.add(tfLogin, "growx");

        panel.add(lblPassword); panel.add(tfPassword, "growx");
        panel.add(new JLabel("Rôle:")); panel.add(cbRole, "growx");
        panel.add(new JLabel("Statut:")); panel.add(cbStatut, "growx");

        JLabel lblHint = new JLabel("<html><small style='color:gray'>Exigences mot de passe : 8 car. min, 1 majuscule, 1 minuscule, 1 chiffre, 1 caractère spécial.</small></html>");
        panel.add(lblHint, "span 6, center, wrap");

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

    private void ouvrirFormulaire(Utilisateur u) {
        if (u != null) {
            editingId = u.getIdUtilisateur();
            formBorder.setTitle("Modifier Utilisateur — ID " + u.getIdUtilisateur());
            tfNom.setText(u.getNom());
            tfPrenom.setText(u.getPrenom());
            tfLogin.setText(u.getLogin());
            tfPassword.setText("");
            lblPassword.setText("Nouveau mot de passe:");
            cbRole.setSelectedItem(u.getRole());
            cbStatut.setSelectedItem(u.getStatut());
        } else {
            editingId = null;
            formBorder.setTitle("Nouvel Utilisateur");
            tfNom.setText("");
            tfPrenom.setText("");
            tfLogin.setText("");
            tfPassword.setText("");
            lblPassword.setText("Mot de passe *:");
            cbRole.setSelectedIndex(0);
            cbStatut.setSelectedIndex(0);
        }
        formPanel.setVisible(true);
        revalidate();
        repaint();
        tfNom.requestFocusInWindow();
    }

    private void fermerFormulaire() {
        editingId = null;
        formPanel.setVisible(false);
        styledTable.getTable().clearSelection();
        revalidate();
        repaint();
    }

    private void sauvegarder() {
        String nom = tfNom.getText().trim();
        String prenom = tfPrenom.getText().trim();
        String login = tfLogin.getText().trim();
        String pwd = new String(tfPassword.getPassword());

        if (nom.isEmpty() || prenom.isEmpty() || login.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Le nom, le prénom et le login sont obligatoires.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            if (editingId == null) {
                // Nouvel utilisateur
                if (pwd.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Le mot de passe est obligatoire pour un nouvel utilisateur.", "Validation", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                Utilisateur user = new Utilisateur();
                user.setNom(nom);
                user.setPrenom(prenom);
                user.setLogin(login);
                user.setRole((String) cbRole.getSelectedItem());
                user.setStatut((String) cbStatut.getSelectedItem());

                boolean ok = service.ajouter(user, pwd);
                if (ok) {
                    JOptionPane.showMessageDialog(this, "Utilisateur créé avec succès !");
                    fermerFormulaire();
                    chargerDonnees(null);
                }
            } else {
                // Modification utilisateur
                Utilisateur user = service.getParId(editingId);
                if (user == null) {
                    JOptionPane.showMessageDialog(this, "Utilisateur introuvable.", "Erreur", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                user.setNom(nom);
                user.setPrenom(prenom);
                user.setLogin(login);
                user.setRole((String) cbRole.getSelectedItem());
                user.setStatut((String) cbStatut.getSelectedItem());

                boolean ok = service.modifier(user);
                if (ok && !pwd.isEmpty()) {
                    service.changerMotDePasse(user.getIdUtilisateur(), pwd);
                }
                if (ok) {
                    JOptionPane.showMessageDialog(this, "Utilisateur mis à jour avec succès !");
                    fermerFormulaire();
                    chargerDonnees(null);
                }
            }
        } catch (BusinessException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Sécurité Utilisateur", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erreur lors de l'enregistrement : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void supprimer(int id) {
        int choix = JOptionPane.showConfirmDialog(this,
                "Êtes-vous sûr de vouloir désactiver cet utilisateur ?",
                "Confirmation de désactivation", JOptionPane.YES_NO_OPTION);
        if (choix == JOptionPane.YES_OPTION) {
            try {
                if (service.desactiver(id)) {
                    JOptionPane.showMessageDialog(this, "Utilisateur désactivé avec succès.");
                }
            } catch (BusinessException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Action interdite", JOptionPane.WARNING_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erreur lors de la désactivation : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
            chargerDonnees(null);
        }
    }

    private void appliquerRecherche() {
        String keyword = searchField.getText().trim();
        chargerDonnees(keyword.isEmpty() ? null : keyword);
    }

    public void chargerDonnees() {
        chargerDonnees(null);
    }

    private void chargerDonnees(String keyword) {
        List<Utilisateur> data = service.getTous();
        if (keyword != null && !keyword.isEmpty()) {
            String kw = keyword.toLowerCase();
            data = data.stream().filter(u ->
                u.getNom().toLowerCase().contains(kw) ||
                u.getPrenom().toLowerCase().contains(kw) ||
                u.getLogin().toLowerCase().contains(kw)
            ).toList();
        }
        styledTable.setData(data.stream().map(u -> new Object[]{
            u.getIdUtilisateur(), u.getNom(), u.getPrenom(), u.getLogin(), u.getRole(), u.getStatut()
        }).toArray(Object[][]::new));
    }
}
