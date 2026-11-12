package com.epharmacy.ui.components;

import com.epharmacy.util.ThemeManager;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.function.Consumer;

/**
 * Barre d'en-tête supérieure avec champ de recherche.
 * <p>
 * Fournit un champ de recherche avec texte indicatif (placeholder),
 * un mécanisme de debounce (300ms) pour limiter les appels au listener,
 * et un affichage optionnel de titre.
 * </p>
 *
 * @author E-PHARMACIE
 * @version 1.0
 */
public class HeaderBar extends JPanel {

    /** Champ de recherche principal */
    private final JTextField searchField;

    /** Listener appelé lors d'une recherche */
    private Consumer<String> searchListener;

    /** Timer de debounce pour limiter la fréquence des appels */
    private Timer debounceTimer;

    /** Texte du placeholder */
    private String placeholder = "Rechercher...";

    /** Label de titre optionnel */
    private final JLabel titleLabel;

    /**
     * Construit la barre d'en-tête avec un champ de recherche.
     */
    public HeaderBar() {
        setLayout(new MigLayout("insets 10 15 10 15", "[grow][]", "[]"));
        setBackground(getBarBackground());
        setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, getBorderColor()));

        // Champ de recherche
        searchField = new JTextField();
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.putClientProperty("JTextField.placeholderText", placeholder);
        searchField.putClientProperty("JTextField.leadingIcon",
                UIManager.getIcon("Tree.openIcon")); // FlatLaf icon
        searchField.setPreferredSize(new Dimension(0, 36));

        // Debounce listener sur le document
        debounceTimer = new Timer(300, e -> {
            if (searchListener != null) {
                searchListener.accept(searchField.getText().trim());
            }
        });
        debounceTimer.setRepeats(false);

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                restartDebounce();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                restartDebounce();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                restartDebounce();
            }
        });

        // Label de titre
        titleLabel = new JLabel();
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setVisible(false);

        add(searchField, "growx, h 36!");
        add(titleLabel, "gapleft 15");

        ThemeManager.addThemeChangeListener(this::onThemeChanged);
    }

    /**
     * Redémarre le timer de debounce.
     */
    private void restartDebounce() {
        if (debounceTimer.isRunning()) {
            debounceTimer.restart();
        } else {
            debounceTimer.start();
        }
    }

    /**
     * Définit le listener appelé lors de la saisie dans le champ de recherche.
     * Le listener est appelé avec un debounce de 300ms.
     *
     * @param listener le consumer recevant le texte de recherche
     */
    public void setSearchListener(Consumer<String> listener) {
        this.searchListener = listener;
    }

    /**
     * Définit le texte du placeholder du champ de recherche.
     *
     * @param text le texte indicatif à afficher
     */
    public void setPlaceholder(String text) {
        this.placeholder = text;
        searchField.putClientProperty("JTextField.placeholderText", text);
    }

    /**
     * Efface le contenu du champ de recherche.
     */
    public void clearSearch() {
        searchField.setText("");
    }

    /**
     * Retourne le texte actuellement saisi dans le champ de recherche.
     *
     * @return le texte de recherche (trimé)
     */
    public String getSearchText() {
        return searchField.getText().trim();
    }

    /**
     * Définit le titre affiché à droite de la barre.
     *
     * @param title le titre à afficher, ou {@code null} pour masquer
     */
    public void setTitle(String title) {
        if (title != null && !title.isEmpty()) {
            titleLabel.setText(title);
            titleLabel.setVisible(true);
        } else {
            titleLabel.setVisible(false);
        }
    }

    /**
     * Retourne le champ de recherche pour personnalisation avancée.
     *
     * @return le JTextField de recherche
     */
    public JTextField getSearchField() {
        return searchField;
    }

    /**
     * Callback déclenché lors d'un changement de thème.
     */
    private void onThemeChanged() {
        setBackground(getBarBackground());
        setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, getBorderColor()));
        repaint();
    }

    /**
     * Retourne la couleur de fond de la barre.
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
