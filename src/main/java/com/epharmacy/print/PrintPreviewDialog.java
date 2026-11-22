package com.epharmacy.print;

import com.epharmacy.util.ThemeManager;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

/**
 * Dialogue de prévisualisation d'impression aux normes ERP d'entreprise (Sage 100 / Odoo / SAP).
 * Supporte le rendu haute résolution High-DPI (2.0x bicubique), le cadrage centré sur canevas neutre,
 * et le dimensionnement adaptatif selon la résolution d'écran et le format (A4 vs Ticket 80mm).
 *
 * @author E-PHARMACIE
 * @version 3.0
 */
public class PrintPreviewDialog extends JDialog {

    private final String originalTitle;
    private final Printable printable;
    private final PageFormat pageFormat;

    public PrintPreviewDialog(Window parent, String title, Printable printable, PageFormat pageFormat) {
        super(parent, title, ModalityType.APPLICATION_MODAL);
        this.originalTitle = title;
        this.printable = printable;
        this.pageFormat = pageFormat;
        initUI(parent);
    }

    private void initUI(Window parent) {
        boolean isThermalReceipt = pageFormat.getWidth() < 400; // Ticket ~226pt, A4 ~595pt

        // 1. Calcul dynamique des dimensions selon la résolution de l'écran (identique A4 et Ticket)
        Rectangle screenBounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        int dialogWidth = Math.min(1150, Math.max(960, (int) (screenBounds.width * 0.72)));
        int dialogHeight = Math.max(680, (int) (screenBounds.height * 0.90));
        setMinimumSize(new Dimension(850, 600));

        setSize(dialogWidth, dialogHeight);
        setResizable(true);
        setLocationRelativeTo(parent);

        // 2. Formatage du titre et badge de format
        String displayTitle = originalTitle;
        if (displayTitle == null || displayTitle.isBlank()) {
            displayTitle = isThermalReceipt ? "Aperçu avant impression — Ticket de Caisse (80mm)"
                                            : "Aperçu avant impression — Document Officiel (A4)";
        } else if (!displayTitle.toLowerCase().contains("aperçu")) {
            displayTitle = "Aperçu avant impression — " + displayTitle;
        }
        setTitle(displayTitle);

        // 3. Layout principal MigLayout
        setLayout(new MigLayout("fill, insets 12", "[grow, fill]", "[]10[grow, fill]12[]"));

        // 4. Toolbar supérieure (Titre + Badge Format)
        JPanel topBar = new JPanel(new MigLayout("insets 0, fillx", "[grow]push[]", "[]"));
        topBar.setOpaque(false);

        JPanel leftTitlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftTitlePanel.setOpaque(false);

        JLabel lblTitle = new JLabel(displayTitle);
        lblTitle.setFont(lblTitle.getFont().deriveFont(Font.BOLD, 15f));
        leftTitlePanel.add(lblTitle);

        JLabel badge = new JLabel(isThermalReceipt ? " TICKET 80MM " : " FORMAT A4 ");
        badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        badge.setOpaque(true);
        if (isThermalReceipt) {
            badge.setBackground(ThemeManager.isDarkMode() ? new Color(80, 60, 20) : new Color(254, 243, 199));
            badge.setForeground(ThemeManager.isDarkMode() ? new Color(250, 204, 21) : new Color(146, 64, 14));
            badge.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(217, 119, 6), 1),
                    BorderFactory.createEmptyBorder(2, 6, 2, 6)
            ));
        } else {
            badge.setBackground(ThemeManager.isDarkMode() ? new Color(30, 58, 95) : new Color(219, 234, 254));
            badge.setForeground(ThemeManager.isDarkMode() ? new Color(96, 165, 250) : new Color(29, 78, 216));
            badge.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(37, 99, 235), 1),
                    BorderFactory.createEmptyBorder(2, 6, 2, 6)
            ));
        }
        leftTitlePanel.add(badge);
        topBar.add(leftTitlePanel, "growx");
        add(topBar, "wrap");

        // 5. Rendu haute résolution High-DPI (2.5x pour Ticket, 2.0x pour A4)
        double scale = isThermalReceipt ? 2.5 : 2.0;
        int imgW = (int) Math.round(pageFormat.getWidth() * scale);
        int imgH = (int) Math.round(pageFormat.getHeight() * scale);

        BufferedImage image = new BufferedImage(imgW, imgH, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, imgW, imgH);
        g2.scale(scale, scale);

        try {
            printable.print(g2, pageFormat, 0);
        } catch (PrinterException ex) {
            ex.printStackTrace();
        }
        g2.dispose();

        // 6. Viewport de prévisualisation avec canevas d'entreprise neutre
        // Pour les tickets thermiques dans une grande fenêtre, un zoom de 1.6 offre une netteté et un confort de lecture identique aux factures A4
        double displayZoom = isThermalReceipt ? 1.6 : 1.0;
        int displayW = (int) Math.round(pageFormat.getWidth() * displayZoom);
        int displayH = (int) Math.round(pageFormat.getHeight() * displayZoom);

        Color canvasBg = ThemeManager.isDarkMode() ? new Color(43, 45, 48) : new Color(226, 230, 235);
        JPanel canvas = new JPanel(new GridBagLayout());
        canvas.setBackground(canvasBg);
        canvas.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        canvas.add(new PaperSheetPanel(image, displayW, displayH), gbc);

        JScrollPane scrollPane = new JScrollPane(canvas);
        scrollPane.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0,
                ThemeManager.isDarkMode() ? new Color(60, 64, 68) : new Color(205, 210, 215)));
        scrollPane.getViewport().setBackground(canvasBg);
        scrollPane.getVerticalScrollBar().setUnitIncrement(28);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(28);
        add(scrollPane, "grow, push, wrap");

        // 7. Barre inférieure d'actions avec boutons espacés
        JPanel bottomBar = new JPanel(new MigLayout("insets 0, fillx", "[grow]push[]12[]", "[]"));
        bottomBar.setOpaque(false);

        JLabel lblInfo = new JLabel("Document haute résolution (High-DPI) • Prêt pour impression physique ou export PDF");
        lblInfo.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblInfo.setForeground(new Color(120, 125, 130));
        bottomBar.add(lblInfo);

        JButton btnPrint = new JButton("Lancer l'impression");
        btnPrint.setBackground(new Color(40, 167, 69)); // #28A745
        btnPrint.setForeground(Color.WHITE);
        btnPrint.setFont(btnPrint.getFont().deriveFont(Font.BOLD, 13f));
        btnPrint.setMargin(new Insets(8, 20, 8, 20));
        btnPrint.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnPrint.addActionListener(e -> doPrint());

        JButton btnClose = new JButton("Fermer");
        btnClose.setFont(btnClose.getFont().deriveFont(Font.PLAIN, 13f));
        btnClose.setMargin(new Insets(8, 20, 8, 20));
        btnClose.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnClose.addActionListener(e -> dispose());

        bottomBar.add(btnPrint);
        bottomBar.add(btnClose);
        add(bottomBar);
    }

    private void doPrint() {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintable(printable, pageFormat);
        if (job.printDialog()) {
            try {
                job.print();
                JOptionPane.showMessageDialog(this, "Impression envoyée avec succès !", "Impression", JOptionPane.INFORMATION_MESSAGE);
            } catch (PrinterException ex) {
                JOptionPane.showMessageDialog(this, "Erreur d'impression : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Composant représentant la feuille de papier cadrée avec fond blanc et bordure 1px subtile.
     */
    private static class PaperSheetPanel extends JPanel {
        private final BufferedImage image;
        private final int displayW;
        private final int displayH;

        public PaperSheetPanel(BufferedImage image, int displayW, int displayH) {
            this.image = image;
            this.displayW = displayW;
            this.displayH = displayH;
            setPreferredSize(new Dimension(displayW, displayH));
            setMinimumSize(new Dimension(displayW, displayH));
            setMaximumSize(new Dimension(displayW, displayH));
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2d.setRenderingHints(new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY));
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Fond blanc papier
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, displayW, displayH);

            if (image != null) {
                g2d.drawImage(image, 0, 0, displayW, displayH, null);
            }

            // Bordure 1px subtile autour de la feuille de papier
            g2d.setColor(ThemeManager.isDarkMode() ? new Color(75, 80, 85) : new Color(195, 200, 206));
            g2d.drawRect(0, 0, displayW - 1, displayH - 1);

            g2d.dispose();
        }
    }

    /**
     * Crée un PageFormat pour ticket thermique 80mm (226pt de large).
     */
    public static PageFormat createTicketPageFormat() {
        PageFormat pf = new PageFormat();
        Paper paper = new Paper();
        double width = 226;
        double height = 800;
        paper.setSize(width, height);
        paper.setImageableArea(5, 5, width - 10, height - 10);
        pf.setPaper(paper);
        pf.setOrientation(PageFormat.PORTRAIT);
        return pf;
    }

    /**
     * Crée un PageFormat pour A4 (595 x 842 pt).
     */
    public static PageFormat createA4PageFormat() {
        PageFormat pf = new PageFormat();
        Paper paper = new Paper();
        double width = 595;
        double height = 842;
        paper.setSize(width, height);
        paper.setImageableArea(30, 30, width - 60, height - 60);
        pf.setPaper(paper);
        pf.setOrientation(PageFormat.PORTRAIT);
        return pf;
    }
}
