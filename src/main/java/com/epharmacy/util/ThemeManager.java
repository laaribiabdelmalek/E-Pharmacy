package com.epharmacy.util;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Gère le basculement entre Mode Sombre et Mode Clair via FlatLaf.
 * <p>
 * Permet de changer dynamiquement le thème de l'application
 * et de notifier les composants enregistrés du changement
 * via un système de listeners.
 * </p>
 *
 * @author E-PHARMACIE
 * @version 1.0
 */
public class ThemeManager {

    /** État actuel du mode sombre */
    private static boolean darkMode = false;

    /** Liste des listeners notifiés lors d'un changement de thème */
    private static final List<Runnable> listeners = new ArrayList<>();

    /**
     * Constructeur privé pour empêcher l'instanciation.
     */
    private ThemeManager() {}

    /**
     * Indique si le mode sombre est actuellement activé.
     *
     * @return {@code true} si le mode sombre est actif, {@code false} sinon
     */
    public static boolean isDarkMode() {
        return darkMode;
    }

    /**
     * Bascule entre le mode sombre et le mode clair.
     */
    public static void toggleTheme() {
        darkMode = !darkMode;
        applyTheme();
    }

    /**
     * Définit explicitement le mode sombre ou clair.
     *
     * @param dark {@code true} pour activer le mode sombre,
     *             {@code false} pour le mode clair
     */
    public static void setDarkMode(boolean dark) {
        darkMode = dark;
        applyTheme();
    }

    /**
     * Applique le thème actuel à toutes les fenêtres ouvertes
     * et notifie les listeners enregistrés.
     */
    public static void applyTheme() {
        try {
            if (darkMode) {
                UIManager.setLookAndFeel(new FlatDarkLaf());
            } else {
                UIManager.setLookAndFeel(new FlatLightLaf());
            }
            // Mettre à jour toutes les fenêtres ouvertes
            for (java.awt.Window window : java.awt.Window.getWindows()) {
                SwingUtilities.updateComponentTreeUI(window);
            }
            // Notifier les listeners
            for (Runnable listener : listeners) {
                listener.run();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Enregistre un listener qui sera notifié à chaque changement de thème.
     *
     * @param listener le callback à exécuter lors d'un changement de thème
     */
    public static void addThemeChangeListener(Runnable listener) {
        listeners.add(listener);
    }

    /**
     * Supprime un listener précédemment enregistré.
     *
     * @param listener le callback à supprimer
     */
    public static void removeThemeChangeListener(Runnable listener) {
        listeners.remove(listener);
    }
}
