package com.epharmacy.ui.components;

import com.epharmacy.util.SessionManager;
import com.epharmacy.util.ThemeManager;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Panneau de navigation latéral (sidebar) pour l'application E-PHARMACIE.
 * <p>
 * Affiche un menu de navigation organisé par sections avec des éléments
 * cliquables, un logo en haut, et un bouton de déconnexion/mode sombre en bas.
 * Les sections d'administration ne sont visibles que pour les utilisateurs
 * ayant le rôle ADMIN.
 * </p>
 *
 * @author E-PHARMACIE
 * @version 1.0
 */
public class Sidebar extends JPanel {

    /** Largeur fixe de la sidebar en pixels */
    private static final int SIDEBAR_WIDTH = 220;

    /** Couleur d'accentuation pour l'élément sélectionné */
    private static final Color ACCENT_COLOR = new Color(33, 150, 243);

    /** Listener de navigation */
    private NavigationListener navigationListener;

    /** Nom de l'élément actuellement sélectionné */
    private String selectedItem = "Tableau de bord";

    /** Liste de tous les boutons de navigation */
    private final List<JPanel> navItems = new ArrayList<>();

    /** Liste des noms des boutons de navigation */
    private final List<String> navItemNames = new ArrayList<>();

    /** Panneau contenant les éléments d'administration */
    private JPanel adminSection;

    /** Bouton bascule pour le mode sombre */
    private JToggleButton darkModeToggle;

    /**
     * Interface de callback pour les événements de navigation.
     */
    public interface NavigationListener {
        /**
         * Appelé lorsqu'un élément de navigation est sélectionné.
         *
         * @param pageName le nom de la page sélectionnée
         */
        void onNavigate(String pageName);

        /**
         * Appelé lorsque l'utilisateur clique sur le bouton de déconnexion.
         */
        void onDeconnexion();
    }

    /**
     * Construit la sidebar avec tous les éléments de navigation.
     */
    public Sidebar() {
        initComponents();
        ThemeManager.addThemeChangeListener(this::onThemeChanged);
    }

    /**
     * Définit le listener de navigation.
     *
     * @param listener le listener à enregistrer
     */
    public void setNavigationListener(NavigationListener listener) {
        this.navigationListener = listener;
    }

    /**
     * Définit l'élément de navigation actuellement sélectionné.
     *
     * @param pageName le nom de la page à sélectionner
     */
    public void setSelectedItem(String pageName) {
        this.selectedItem = pageName;
        updateSelection();
    }

