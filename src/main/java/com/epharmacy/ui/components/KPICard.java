package com.epharmacy.ui.components;

import com.epharmacy.util.ThemeManager;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Carte KPI (Key Performance Indicator) pour le tableau de bord.
 * <p>
 * Affiche un indicateur clé avec :
 * <ul>
 *   <li>Une icône (emoji/Unicode) à gauche dans un cercle coloré</li>
 *   <li>Un titre descriptif en texte gris petit</li>
 *   <li>Une valeur en texte gras grand</li>
 *   <li>Un sous-titre optionnel</li>
 *   <li>Effet de bordure arrondie avec ombre</li>
 * </ul>
 * </p>
 *
 * @author E-PHARMACIE
 * @version 1.0
 */
public class KPICard extends JPanel {

    /** Label de la valeur principale */
    private final JLabel valueLabel;

    /** Label du titre */
    private final JLabel titleLabel;

    /** Label du sous-titre optionnel */
    private final JLabel subtitleLabel;

    /** Couleur d'accent de la carte */
    private final Color accentColor;

    /** Rayon des coins arrondis */
    private static final int ARC_RADIUS = 16;

    /**
     * Construit une carte KPI.
     *
     * @param icon        l'icône emoji ou Unicode
     * @param title       le titre descriptif
     * @param value       la valeur à afficher
     * @param accentColor la couleur d'accent pour l'icône et la bordure
     */
    public KPICard(String icon, String title, String value, Color accentColor) {
        this.accentColor = accentColor;
        setLayout(new MigLayout("insets 18 20 18 20", "[]15[grow]", "[]4[]4[]"));
        setOpaque(false);
        setMinimumSize(new Dimension(200, 100));
        setPreferredSize(new Dimension(250, 110));

        // Panneau d'icône circulaire
        JPanel iconPanel = createIconPanel(icon);
        add(iconPanel, "spany 3, ay center");

        // Titre
        titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        titleLabel.setForeground(new Color(158, 158, 158));
        add(titleLabel, "wrap");

        // Valeur
        valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        add(valueLabel, "wrap");

        // Sous-titre
        subtitleLabel = new JLabel();
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subtitleLabel.setForeground(new Color(158, 158, 158));
        subtitleLabel.setVisible(false);
        add(subtitleLabel);

        ThemeManager.addThemeChangeListener(this::repaint);
    }

    /**
     * Crée le panneau circulaire contenant l'icône.
     *
     * @param icon l'icône à afficher
     * @return le panneau d'icône
     */
    private JPanel createIconPanel(String icon) {
        JPanel iconPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Fond circulaire avec couleur d'accent transparente
                Color bgColor = new Color(
                        accentColor.getRed(),
                        accentColor.getGreen(),
                        accentColor.getBlue(),
                        40
                );
                g2.setColor(bgColor);
                int size = Math.min(getWidth(), getHeight());
                g2.fillOval(0, 0, size, size);

                g2.dispose();
            }
        };
        iconPanel.setOpaque(false);
        iconPanel.setPreferredSize(new Dimension(50, 50));
        iconPanel.setLayout(new MigLayout("insets 0, fill", "[center]", "[center]"));

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        iconPanel.add(iconLabel);

        return iconPanel;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        // Ombre portée
        g2.setColor(new Color(0, 0, 0, 20));
        g2.fill(new RoundRectangle2D.Float(2, 2, width - 2, height - 2, ARC_RADIUS, ARC_RADIUS));

        // Fond de la carte
        Color bg = getCardBackground();
        g2.setColor(bg);
        g2.fill(new RoundRectangle2D.Float(0, 0, width - 3, height - 3, ARC_RADIUS, ARC_RADIUS));

        // Bordure subtile
        g2.setColor(getBorderColor());
        g2.setStroke(new BasicStroke(1f));
        g2.draw(new RoundRectangle2D.Float(0, 0, width - 3, height - 3, ARC_RADIUS, ARC_RADIUS));

        // Ligne d'accent en haut
        g2.setColor(accentColor);
        g2.setStroke(new BasicStroke(3f));
        g2.drawLine(ARC_RADIUS / 2, 1, width - ARC_RADIUS / 2 - 3, 1);

        g2.dispose();
        super.paintComponent(g);
    }

    /**
     * Met à jour la valeur affichée.
     *
     * @param newValue la nouvelle valeur
     */
    public void updateValue(String newValue) {
        valueLabel.setText(newValue);
    }

    /**
     * Définit un sous-titre sous la valeur.
     *
     * @param subtitle le texte du sous-titre
     */
    public void setSubtitle(String subtitle) {
        if (subtitle != null && !subtitle.isEmpty()) {
            subtitleLabel.setText(subtitle);
            subtitleLabel.setVisible(true);
        } else {
            subtitleLabel.setVisible(false);
        }
    }

    /**
     * Définit une variation en pourcentage (affichée en vert ou rouge).
     *
     * @param percentage la variation (ex: "+5.2%" ou "-3.1%")
     */
    public void setChangePercentage(String percentage) {
        if (percentage != null && !percentage.isEmpty()) {
            boolean positive = percentage.startsWith("+");
            subtitleLabel.setForeground(positive ? new Color(46, 125, 50) : new Color(211, 47, 47));
            subtitleLabel.setText(percentage);
            subtitleLabel.setVisible(true);
        } else {
            subtitleLabel.setVisible(false);
        }
    }

    /**
     * Retourne la couleur de fond de la carte.
     *
     * @return la couleur de fond
     */
    private Color getCardBackground() {
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
