package com.epharmacy.ui.components;

import com.epharmacy.util.ThemeManager;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * Composant de tableau stylisé pour l'affichage des données.
 * <p>
 * Encapsule un {@link JTable} dans un {@link JScrollPane} avec :
 * <ul>
 *   <li>Couleurs de lignes alternées</li>
 *   <li>En-tête de colonnes personnalisé (gras, fond clair)</li>
 *   <li>Pas de lignes de grille, hauteur de ligne 45px</li>
 *   <li>Surlignage de la sélection avec couleur d'accent</li>
 *   <li>Cellules non éditables par défaut</li>
 * </ul>
 * </p>
 *
 * @author E-PHARMACIE
 * @version 1.0
 */
public class StyledTable extends JPanel {

    /** Le tableau interne */
    private final JTable table;

    /** Le modèle de données du tableau */
    private final DefaultTableModel model;

    /** Le panneau de défilement */
    private final JScrollPane scrollPane;

    /** Couleur d'accent pour la sélection */
    private static final Color ACCENT_COLOR = new Color(33, 150, 243);

    /**
     * Construit un tableau stylisé avec les colonnes spécifiées.
     *
     * @param columns les noms des colonnes du tableau
     */
    public StyledTable(String[] columns) {
        setLayout(new MigLayout("insets 0, fill", "[grow]", "[grow]"));
        setOpaque(false);

        // Modèle non éditable
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        configureTable();

        scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(getBorderColor()));
        scrollPane.getViewport().setBackground(getTableBackground());

        add(scrollPane, "grow");

        ThemeManager.addThemeChangeListener(this::onThemeChanged);
    }

    /**
     * Configure l'apparence et le comportement du tableau.
     */
    private void configureTable() {
        table.setRowHeight(45);
        table.setShowGrid(false);
        table.setShowHorizontalLines(false);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setBackground(getTableBackground());
        table.setSelectionBackground(getSelectionColor());
        table.setSelectionForeground(UIManager.getColor("Table.foreground"));

        // Renderer personnalisé pour l'alternance de couleurs
        table.setDefaultRenderer(Object.class, new AlternatingRowRenderer());

        // En-tête personnalisé
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setPreferredSize(new Dimension(0, 42));
        header.setBackground(getHeaderBackground());
        header.setForeground(getHeaderForeground());
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, getBorderColor()));
        header.setReorderingAllowed(false);
        header.setDefaultRenderer(new HeaderRenderer());
    }

    /**
     * Retourne le {@link JTable} interne.
     *
     * @return le JTable
     */
    public JTable getTable() {
        return table;
    }

    /**
     * Retourne le {@link DefaultTableModel} du tableau.
     *
     * @return le modèle de données
     */
    public DefaultTableModel getModel() {
        return model;
    }

    /**
     * Remplace toutes les données du tableau.
     *
     * @param data les nouvelles données (tableau 2D)
     */
    public void setData(Object[][] data) {
        model.setRowCount(0);
        if (data != null) {
            for (Object[] row : data) {
                model.addRow(row);
            }
        }
    }

    /**
     * Ajoute une ligne au tableau.
     *
     * @param row les données de la ligne
     */
    public void addRow(Object[] row) {
        model.addRow(row);
    }

    /**
     * Supprime la ligne sélectionnée du tableau.
     */
    public void removeSelectedRow() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            model.removeRow(selectedRow);
        }
    }

    /**
     * Retourne la valeur de la colonne 0 (ID) de la ligne sélectionnée.
     *
     * @return l'ID de la ligne sélectionnée, ou -1 si aucune sélection
     */
    public int getSelectedId() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            Object value = model.getValueAt(selectedRow, 0);
            if (value instanceof Number) {
                return ((Number) value).intValue();
            }
            try {
                return Integer.parseInt(value.toString());
            } catch (NumberFormatException e) {
                return -1;
            }
        }
        return -1;
    }

    /**
     * Définit les largeurs préférées des colonnes.
     *
     * @param widths les largeurs en pixels pour chaque colonne
     */
    public void setColumnWidths(int... widths) {
        for (int i = 0; i < widths.length && i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
    }

    /**
     * Retourne le JScrollPane contenant le tableau.
     *
     * @return le scroll pane
     */
    public JScrollPane getScrollPane() {
        return scrollPane;
    }

    /**
     * Callback déclenché lors d'un changement de thème.
     */
    private void onThemeChanged() {
        table.setBackground(getTableBackground());
        table.setSelectionBackground(getSelectionColor());
        scrollPane.getViewport().setBackground(getTableBackground());
        scrollPane.setBorder(BorderFactory.createLineBorder(getBorderColor()));

        JTableHeader header = table.getTableHeader();
        header.setBackground(getHeaderBackground());
        header.setForeground(getHeaderForeground());
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, getBorderColor()));

        repaint();
    }

    // --- Renderers personnalisés ---

    /**
     * Renderer pour l'alternance de couleurs de lignes.
     */
    private class AlternatingRowRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if (isSelected) {
                c.setBackground(getSelectionColor());
                c.setForeground(UIManager.getColor("Table.foreground"));
            } else {
                c.setBackground(row % 2 == 0 ? getTableBackground() : getAlternateRowColor());
                c.setForeground(UIManager.getColor("Table.foreground"));
            }

            setHorizontalAlignment(SwingConstants.CENTER);
            setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

            // Support des composants personnalisés (ex: StatusBadge)
            if (value instanceof Component) {
                return (Component) value;
            }

            return c;
        }
    }

    /**
     * Renderer personnalisé pour l'en-tête du tableau.
     */
    private class HeaderRenderer extends DefaultTableCellRenderer {

        public HeaderRenderer() {
            setHorizontalAlignment(SwingConstants.CENTER);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);
            label.setFont(new Font("Segoe UI", Font.BOLD, 13));
            label.setBackground(getHeaderBackground());
            label.setForeground(getHeaderForeground());
            label.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 2, 0, getBorderColor()),
                    BorderFactory.createEmptyBorder(5, 10, 5, 10)
            ));
            label.setOpaque(true);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            return label;
        }
    }

    // --- Couleurs dynamiques ---

    private Color getTableBackground() {
        Color c = UIManager.getColor("Table.background");
        return c != null ? c : Color.WHITE;
    }

    private Color getAlternateRowColor() {
        Color c = UIManager.getColor("Table.alternateRowColor");
        if (c != null) return c;
        return ThemeManager.isDarkMode() ? new Color(45, 45, 48) : new Color(245, 247, 250);
    }

    private Color getSelectionColor() {
        return ThemeManager.isDarkMode() ? new Color(40, 60, 90) : new Color(215, 230, 255);
    }

    private Color getHeaderBackground() {
        Color c = UIManager.getColor("TableHeader.background");
        if (c != null) return c;
        return ThemeManager.isDarkMode() ? new Color(50, 50, 55) : new Color(245, 245, 245);
    }

    private Color getHeaderForeground() {
        Color c = UIManager.getColor("TableHeader.foreground");
        return c != null ? c : (ThemeManager.isDarkMode() ? Color.WHITE : new Color(66, 66, 66));
    }

    private Color getBorderColor() {
        Color c = UIManager.getColor("Component.borderColor");
        return c != null ? c : new Color(224, 224, 224);
    }
}
