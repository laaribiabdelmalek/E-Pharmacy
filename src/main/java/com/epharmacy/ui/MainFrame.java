package com.epharmacy.ui;

import com.epharmacy.security.AccessControl;
import com.epharmacy.service.AuthService;
import com.epharmacy.ui.components.Sidebar;
import com.epharmacy.ui.panels.*;
import com.epharmacy.util.SessionManager;
import net.miginfocom.swing.MigLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Fenêtre principale de l'application E-PHARMACIE.
 * Implémente le chargement paresseux (lazy loading) des panneaux,
 * le contrôle d'accès par rôle (RBAC) et le rafraîchissement automatique des données.
 *
 * @author E-PHARMACIE
 * @version 3.0
 */
public class MainFrame extends JFrame {

    private static final Logger logger = LoggerFactory.getLogger(MainFrame.class);

    private Sidebar sidebar;
    private JPanel contentPanel;
    private CardLayout cardLayout;
    private final AuthService authService = new AuthService();

    // Map des panneaux instanciés à la demande (Lazy Loading)
    private final Map<String, Component> panelsMap = new HashMap<>();

    private Runnable onLogout;

    public MainFrame() {
        initUI();
    }

    public void setOnLogout(Runnable callback) {
        this.onLogout = callback;
    }

    private void initUI() {
        setTitle("E-PHARMACIE — Gestion de Pharmacie");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1280, 800);
        setMinimumSize(new Dimension(1024, 600));
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new MigLayout("insets 0, fill, gap 0", "[][grow]", "[grow]"));

        // Sidebar de navigation
        sidebar = new Sidebar();
        sidebar.refreshAdminVisibility();
        sidebar.setNavigationListener(new Sidebar.NavigationListener() {
            @Override
            public void onNavigate(String pageName) {
                navigateTo(pageName);
            }

            @Override
            public void onDeconnexion() {
                int choice = JOptionPane.showConfirmDialog(
                    MainFrame.this,
                    "Voulez-vous vraiment vous déconnecter ?",
                    "Déconnexion",
                    JOptionPane.YES_NO_OPTION
                );
                if (choice == JOptionPane.YES_OPTION) {
                    authService.deconnecter();
                    if (onLogout != null) {
                        onLogout.run();
                    }
                }
            }
        });
        mainPanel.add(sidebar, "grow, w 220!");

        // Zone de contenu avec CardLayout
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        mainPanel.add(contentPanel, "grow");

        setContentPane(mainPanel);

        // Chargement du tableau de bord par défaut
        navigateTo("Tableau de bord");
    }

    /**
     * Navigue vers la page demandée avec contrôle d'accès RBAC et instanciation paresseuse.
     */
    public void navigateTo(String pageName) {
        // Contrôle d'accès RBAC
        if ("Utilisateurs".equals(pageName) || "Paramètres".equals(pageName)) {
            if (!AccessControl.hasRole("ADMIN")) {
                JOptionPane.showMessageDialog(
                        this,
                        "Accès refusé : la section '" + pageName + "' est réservée aux administrateurs.",
                        "Contrôle d'accès",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }
        }

        // Instanciation paresseuse (Lazy loading)
        Component panel = panelsMap.get(pageName);
        if (panel == null) {
            panel = createPanel(pageName);
            if (panel != null) {
                panelsMap.put(pageName, panel);
                contentPanel.add(panel, pageName);
            }
        }

        if (panel != null) {
            cardLayout.show(contentPanel, pageName);
            sidebar.setSelectedItem(pageName);
            rafraichirDonneesPanel(pageName, panel);
        }
    }

    /**
     * Fabrique le composant du panneau selon le nom demandé.
     */
    private Component createPanel(String pageName) {
        logger.debug("Instanciation paresseuse du panneau : {}", pageName);
        return switch (pageName) {
            case "Tableau de bord" -> new DashboardPanel();
            case "Médicaments" -> new MedicamentPanel();
            case "Stock & Lots" -> new StockLotPanel();
            case "Clients" -> new ClientPanel();
            case "Fournisseurs" -> new FournisseurPanel();
            case "Ventes" -> new VentePanel();
            case "Approvisionnements" -> new ApprovisionnementPanel();
            case "Utilisateurs" -> new UtilisateurPanel();
            case "Paramètres" -> new ParametresPanel();
            default -> null;
        };
    }

    /**
     * Déclenche le rechargement asynchrone / automatique des données du panneau actif.
     */
    private void rafraichirDonneesPanel(String pageName, Component panel) {
        try {
            switch (pageName) {
                case "Tableau de bord" -> ((DashboardPanel) panel).chargerDonnees();
                case "Médicaments" -> ((MedicamentPanel) panel).chargerDonnees();
                case "Stock & Lots" -> ((StockLotPanel) panel).chargerDonnees();
                case "Clients" -> ((ClientPanel) panel).chargerDonnees();
                case "Fournisseurs" -> ((FournisseurPanel) panel).chargerDonnees();
                case "Ventes" -> ((VentePanel) panel).chargerDonnees();
                case "Approvisionnements" -> ((ApprovisionnementPanel) panel).chargerDonnees();
                case "Utilisateurs" -> ((UtilisateurPanel) panel).chargerDonnees();
                case "Paramètres" -> ((ParametresPanel) panel).chargerDonnees();
            }
        } catch (Exception e) {
            logger.error("Erreur lors du rafraîchissement du panneau '{}' : {}", pageName, e.getMessage(), e);
        }
    }
}
