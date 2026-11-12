package com.epharmacy.ui.components;

import javax.swing.*;
import java.awt.*;

/**
 * Menu contextuel stylisé pour les actions sur les lignes d'un tableau.
 * <p>
 * Fournit des éléments de menu pré-configurés pour les opérations
 * courantes : voir, modifier, supprimer. L'élément "Supprimer" est
 * affiché en rouge pour attirer l'attention.
 * </p>
 *
 * @author E-PHARMACIE
 * @version 1.0
 */
public class ActionPopupMenu extends JPopupMenu {

    /**
     * Constructeur privé. Utilisez les méthodes de fabrique statiques.
     */
    private ActionPopupMenu() {
        super();
        setFont(new Font("Segoe UI", Font.PLAIN, 13));
    }

    /**
     * Crée un menu contextuel avec les actions Modifier et Supprimer.
     *
     * @param onEdit   l'action exécutée lors du clic sur "Modifier"
     * @param onDelete l'action exécutée lors du clic sur "Supprimer"
     * @return le menu contextuel configuré
     */
    public static ActionPopupMenu create(Runnable onEdit, Runnable onDelete) {
        ActionPopupMenu menu = new ActionPopupMenu();

        // Modifier
        JMenuItem editItem = createMenuItem("✏ Modifier", null);
        editItem.addActionListener(e -> {
            if (onEdit != null) onEdit.run();
        });
        menu.add(editItem);

        menu.addSeparator();

        // Supprimer
        JMenuItem deleteItem = createMenuItem("🗑 Supprimer", new Color(211, 47, 47));
        deleteItem.addActionListener(e -> {
            if (onDelete != null) onDelete.run();
        });
        menu.add(deleteItem);

        return menu;
    }

    /**
     * Crée un menu contextuel complet avec les actions Voir détails, Modifier et Supprimer.
     *
     * @param onView   l'action exécutée lors du clic sur "Voir détails"
     * @param onEdit   l'action exécutée lors du clic sur "Modifier"
     * @param onDelete l'action exécutée lors du clic sur "Supprimer"
     * @return le menu contextuel configuré
     */
    public static ActionPopupMenu createFull(Runnable onView, Runnable onEdit, Runnable onDelete) {
        ActionPopupMenu menu = new ActionPopupMenu();

        // Voir détails
        JMenuItem viewItem = createMenuItem("👁 Voir détails", null);
        viewItem.addActionListener(e -> {
            if (onView != null) onView.run();
        });
        menu.add(viewItem);

        menu.addSeparator();

        // Modifier
        JMenuItem editItem = createMenuItem("✏ Modifier", null);
        editItem.addActionListener(e -> {
            if (onEdit != null) onEdit.run();
        });
        menu.add(editItem);

        menu.addSeparator();

        // Supprimer
        JMenuItem deleteItem = createMenuItem("🗑 Supprimer", new Color(211, 47, 47));
        deleteItem.addActionListener(e -> {
            if (onDelete != null) onDelete.run();
        });
        menu.add(deleteItem);

        return menu;
    }

    /**
     * Crée un élément de menu stylisé.
     *
     * @param text      le texte de l'élément
     * @param textColor la couleur du texte, ou {@code null} pour la couleur par défaut
     * @return l'élément de menu configuré
     */
    private static JMenuItem createMenuItem(String text, Color textColor) {
        JMenuItem item = new JMenuItem(text);
        item.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        item.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        if (textColor != null) {
            item.setForeground(textColor);
        }

        return item;
    }
}
