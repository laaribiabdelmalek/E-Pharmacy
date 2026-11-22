package com.epharmacy.util;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Gère le basculement entre Mode Sombre, Mode Clair et Thème Système via FlatLaf.
 * Détecte les paramètres système sous Windows via le registre.
 *
 * @author E-PHARMACIE
 * @version 2.0
 */
public class ThemeManager {

    /** État actuel du mode sombre effectif */
    private static boolean darkMode = false;

    /** Mode sélectionné : "CLAIR", "SOMBRE", ou "SYSTEME" */
    private static String currentThemeMode = "CLAIR";

    /** Liste des listeners notifiés lors d'un changement de thème */
    private static final List<Runnable> listeners = new ArrayList<>();

    private ThemeManager() {}

    /**
     * Indique si le mode sombre est actuellement actif à l'écran.
     */
    public static boolean isDarkMode() {
        return darkMode;
    }

    /**
     * Retourne le mode de thème configuré ("CLAIR", "SOMBRE", "SYSTEME").
     */
    public static String getCurrentThemeMode() {
        return currentThemeMode;
    }

    /**
     * Bascule entre le mode sombre et le mode clair.
     */
    public static void toggleTheme() {
        darkMode = !darkMode;
        currentThemeMode = darkMode ? "SOMBRE" : "CLAIR";
        applyTheme();
    }

    /**
     * Définit explicitement le mode sombre ou clair.
     */
    public static void setDarkMode(boolean dark) {
        darkMode = dark;
        currentThemeMode = dark ? "SOMBRE" : "CLAIR";
        applyTheme();
    }

    /**
     * Définit le mode de thème parmi "CLAIR", "SOMBRE", "SYSTEME".
     *
     * @param theme nom du thème
     */
    public static void setTheme(String theme) {
        if (theme == null || theme.isBlank()) {
            theme = "CLAIR";
        }
        currentThemeMode = theme.toUpperCase();

        switch (currentThemeMode) {
            case "SOMBRE" -> darkMode = true;
            case "SYSTEME" -> darkMode = isSystemDarkMode();
            default -> {
                currentThemeMode = "CLAIR";
                darkMode = false;
            }
        }
        applyTheme();
    }

    /**
     * Détecte si le mode sombre système de Windows est activé en lisant le registre :
     * HKCU\Software\Microsoft\Windows\CurrentVersion\Themes\Personalize /v AppsUseLightTheme
     * Valeur 0x0 => Dark mode
     * Valeur 0x1 => Light mode
     *
     * @return true si le système Windows est en mode sombre, false par défaut.
     */
    public static boolean isSystemDarkMode() {
        String os = System.getProperty("os.name", "").toLowerCase();
        if (!os.contains("win")) {
            return false;
        }

        try {
            Process process = new ProcessBuilder("reg", "query",
                    "HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize",
                    "/v", "AppsUseLightTheme").start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains("AppsUseLightTheme")) {
                        if (line.contains("0x0")) {
                            return true;
                        } else if (line.contains("0x1")) {
                            return false;
                        }
                    }
                }
            }
            process.waitFor();
        } catch (Exception e) {
            // Repli gracieux en mode clair
        }
        return false;
    }

    /**
     * Applique le thème actuel à toutes les fenêtres ouvertes
     * et notifie les listeners enregistrés.
     */
    public static void applyTheme() {
        try {
            if (darkMode) {
                FlatDarkLaf.setup();
            } else {
                FlatLightLaf.setup();
            }

            // Mettre à jour toutes les fenêtres ouvertes
            for (java.awt.Window window : java.awt.Window.getWindows()) {
                SwingUtilities.updateComponentTreeUI(window);
            }

            // Notifier les listeners
            for (Runnable listener : new ArrayList<>(listeners)) {
                listener.run();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Enregistre un listener qui sera notifié à chaque changement de thème.
     */
    public static void addThemeChangeListener(Runnable listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * Supprime un listener précédemment enregistré.
     */
    public static void removeThemeChangeListener(Runnable listener) {
        listeners.remove(listener);
    }
}