    /**
     * Initialise tous les composants de la sidebar.
     */
    private void initComponents() {
        setLayout(new MigLayout("insets 0, flowy, fill", "[grow]", "[]10[]push[]"));
        setPreferredSize(new Dimension(SIDEBAR_WIDTH, 0));
        setMinimumSize(new Dimension(SIDEBAR_WIDTH, 0));
        setMaximumSize(new Dimension(SIDEBAR_WIDTH, Integer.MAX_VALUE));
        setBackground(getBackgroundColor());
        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, getBorderColor()));

        // --- Logo / Titre ---
        add(createLogoPanel(), "growx, h 70!");

        // --- Navigation scrollable ---
        JPanel navPanel = new JPanel(new MigLayout("insets 5 0 5 0, flowy, gap 0", "[grow]", ""));
        navPanel.setOpaque(false);

        // Section: Analyse
        navPanel.add(createSectionHeader("ANALYSE"), "growx, gaptop 5");
        navPanel.add(createNavItem("Tableau de bord", "📊"), "growx");

        // Section: Catalogue
        navPanel.add(createSectionHeader("CATALOGUE"), "growx, gaptop 12");
        navPanel.add(createNavItem("Médicaments", "💊"), "growx");

        // Section: Gestion des stocks
        navPanel.add(createSectionHeader("GESTION DES STOCKS"), "growx, gaptop 12");
        navPanel.add(createNavItem("Stock & Lots", "📦"), "growx");

        // Section: Gestion des tiers
        navPanel.add(createSectionHeader("GESTION DES TIERS"), "growx, gaptop 12");
        navPanel.add(createNavItem("Clients", "👥"), "growx");
        navPanel.add(createNavItem("Fournisseurs", "🏭"), "growx");

        // Section: Documents commerciaux
        navPanel.add(createSectionHeader("DOCUMENTS COMMERCIAUX"), "growx, gaptop 12");
        navPanel.add(createNavItem("Approvisionnements", "📋"), "growx");
        navPanel.add(createNavItem("Ventes", "🛒"), "growx");

        // Section: Administration (visible uniquement pour les admins)
        adminSection = new JPanel(new MigLayout("insets 0, flowy, gap 0", "[grow]", ""));
        adminSection.setOpaque(false);
        adminSection.add(createSectionHeader("ADMINISTRATION"), "growx, gaptop 12");
        adminSection.add(createNavItem("Utilisateurs", "👤"), "growx");
        adminSection.add(createNavItem("Paramètres", "⚙"), "growx");
        adminSection.setVisible(SessionManager.getInstance().isAdmin());
        navPanel.add(adminSection, "growx");

        JScrollPane scrollPane = new JScrollPane(navPanel);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        add(scrollPane, "grow, push");

        // --- Bottom panel ---
        add(createBottomPanel(), "growx, dock south");

        updateSelection();
    }

    /**
     * Crée le panneau du logo en haut de la sidebar.
     *
     * @return le panneau contenant le logo et le titre
     */
    private JPanel createLogoPanel() {
        JPanel logoPanel = new JPanel(new MigLayout("insets 15 15 10 15", "[]8[]", "[]"));
        logoPanel.setOpaque(false);
        logoPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, getBorderColor()));

        JLabel iconLabel = new JLabel("⚕");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        iconLabel.setForeground(ACCENT_COLOR);

        JLabel titleLabel = new JLabel("E-PHARMACIE");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(ACCENT_COLOR);

        logoPanel.add(iconLabel);
        logoPanel.add(titleLabel);

        return logoPanel;
    }

    /**
     * Crée un en-tête de section de navigation.
     *
     * @param title le titre de la section en majuscules
     * @return le JLabel de l'en-tête
     */
    private JLabel createSectionHeader(String title) {
        JLabel header = new JLabel(title);
        header.setFont(new Font("Segoe UI", Font.BOLD, 10));
        header.setForeground(new Color(158, 158, 158));
        header.setBorder(new EmptyBorder(0, 18, 2, 0));
        return header;
    }

    /**
     * Crée un élément de navigation cliquable avec icône et effet de survol.
     *
     * @param name le nom affiché de l'élément
     * @param icon l'icône emoji de l'élément
     * @return le panneau représentant l'élément de navigation
     */
    private JPanel createNavItem(String name, String icon) {
        JPanel itemPanel = new JPanel(new MigLayout("insets 0, fill", "3[4!]8[]8[]push", "[35!]"));
        itemPanel.setOpaque(true);
        itemPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        itemPanel.setBackground(getBackgroundColor());

        // Barre de sélection à gauche
        JPanel indicator = new JPanel();
        indicator.setPreferredSize(new Dimension(4, 35));
        indicator.setOpaque(true);
        indicator.setBackground(getBackgroundColor());

        // Icône
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 15));

        // Texte
        JLabel textLabel = new JLabel(name);
        textLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        itemPanel.add(indicator, "cell 0 0, growy");
        itemPanel.add(iconLabel, "cell 1 0");
        itemPanel.add(textLabel, "cell 2 0");

        // Stocker les sous-composants pour mise à jour de style
        itemPanel.putClientProperty("indicator", indicator);
        itemPanel.putClientProperty("textLabel", textLabel);
        itemPanel.putClientProperty("navName", name);

        // Effets de survol
        itemPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!name.equals(selectedItem)) {
                    itemPanel.setBackground(getHoverColor());
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!name.equals(selectedItem)) {
                    itemPanel.setBackground(getBackgroundColor());
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                selectedItem = name;
                updateSelection();
                if (navigationListener != null) {
                    navigationListener.onNavigate(name);
                }
            }
        });

        navItems.add(itemPanel);
        navItemNames.add(name);
        return itemPanel;
    }

    /**
     * Met à jour l'apparence de tous les éléments en fonction de la sélection actuelle.
     */
    private void updateSelection() {
        for (JPanel item : navItems) {
            String name = (String) item.getClientProperty("navName");
            JPanel indicator = (JPanel) item.getClientProperty("indicator");
            JLabel textLabel = (JLabel) item.getClientProperty("textLabel");

            boolean isSelected = name.equals(selectedItem);

            if (isSelected) {
                item.setBackground(getSelectedColor());
                indicator.setBackground(ACCENT_COLOR);
                textLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
                textLabel.setForeground(ACCENT_COLOR);
            } else {
                item.setBackground(getBackgroundColor());
                indicator.setBackground(getBackgroundColor());
                textLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                textLabel.setForeground(UIManager.getColor("Label.foreground"));
            }
        }
    }

    /**
     * Crée le panneau inférieur contenant le toggle mode sombre et la déconnexion.
     *
     * @return le panneau du bas
     */
    private JPanel createBottomPanel() {
        JPanel bottomPanel = new JPanel(new MigLayout("insets 10 15 10 15, flowy", "[grow]", "[]8[]"));
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, getBorderColor()));

        // Toggle mode sombre
        darkModeToggle = new JToggleButton(ThemeManager.isDarkMode() ? "🌙 Mode sombre" : "☀ Mode clair");
        darkModeToggle.setSelected(ThemeManager.isDarkMode());
        darkModeToggle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        darkModeToggle.setFocusPainted(false);
        darkModeToggle.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        darkModeToggle.addActionListener(e -> {
            ThemeManager.toggleTheme();
            darkModeToggle.setText(ThemeManager.isDarkMode() ? "🌙 Mode sombre" : "☀ Mode clair");
        });

        // Bouton déconnexion
        JButton logoutButton = new JButton("🚪 Déconnexion");
        logoutButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        logoutButton.setForeground(new Color(211, 47, 47));
        logoutButton.setFocusPainted(false);
        logoutButton.setBorderPainted(false);
        logoutButton.setContentAreaFilled(false);
        logoutButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logoutButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                logoutButton.setForeground(new Color(244, 67, 54));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                logoutButton.setForeground(new Color(211, 47, 47));
            }
        });
        logoutButton.addActionListener(e -> {
            if (navigationListener != null) {
                navigationListener.onDeconnexion();
            }
        });

        bottomPanel.add(darkModeToggle, "growx");
        bottomPanel.add(logoutButton, "growx");

        return bottomPanel;
    }

    /**
     * Rafraîchit la visibilité de la section admin selon le rôle de l'utilisateur.
     */
    public void refreshAdminVisibility() {
        if (adminSection != null) {
            adminSection.setVisible(SessionManager.getInstance().isAdmin());
            revalidate();
            repaint();
        }
    }

    /**
     * Callback déclenché lors d'un changement de thème.
     */
    private void onThemeChanged() {
        setBackground(getBackgroundColor());
        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, getBorderColor()));
        updateSelection();
        if (darkModeToggle != null) {
            darkModeToggle.setText(ThemeManager.isDarkMode() ? "🌙 Mode sombre" : "☀ Mode clair");
            darkModeToggle.setSelected(ThemeManager.isDarkMode());
        }
        repaint();
    }

    // --- Méthodes de couleurs dynamiques selon le thème ---

    /**
     * Retourne la couleur de fond de la sidebar.
     *
     * @return la couleur de fond
     */
    private Color getBackgroundColor() {
        Color c = UIManager.getColor("Panel.background");
        return c != null ? c : (ThemeManager.isDarkMode() ? new Color(30, 30, 30) : Color.WHITE);
    }

    /**
     * Retourne la couleur de survol.
     *
     * @return la couleur de survol
     */
    private Color getHoverColor() {
        return ThemeManager.isDarkMode() ? new Color(50, 50, 55) : new Color(237, 242, 255);
    }

    /**
     * Retourne la couleur de l'élément sélectionné.
     *
     * @return la couleur de sélection
     */
    private Color getSelectedColor() {
        return ThemeManager.isDarkMode() ? new Color(40, 55, 80) : new Color(227, 237, 255);
    }

    /**
     * Retourne la couleur des bordures.
     *
     * @return la couleur de bordure
     */
    private Color getBorderColor() {
        Color c = UIManager.getColor("Component.borderColor");
        return c != null ? c : (ThemeManager.isDarkMode() ? new Color(60, 60, 60) : new Color(224, 224, 224));
    }
}
