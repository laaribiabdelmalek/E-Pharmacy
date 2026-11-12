package com.epharmacy.util;

import com.epharmacy.model.LigneVente;
import com.epharmacy.model.Vente;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Utilitaire de génération de factures PDF pour les ventes de la pharmacie.
 * <p>
 * Utilise Apache PDFBox pour créer des documents PDF contenant
 * les informations de vente, le détail des lignes et le total.
 * </p>
 *
 * @author E-PHARMACIE
 * @version 1.0
 */
public final class PDFGenerator {

    /** Marge gauche du document */
    private static final float MARGIN_LEFT = 50f;

    /** Marge droite du document */
    private static final float MARGIN_RIGHT = 50f;

    /** Marge haute du document */
    private static final float MARGIN_TOP = 50f;

    /** Hauteur d'une ligne de texte */
    private static final float LINE_HEIGHT = 18f;

    /** Hauteur d'une ligne de tableau */
    private static final float TABLE_ROW_HEIGHT = 20f;

    /** Formateur de date/heure pour la facture */
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Constructeur privé pour empêcher l'instanciation.
     */
    private PDFGenerator() {
        throw new UnsupportedOperationException("Classe utilitaire non instanciable");
    }

    /**
     * Génère une facture PDF pour une vente donnée.
     *
     * @param vente         la vente à facturer
     * @param lignes        la liste des lignes de vente (détail des médicaments)
     * @param cheminFichier le chemin complet du fichier PDF à créer
     * @throws IOException si une erreur survient lors de la création du PDF
     */
    public static void genererFacture(Vente vente, List<LigneVente> lignes,
                                       String cheminFichier) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            float pageWidth = page.getMediaBox().getWidth();
            float pageHeight = page.getMediaBox().getHeight();
            float contentWidth = pageWidth - MARGIN_LEFT - MARGIN_RIGHT;
            float yPosition = pageHeight - MARGIN_TOP;

            try (PDPageContentStream contentStream =
                         new PDPageContentStream(document, page)) {

                // --- En-tete ---
                yPosition = drawHeader(contentStream, yPosition, contentWidth);

                // --- Ligne de separation ---
                yPosition -= 10f;
                drawLine(contentStream, MARGIN_LEFT, yPosition,
                        pageWidth - MARGIN_RIGHT, yPosition);
                yPosition -= 20f;

                // --- Informations de la vente ---
                yPosition = drawVenteInfo(contentStream, vente, yPosition);

                // --- Ligne de separation ---
                yPosition -= 10f;
                drawLine(contentStream, MARGIN_LEFT, yPosition,
                        pageWidth - MARGIN_RIGHT, yPosition);
                yPosition -= 15f;

                // --- Tableau des articles ---
                yPosition = drawTableHeader(contentStream, yPosition, contentWidth);
                yPosition = drawTableRows(contentStream, lignes, yPosition, contentWidth);

                // --- Ligne de separation ---
                yPosition -= 5f;
                drawLine(contentStream, MARGIN_LEFT, yPosition,
                        pageWidth - MARGIN_RIGHT, yPosition);
                yPosition -= 20f;

                // --- Total ---
                yPosition = drawTotal(contentStream, vente, yPosition, contentWidth);

                // --- Pied de page ---
                drawFooter(contentStream, pageWidth);
            }

