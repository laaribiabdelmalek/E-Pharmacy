package com.epharmacy.ui.panels;

import com.epharmacy.dao.ClientDAO;
import com.epharmacy.model.*;
import com.epharmacy.service.*;
import com.epharmacy.ui.components.StyledTable;
import com.epharmacy.util.*;
import net.miginfocom.swing.MigLayout;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Panneau Point de Vente (POS) et historique des ventes.
 * @author E-PHARMACIE
 */
public class VentePanel extends JPanel {
    private final VenteService venteService = new VenteService();
    private final MedicamentService medicamentService = new MedicamentService();
    private final StockService stockService = new StockService();
    private final ClientDAO clientDAO = new ClientDAO();

    // POS fields
    private JComboBox<String> cbClient;
    private List<Client> clients;
    private JTextField tfSearchMed;
    private StyledTable resultTable;
    private DefaultTableModel cartModel;
    private JTable cartTable;
    private JLabel lblTotal;
    private JComboBox<String> cbPaiement;
    private final List<LigneVente> cartLignes = new ArrayList<>();

    // History
    private StyledTable historyTable;

    public VentePanel() {
        initUI();
    }

    private void initUI() {
        setLayout(new MigLayout("wrap, insets 10 15 10 15, gap 10", "[grow]", "[][grow][][grow]"));

        // === Section POS ===
        JLabel posTitle = new JLabel("Nouvelle Vente");
        posTitle.setFont(posTitle.getFont().deriveFont(Font.BOLD, 20f));
        add(posTitle, "wrap");

        JPanel posPanel = new JPanel(new MigLayout("insets 5, gap 10", "[55%,grow][45%,grow]", "[grow]"));
        posPanel.setOpaque(false);

        // Left: search & product selection
        JPanel leftPanel = new JPanel(new MigLayout("wrap, insets 5, gap 5", "[grow]", "[][grow]"));
        leftPanel.setBorder(BorderFactory.createTitledBorder("Sélection Médicament"));
        tfSearchMed = new JTextField();
        tfSearchMed.putClientProperty("JTextField.placeholderText", "Rechercher un médicament...");
        tfSearchMed.addActionListener(e -> rechercherMedicaments());
        leftPanel.add(tfSearchMed, "growx");

        resultTable = new StyledTable(new String[]{"ID", "Nom", "Stock", "Prix (DH)"});
        resultTable.getTable().addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) ajouterAuPanier();
            }
        });
        leftPanel.add(resultTable, "grow, push");
        posPanel.add(leftPanel, "grow");

        // Right: cart
        JPanel rightPanel = new JPanel(new MigLayout("wrap, insets 5, gap 5", "[grow]", "[][][][grow][][][][]"));
        rightPanel.setBorder(BorderFactory.createTitledBorder("Panier"));

        // Client selection
        clients = clientDAO.findAll();
        cbClient = new JComboBox<>();
        cbClient.addItem("-- Vente comptoir --");
        for (Client c : clients) {
            cbClient.addItem(c.getPrenom() + " " + c.getNom() + " (ID:" + c.getIdClient() + ")");
        }
        rightPanel.add(new JLabel("Client:"), "split 2");
        rightPanel.add(cbClient, "growx, wrap");

        // Cart table
        cartModel = new DefaultTableModel(new String[]{"Médicament", "Lot", "Qté", "Prix Unit.", "Sous-Total"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        cartTable = new JTable(cartModel);
        cartTable.setRowHeight(30);
        JScrollPane cartScroll = new JScrollPane(cartTable);
        rightPanel.add(cartScroll, "grow, push, wrap");

        // Remove button
        JButton btnRemove = new JButton("Retirer sélection");
        btnRemove.addActionListener(e -> retirerDuPanier());
        rightPanel.add(btnRemove, "right, wrap");

        // Total
        lblTotal = new JLabel("Total: 0.00 DH");
        lblTotal.setFont(lblTotal.getFont().deriveFont(Font.BOLD, 18f));
        rightPanel.add(lblTotal, "center, wrap");

        // Payment mode
        cbPaiement = new JComboBox<>(new String[]{"ESPECES", "CARTE", "CREDIT"});
        rightPanel.add(new JLabel("Paiement:"), "split 2");
        rightPanel.add(cbPaiement, "growx, wrap");

        // Buttons
        JPanel btnPanel = new JPanel(new MigLayout("insets 0, gap 10", "push[][]push"));
        JButton btnValider = new JButton("Valider la Vente");
        btnValider.setBackground(new Color(34, 197, 94));
        btnValider.setForeground(Color.WHITE);
        btnValider.setFont(btnValider.getFont().deriveFont(Font.BOLD, 14f));
        btnValider.addActionListener(e -> validerVente());
        JButton btnAnnuler = new JButton("Vider le panier");
        btnAnnuler.setForeground(Color.RED);
        btnAnnuler.addActionListener(e -> viderPanier());
        btnPanel.add(btnValider);
        btnPanel.add(btnAnnuler);
        rightPanel.add(btnPanel, "growx");

        posPanel.add(rightPanel, "grow");
        add(posPanel, "grow, push");

        // === Section Historique ===
        JLabel histTitle = new JLabel("Historique des Ventes");
        histTitle.setFont(histTitle.getFont().deriveFont(Font.BOLD, 18f));
        add(histTitle, "wrap");

        historyTable = new StyledTable(new String[]{"ID", "Date", "Client", "Montant (DH)", "Paiement", "Statut"});
        add(historyTable, "grow, h 200!");
        chargerHistorique();
    }

    private void rechercherMedicaments() {
        String keyword = tfSearchMed.getText().trim();
        List<Medicament> meds = keyword.isEmpty() ? medicamentService.getTous() : medicamentService.rechercher(keyword);
        resultTable.setData(meds.stream()
            .filter(m -> m.getQuantiteDisponible() > 0)
            .map(m -> new Object[]{m.getIdMedicament(), m.getNomCommercial(), m.getQuantiteDisponible(), String.format("%.2f", m.getPrixVentePublic())})
            .toArray(Object[][]::new));
    }

    private void ajouterAuPanier() {
        int id = resultTable.getSelectedId();
        if (id <= 0) return;
        Medicament med = medicamentService.getParId(id);
        if (med == null) return;

        String qtyStr = JOptionPane.showInputDialog(this, "Quantité pour " + med.getNomCommercial() + " :", "1");
        if (qtyStr == null || !ValidationUtils.isPositiveInteger(qtyStr)) return;
        int qty = Integer.parseInt(qtyStr);

        if (qty > med.getQuantiteDisponible()) {
            JOptionPane.showMessageDialog(this, "Stock insuffisant ! Disponible: " + med.getQuantiteDisponible());
            return;
        }

        try {
            List<LigneVente> lignes = stockService.decrementerStock(med.getIdMedicament(), qty, med.getPrixVentePublic());
            for (LigneVente lv : lignes) {
                cartLignes.add(lv);
                cartModel.addRow(new Object[]{
                    lv.getNomMedicament(), lv.getNumeroLot(), lv.getQuantite(),
                    String.format("%.2f", lv.getPrixUnitaireVente()), String.format("%.2f", lv.getSousTotal())
                });
            }
            majTotal();
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void retirerDuPanier() {
        int row = cartTable.getSelectedRow();
        if (row >= 0 && row < cartLignes.size()) {
            cartLignes.remove(row);
            cartModel.removeRow(row);
            majTotal();
        }
    }

    private void viderPanier() {
        cartLignes.clear();
        cartModel.setRowCount(0);
        majTotal();
    }

    private void majTotal() {
        double total = cartLignes.stream().mapToDouble(LigneVente::getSousTotal).sum();
        lblTotal.setText("Total: " + String.format("%.2f", total) + " DH");
    }

    private void validerVente() {
        if (cartLignes.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Le panier est vide !");
            return;
        }

        Vente vente = new Vente();
        vente.setIdUtilisateur(SessionManager.getInstance().getUtilisateurConnecte().getIdUtilisateur());
        vente.setDateVente(LocalDateTime.now());
        vente.setModePaiement((String) cbPaiement.getSelectedItem());
        vente.setStatut("VALIDEE");

        int clientIdx = cbClient.getSelectedIndex();
        if (clientIdx > 0 && clientIdx <= clients.size()) {
            vente.setIdClient(clients.get(clientIdx - 1).getIdClient());
        }

        if ("CREDIT".equals(vente.getModePaiement()) && vente.getIdClient() <= 0) {
            JOptionPane.showMessageDialog(this, "Le paiement à crédit nécessite un client sélectionné !", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean success = venteService.enregistrerVente(vente, new ArrayList<>(cartLignes));
        if (success) {
            JOptionPane.showMessageDialog(this, "Vente enregistrée avec succès !\nMontant: " + ValidationUtils.formatPrice(vente.getMontantTotal()));
            viderPanier();
            chargerHistorique();
        } else {
            JOptionPane.showMessageDialog(this, "Erreur lors de l'enregistrement de la vente.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void chargerHistorique() {
        List<Vente> ventes = venteService.getToutesLesVentes();
        historyTable.setData(ventes.stream().map(v -> new Object[]{
            v.getIdVente(),
            v.getDateVente() != null ? ValidationUtils.formatDateTime(v.getDateVente()) : "",
            v.getNomClient() != null ? v.getNomClient() : "Comptoir",
            String.format("%.2f", v.getMontantTotal()),
            v.getModePaiement(), v.getStatut()
        }).toArray(Object[][]::new));
    }
}
