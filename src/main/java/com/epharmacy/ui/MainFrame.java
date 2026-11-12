package com.epharmacy.ui;

import com.epharmacy.service.AuthService;
import com.epharmacy.ui.components.Sidebar;
import com.epharmacy.ui.panels.*;
import com.epharmacy.util.SessionManager;
import net.miginfocom.swing.MigLayout;
import javax.swing.*;
import java.awt.*;

/**
 * Fenêtre principale de l'application E-PHARMACIE.
 * Contient la Sidebar de navigation et le CardLayout pour basculer entre les panneaux.
 * @author E-PHARMACIE
 */
public class MainFrame extends JFrame {
    private Sidebar sidebar;
    private JPanel contentPanel;
    private CardLayout cardLayout;
    private final AuthService authService = new AuthService();

    // Panels
    private DashboardPanel dashboardPanel;
    private MedicamentPanel medicamentPanel;
    private StockLotPanel stockLotPanel;
    private ClientPanel clientPanel;
    private FournisseurPanel fournisseurPanel;
    private VentePanel ventePanel;
    private ApprovisionnementPanel approvisionnementPanel;
    private UtilisateurPanel utilisateurPanel;

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

        // Sidebar
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

        // Content area with CardLayout
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);

        dashboardPanel = new DashboardPanel();
        medicamentPanel = new MedicamentPanel();
        stockLotPanel = new StockLotPanel();
        clientPanel = new ClientPanel();
        fournisseurPanel = new FournisseurPanel();
        ventePanel = new VentePanel();
        approvisionnementPanel = new ApprovisionnementPanel();
        utilisateurPanel = new UtilisateurPanel();

        contentPanel.add(dashboardPanel, "Tableau de bord");
        contentPanel.add(medicamentPanel, "Médicaments");
        contentPanel.add(stockLotPanel, "Stock & Lots");
        contentPanel.add(clientPanel, "Clients");
        contentPanel.add(fournisseurPanel, "Fournisseurs");
        contentPanel.add(ventePanel, "Ventes");
        contentPanel.add(approvisionnementPanel, "Approvisionnements");
        contentPanel.add(utilisateurPanel, "Utilisateurs");

        // Paramètres placeholder
        JPanel paramPanel = new JPanel(new MigLayout("fill, insets 30", "[center]", "[center]"));
        JLabel paramLabel = new JLabel("Paramètres — En cours de développement");
        paramLabel.setFont(paramLabel.getFont().deriveFont(Font.BOLD, 18f));
        paramPanel.add(paramLabel);
        contentPanel.add(paramPanel, "Paramètres");

        mainPanel.add(contentPanel, "grow");

        setContentPane(mainPanel);

        // Show dashboard by default
        cardLayout.show(contentPanel, "Tableau de bord");
    }

    private void navigateTo(String pageName) {
        cardLayout.show(contentPanel, pageName);
        sidebar.setSelectedItem(pageName);

        // Refresh data on navigation
        switch (pageName) {
            case "Tableau de bord" -> dashboardPanel.chargerDonnees();
            case "Stock & Lots" -> stockLotPanel.chargerDonnees();
        }
    }
}