            document.save(cheminFichier);
        }
    }

    /**
     * Dessine l'en-tête de la facture.
     */
    private static float drawHeader(PDPageContentStream cs, float y,
                                     float contentWidth) throws IOException {
        // Titre principal
        String title = "E-PHARMACIE";
        float titleWidth = PDType1Font.HELVETICA_BOLD.getStringWidth(title) / 1000f * 20f;
        float centerX = MARGIN_LEFT + (contentWidth - titleWidth) / 2f;

        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA_BOLD, 20);
        cs.newLineAtOffset(centerX, y);
        cs.showText(title);
        cs.endText();
        y -= LINE_HEIGHT + 5f;

        // Sous-titre
        String subtitle = "Facture de Vente";
        float subtitleWidth = PDType1Font.HELVETICA.getStringWidth(subtitle) / 1000f * 14f;
        centerX = MARGIN_LEFT + (contentWidth - subtitleWidth) / 2f;

        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA, 14);
        cs.newLineAtOffset(centerX, y);
        cs.showText(subtitle);
        cs.endText();
        y -= LINE_HEIGHT;

        return y;
    }

    /**
     * Dessine les informations de la vente (numéro, date, client, paiement).
     */
    private static float drawVenteInfo(PDPageContentStream cs, Vente vente,
                                        float y) throws IOException {
        float fontSize = 10f;

        // Numero de vente
        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA_BOLD, fontSize);
        cs.newLineAtOffset(MARGIN_LEFT, y);
        cs.showText("N. Vente : ");
        cs.setFont(PDType1Font.HELVETICA, fontSize);
        cs.showText(String.valueOf(vente.getIdVente()));
        cs.endText();
        y -= LINE_HEIGHT;

        // Date de vente
        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA_BOLD, fontSize);
        cs.newLineAtOffset(MARGIN_LEFT, y);
        cs.showText("Date : ");
        cs.setFont(PDType1Font.HELVETICA, fontSize);
        String dateStr = vente.getDateVente() != null
                ? vente.getDateVente().format(DATE_FORMATTER)
                : "N/A";
        cs.showText(dateStr);
        cs.endText();
        y -= LINE_HEIGHT;

        // Client
        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA_BOLD, fontSize);
        cs.newLineAtOffset(MARGIN_LEFT, y);
        cs.showText("Client : ");
        cs.setFont(PDType1Font.HELVETICA, fontSize);
        String clientName = vente.getNomClient() != null
                ? vente.getNomClient()
                : "Client comptoir";
        cs.showText(clientName);
        cs.endText();
        y -= LINE_HEIGHT;

        // Mode de paiement
        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA_BOLD, fontSize);
        cs.newLineAtOffset(MARGIN_LEFT, y);
        cs.showText("Mode de paiement : ");
        cs.setFont(PDType1Font.HELVETICA, fontSize);
        String modePaiement = vente.getModePaiement() != null
                ? vente.getModePaiement()
                : "Especes";
        cs.showText(modePaiement);
        cs.endText();
        y -= LINE_HEIGHT;

        return y;
    }

    /**
     * Dessine l'en-tête du tableau des articles.
     */
    private static float drawTableHeader(PDPageContentStream cs, float y,
                                          float contentWidth) throws IOException {
        float fontSize = 9f;

        // Colonnes : Medicament | N. Lot | Qte | Prix Unit. | Sous-Total
        float[] colWidths = calculateColumnWidths(contentWidth);
        float xPos = MARGIN_LEFT;

        // Fond gris pour l'en-tete
        cs.setNonStrokingColor(0.9f, 0.9f, 0.9f);
        cs.addRect(MARGIN_LEFT, y - TABLE_ROW_HEIGHT + 4f, contentWidth, TABLE_ROW_HEIGHT);
        cs.fill();
        cs.setNonStrokingColor(0f, 0f, 0f);

        String[] safeHeaders = {"Medicament", "N. Lot", "Qte", "Prix Unit.", "Sous-Total"};

        for (int i = 0; i < safeHeaders.length; i++) {
            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA_BOLD, fontSize);
            cs.newLineAtOffset(xPos + 4f, y - TABLE_ROW_HEIGHT + 8f);
            cs.showText(safeHeaders[i]);
            cs.endText();
            xPos += colWidths[i];
        }

        y -= TABLE_ROW_HEIGHT;

        // Ligne sous l'en-tete
        drawLine(cs, MARGIN_LEFT, y + 3f,
                MARGIN_LEFT + contentWidth, y + 3f);

        return y;
    }

    /**
     * Dessine les lignes du tableau des articles.
     */
    private static float drawTableRows(PDPageContentStream cs,
                                        List<LigneVente> lignes, float y,
                                        float contentWidth) throws IOException {
        float fontSize = 9f;
        float[] colWidths = calculateColumnWidths(contentWidth);

        for (int row = 0; row < lignes.size(); row++) {
            LigneVente ligne = lignes.get(row);

            // Fond alterne
            if (row % 2 == 0) {
                cs.setNonStrokingColor(0.96f, 0.96f, 0.96f);
                cs.addRect(MARGIN_LEFT, y - TABLE_ROW_HEIGHT + 4f,
                        contentWidth, TABLE_ROW_HEIGHT);
                cs.fill();
                cs.setNonStrokingColor(0f, 0f, 0f);
            }

            float xPos = MARGIN_LEFT;
            String[] values = {
                    safe(ligne.getNomMedicament()),
                    safe(ligne.getNumeroLot()),
                    String.valueOf(ligne.getQuantite()),
                    String.format("%.2f", ligne.getPrixUnitaireVente()),
                    String.format("%.2f", ligne.getSousTotal())
            };

            for (int i = 0; i < values.length; i++) {
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA, fontSize);
                cs.newLineAtOffset(xPos + 4f, y - TABLE_ROW_HEIGHT + 8f);
                cs.showText(values[i]);
                cs.endText();
                xPos += colWidths[i];
            }

            y -= TABLE_ROW_HEIGHT;
        }

        return y;
    }

    /**
     * Dessine le montant total de la vente.
     */
    private static float drawTotal(PDPageContentStream cs, Vente vente,
                                    float y, float contentWidth) throws IOException {
        String totalText = "TOTAL : " + String.format("%.2f DH", vente.getMontantTotal());
        float totalWidth = PDType1Font.HELVETICA_BOLD.getStringWidth(totalText) / 1000f * 14f;

        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA_BOLD, 14);
        cs.newLineAtOffset(MARGIN_LEFT + contentWidth - totalWidth, y);
        cs.showText(totalText);
        cs.endText();

        y -= LINE_HEIGHT * 2;
        return y;
    }

    /**
     * Dessine le pied de page de la facture.
     */
    private static void drawFooter(PDPageContentStream cs,
                                    float pageWidth) throws IOException {
        float footerY = 50f;

        String footerText = "Merci pour votre confiance";
        float textWidth = PDType1Font.HELVETICA_OBLIQUE.getStringWidth(footerText) / 1000f * 10f;
        float centerX = (pageWidth - textWidth) / 2f;

        // Ligne de separation
        drawLine(cs, MARGIN_LEFT, footerY + 15f,
                pageWidth - MARGIN_RIGHT, footerY + 15f);

        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA_OBLIQUE, 10);
        cs.newLineAtOffset(centerX, footerY);
        cs.showText(footerText);
        cs.endText();
    }

    /**
     * Dessine une ligne horizontale.
     */
    private static void drawLine(PDPageContentStream cs, float x1, float y1,
                                  float x2, float y2) throws IOException {
        cs.setLineWidth(0.5f);
        cs.moveTo(x1, y1);
        cs.lineTo(x2, y2);
        cs.stroke();
    }

    /**
     * Calcule les largeurs des colonnes du tableau.
     *
     * @param totalWidth la largeur totale disponible
     * @return un tableau des largeurs de colonnes
     */
    private static float[] calculateColumnWidths(float totalWidth) {
        // Medicament (35%), N. Lot (20%), Qte (10%), Prix Unit. (17.5%), Sous-Total (17.5%)
        return new float[]{
                totalWidth * 0.35f,
                totalWidth * 0.20f,
                totalWidth * 0.10f,
                totalWidth * 0.175f,
                totalWidth * 0.175f
        };
    }

    /**
     * Retourne une chaine sure pour l'affichage PDF (jamais null).
     *
     * @param value la valeur a securiser
     * @return la valeur ou "N/A" si null/vide
     */
    private static String safe(String value) {
        return (value == null || value.trim().isEmpty()) ? "N/A" : value.trim();
    }
}
