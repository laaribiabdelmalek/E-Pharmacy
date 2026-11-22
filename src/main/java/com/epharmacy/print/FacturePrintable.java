package com.epharmacy.print;

import com.epharmacy.model.Client;
import com.epharmacy.model.LigneVente;
import com.epharmacy.model.ParametresSociete;
import com.epharmacy.model.Vente;
import com.epharmacy.util.FrenchNumberToWords;
import com.epharmacy.util.ValidationUtils;

import java.awt.*;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.time.LocalDate;
import java.util.List;

/**
 * Impression officielle de facture A4 (595 x 842 pt).
 * Conforme aux exigences fiscales marocaines et aux spécifications E-PHARMACIE.
 *
 * @author E-PHARMACIE
 */
public class FacturePrintable implements Printable {

    private final Vente vente;
    private final List<LigneVente> lignes;
    private final ParametresSociete params;
    private final Client client;

    public FacturePrintable(Vente vente, List<LigneVente> lignes, ParametresSociete params, Client client) {
        this.vente = vente;
        this.lignes = lignes;
        this.params = params != null ? params : new ParametresSociete();
        this.client = client;
    }

    @Override
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
        if (pageIndex > 0) {
            return NO_SUCH_PAGE;
        }

        Graphics2D g2 = (Graphics2D) graphics;
        g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
        g2.setColor(Color.BLACK);

        int leftX = 20;
        int topY = 25;
        int contentWidth = (int) pageFormat.getImageableWidth() - 40;
        if (contentWidth <= 0) contentWidth = 535;

        // ==========================================
        // 1. EN-TÊTE : PHARMACIE & BLOC FISCAL
        // ==========================================
        g2.setFont(new Font("SansSerif", Font.BOLD, 16));
        g2.setColor(new Color(24, 76, 120));
        String nomPharmacie = params.getNomPharmacie() != null && !params.getNomPharmacie().isBlank()
                ? params.getNomPharmacie() : "PHARMACIE E-PHARMACIE";
        g2.drawString(nomPharmacie, leftX, topY);

