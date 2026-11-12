package com.epharmacy;

import com.epharmacy.ui.LoginFrame;
import com.epharmacy.ui.MainFrame;
import com.epharmacy.util.ThemeManager;
import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;

/**
 * Point d'entrée principal de l'application E-PHARMACIE.
 * <p>
 * Configure le Look and Feel FlatLaf, initialise la fenêtre de connexion,
 * puis bascule vers la fenêtre principale après authentification réussie.
 * </p>
 *
 * @author E-PHARMACIE
 * @version 1.0
 */
public class App {

    /**
     * Méthode principale de lancement de l'application.
     *
     * @param args arguments de la ligne de commande (non utilisés)
     */
    public static void main(String[] args) {
        // Configurer FlatLaf
        try {
            FlatLightLaf.setup();
            UIManager.put("Button.arc", 8);
            UIManager.put("Component.arc", 8);
            UIManager.put("TextComponent.arc", 8);
            UIManager.put("ScrollBar.thumbArc", 999);
            UIManager.put("ScrollBar.thumbInsets", new java.awt.Insets(2, 2, 2, 2));
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            showLogin();
        });
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
