package com.epharmacy;

import com.epharmacy.config.DBConnection;
import com.epharmacy.dao.ParametresSocieteDAO;
import com.epharmacy.ui.LoginFrame;
import com.epharmacy.ui.MainFrame;
import com.epharmacy.util.ThemeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;

/**
 * Point d'entrée principal de l'application E-PHARMACIE.
 * <p>
 * Configure le Look and Feel FlatLaf selon le thème configuré en base,
 * enregistre le hook d'arrêt propre du pool HikariCP, initialise la fenêtre
 * de connexion, puis bascule vers la fenêtre principale.
 * </p>
 *
 * @author E-PHARMACIE
 * @version 3.0
 */
public class App {

    private static final Logger logger = LoggerFactory.getLogger(App.class);

    /**
     * Méthode principale de lancement de l'application.
     *
     * @param args arguments de la ligne de commande (non utilisés)
     */
    public static void main(String[] args) {
        logger.info("Démarrage de l'application E-PHARMACIE...");

        // Hook d'arrêt pour fermer proprement le pool HikariCP
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Arrêt de l'application détecté, fermeture des ressources...");
            DBConnection.shutdown();
        }));

        // Configurer FlatLaf & Thème sauvegardé
        try {
            String savedTheme = "CLAIR";
            try {
                ParametresSocieteDAO paramsDAO = new ParametresSocieteDAO();
                savedTheme = paramsDAO.getTheme();
            } catch (Exception e) {
                logger.warn("Impossible de charger le thème depuis la base, utilisation du thème CLAIR : {}", e.getMessage());
                savedTheme = "CLAIR";
            }

            ThemeManager.setTheme(savedTheme);

            UIManager.put("Button.arc", 8);
            UIManager.put("Component.arc", 8);
            UIManager.put("TextComponent.arc", 8);
            UIManager.put("ScrollBar.thumbArc", 999);
            UIManager.put("ScrollBar.thumbInsets", new java.awt.Insets(2, 2, 2, 2));
        } catch (Exception e) {
            logger.error("Erreur lors de l'initialisation du thème : {}", e.getMessage(), e);
        }

        SwingUtilities.invokeLater(App::showLogin);
    }

    /**
     * Affiche la fenêtre de connexion.
     */
    private static void showLogin() {
        LoginFrame loginFrame = new LoginFrame();
        loginFrame.setOnLoginSuccess(() -> {
            loginFrame.dispose();
            showMainFrame();
        });
        loginFrame.setVisible(true);
    }

    /**
     * Affiche la fenêtre principale après connexion réussie.
     */
    private static void showMainFrame() {
        MainFrame mainFrame = new MainFrame();
        mainFrame.setOnLogout(() -> {
            mainFrame.dispose();
            showLogin();
        });
        mainFrame.setVisible(true);
    }
}
