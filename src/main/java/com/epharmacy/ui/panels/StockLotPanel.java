package com.epharmacy.ui.panels;

import com.epharmacy.model.Lot;
import com.epharmacy.service.StockService;
import com.epharmacy.ui.components.StyledTable;
import com.epharmacy.util.ValidationUtils;
import net.miginfocom.swing.MigLayout;
import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Panneau de gestion du Stock et traçabilité des Lots.
 * @author E-PHARMACIE
 */
public class StockLotPanel extends JPanel {
    private StyledTable styledTable;
    private JCheckBox cbExpirationProche, cbExpires;
    private final StockService stockService = new StockService();

    public StockLotPanel() {
        initUI();
        chargerDonnees();
    }

    private void initUI() {
        setLayout(new MigLayout("wrap, insets 15 20 15 20, gap 10", "[grow]", "[][grow]"));

        JPanel header = new JPanel(new MigLayout("insets 0, gap 10", "[][][][grow]", "[]"));
        header.setOpaque(false);
        JLabel title = new JLabel("Stock & Lots");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        cbExpirationProche = new JCheckBox("Expiration proche (90j)");
        cbExpirationProche.addActionListener(e -> { cbExpires.setSelected(false); chargerDonnees(); });
        cbExpires = new JCheckBox("Lots expirés");
        cbExpires.addActionListener(e -> { cbExpirationProche.setSelected(false); chargerDonnees(); });
        header.add(title);
        header.add(cbExpirationProche);
        header.add(cbExpires);
        add(header, "growx");

        styledTable = new StyledTable(new String[]{
            "ID", "Médicament", "N° Lot", "Date Expiration", "Qté Initiale", "Qté Restante", "Statut"
        });
        styledTable.setColumnWidths(40, 180, 100, 110, 80, 80, 110);
        add(styledTable, "grow, push");
    }

    public void chargerDonnees() {
        List<Lot> data;
        if (cbExpires.isSelected()) {
            data = stockService.getLotsExpires();
        } else if (cbExpirationProche.isSelected()) {
            data = stockService.getLotsExpirationProche();
        } else {
            data = stockService.getTousLesLots();
        }

        styledTable.setData(data.stream().map(l -> {
            String statut;
            if (l.isExpire()) statut = "Expiré";
            else if (l.isProcheExpiration()) statut = "Bientôt expiré";
            else statut = "Valide";
            return new Object[]{
                l.getIdLot(), l.getNomMedicament(), l.getNumeroLot(),
                ValidationUtils.formatDate(l.getDateExpiration()),
                l.getQuantiteInitiale(), l.getQuantiteRestante(), statut
            };
        }).toArray(Object[][]::new));
    }
}
