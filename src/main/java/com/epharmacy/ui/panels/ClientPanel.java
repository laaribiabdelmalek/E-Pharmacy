package com.epharmacy.ui.panels;

import com.epharmacy.exception.BusinessException;
import com.epharmacy.model.Client;
import com.epharmacy.service.ClientService;
import com.epharmacy.ui.components.ActionPopupMenu;
import com.epharmacy.ui.components.StyledTable;
import com.epharmacy.util.SessionManager;
import com.epharmacy.util.ValidationUtils;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.util.List;

/**
 * Panneau de gestion des clients avec suivi des crédits, des plafonds
 * et enregistrement des règlements.
 *
 * @author E-PHARMACIE
 * @version 3.0
 */
public class ClientPanel extends JPanel {

    private final ClientService clientService = new ClientService();
    private StyledTable styledTable;
    private JTextField searchField;
    private JPanel formPanel;
    private TitledBorder formBorder;
    private Integer editingId = null;

    // Champs de formulaire
    private JTextField tfNom;
    private JTextField tfPrenom;
    private JTextField tfTelephone;
    private JTextField tfEmail;
    private JTextField tfAdresse;
    private JTextField tfCredit;          // Read-only (solde calculé)
    private JTextField tfPlafondCredit;   // Plafond autorisé
    private JComboBox<String> cbStatut;

    private JButton btnReglement;

    public ClientPanel() {
        initUI();
        chargerDonnees(null);
    }

