package com.epharmacy.ui;

import com.epharmacy.model.Utilisateur;
import com.epharmacy.service.AuthService;
import com.epharmacy.util.ThemeManager;
import net.miginfocom.swing.MigLayout;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * Fenêtre de connexion sécurisée de l'application E-PHARMACIE.
 * @author E-PHARMACIE
 */
public class LoginFrame extends JFrame {
    private JTextField tfLogin;
    private JPasswordField tfPassword;
    private JLabel lblError;
    private final AuthService authService = new AuthService();
    private Runnable onLoginSuccess;

    public LoginFrame() {
        initUI();
    }

    public void setOnLoginSuccess(Runnable callback) {
        this.onLoginSuccess = callback;
    }

    private void initUI() {
        setTitle("E-PHARMACIE — Connexion");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(480, 420);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel(new MigLayout("fill, insets 40", "[center]", "[]20[]10[]10[]15[]10[]"));
        mainPanel.setBackground(UIManager.getColor("Panel.background"));

        // Logo
        JLabel logo = new JLabel("⚕ E-PHARMACIE");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 28));
        logo.setForeground(new Color(33, 150, 243));
        mainPanel.add(logo, "center, wrap");

        JLabel subtitle = new JLabel("Système de Gestion de Pharmacie");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(Color.GRAY);
        mainPanel.add(subtitle, "center, wrap 25");

        // Login field
        JLabel lblLogin = new JLabel("Identifiant");
        lblLogin.setFont(new Font("Segoe UI", Font.BOLD, 13));
        mainPanel.add(lblLogin, "left, wrap 5");

        tfLogin = new JTextField(25);
        tfLogin.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tfLogin.putClientProperty("JTextField.placeholderText", "Entrez votre login...");
        tfLogin.setPreferredSize(new Dimension(300, 40));
        mainPanel.add(tfLogin, "growx, h 40!, wrap 10");

        // Password field
        JLabel lblPassword = new JLabel("Mot de passe");
        lblPassword.setFont(new Font("Segoe UI", Font.BOLD, 13));
        mainPanel.add(lblPassword, "left, wrap 5");

        tfPassword = new JPasswordField(25);
        tfPassword.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tfPassword.putClientProperty("JTextField.placeholderText", "Entrez votre mot de passe...");
        tfPassword.setPreferredSize(new Dimension(300, 40));
        mainPanel.add(tfPassword, "growx, h 40!, wrap 15");

        // Error label
        lblError = new JLabel(" ");
        lblError.setForeground(Color.RED);
        lblError.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        mainPanel.add(lblError, "center, wrap 5");

        // Login button
        JButton btnLogin = new JButton("Se connecter");
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogin.setBackground(new Color(33, 150, 243));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFocusPainted(false);
        btnLogin.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogin.setPreferredSize(new Dimension(300, 42));
        btnLogin.addActionListener(e -> attemptLogin());
        mainPanel.add(btnLogin, "growx, h 42!, wrap 10");

        // Theme toggle
        JToggleButton themeToggle = new JToggleButton(ThemeManager.isDarkMode() ? "🌙 Mode Sombre" : "☀ Mode Clair");
        themeToggle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        themeToggle.setSelected(ThemeManager.isDarkMode());
        themeToggle.addActionListener(e -> {
            ThemeManager.toggleTheme();
            themeToggle.setText(ThemeManager.isDarkMode() ? "🌙 Mode Sombre" : "☀ Mode Clair");
        });
        mainPanel.add(themeToggle, "center");

        setContentPane(mainPanel);

        // Enter key triggers login
        getRootPane().registerKeyboardAction(
            e -> attemptLogin(),
            KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }

    private void attemptLogin() {
        String login = tfLogin.getText().trim();
        String password = new String(tfPassword.getPassword());

        if (login.isEmpty() || password.isEmpty()) {
            lblError.setText("Veuillez remplir tous les champs.");
            return;
        }

        lblError.setText("Connexion en cours...");
        lblError.setForeground(Color.GRAY);

        SwingWorker<Utilisateur, Void> worker = new SwingWorker<>() {
            @Override
            protected Utilisateur doInBackground() {
                return authService.authentifier(login, password);
            }

            @Override
            protected void done() {
                try {
                    Utilisateur user = get();
                    if (user != null) {
                        lblError.setText(" ");
                        if (onLoginSuccess != null) {
                            onLoginSuccess.run();
                        }
                    } else {
                        lblError.setForeground(Color.RED);
                        lblError.setText("Identifiant ou mot de passe incorrect.");
                        tfPassword.setText("");
                        tfPassword.requestFocus();
                    }
                } catch (Exception ex) {
                    lblError.setForeground(Color.RED);
                    lblError.setText("Erreur de connexion à la base de données.");
                    ex.printStackTrace();
                }
            }
        };
        worker.execute();
    }
}
