package com.epharmacy.ui;

import com.epharmacy.exception.UnauthorizedException;
import com.epharmacy.model.Utilisateur;
import com.epharmacy.service.AuthService;
import com.epharmacy.util.ThemeManager;
import net.miginfocom.swing.MigLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.concurrent.ExecutionException;

/**
 * Fenêtre de connexion sécurisée de l'application E-PHARMACIE.
 * Gère l'affichage des messages de verrouillage temporaire et l'inactivité du compte.
 *
 * @author E-PHARMACIE
 * @version 3.0
 */
public class LoginFrame extends JFrame {

    private static final Logger logger = LoggerFactory.getLogger(LoginFrame.class);

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
        setSize(480, 440);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel(new MigLayout("fill, insets 40", "[center]", "[]15[]10[]10[]15[]10[]"));
        mainPanel.setBackground(UIManager.getColor("Panel.background"));

        // Logo
        JLabel logo = new JLabel("⚕ E-PHARMACIE");
        logo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28));
        logo.setForeground(new Color(33, 150, 243));
        mainPanel.add(logo, "center, wrap");

        JLabel subtitle = new JLabel("Système de Gestion de Pharmacie");
        subtitle.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        subtitle.setForeground(Color.GRAY);
        mainPanel.add(subtitle, "center, wrap 20");

        // Login field
        JLabel lblLogin = new JLabel("Identifiant");
        lblLogin.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        mainPanel.add(lblLogin, "left, wrap 5");

        tfLogin = new JTextField(25);
        tfLogin.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        tfLogin.putClientProperty("JTextField.placeholderText", "Entrez votre identifiant...");
        tfLogin.setPreferredSize(new Dimension(300, 40));
        mainPanel.add(tfLogin, "growx, h 40!, wrap 10");

        // Password field
        JLabel lblPassword = new JLabel("Mot de passe");
        lblPassword.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        mainPanel.add(lblPassword, "left, wrap 5");

        tfPassword = new JPasswordField(25);
        tfPassword.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        tfPassword.putClientProperty("JTextField.placeholderText", "Entrez votre mot de passe...");
        tfPassword.setPreferredSize(new Dimension(300, 40));
        mainPanel.add(tfPassword, "growx, h 40!, wrap 15");

        // Error label (multi-line capable)
        lblError = new JLabel(" ");
        lblError.setForeground(new Color(220, 53, 69));
        lblError.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        lblError.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(lblError, "center, growx, wrap 5");

        // Login button
        JButton btnLogin = new JButton("Se connecter");
        btnLogin.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        btnLogin.setBackground(new Color(33, 150, 243));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFocusPainted(false);
        btnLogin.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogin.setPreferredSize(new Dimension(300, 42));
        btnLogin.addActionListener(e -> attemptLogin());
        mainPanel.add(btnLogin, "growx, h 42!, wrap 10");

        // Theme toggle
        JToggleButton themeToggle = new JToggleButton(ThemeManager.isDarkMode() ? "🌙 Mode Sombre" : "☀ Mode Clair");
        themeToggle.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
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
            lblError.setForeground(new Color(220, 53, 69));
            lblError.setText("Veuillez renseigner tous les champs.");
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
                        lblError.setForeground(new Color(220, 53, 69));
                        lblError.setText("Identifiant ou mot de passe incorrect.");
                        tfPassword.setText("");
                        tfPassword.requestFocus();
                    }
                } catch (ExecutionException ex) {
                    lblError.setForeground(new Color(220, 53, 69));
                    Throwable cause = ex.getCause();
                    if (cause instanceof UnauthorizedException) {
                        lblError.setText("<html><center>" + cause.getMessage() + "</center></html>");
                    } else {
                        logger.error("Erreur technique lors de la connexion : {}", ex.getMessage(), ex);
                        lblError.setText("Erreur de connexion à la base de données.");
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    lblError.setText("Opération interrompue.");
                }
            }
        };
        worker.execute();
    }
}
