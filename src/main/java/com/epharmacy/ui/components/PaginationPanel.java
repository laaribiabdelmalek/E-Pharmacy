package com.epharmacy.ui.components;

import com.epharmacy.util.ThemeManager;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

/**
 * Panneau de pagination avec contrôles de navigation.
 * <p>
 * Affiche des boutons de navigation (premier, précédent, suivant, dernier),
 * un indicateur de page courante, et un résumé "Affichage X - Y sur Z".
 * </p>
 *
 * @author E-PHARMACIE
 * @version 1.0
 */
public class PaginationPanel extends JPanel {

    /** Page courante (1-indexée) */
    private int currentPage = 1;

    /** Nombre total d'éléments */
    private int totalItems = 0;

    /** Nombre d'éléments par page */
    private int pageSize = 20;

    /** Nombre total de pages */
    private int totalPages = 1;

    /** Listener appelé lors d'un changement de page */
    private Consumer<Integer> pageChangeListener;

    /** Label d'information "Affichage X - Y sur Z" */
    private final JLabel infoLabel;

    /** Label du numéro de page */
    private final JLabel pageLabel;

    /** Bouton première page */
    private final JButton firstButton;

    /** Bouton page précédente */
    private final JButton prevButton;

    /** Bouton page suivante */
    private final JButton nextButton;

    /** Bouton dernière page */
    private final JButton lastButton;

    /**
     * Construit un panneau de pagination.
     */
    public PaginationPanel() {
        setLayout(new MigLayout("insets 8 15 8 15", "[]push[]5[]10[]10[]5[]", "[]"));
        setOpaque(true);
        setBackground(getBarBackground());
        setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, getBorderColor()));

        // Label d'information
        infoLabel = new JLabel("Affichage 0 - 0 sur 0");
        infoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        infoLabel.setForeground(new Color(117, 117, 117));

        // Boutons de navigation
        firstButton = createNavButton("«", "Première page");
        prevButton = createNavButton("‹", "Page précédente");
        nextButton = createNavButton("›", "Page suivante");
        lastButton = createNavButton("»", "Dernière page");

        // Label de page
        pageLabel = new JLabel("1 de 1");
        pageLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        pageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        pageLabel.setPreferredSize(new Dimension(80, 30));

        // Actions
        firstButton.addActionListener(e -> goToPage(1));
        prevButton.addActionListener(e -> goToPage(currentPage - 1));
        nextButton.addActionListener(e -> goToPage(currentPage + 1));
        lastButton.addActionListener(e -> goToPage(totalPages));

        // Ajout des composants
        add(infoLabel);
        add(firstButton);
        add(prevButton);
        add(pageLabel);
        add(nextButton);
        add(lastButton);

        updateDisplay();
        ThemeManager.addThemeChangeListener(this::onThemeChanged);
    }

    /**
     * Crée un bouton de navigation stylisé.
     *
     * @param text    le texte du bouton
     * @param tooltip l'info-bulle
     * @return le bouton créé
     */
    private JButton createNavButton(String text, String tooltip) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 16));
        button.setPreferredSize(new Dimension(36, 30));
        button.setFocusPainted(false);
        button.setToolTipText(tooltip);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    /**
     * Configure la pagination avec le nombre total d'éléments et la taille de page.
     *
     * @param totalItems le nombre total d'éléments
     * @param pageSize   le nombre d'éléments par page
     */
    public void setup(int totalItems, int pageSize) {
        this.totalItems = Math.max(0, totalItems);
        this.pageSize = Math.max(1, pageSize);
        this.totalPages = Math.max(1, (int) Math.ceil((double) this.totalItems / this.pageSize));
        this.currentPage = Math.min(this.currentPage, this.totalPages);
        if (this.currentPage < 1) this.currentPage = 1;
        updateDisplay();
    }

    /**
     * Retourne la page courante (1-indexée).
     *
     * @return le numéro de la page courante
     */
    public int getCurrentPage() {
        return currentPage;
    }

    /**
     * Définit le listener appelé lors d'un changement de page.
     *
     * @param listener le consumer recevant le numéro de la nouvelle page
     */
    public void setPageChangeListener(Consumer<Integer> listener) {
        this.pageChangeListener = listener;
    }

    /**
     * Retourne l'offset SQL correspondant à la page courante.
     *
     * @return (currentPage - 1) * pageSize
     */
    public int getOffset() {
        return (currentPage - 1) * pageSize;
    }

    /**
     * Navigue vers la page spécifiée.
     *
     * @param page le numéro de la page cible
     */
    private void goToPage(int page) {
        if (page < 1 || page > totalPages || page == currentPage) {
            return;
        }
        currentPage = page;
        updateDisplay();
        if (pageChangeListener != null) {
            pageChangeListener.accept(currentPage);
        }
    }

    /**
     * Met à jour l'affichage du panneau (labels et état des boutons).
     */
    private void updateDisplay() {
        // Calcul des indices affichés
        int start = totalItems == 0 ? 0 : getOffset() + 1;
        int end = Math.min(getOffset() + pageSize, totalItems);

        infoLabel.setText(String.format("Affichage %d - %d sur %d", start, end, totalItems));
        pageLabel.setText(String.format("%d de %d", currentPage, totalPages));

        // État des boutons
        firstButton.setEnabled(currentPage > 1);
        prevButton.setEnabled(currentPage > 1);
        nextButton.setEnabled(currentPage < totalPages);
        lastButton.setEnabled(currentPage < totalPages);
    }

    /**
     * Réinitialise la pagination à la première page.
     */
    public void reset() {
        currentPage = 1;
        updateDisplay();
    }

    /**
     * Callback déclenché lors d'un changement de thème.
     */
    private void onThemeChanged() {
        setBackground(getBarBackground());
        setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, getBorderColor()));
        repaint();
    }

    /**
     * Retourne la couleur de fond.
     *
     * @return la couleur de fond
     */
    private Color getBarBackground() {
        Color c = UIManager.getColor("Panel.background");
        return c != null ? c : Color.WHITE;
    }

    /**
     * Retourne la couleur de bordure.
     *
     * @return la couleur de bordure
     */
    private Color getBorderColor() {
        Color c = UIManager.getColor("Component.borderColor");
        return c != null ? c : new Color(224, 224, 224);
    }
}