        g2.setColor(Color.DARK_GRAY);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 9));
        int yPharm = topY + 15;
        if (params.getAdresse() != null && !params.getAdresse().isBlank()) {
            g2.drawString(params.getAdresse() + (params.getVille() != null ? ", " + params.getVille() : ""), leftX, yPharm);
            yPharm += 12;
        }
        String telMail = "";
        if (params.getTelephone() != null && !params.getTelephone().isBlank()) {
            telMail += "Tél: " + params.getTelephone();
        }
        if (params.getEmail() != null && !params.getEmail().isBlank()) {
            telMail += (telMail.isEmpty() ? "" : " | ") + "Email: " + params.getEmail();
        }
        if (!telMail.isEmpty()) {
            g2.drawString(telMail, leftX, yPharm);
            yPharm += 12;
        }

        // Bloc fiscal à droite
        int fiscalBoxX = leftX + contentWidth - 190;
        int yFiscal = topY;
        g2.setFont(new Font("SansSerif", Font.BOLD, 9));
        g2.setColor(new Color(60, 60, 60));
        g2.drawString("INFORMATIONS FISCALES", fiscalBoxX, yFiscal);
        yFiscal += 12;
        g2.setFont(new Font("SansSerif", Font.PLAIN, 8));
        g2.drawString("ICE : " + (params.getIce() != null ? params.getIce() : "-"), fiscalBoxX, yFiscal);
        yFiscal += 11;
        g2.drawString("I.F. : " + (params.getIdentifiantFiscal() != null ? params.getIdentifiantFiscal() : "-"), fiscalBoxX, yFiscal);
        yFiscal += 11;
        g2.drawString("R.C. : " + (params.getRc() != null ? params.getRc() : "-"), fiscalBoxX, yFiscal);
        yFiscal += 11;
        g2.drawString("Patente : " + (params.getPatente() != null ? params.getPatente() : "-"), fiscalBoxX, yFiscal);

        int currentY = Math.max(yPharm, yFiscal) + 15;

        // Ligne décorative
        g2.setColor(new Color(24, 76, 120));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawLine(leftX, currentY, leftX + contentWidth, currentY);
        currentY += 15;

        // ==========================================
        // 2. EN-TÊTE FACTURE & CADRE CLIENT
        // ==========================================
        int year = (vente.getDateVente() != null) ? vente.getDateVente().getYear() : LocalDate.now().getYear();
        String numFacture = String.format("FACTURE N° FAC-%d-%04d", year, vente.getIdVente());

        g2.setFont(new Font("SansSerif", Font.BOLD, 13));
        g2.setColor(new Color(24, 76, 120));
        g2.drawString(numFacture, leftX, currentY);

        g2.setFont(new Font("SansSerif", Font.PLAIN, 9));
        g2.setColor(Color.BLACK);
        String dateStr = vente.getDateVente() != null ? ValidationUtils.formatDateTime(vente.getDateVente()) : "";
        g2.drawString("Date d'émission : " + dateStr, leftX, currentY + 14);
        g2.drawString("Mode de règlement : " + (vente.getModePaiement() != null ? vente.getModePaiement() : "ESPECES"), leftX, currentY + 26);

        // Boîte Client (à droite)
        int clientBoxW = 210;
        int clientBoxH = 60;
        int clientBoxX = leftX + contentWidth - clientBoxW;
        int clientBoxY = currentY - 8;

        g2.setColor(new Color(245, 247, 250));
        g2.fillRect(clientBoxX, clientBoxY, clientBoxW, clientBoxH);
        g2.setColor(new Color(200, 210, 220));
        g2.setStroke(new BasicStroke(1.0f));
        g2.drawRect(clientBoxX, clientBoxY, clientBoxW, clientBoxH);

        g2.setFont(new Font("SansSerif", Font.BOLD, 9));
        g2.setColor(new Color(24, 76, 120));
        g2.drawString("DESTINATAIRE (CLIENT)", clientBoxX + 10, clientBoxY + 14);

        g2.setFont(new Font("SansSerif", Font.PLAIN, 9));
        g2.setColor(Color.BLACK);
        if (client != null && client.getIdClient() > 0) {
            g2.drawString(client.getPrenom() + " " + client.getNom(), clientBoxX + 10, clientBoxY + 28);
            String tel = client.getTelephone() != null ? "Tél: " + client.getTelephone() : "";
            g2.drawString(tel, clientBoxX + 10, clientBoxY + 41);
            String adr = client.getAdresse() != null ? client.getAdresse() : "";
            if (adr.length() > 28) adr = adr.substring(0, 26) + "..";
            g2.drawString(adr, clientBoxX + 10, clientBoxY + 54);
        } else if (vente.getNomClient() != null && !vente.getNomClient().isBlank() && !vente.getNomClient().equalsIgnoreCase("Comptoir")) {
            g2.drawString(vente.getNomClient(), clientBoxX + 10, clientBoxY + 28);
            g2.drawString("Client régulier", clientBoxX + 10, clientBoxY + 41);
        } else {
            g2.drawString("Client Comptoir", clientBoxX + 10, clientBoxY + 28);
            g2.drawString("Vente directe au comptoir", clientBoxX + 10, clientBoxY + 41);
        }

        currentY = clientBoxY + clientBoxH + 20;

        // ==========================================
        // 3. TABLEAU DES ARTICLES BORDURÉ
        // ==========================================
        int col1W = 205; // Désignation
        int col2W = 90;  // Dosage
        int col3W = 55;  // Qté
        int col4W = 85;  // P.U. (DH)
        int col5W = 100; // Sous-total (DH)

        // Entête tableau
        int thY = currentY;
        int thH = 22;
        g2.setColor(new Color(230, 238, 248));
        g2.fillRect(leftX, thY, contentWidth, thH);
        g2.setColor(new Color(24, 76, 120));
        g2.drawRect(leftX, thY, contentWidth, thH);

        g2.setFont(new Font("SansSerif", Font.BOLD, 9));
        g2.drawString("Désignation", leftX + 8, thY + 15);
        g2.drawString("Dosage", leftX + col1W + 8, thY + 15);
        g2.drawString("Qté", leftX + col1W + col2W + 15, thY + 15);
        g2.drawString("P.U. (DH)", leftX + col1W + col2W + col3W + 20, thY + 15);
        g2.drawString("Sous-total (DH)", leftX + col1W + col2W + col3W + col4W + 15, thY + 15);

        // Lignes du tableau
        int rowY = thY + thH;
        int rowH = 19;
        g2.setFont(new Font("SansSerif", Font.PLAIN, 9));

        for (int i = 0; i < lignes.size(); i++) {
            LigneVente lv = lignes.get(i);

            if (i % 2 == 1) {
                g2.setColor(new Color(248, 250, 252));
                g2.fillRect(leftX, rowY, contentWidth, rowH);
            }

            g2.setColor(new Color(215, 222, 230));
            g2.drawRect(leftX, rowY, contentWidth, rowH);

            g2.setColor(Color.BLACK);
            String desig = lv.getNomMedicament() != null ? lv.getNomMedicament() : "Médicament";
            if (desig.length() > 32) desig = desig.substring(0, 30) + "..";
            g2.drawString(desig, leftX + 8, rowY + 13);

            String dos = lv.getDosage() != null ? lv.getDosage() : "-";
            g2.drawString(dos, leftX + col1W + 8, rowY + 13);

            g2.drawString(String.valueOf(lv.getQuantite()), leftX + col1W + col2W + 20, rowY + 13);

            String puStr = String.format("%.2f", lv.getPrixUnitaireVente());
            g2.drawString(puStr, leftX + col1W + col2W + col3W + 25, rowY + 13);

            String stStr = String.format("%.2f", lv.getSousTotal());
            g2.drawString(stStr, leftX + col1W + col2W + col3W + col4W + 30, rowY + 13);

            rowY += rowH;
        }

        currentY = rowY + 12;

        // ==========================================
        // 4. RÉCAPITULATIF & MONTANT EN TOUTES LETTRES
        // ==========================================
        int totalBoxW = 200;
        int totalBoxH = 50;
        int totalBoxX = leftX + contentWidth - totalBoxW;

        g2.setColor(new Color(245, 247, 250));
        g2.fillRect(totalBoxX, currentY, totalBoxW, totalBoxH);
        g2.setColor(new Color(24, 76, 120));
        g2.drawRect(totalBoxX, currentY, totalBoxW, totalBoxH);

        g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
        g2.setColor(Color.DARK_GRAY);
        g2.drawString("Total Net à Payer (TTC) :", totalBoxX + 12, currentY + 18);

        g2.setFont(new Font("SansSerif", Font.BOLD, 14));
        g2.setColor(new Color(24, 76, 120));
        String totalFormate = String.format("%.2f DH", vente.getMontantTotal());
        g2.drawString(totalFormate, totalBoxX + 12, currentY + 38);

        // Montant en toutes lettres
        int wordsX = leftX;
        int wordsY = currentY + 16;
        g2.setFont(new Font("SansSerif", Font.ITALIC, 9));
        g2.setColor(Color.DARK_GRAY);
        g2.drawString("Arrêtée la présente facture à la somme de :", wordsX, wordsY);

        g2.setFont(new Font("SansSerif", Font.BOLD | Font.ITALIC, 9));
        g2.setColor(Color.BLACK);
        String montantLettres = FrenchNumberToWords.convert(vente.getMontantTotal());
        g2.drawString(montantLettres, wordsX, wordsY + 15);

        currentY += totalBoxH + 30;

        // ==========================================
        // 5. CACHET ET SIGNATURE
        // ==========================================
        int stampBoxW = 180;
        int stampBoxH = 75;
        int stampBoxX = leftX + contentWidth - stampBoxW;

        g2.setColor(new Color(210, 215, 225));
        g2.setStroke(new BasicStroke(1.0f));
        g2.drawRect(stampBoxX, currentY, stampBoxW, stampBoxH);

        g2.setFont(new Font("SansSerif", Font.BOLD, 8));
        g2.setColor(new Color(100, 100, 100));
        g2.drawString("CACHET ET SIGNATURE", stampBoxX + 35, currentY + 14);

        return PAGE_EXISTS;
    }
}
