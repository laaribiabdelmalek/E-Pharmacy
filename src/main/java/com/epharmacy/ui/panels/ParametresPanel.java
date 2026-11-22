package com.epharmacy.ui.panels;

import com.epharmacy.dao.ParametresSocieteDAO;
import com.epharmacy.model.ParametresSociete;
import com.epharmacy.util.ThemeManager;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

/**
 * Panneau des Paramètres généraux de la pharmacie et personnalisation du thème.
 *
 * @author E-PHARMACIE
 */
public class ParametresPanel extends JPanel {

    private final ParametresSocieteDAO dao = new ParametresSocieteDAO();

    // Thème
    private JRadioButton rbClair;
    private JRadioButton rbSombre;
    private JRadioButton rbSysteme;

    // Champs Société
    private JTextField tfNomPharmacie;
    private JTextField tfIce;
    private JTextField tfIdentifiantFiscal;
    private JTextField tfRc;
    private JTextField tfPatente;
    private JTextField tfTelephone;
    private JTextField tfEmail;
    private JTextField tfAdresse;
    private JTextField tfVille;
    private JTextField tfMessageTicket;

    public ParametresPanel() {
        initUI();
        chargerDonnees();
    }

    private void initUI() {
        setLayout(new MigLayout("fillx, insets 20 25 20 25, gap 15", "[grow]", "[]15[]15[]push"));

        // Titre principal
        JLabel title = new JLabel("Paramètres du Système");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));
        add(title, "wrap");

        // ==========================================
        // 1. SECTION APPARENCE & THÈME
        // ==========================================
        JPanel themeSection = new JPanel(new MigLayout("insets 15, gap 15", "[][][]push", "[]"));
        themeSection.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(24, 76, 120), 1),
                "Apparence & Thème",
                TitledBorder.LEFT, TitledBorder.TOP,
                getFont().deriveFont(Font.BOLD, 13f),
                new Color(24, 76, 120)
        ));

        rbClair = new JRadioButton("Mode Clair (☀)");
        rbSombre = new JRadioButton("Mode Sombre (🌙)");
        rbSysteme = new JRadioButton("Système (Détection Windows 💻)");

        ButtonGroup bg = new ButtonGroup();
        bg.add(rbClair);
        bg.add(rbSombre);
        bg.add(rbSysteme);

        rbClair.addActionListener(e -> changerTheme("CLAIR"));
        rbSombre.addActionListener(e -> changerTheme("SOMBRE"));
        rbSysteme.addActionListener(e -> changerTheme("SYSTEME"));

        themeSection.add(rbClair);
        themeSection.add(rbSombre);
        themeSection.add(rbSysteme);

        add(themeSection, "growx, wrap");

        // ==========================================
        // 2. SECTION INFORMATIONS DE LA PHARMACIE
        // ==========================================
        JPanel infoSection = new JPanel(new MigLayout("fillx, insets 15, gap 10", "[right]10[grow,fill]20[right]10[grow,fill]", "[]"));
        infoSection.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(24, 76, 120), 1),
                "Informations de la Pharmacie & Mentions Fiscales",
                TitledBorder.LEFT, TitledBorder.TOP,
                getFont().deriveFont(Font.BOLD, 13f),
                new Color(24, 76, 120)
        ));

        tfNomPharmacie = new JTextField();
        tfIce = new JTextField();
        tfIdentifiantFiscal = new JTextField();
        tfRc = new JTextField();
        tfPatente = new JTextField();
        tfTelephone = new JTextField();
        tfEmail = new JTextField();
        tfAdresse = new JTextField();
        tfVille = new JTextField();
        tfMessageTicket = new JTextField();

        // Ligne 1
        infoSection.add(new JLabel("Nom Pharmacie * :"));
        infoSection.add(tfNomPharmacie);
        infoSection.add(new JLabel("ICE :"));
        infoSection.add(tfIce, "wrap");

        // Ligne 2
        infoSection.add(new JLabel("Identifiant Fiscal (I.F.) :"));
        infoSection.add(tfIdentifiantFiscal);
        infoSection.add(new JLabel("Registre Commerce (R.C.) :"));
        infoSection.add(tfRc, "wrap");

        // Ligne 3
        infoSection.add(new JLabel("Patente :"));
        infoSection.add(tfPatente);
        infoSection.add(new JLabel("Téléphone :"));
        infoSection.add(tfTelephone, "wrap");

        // Ligne 4
        infoSection.add(new JLabel("Email :"));
        infoSection.add(tfEmail);
        infoSection.add(new JLabel("Ville :"));
        infoSection.add(tfVille, "wrap");

        // Ligne 5 (Adresse pleine largeur)
        infoSection.add(new JLabel("Adresse :"));
        infoSection.add(tfAdresse, "span 3, wrap");

        // Ligne 6 (Message ticket pleine largeur)
        infoSection.add(new JLabel("Pied de Ticket :"));
        infoSection.add(tfMessageTicket, "span 3, wrap");

        add(infoSection, "growx, wrap");

        // ==========================================
        // 3. BOUTON ENREGISTRER
        // ==========================================
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setOpaque(false);

        JButton btnSave = new JButton("Enregistrer les paramètres");
        btnSave.setBackground(new Color(40, 167, 69));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFont(btnSave.getFont().deriveFont(Font.BOLD, 13f));
        btnSave.setPreferredSize(new Dimension(220, 36));
        btnSave.addActionListener(e -> enregistrerParametres());
        btnPanel.add(btnSave);

        add(btnPanel, "growx");
    }

    private void changerTheme(String theme) {
        ThemeManager.setTheme(theme);
        dao.updateTheme(theme);
    }

    public void chargerDonnees() {
        ParametresSociete p = dao.find();
        if (p == null) return;

        tfNomPharmacie.setText(p.getNomPharmacie() != null ? p.getNomPharmacie() : "");
        tfIce.setText(p.getIce() != null ? p.getIce() : "");
        tfIdentifiantFiscal.setText(p.getIdentifiantFiscal() != null ? p.getIdentifiantFiscal() : "");
        tfRc.setText(p.getRc() != null ? p.getRc() : "");
        tfPatente.setText(p.getPatente() != null ? p.getPatente() : "");
        tfTelephone.setText(p.getTelephone() != null ? p.getTelephone() : "");
        tfEmail.setText(p.getEmail() != null ? p.getEmail() : "");
        tfAdresse.setText(p.getAdresse() != null ? p.getAdresse() : "");
        tfVille.setText(p.getVille() != null ? p.getVille() : "");
        tfMessageTicket.setText(p.getMessageTicket() != null ? p.getMessageTicket() : "");

        String currentTheme = p.getThemeApp();
        if ("SOMBRE".equalsIgnoreCase(currentTheme)) {
            rbSombre.setSelected(true);
        } else if ("SYSTEME".equalsIgnoreCase(currentTheme)) {
            rbSysteme.setSelected(true);
        } else {
            rbClair.setSelected(true);
        }
    }

    private void enregistrerParametres() {
        String nom = tfNomPharmacie.getText().trim();
        if (nom.isBlank()) {
            JOptionPane.showMessageDialog(this, "Le nom de la pharmacie est obligatoire.", "Validation", JOptionPane.WARNING_MESSAGE);
            tfNomPharmacie.requestFocus();
            return;
        }

        ParametresSociete p = new ParametresSociete();
        p.setId(1);
        p.setNomPharmacie(nom);
        p.setIce(tfIce.getText().trim());
        p.setIdentifiantFiscal(tfIdentifiantFiscal.getText().trim());
        p.setRc(tfRc.getText().trim());
        p.setPatente(tfPatente.getText().trim());
        p.setTelephone(tfTelephone.getText().trim());
        p.setEmail(tfEmail.getText().trim());
        p.setAdresse(tfAdresse.getText().trim());
        p.setVille(tfVille.getText().trim());
        p.setMessageTicket(tfMessageTicket.getText().trim());

        String theme = rbSombre.isSelected() ? "SOMBRE" : (rbSysteme.isSelected() ? "SYSTEME" : "CLAIR");
        p.setThemeApp(theme);

        boolean ok = dao.save(p);
        if (ok) {
            JOptionPane.showMessageDialog(this, "Paramètres de la pharmacie enregistrés avec succès !", "Succès", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Erreur lors de l'enregistrement des paramètres.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
}
