package com.epharmacy.print;

import com.epharmacy.model.Approvisionnement;
import com.epharmacy.model.Fournisseur;
import com.epharmacy.model.LigneApprovisionnement;
import com.epharmacy.model.ParametresSociete;
import com.epharmacy.util.FrenchNumberToWords;
import com.epharmacy.util.ValidationUtils;

import java.awt.*;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.time.LocalDate;
import java.util.List;

/**
 * Impression d'un Bon de Commande Fournisseur au format A4 (595 x 842 pt).
 * Conforme au module Approvisionnements E-PHARMACIE.
 *
 * @author E-PHARMACIE
 */
public class BonCommandePrintable implements Printable {

    private final Approvisionnement appro;
    private final List<LigneApprovisionnement> lignes;
    private final ParametresSociete params;
    private final Fournisseur fournisseur;

    public BonCommandePrintable(Approvisionnement appro, List<LigneApprovisionnement> lignes,
                                ParametresSociete params, Fournisseur fournisseur) {
        this.appro = appro;
        this.lignes = lignes;
        this.params = params != null ? params : new ParametresSociete();
        this.fournisseur = fournisseur;
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
        // 1. EN-TÊTE PHARMACIE
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
        String contactInfo = "";
        if (params.getTelephone() != null && !params.getTelephone().isBlank()) {
            contactInfo += "Tél: " + params.getTelephone();
        }
        if (params.getEmail() != null && !params.getEmail().isBlank()) {
            contactInfo += (contactInfo.isEmpty() ? "" : " | ") + "Email: " + params.getEmail();
        }
        if (!contactInfo.isEmpty()) {
            g2.drawString(contactInfo, leftX, yPharm);
            yPharm += 12;
        }
        if (params.getIce() != null && !params.getIce().isBlank()) {
            g2.drawString("ICE: " + params.getIce(), leftX, yPharm);
            yPharm += 12;
        }

        // ==========================================
        // 2. CADRE FOURNISSEUR
        // ==========================================
        int fourBoxW = 210;
        int fourBoxH = 65;
        int fourBoxX = leftX + contentWidth - fourBoxW;
        int fourBoxY = topY - 5;

        g2.setColor(new Color(245, 247, 250));
        g2.fillRect(fourBoxX, fourBoxY, fourBoxW, fourBoxH);
        g2.setColor(new Color(200, 210, 220));
        g2.setStroke(new BasicStroke(1.0f));
        g2.drawRect(fourBoxX, fourBoxY, fourBoxW, fourBoxH);

        g2.setFont(new Font("SansSerif", Font.BOLD, 9));
        g2.setColor(new Color(24, 76, 120));
        g2.drawString("FOURNISSEUR", fourBoxX + 10, fourBoxY + 14);

        g2.setFont(new Font("SansSerif", Font.PLAIN, 9));
        g2.setColor(Color.BLACK);
        if (fournisseur != null) {
            g2.drawString(fournisseur.getRaisonSociale(), fourBoxX + 10, fourBoxY + 28);
            String fIce = (fournisseur.getIce() != null && !fournisseur.getIce().isBlank()) ? "ICE: " + fournisseur.getIce() : "";
            g2.drawString(fIce, fourBoxX + 10, fourBoxY + 41);
            String fTel = (fournisseur.getTelephone() != null ? "Tél: " + fournisseur.getTelephone() : "");
            g2.drawString(fTel + (fournisseur.getVille() != null ? " (" + fournisseur.getVille() + ")" : ""), fourBoxX + 10, fourBoxY + 54);
        } else {
            String fNom = appro.getNomFournisseur() != null ? appro.getNomFournisseur() : "Fournisseur";
            g2.drawString(fNom, fourBoxX + 10, fourBoxY + 28);
        }

        int currentY = Math.max(yPharm, fourBoxY + fourBoxH) + 15;

        // Ligne de séparation
        g2.setColor(new Color(24, 76, 120));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawLine(leftX, currentY, leftX + contentWidth, currentY);
        currentY += 18;

        // ==========================================
        // 3. TITRE DU BON DE COMMANDE
        // ==========================================
        int year = (appro.getDateApprovisionnement() != null) ? appro.getDateApprovisionnement().getYear() : LocalDate.now().getYear();
        String numBC = String.format("BON DE COMMANDE N° BC-%d-%04d", year, appro.getIdApprovisionnement());

        g2.setFont(new Font("SansSerif", Font.BOLD, 13));
        g2.setColor(new Color(24, 76, 120));
        g2.drawString(numBC, leftX, currentY);

        g2.setFont(new Font("SansSerif", Font.PLAIN, 9));
        g2.setColor(Color.BLACK);
        String dateStr = appro.getDateApprovisionnement() != null ? ValidationUtils.formatDateTime(appro.getDateApprovisionnement()) : "";
        g2.drawString("Date d'émission : " + dateStr, leftX, currentY + 14);
        g2.drawString("Statut : " + (appro.getStatut() != null ? appro.getStatut() : "En attente"), leftX, currentY + 26);

        currentY += 40;

        // ==========================================
        // 4. TABLEAU DES ARTICLES COMMANDÉS
        // ==========================================
        int col1W = 235; // Médicament
        int col2W = 95;  // Qté Commandée
        int col3W = 100; // Prix Achat Unit.
        int col4W = 105; // Sous-total estimé

        int thY = currentY;
        int thH = 22;
        g2.setColor(new Color(230, 238, 248));
        g2.fillRect(leftX, thY, contentWidth, thH);
        g2.setColor(new Color(24, 76, 120));
        g2.drawRect(leftX, thY, contentWidth, thH);

        g2.setFont(new Font("SansSerif", Font.BOLD, 9));
        g2.drawString("Désignation du Médicament", leftX + 8, thY + 15);
        g2.drawString("Qté Commandée", leftX + col1W + 10, thY + 15);
        g2.drawString("P.U. Achat (DH)", leftX + col1W + col2W + 15, thY + 15);
        g2.drawString("Total Estimé (DH)", leftX + col1W + col2W + col3W + 15, thY + 15);

        int rowY = thY + thH;
        int rowH = 19;
        g2.setFont(new Font("SansSerif", Font.PLAIN, 9));

        double totalCalcule = 0.0;
        for (int i = 0; i < lignes.size(); i++) {
            LigneApprovisionnement la = lignes.get(i);
            double st = la.getQuantiteCommandee() * la.getPrixAchatUnitaire();
            totalCalcule += st;

            if (i % 2 == 1) {
                g2.setColor(new Color(248, 250, 252));
                g2.fillRect(leftX, rowY, contentWidth, rowH);
            }

            g2.setColor(new Color(215, 222, 230));
            g2.drawRect(leftX, rowY, contentWidth, rowH);

            g2.setColor(Color.BLACK);
            String desig = la.getNomMedicament() != null ? la.getNomMedicament() : "Médicament #" + la.getIdMedicament();
            if (desig.length() > 36) desig = desig.substring(0, 34) + "..";
            g2.drawString(desig, leftX + 8, rowY + 13);

            g2.drawString(String.valueOf(la.getQuantiteCommandee()), leftX + col1W + 30, rowY + 13);

            g2.drawString(String.format("%.2f", la.getPrixAchatUnitaire()), leftX + col1W + col2W + 25, rowY + 13);

            g2.drawString(String.format("%.2f", st), leftX + col1W + col2W + col3W + 25, rowY + 13);

            rowY += rowH;
        }

        currentY = rowY + 12;

        // ==========================================
        // 5. TOTAL & SIGNATURE
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
        g2.drawString("Montant Total Estimé (DH) :", totalBoxX + 12, currentY + 18);

        g2.setFont(new Font("SansSerif", Font.BOLD, 14));
        g2.setColor(new Color(24, 76, 120));
        double mont = appro.getMontantTotal() > 0 ? appro.getMontantTotal() : totalCalcule;
        g2.drawString(String.format("%.2f DH", mont), totalBoxX + 12, currentY + 38);

        // Montant en toutes lettres
        int wordsX = leftX;
        int wordsY = currentY + 16;
        g2.setFont(new Font("SansSerif", Font.ITALIC, 9));
        g2.setColor(Color.DARK_GRAY);
        g2.drawString("Arrêté le présent bon de commande à la somme estimée de :", wordsX, wordsY);

        g2.setFont(new Font("SansSerif", Font.BOLD | Font.ITALIC, 9));
        g2.setColor(Color.BLACK);
        String lettres = FrenchNumberToWords.convert(mont);
        g2.drawString(lettres, wordsX, wordsY + 15);

        currentY += totalBoxH + 30;

        // Cadre Cachet & Signature
        int stampBoxW = 190;
        int stampBoxH = 75;
        int stampBoxX = leftX + contentWidth - stampBoxW;

        g2.setColor(new Color(210, 215, 225));
        g2.setStroke(new BasicStroke(1.0f));
        g2.drawRect(stampBoxX, currentY, stampBoxW, stampBoxH);

        g2.setFont(new Font("SansSerif", Font.BOLD, 8));
        g2.setColor(new Color(100, 100, 100));
        g2.drawString("SIGNATURE & CACHET PHARMACIE", stampBoxX + 15, currentY + 14);

        return PAGE_EXISTS;
    }
}