    private void initUI() {
        setLayout(new MigLayout("fill, insets 20, hidemode 3", "[grow, fill, 0::]", "[]12[]12[grow, fill]"));

        // === Header Bar ===
        JPanel header = new JPanel(new MigLayout("insets 0", "[grow][250!][][]", "[]"));
        JLabel title = new JLabel("Clients");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));

        searchField = new JTextField();
        searchField.putClientProperty("JTextField.placeholderText", "Rechercher par nom, prénom, tél...");
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { appliquerRecherche(); }
            public void removeUpdate(DocumentEvent e) { appliquerRecherche(); }
            public void changedUpdate(DocumentEvent e) { appliquerRecherche(); }
        });

        btnReglement = new JButton("Enregistrer Règlement");
        btnReglement.setBackground(new Color(23, 162, 184)); // Cyan/Info
        btnReglement.setForeground(Color.WHITE);
        btnReglement.setEnabled(false);
        btnReglement.addActionListener(e -> ouvrirDialogueReglement());

        JButton btnAjouter = new JButton("+ Ajouter Client");
        btnAjouter.setBackground(new Color(40, 167, 69));
        btnAjouter.setForeground(Color.WHITE);
        btnAjouter.addActionListener(e -> ouvrirFormulaire(null));

        header.add(title);
        header.add(searchField, "growx");
        header.add(btnReglement);
        header.add(btnAjouter);
        add(header, "growx, wrap");

        // === Inline Collapsible Form ===
        formPanel = createFormPanel();
        formPanel.setVisible(false);
        add(formPanel, "growx, wrap");

        // === Table ===
        styledTable = new StyledTable(new String[]{"ID", "Nom", "Prénom", "Téléphone", "Email", "Adresse", "Crédit (DH)", "Plafond (DH)", "Statut"});
        styledTable.getTable().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int id = styledTable.getSelectedId();
                btnReglement.setEnabled(id > 0);
            }
        });

        styledTable.getTable().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int id = styledTable.getSelectedId();
                    if (id > 0) ouvrirFormulaire(clientService.getParId(id));
                }
            }

            @Override
            public void mousePressed(MouseEvent e) { showPopup(e); }

            @Override
            public void mouseReleased(MouseEvent e) { showPopup(e); }

            private void showPopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    int row = styledTable.getTable().rowAtPoint(e.getPoint());
                    if (row >= 0) {
                        styledTable.getTable().setRowSelectionInterval(row, row);
                        int id = styledTable.getSelectedId();
                        JPopupMenu popup = ActionPopupMenu.create(
                            () -> ouvrirFormulaire(clientService.getParId(id)),
                            () -> supprimer(id)
                        );
                        JMenuItem itemReglement = new JMenuItem("Enregistrer un règlement");
                        itemReglement.addActionListener(ev -> ouvrirDialogueReglement());
                        popup.add(itemReglement, 0);
                        popup.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            }
        });
        add(styledTable, "grow, push");
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new MigLayout("wrap 6, insets 12, gap 10", "[right][grow][right][grow][right][grow]", "[][][]"));
        formBorder = BorderFactory.createTitledBorder("Nouveau Client");
        panel.setBorder(formBorder);

        tfNom = new JTextField();
        tfPrenom = new JTextField();
        tfTelephone = new JTextField();
        tfEmail = new JTextField();
        tfAdresse = new JTextField();
        tfCredit = new JTextField("0.00");
        tfCredit.setEditable(false);
        tfCredit.setToolTipText("Le crédit est calculé automatiquement à partir des ventes et des règlements");

        tfPlafondCredit = new JTextField("1000.00");
        cbStatut = new JComboBox<>(new String[]{"ACTIF", "INACTIF"});

        panel.add(new JLabel("Nom *:"));
        panel.add(tfNom, "growx");
        panel.add(new JLabel("Prénom *:"));
        panel.add(tfPrenom, "growx");
        panel.add(new JLabel("Téléphone:"));
        panel.add(tfTelephone, "growx");

        panel.add(new JLabel("Email:"));
        panel.add(tfEmail, "growx");
        panel.add(new JLabel("Adresse:"));
        panel.add(tfAdresse, "growx, span 3");

        panel.add(new JLabel("Crédit (DH) :"));
        panel.add(tfCredit, "growx");
        panel.add(new JLabel("Plafond (DH) :"));
        panel.add(tfPlafondCredit, "growx");
        panel.add(new JLabel("Statut :"));
        panel.add(cbStatut, "growx");

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

    private void ouvrirFormulaire(Client c) {
        if (c != null) {
            editingId = c.getIdClient();
            formBorder.setTitle("Modifier Client — ID " + c.getIdClient());
            tfNom.setText(c.getNom());
            tfPrenom.setText(c.getPrenom());
            tfTelephone.setText(c.getTelephone() != null ? c.getTelephone() : "");
            tfEmail.setText(c.getEmail() != null ? c.getEmail() : "");
            tfAdresse.setText(c.getAdresse() != null ? c.getAdresse() : "");
            tfCredit.setText(String.format(java.util.Locale.US, "%.2f", c.getCredit()));
            tfPlafondCredit.setText(String.format(java.util.Locale.US, "%.2f", c.getPlafondCreditDouble()));
            cbStatut.setSelectedItem(c.getStatut());
        } else {
            editingId = null;
            formBorder.setTitle("Nouveau Client");
            tfNom.setText("");
            tfPrenom.setText("");
            tfTelephone.setText("");
            tfEmail.setText("");
            tfAdresse.setText("");
            tfCredit.setText("0.00");
            tfPlafondCredit.setText("1000.00");
            cbStatut.setSelectedItem("ACTIF");
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
        btnReglement.setEnabled(false);
        revalidate();
        repaint();
    }

    private void sauvegarder() {
        String nom = tfNom.getText().trim();
        String prenom = tfPrenom.getText().trim();
        if (nom.isEmpty() || prenom.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Le nom et le prénom sont obligatoires.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        BigDecimal plafond = new BigDecimal("1000.00");
        try {
            String pStr = tfPlafondCredit.getText().trim().replace(',', '.');
            if (!pStr.isEmpty()) plafond = new BigDecimal(pStr);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Le plafond de crédit saisi est invalide.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Client c;
        if (editingId != null) {
            c = clientService.getParId(editingId);
            if (c == null) {
                JOptionPane.showMessageDialog(this, "Client introuvable.", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } else {
            c = new Client();
        }
        c.setNom(nom);
        c.setPrenom(prenom);
        c.setTelephone(tfTelephone.getText().trim());
        c.setEmail(tfEmail.getText().trim());
        c.setAdresse(tfAdresse.getText().trim());
        c.setPlafondCredit(plafond);
        c.setStatut((String) cbStatut.getSelectedItem());

        try {
            boolean ok = editingId != null ? clientService.modifier(c) : clientService.ajouter(c);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Client enregistré avec succès !");
                fermerFormulaire();
                chargerDonnees(null);
            }
        } catch (BusinessException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Règle de Gestion", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erreur lors de l'enregistrement : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void ouvrirDialogueReglement() {
        int idClient = styledTable.getSelectedId();
        if (idClient <= 0) return;

        Client client = clientService.getParId(idClient);
        if (client == null) return;

        if (client.getCreditBD().compareTo(BigDecimal.ZERO) <= 0) {
            JOptionPane.showMessageDialog(this, "Ce client n'a aucun crédit en cours (solde: 0.00 DH).", "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JPanel dialogPanel = new JPanel(new MigLayout("insets 15, wrap 2, gap 10", "[right][grow]", "[][][][]"));
        dialogPanel.add(new JLabel("Client :"));
        dialogPanel.add(new JLabel("<html><b>" + client.getNomComplet() + "</b> (ID #" + client.getIdClient() + ")</html>"));

        dialogPanel.add(new JLabel("Crédit en cours :"));
        JLabel lblCreditActuel = new JLabel(String.format(java.util.Locale.US, "%.2f DH", client.getCredit()));
        lblCreditActuel.setFont(lblCreditActuel.getFont().deriveFont(Font.BOLD));
        lblCreditActuel.setForeground(new Color(220, 53, 69));
        dialogPanel.add(lblCreditActuel);

        JTextField tfMontant = new JTextField(String.format(java.util.Locale.US, "%.2f", client.getCredit()), 10);
        dialogPanel.add(new JLabel("Montant réglé (DH) *:"));
        dialogPanel.add(tfMontant, "growx");

        JComboBox<String> cbMode = new JComboBox<>(new String[]{"ESPECES", "CARTE", "VIREMENT"});
        dialogPanel.add(new JLabel("Mode de paiement *:"));
        dialogPanel.add(cbMode, "growx");

        int res = JOptionPane.showConfirmDialog(
                this,
                dialogPanel,
                "Enregistrer un Règlement de Crédit",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (res == JOptionPane.OK_OPTION) {
            String mStr = tfMontant.getText().trim().replace(',', '.');
            if (!ValidationUtils.isPositiveNumber(mStr)) {
                JOptionPane.showMessageDialog(this, "Montant de règlement invalide.", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
            BigDecimal montant = new BigDecimal(mStr);
            String mode = (String) cbMode.getSelectedItem();
            int userId = SessionManager.getInstance().isConnecte()
                    ? SessionManager.getInstance().getUtilisateurConnecte().getIdUtilisateur() : 1;

            try {
                boolean ok = clientService.enregistrerReglement(client.getIdClient(), montant, mode, userId);
                if (ok) {
                    JOptionPane.showMessageDialog(this, "Règlement de " + montant + " DH enregistré avec succès !", "Succès", JOptionPane.INFORMATION_MESSAGE);
                    chargerDonnees(null);
                }
            } catch (BusinessException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Attention", JOptionPane.WARNING_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erreur lors de l'enregistrement du règlement : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void supprimer(int id) {
        int choix = JOptionPane.showConfirmDialog(this,
                "Êtes-vous sûr de vouloir désactiver ce client ?",
                "Confirmation de désactivation", JOptionPane.YES_NO_OPTION);
        if (choix == JOptionPane.YES_OPTION) {
            try {
                if (clientService.supprimer(id)) {
                    JOptionPane.showMessageDialog(this, "Client désactivé avec succès.");
                }
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
        List<Client> data = keyword != null && !keyword.isEmpty() ? clientService.rechercher(keyword) : clientService.getTous();
        styledTable.setData(data.stream().map(c -> new Object[]{
            c.getIdClient(), c.getNom(), c.getPrenom(), c.getTelephone(), c.getEmail(),
            c.getAdresse(), String.format(java.util.Locale.US, "%.2f", c.getCredit()),
            String.format(java.util.Locale.US, "%.2f", c.getPlafondCreditDouble()),
            c.getStatut()
        }).toArray(Object[][]::new));
    }
}
