package com.epharmacy.ui.panels;

import com.epharmacy.dao.ClientDAO;
import com.epharmacy.model.Medicament;
import com.epharmacy.service.*;
import com.epharmacy.ui.components.KPICard;
import com.epharmacy.ui.components.StyledTable;
import com.epharmacy.model.Lot;
import com.epharmacy.util.ValidationUtils;
import net.miginfocom.swing.MigLayout;
import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Panneau du Tableau de Bord (Dashboard) avec KPIs et alertes.
 * @author E-PHARMACIE
 */
public class DashboardPanel extends JPanel {
    private KPICard cardMedicaments, cardClients, cardVentes, cardCA;
    private JPanel alertesPanel;
    private StyledTable expirationTable;
    private final MedicamentService medicamentService = new MedicamentService();
    private final VenteService venteService = new VenteService();
    private final ClientDAO clientDAO = new ClientDAO();
    private final StockService stockService = new StockService();

    public DashboardPanel() {
        initUI();
        chargerDonnees();
    }

    private void initUI() {
        setLayout(new MigLayout("fill, insets 20, hidemode 3", "[grow, fill, 0::]", "[]15[]15[]10[]10[]10[grow, fill]"));

        JLabel title = new JLabel("Tableau de Bord");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 24f));
        add(title, "wrap");

        // KPI Cards
        JPanel cardsPanel = new JPanel(new MigLayout("insets 0, gap 15", "[25%,grow][25%,grow][25%,grow][25%,grow]", "[]"));
        cardsPanel.setOpaque(false);
        cardMedicaments = new KPICard("\uD83D\uDC8A", "Total Médicaments", "0", new Color(59, 130, 246));
        cardClients = new KPICard("\uD83D\uDC65", "Total Clients", "0", new Color(16, 185, 129));
        cardVentes = new KPICard("\uD83D\uDED2", "Total Ventes", "0", new Color(245, 158, 11));
        cardCA = new KPICard("\uD83D\uDCB0", "Chiffre d'Affaires", "0.00 DH", new Color(139, 92, 246));
        cardsPanel.add(cardMedicaments, "grow"); cardsPanel.add(cardClients, "grow");
        cardsPanel.add(cardVentes, "grow"); cardsPanel.add(cardCA, "grow");
        add(cardsPanel, "growx, wrap");

        // Alertes Stock
        JLabel alertTitle = new JLabel("\u26A0 Alertes Stock Faible");
        alertTitle.setFont(alertTitle.getFont().deriveFont(Font.BOLD, 16f));
        alertTitle.setForeground(new Color(220, 38, 38));
        add(alertTitle, "wrap");

        alertesPanel = new JPanel(new MigLayout("wrap, insets 0, gap 5", "[grow]", ""));
        alertesPanel.setOpaque(false);
        JScrollPane scrollAlertes = new JScrollPane(alertesPanel);
        scrollAlertes.setBorder(BorderFactory.createEmptyBorder());
        add(scrollAlertes, "growx, h 180!, wrap");

        // Lots expiration
        JLabel expiTitle = new JLabel("\u23F0 Lots Proches de Péremption (90 jours)");
        expiTitle.setFont(expiTitle.getFont().deriveFont(Font.BOLD, 16f));
        expiTitle.setForeground(new Color(245, 158, 11));
        add(expiTitle, "wrap");

        expirationTable = new StyledTable(new String[]{"ID", "Médicament", "N° Lot", "Date Expiration", "Qté Restante"});
        add(expirationTable, "grow, push");
    }

    public void chargerDonnees() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            int nbMed, nbClients, nbVentes; double ca;
            List<Medicament> alertes; List<Lot> lotsExp;

            @Override protected Void doInBackground() {
                try {
                    nbMed = medicamentService.getNombreTotal();
                    nbClients = clientDAO.count();
                    nbVentes = venteService.getNombreVentes();
                    ca = venteService.getChiffreAffaires();
                    alertes = medicamentService.getAlertesStock();
                    lotsExp = stockService.getLotsExpirationProche();
                } catch (Exception e) { e.printStackTrace(); }
                return null;
            }

            @Override protected void done() {
                cardMedicaments.updateValue(String.valueOf(nbMed));
                cardClients.updateValue(String.valueOf(nbClients));
                cardVentes.updateValue(String.valueOf(nbVentes));
                cardCA.updateValue(ValidationUtils.formatPrice(ca));

                alertesPanel.removeAll();
                if (alertes == null || alertes.isEmpty()) {
                    alertesPanel.add(new JLabel("\u2705 Aucune alerte de stock"), "growx");
                } else {
                    for (Medicament m : alertes) {
                        JPanel row = new JPanel(new MigLayout("insets 8 12 8 12", "[][grow][]", "[]"));
                        row.setBorder(BorderFactory.createMatteBorder(0, 3, 0, 0, Color.RED));
                        JLabel name = new JLabel(m.getNomCommercial() + " (" + m.getDci() + ")");
                        name.setFont(name.getFont().deriveFont(Font.BOLD));
                        JLabel qty = new JLabel("Stock: " + m.getQuantiteDisponible() + " / Seuil: " + m.getSeuilMinimal());
                        qty.setForeground(Color.RED);
                        row.add(new JLabel("\uD83D\uDD34")); row.add(name, "growx"); row.add(qty);
                        alertesPanel.add(row, "growx");
                    }
                }
                alertesPanel.revalidate(); alertesPanel.repaint();

                // Lots expiration
                if (lotsExp != null) {
                    Object[][] data = lotsExp.stream().map(l -> new Object[]{
                        l.getIdLot(), l.getNomMedicament(), l.getNumeroLot(),
                        ValidationUtils.formatDate(l.getDateExpiration()), l.getQuantiteRestante()
                    }).toArray(Object[][]::new);
                    expirationTable.setData(data);
                }
            }
        };
        worker.execute();
    }
}
