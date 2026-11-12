package com.epharmacy.ui.components;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Badge de statut coloré sous forme de pilule arrondie.
 * <p>
 * Affiche un texte dans un rectangle arrondi coloré,
 * utilisé pour indiquer visuellement un statut (actif, inactif,
 * commercialisé, en rupture, etc.).
 * </p>
 * <p>
 * Des méthodes de fabrique statiques sont fournies pour les
 * statuts les plus courants.
 * </p>
 *
 * @author E-PHARMACIE
 * @version 1.0
 */
public class StatusBadge extends JLabel {

    /** Couleur de fond du badge */
    private final Color bgColor;

    /** Couleur du texte du badge */
    private final Color textColor;

    /** Rayon des coins arrondis */
    private static final int ARC_RADIUS = 12;

    /** Padding horizontal */
    private static final int PAD_X = 12;

    /** Padding vertical */
    private static final int PAD_Y = 4;

    /**
     * Construit un badge de statut avec les couleurs spécifiées.
     *
     * @param text      le texte du statut
     * @param bgColor   la couleur de fond
     * @param textColor la couleur du texte
     */
    public StatusBadge(String text, Color bgColor, Color textColor) {
        super(text);
        this.bgColor = bgColor;
        this.textColor = textColor;

        setForeground(textColor);
        setFont(new Font("Segoe UI", Font.BOLD, 11));
        setHorizontalAlignment(SwingConstants.CENTER);
        setOpaque(false);
    }

    @Override
    public Dimension getPreferredSize() {
        FontMetrics fm = getFontMetrics(getFont());
        int width = fm.stringWidth(getText()) + PAD_X * 2;
        int height = fm.getHeight() + PAD_Y * 2;
        return new Dimension(width, height);
    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        // Dessiner le fond arrondi
        RoundRectangle2D roundRect = new RoundRectangle2D.Float(
                0, 0, width - 1, height - 1, ARC_RADIUS, ARC_RADIUS);
        g2.setColor(bgColor);
        g2.fill(roundRect);

        // Dessiner le texte centré
        g2.setColor(textColor);
        g2.setFont(getFont());
        FontMetrics fm = g2.getFontMetrics();
        int textX = (width - fm.stringWidth(getText())) / 2;
        int textY = (height - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(getText(), textX, textY);

        g2.dispose();
    }

    // --- Méthodes de fabrique statiques ---

    /**
     * Crée un badge "Commercialisé" vert.
     *
     * @return le badge de statut
     */
    public static StatusBadge commercialise() {
        return new StatusBadge("Commercialisé", new Color(200, 245, 200), new Color(27, 94, 32));
    }

    /**
     * Crée un badge "Non Commercialisé" gris.
     *
     * @return le badge de statut
     */
    public static StatusBadge nonCommercialise() {
        return new StatusBadge("Non Commercialisé", new Color(224, 224, 224), new Color(97, 97, 97));
    }

    /**
     * Crée un badge "En Rupture" rouge.
     *
     * @return le badge de statut
     */
    public static StatusBadge enRupture() {
        return new StatusBadge("En Rupture", new Color(255, 205, 210), new Color(183, 28, 28));
    }

    /**
     * Crée un badge "Actif" vert.
     *
     * @return le badge de statut
     */
    public static StatusBadge actif() {
        return new StatusBadge("Actif", new Color(200, 245, 200), new Color(27, 94, 32));
    }

    /**
     * Crée un badge "Inactif" rouge.
     *
     * @return le badge de statut
     */
    public static StatusBadge inactif() {
        return new StatusBadge("Inactif", new Color(255, 205, 210), new Color(183, 28, 28));
    }

    /**
     * Crée un badge "Validée" vert.
     *
     * @return le badge de statut
     */
    public static StatusBadge validee() {
        return new StatusBadge("Validée", new Color(200, 245, 200), new Color(27, 94, 32));
    }

    /**
     * Crée un badge "En Cours" orange.
     *
     * @return le badge de statut
     */
    public static StatusBadge enCours() {
        return new StatusBadge("En Cours", new Color(255, 243, 224), new Color(230, 126, 34));
    }

    /**
     * Crée un badge "Annulée" rouge.
     *
     * @return le badge de statut
     */
    public static StatusBadge annulee() {
        return new StatusBadge("Annulée", new Color(255, 205, 210), new Color(183, 28, 28));
    }

    /**
     * Crée un badge automatiquement en fonction de la chaîne de statut fournie.
     * <p>
     * Reconnaît les statuts suivants (insensible à la casse) :
     * Commercialisé, Non Commercialisé, En Rupture, Actif, Inactif,
     * Validée, En Cours, Annulée.
     * </p>
     *
     * @param statut le texte du statut
     * @return le badge correspondant, ou un badge gris par défaut
     */
    public static StatusBadge forStatut(String statut) {
        if (statut == null) {
            return new StatusBadge("Inconnu", new Color(224, 224, 224), new Color(97, 97, 97));
        }

        String normalized = statut.trim().toLowerCase();

        if (normalized.contains("commercialis") && !normalized.contains("non")) {
            return commercialise();
        } else if (normalized.contains("non commercialis") || normalized.contains("non_commercialis")) {
            return nonCommercialise();
        } else if (normalized.contains("rupture")) {
            return enRupture();
        } else if (normalized.equals("actif") || normalized.equals("active")) {
            return actif();
        } else if (normalized.equals("inactif") || normalized.equals("inactive")) {
            return inactif();
        } else if (normalized.contains("valid")) {
            return validee();
        } else if (normalized.contains("en cours") || normalized.contains("en_cours")) {
            return enCours();
        } else if (normalized.contains("annul")) {
            return annulee();
        }

        // Badge par défaut gris
        return new StatusBadge(statut, new Color(224, 224, 224), new Color(97, 97, 97));
    }
}
