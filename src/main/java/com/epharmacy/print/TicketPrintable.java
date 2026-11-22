package com.epharmacy.print;

import com.epharmacy.model.LigneVente;
import com.epharmacy.model.ParametresSociete;
import com.epharmacy.model.Vente;
import com.epharmacy.util.ValidationUtils;

import java.awt.*;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.util.List;

/**
 * Impression ticket de caisse thermique (format 80mm / 226 pt).
 * Conforme aux spécifications du module Ventes.
 *
 * @author E-PHARMACIE
 */
public class TicketPrintable implements Printable {

    private final Vente vente;
    private final List<LigneVente> lignes;
    private final ParametresSociete params;
    private final String nomVendeur;

    public TicketPrintable(Vente vente, List<LigneVente> lignes, ParametresSociete params, String nomVendeur) {
        this.vente = vente;
        this.lignes = lignes;
        this.params = params != null ? params : new ParametresSociete();
        this.nomVendeur = nomVendeur != null && !nomVendeur.isBlank() ? nomVendeur : "Pharmacien";
    }

    @Override
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
        if (pageIndex > 0) {
            return NO_SUCH_PAGE;
        }

        Graphics2D g2 = (Graphics2D) graphics;
        g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
        g2.setColor(Color.BLACK);

        int x = 8;
        int y = 14;
        int width = (int) pageFormat.getImageableWidth() - 16;
        if (width <= 0) width = 210;

        // Entête Pharmacie
        g2.setFont(new Font("Monospaced", Font.BOLD, 11));
        drawCenteredString(g2, params.getNomPharmacie() != null ? params.getNomPharmacie() : "E-PHARMACIE", x, y, width);
        y += 13;

        g2.setFont(new Font("Monospaced", Font.PLAIN, 8));
        if (params.getAdresse() != null && !params.getAdresse().isBlank()) {
            drawCenteredString(g2, params.getAdresse(), x, y, width);
            y += 10;
        }
        if (params.getVille() != null && !params.getVille().isBlank()) {
            drawCenteredString(g2, params.getVille(), x, y, width);
            y += 10;
        }
        if (params.getTelephone() != null && !params.getTelephone().isBlank()) {
            drawCenteredString(g2, "Tél: " + params.getTelephone(), x, y, width);
            y += 10;
        }
        if (params.getIce() != null && !params.getIce().isBlank()) {
            drawCenteredString(g2, "ICE: " + params.getIce(), x, y, width);
            y += 10;
        }

        // Ligne de séparation
        drawDashedLine(g2, x, y, x + width);
        y += 11;

        // Métadonnées du ticket
        g2.setFont(new Font("Monospaced", Font.PLAIN, 8));
        g2.drawString(String.format("Ticket N°: VNT-%04d", vente.getIdVente()), x, y);
        y += 10;
        String dateStr = vente.getDateVente() != null ? ValidationUtils.formatDateTime(vente.getDateVente()) : "";
        g2.drawString("Date: " + dateStr, x, y);
        y += 10;
        g2.drawString("Vendeur: " + nomVendeur, x, y);
        y += 10;
        if (vente.getNomClient() != null && !vente.getNomClient().isBlank() && !vente.getNomClient().equalsIgnoreCase("Comptoir")) {
            g2.drawString("Client: " + vente.getNomClient(), x, y);
            y += 10;
        }

        drawDashedLine(g2, x, y, x + width);
        y += 11;

        // En-tête du tableau articles
        g2.setFont(new Font("Monospaced", Font.BOLD, 8));
        g2.drawString("Désignation", x, y);
        g2.drawString("Qté x P.U", x + 110, y);
        drawRightString(g2, "Total", x + width, y);
        y += 11;

        // Articles
        g2.setFont(new Font("Monospaced", Font.PLAIN, 8));
        for (LigneVente lv : lignes) {
            String nom = lv.getNomMedicament() != null ? lv.getNomMedicament() : "Article";
            if (nom.length() > 20) {
                nom = nom.substring(0, 18) + "..";
            }
            g2.drawString(nom, x, y);
            String qtePu = lv.getQuantite() + " x " + String.format("%.2f", lv.getPrixUnitaireVente());
            g2.drawString(qtePu, x + 105, y);
            drawRightString(g2, String.format("%.2f", lv.getSousTotal()), x + width, y);
            y += 11;
        }

        drawDashedLine(g2, x, y, x + width);
        y += 13;

        // Total
        g2.setFont(new Font("Monospaced", Font.BOLD, 10));
        g2.drawString("TOTAL TTC:", x, y);
        drawRightString(g2, String.format("%.2f DH", vente.getMontantTotal()), x + width, y);
        y += 12;

        g2.setFont(new Font("Monospaced", Font.PLAIN, 8));
        g2.drawString("Paiement: " + (vente.getModePaiement() != null ? vente.getModePaiement() : "ESPECES"), x, y);
        y += 14;

        // Message de pied de ticket
        drawDashedLine(g2, x, y, x + width);
        y += 12;

        g2.setFont(new Font("Monospaced", Font.ITALIC, 8));
        String msg = params.getMessageTicket() != null && !params.getMessageTicket().isBlank()
                ? params.getMessageTicket()
                : "Merci de votre visite. Prompt rétablissement !";
        drawCenteredString(g2, msg, x, y, width);

        return PAGE_EXISTS;
    }

    private void drawCenteredString(Graphics2D g2, String text, int x, int y, int width) {
        FontMetrics fm = g2.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int drawX = x + (width - textWidth) / 2;
        g2.drawString(text, drawX, y);
    }

    private void drawRightString(Graphics2D g2, String text, int rightX, int y) {
        FontMetrics fm = g2.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        g2.drawString(text, rightX - textWidth, y);
    }

    private void drawDashedLine(Graphics2D g2, int x1, int y, int x2) {
        Stroke oldStroke = g2.getStroke();
        float[] dash = {3.0f, 3.0f};
        g2.setStroke(new BasicStroke(0.8f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f));
        g2.drawLine(x1, y, x2, y);
        g2.setStroke(oldStroke);
    }
}
