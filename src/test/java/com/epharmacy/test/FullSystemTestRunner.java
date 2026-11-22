package com.epharmacy.test;

import com.epharmacy.config.DBConnection;
import com.epharmacy.dao.*;
import com.epharmacy.model.*;
import com.epharmacy.print.*;
import com.epharmacy.service.*;
import com.epharmacy.util.*;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Suite de tests d'intégration, de services et d'assurance qualité complète pour l'application E-PHARMACIE.
 * <p>
 * Exécute un plan de vérification programmatique complet avec reporting [PASS] / [FAIL] / [SKIP] :
 * <ul>
 *     <li>Suite 1 : Utilitaires, conversions en toutes lettres, hachage BCrypt, validation et thèmes</li>
 *     <li>Suite 2 : Moteur d'impression graphique headless (Ticket thermique 80mm, Facture A4, Bon de commande)</li>
 *     <li>Suite 3 : Connectivité JDBC, encodage UTF-8 (accents français) et intégrité des 10 tables du schéma</li>
 *     <li>Suite 4 : Authentification, sécurité des rôles (Admin/Pharmacien/Employé) et session</li>
 *     <li>Suite 5 : Opérations CRUD unifiées, contraintes d'intégrité et gestion des exceptions</li>
 *     <li>Suite 6 : Workflow d'approvisionnement en 2 étapes (Commande 'En attente' -> Réception 'Reçu' avec lots)</li>
 *     <li>Suite 7 : Ventes, déduction FEFO (First Expired, First Out), transactions atomiques et ventes à crédit</li>
 *     <li>Suite 8 : KPIs du Tableau de bord, alertes de rupture et lots proches de péremption</li>
 * </ul>
 * Tous les enregistrements créés lors des tests réels sont automatiquement nettoyés pour préserver l'intégrité des données.
 *
 * @author E-PHARMACIE - QA Automation Team
 * @version 2.0
 */
public class FullSystemTestRunner {

    // Compteurs de résultats
    private static int totalTests = 0;
    private static int passedTests = 0;
    private static int failedTests = 0;
    private static int skippedTests = 0;

    private static boolean dbAvailable = false;

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("  E-PHARMACIE — Automated System Integration & Quality Assurance Suite");
        System.out.println("  Java Version : " + System.getProperty("java.version") + " | OS : " + System.getProperty("os.name"));
        System.out.println("================================================================================\n");

        // 1. Détection de la connectivité MySQL
        detectDatabaseAvailability();

        // 2. Exécution des différentes suites
        runSuite1UtilitiesAndSecurity();
        runSuite2PrintEnginesAndGraphics();
        runSuite3DatabaseAndUTF8();
        runSuite4AuthenticationAndRoles();
        runSuite5CRUDAndReferentialIntegrity();
        runSuite6TwoStepProcurementWorkflow();
        runSuite7SalesAndFEFOStockManagement();
        runSuite8DashboardKPIsAndAlerts();

        // 3. Rapport final
        printFinalReport();
    }

    // ==========================================================================
    // 1. DÉTECTION BASE DE DONNÉES
    // ==========================================================================
    private static void detectDatabaseAvailability() {
        System.out.println("[INIT] Vérification de la connectivité à MySQL (localhost:3306/e_pharmacie)...");
        try {
            Connection conn = DBConnection.getInstance().getConnection();
            if (conn != null && !conn.isClosed()) {
                dbAvailable = true;
                System.out.println("  -> [OK] Connexion MySQL établie avec succès.");
            }
        } catch (Exception e) {
            dbAvailable = false;
            System.out.println("  -> [WARN] Serveur MySQL non joignable sur localhost:3306 (" + e.getMessage() + ").");
            System.out.println("     Les tests dépendants de la base seront marqués [SKIP], les tests logiques/graphiques seront exécutés.\n");
        }
    }

    // ==========================================================================
    // SUITE 1 : Utilitaires, Conversions en toutes lettres, Hachage & Thèmes
    // ==========================================================================
    private static void runSuite1UtilitiesAndSecurity() {
        printHeader("SUITE 1 : Utilitaires, Conversion en toutes lettres, BCrypt & Thèmes");

        // 1.1 FrenchNumberToWords
        try {
            String w0 = FrenchNumberToWords.convert(0);
            assertCondition("FrenchNumberToWords(0)", w0.equalsIgnoreCase("Zéro Dirham"));

            String w1 = FrenchNumberToWords.convert(1.0);
            assertCondition("FrenchNumberToWords(1.0)", w1.equalsIgnoreCase("Un Dirham"));

            String w150 = FrenchNumberToWords.convert(150.50);
            assertCondition("FrenchNumberToWords(150.50)",
                    w150.toLowerCase().contains("cent cinquante") && w150.toLowerCase().contains("cinquante centimes"));

            String w71 = FrenchNumberToWords.convert(71.0);
            assertCondition("FrenchNumberToWords(71.0) - soixante et onze",
                    w71.toLowerCase().contains("soixante et onze"));

            String w80 = FrenchNumberToWords.convert(80.0);
            assertCondition("FrenchNumberToWords(80.0) - quatre-vingts",
                    w80.toLowerCase().contains("quatre-vingts"));

            String w1250 = FrenchNumberToWords.convert(1250.75);
            assertCondition("FrenchNumberToWords(1250.75) - milliers et centimes",
                    w1250.toLowerCase().contains("mille deux cent cinquante") && w1250.toLowerCase().contains("centimes"));
        } catch (Exception e) {
            fail("FrenchNumberToWords conversion error: " + e.getMessage());
        }

        // 1.2 PasswordUtils (BCrypt)
        try {
            String rawPass = "SecretPharmacie2026!";
            String hash = PasswordUtils.hashPassword(rawPass);
            assertCondition("BCrypt hashPassword non vide et format standard ($2a$)",
                    hash != null && hash.startsWith("$2a$") && hash.length() >= 59);

            boolean matchOk = PasswordUtils.checkPassword(rawPass, hash);
            assertCondition("BCrypt checkPassword - mot de passe correct", matchOk);

            boolean matchFail = PasswordUtils.checkPassword("MauvaisMotDePasse", hash);
            assertCondition("BCrypt checkPassword - rejet du mauvais mot de passe", !matchFail);

            boolean emptyHandled = false;
            try {
                PasswordUtils.hashPassword("");
            } catch (IllegalArgumentException ex) {
                emptyHandled = true;
            }
            assertCondition("BCrypt rejet mot de passe vide", emptyHandled);
        } catch (Exception e) {
            fail("PasswordUtils error: " + e.getMessage());
        }

        // 1.3 ValidationUtils
        try {
            assertCondition("ValidationUtils.isValidEmail (valide)", ValidationUtils.isValidEmail("contact@pharmacie.ma"));
            assertCondition("ValidationUtils.isValidEmail (invalide)", !ValidationUtils.isValidEmail("contact_invalid_email"));

            assertCondition("ValidationUtils.isValidPhone (06...)", ValidationUtils.isValidPhone("0622334455"));
            assertCondition("ValidationUtils.isValidPhone (+212...)", ValidationUtils.isValidPhone("+212622334455"));
            assertCondition("ValidationUtils.isValidPhone (invalide)", !ValidationUtils.isValidPhone("12345"));

            assertCondition("ValidationUtils.isPositiveNumber (150.75)", ValidationUtils.isPositiveNumber("150.75"));
            assertCondition("ValidationUtils.isPositiveNumber (-10)", !ValidationUtils.isPositiveNumber("-10"));
            assertCondition("ValidationUtils.isPositiveInteger (100)", ValidationUtils.isPositiveInteger("100"));
            assertCondition("ValidationUtils.isPositiveInteger (0)", !ValidationUtils.isPositiveInteger("0"));

            String formattedPrice = ValidationUtils.formatPrice(45.5);
            assertCondition("ValidationUtils.formatPrice", formattedPrice.contains("45.50") && formattedPrice.contains("DH"));
        } catch (Exception e) {
            fail("ValidationUtils error: " + e.getMessage());
        }

        // 1.4 ThemeManager
        try {
            boolean systemDark = ThemeManager.isSystemDarkMode();
            pass("ThemeManager.isSystemDarkMode() interrogation réussie (retourne: " + systemDark + ")");

            ThemeManager.setTheme("CLAIR");
            assertCondition("ThemeManager mode CLAIR actif", !ThemeManager.isDarkMode() && "CLAIR".equals(ThemeManager.getCurrentThemeMode()));

            ThemeManager.setTheme("SOMBRE");
            assertCondition("ThemeManager mode SOMBRE actif", ThemeManager.isDarkMode() && "SOMBRE".equals(ThemeManager.getCurrentThemeMode()));

            final boolean[] listenerTriggered = {false};
            Runnable listener = () -> listenerTriggered[0] = true;
            ThemeManager.addThemeChangeListener(listener);
            ThemeManager.toggleTheme();
            assertCondition("ThemeManager notification des listeners de thème", listenerTriggered[0]);
            ThemeManager.removeThemeChangeListener(listener);

            // Remise au clair par défaut
            ThemeManager.setTheme("CLAIR");
        } catch (Exception e) {
            fail("ThemeManager error: " + e.getMessage());
        }
    }

    // ==========================================================================
    // SUITE 2 : Moteur d'impression Graphique Headless (Ticket, Facture, Bon de Commande)
    // ==========================================================================
    private static void runSuite2PrintEnginesAndGraphics() {
        printHeader("SUITE 2 : Moteur d'impression Graphique Headless & Dimensions");

        // 2.1 PageFormat Dimensions
        try {
            PageFormat ticketPf = PrintPreviewDialog.createTicketPageFormat();
            assertCondition("Dimensions Ticket thermique (largeur = 226 pt)", Math.abs(ticketPf.getWidth() - 226.0) < 1.0);

            PageFormat a4Pf = PrintPreviewDialog.createA4PageFormat();
            assertCondition("Dimensions Facture A4 (largeur = 595 pt, hauteur = 842 pt)",
                    Math.abs(a4Pf.getWidth() - 595.0) < 1.0 && Math.abs(a4Pf.getHeight() - 842.0) < 1.0);
        } catch (Exception e) {
            fail("PageFormat creation error: " + e.getMessage());
        }

        // 2.2 TicketPrintable Rendering
        try {
            ParametresSociete params = new ParametresSociete();
            params.setNomPharmacie("Pharmacie E-Pharmacie Test");
            params.setAdresse("Avenue Mohammed V, Guéliz");
            params.setVille("Marrakech");
            params.setTelephone("05 24 43 00 00");
            params.setIce("001524896000045");
            params.setMessageTicket("Prompt rétablissement !");

            Vente vente = new Vente();
            vente.setIdVente(999);
            vente.setDateVente(LocalDateTime.now());
            vente.setMontantTotal(180.00);
            vente.setModePaiement("ESPECES");
            vente.setNomClient("Client Comptoir");
            vente.setNomUtilisateur("Dr. Amine");

            List<LigneVente> lignes = new ArrayList<>();
            LigneVente lv1 = new LigneVente(1, 999, 10, 2, 50.00, 100.00);
            lv1.setNomMedicament("Doliprane 1000mg");
            lignes.add(lv1);
            LigneVente lv2 = new LigneVente(2, 999, 11, 1, 80.00, 80.00);
            lv2.setNomMedicament("Amoxicilline 500mg");
            lignes.add(lv2);

            TicketPrintable ticketPrintable = new TicketPrintable(vente, lignes, params, "Dr. Amine");
            BufferedImage img = new BufferedImage(300, 800, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = img.createGraphics();

            PageFormat pf = PrintPreviewDialog.createTicketPageFormat();
            int page0 = ticketPrintable.print(g2, pf, 0);
            int page1 = ticketPrintable.print(g2, pf, 1);
            g2.dispose();

            assertCondition("TicketPrintable rendu page 0 (PAGE_EXISTS)", page0 == Printable.PAGE_EXISTS);
            assertCondition("TicketPrintable fin de document page 1 (NO_SUCH_PAGE)", page1 == Printable.NO_SUCH_PAGE);
        } catch (Exception e) {
            fail("TicketPrintable error: " + e.getMessage());
        }

        // 2.3 FacturePrintable Rendering
        try {
            ParametresSociete params = new ParametresSociete();
            params.setNomPharmacie("Pharmacie E-Pharmacie Test");
            params.setIce("001524896000045");
            params.setIdentifiantFiscal("45896321");
            params.setRc("RC 14520");
            params.setPatente("45218963");

            Vente vente = new Vente();
            vente.setIdVente(1001);
            vente.setDateVente(LocalDateTime.now());
            vente.setMontantTotal(250.00);
            vente.setModePaiement("CARTE");

            Client client = new Client(50, "El Amrani", "Karim", "0661122334", "karim@email.ma", "Guéliz, Marrakech", null, 0.0);

            List<LigneVente> lignes = new ArrayList<>();
            LigneVente lv = new LigneVente(1, 1001, 15, 5, 50.0, 250.0);
            lv.setNomMedicament("Augmentin");
            lv.setDosage("1g Adulte");
            lignes.add(lv);

            FacturePrintable facturePrintable = new FacturePrintable(vente, lignes, params, client);
            BufferedImage img = new BufferedImage(595, 842, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = img.createGraphics();

            PageFormat pf = PrintPreviewDialog.createA4PageFormat();
            int res0 = facturePrintable.print(g2, pf, 0);
            int res1 = facturePrintable.print(g2, pf, 1);
            g2.dispose();

            assertCondition("FacturePrintable rendu page 0 (PAGE_EXISTS)", res0 == Printable.PAGE_EXISTS);
            assertCondition("FacturePrintable page 1 (NO_SUCH_PAGE)", res1 == Printable.NO_SUCH_PAGE);
        } catch (Exception e) {
            fail("FacturePrintable error: " + e.getMessage());
        }

        // 2.4 BonCommandePrintable Rendering
        try {
            ParametresSociete params = new ParametresSociete();
            params.setNomPharmacie("Pharmacie E-Pharmacie Test");
            params.setTelephone("0524000000");

            Approvisionnement appro = new Approvisionnement();
            appro.setIdApprovisionnement(800);
            appro.setDateApprovisionnement(LocalDateTime.now());
            appro.setStatut("En attente");
            appro.setMontantTotal(3500.00);

            Fournisseur four = new Fournisseur();
            four.setIdFournisseur(1);
            four.setRaisonSociale("Sothema Laboratoires");
            four.setIce("001899999000012");
            four.setTelephone("0522112233");
            four.setVille("Casablanca");

            List<LigneApprovisionnement> lignes = new ArrayList<>();
            LigneApprovisionnement la = new LigneApprovisionnement();
            la.setIdMedicament(1);
            la.setNomMedicament("Paracétamol 500mg");
            la.setQuantiteCommandee(100);
            la.setPrixAchatUnitaire(35.0);
            lignes.add(la);

            BonCommandePrintable bcPrintable = new BonCommandePrintable(appro, lignes, params, four);
            BufferedImage img = new BufferedImage(595, 842, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = img.createGraphics();

            PageFormat pf = PrintPreviewDialog.createA4PageFormat();
            int res0 = bcPrintable.print(g2, pf, 0);
            g2.dispose();

            assertCondition("BonCommandePrintable rendu page 0 (PAGE_EXISTS)", res0 == Printable.PAGE_EXISTS);
        } catch (Exception e) {
            fail("BonCommandePrintable error: " + e.getMessage());
        }
    }

    // ==========================================================================
    // SUITE 3 : Connectivité JDBC, Encodage UTF-8 et Schéma des 10 Tables
    // ==========================================================================
    private static void runSuite3DatabaseAndUTF8() {
        printHeader("SUITE 3 : Connectivité JDBC, Encodage UTF-8 & Schéma");

        if (!dbAvailable) {
            skipSuite("Base MySQL locale non disponible");
            return;
        }

        try {
            Connection conn = DBConnection.getInstance().getConnection();
            assertCondition("Singleton DBConnection non nul", conn != null && !conn.isClosed());

            // Vérification des 10 tables
            String[] tablesAttendues = {
                    "utilisateur", "client", "fournisseur", "medicament",
                    "approvisionnement", "ligne_approvisionnement", "lot",
                    "vente", "ligne_vente", "parametres_societe"
            };

            for (String tbl : tablesAttendues) {
                PreparedStatement ps = conn.prepareStatement("SELECT 1 FROM " + tbl + " LIMIT 1");
                boolean ok = false;
                try {
                    ResultSet rs = ps.executeQuery();
                    ok = true;
                    rs.close();
                } catch (SQLException ignored) {
                } finally {
                    ps.close();
                }
                assertCondition("Existence de la table : " + tbl, ok);
            }

            // Test d'encodage UTF-8 avec caractères accentués français
            String testFrenchText = "Hôpital & Mère-Enfant : Célestène, Éphédrine, Guéliz Marrakech — Çà et là";
            PreparedStatement psInsert = conn.prepareStatement(
                    "INSERT INTO client (nom, prenom, telephone, email, adresse, credit) VALUES (?, ?, ?, ?, ?, ?)",
                    PreparedStatement.RETURN_GENERATED_KEYS);
            psInsert.setString(1, "TestAccents");
            psInsert.setString(2, "François-Éléonore");
            psInsert.setString(3, "0600000000");
            psInsert.setString(4, "test.accents@epharmacie.ma");
            psInsert.setString(5, testFrenchText);
            psInsert.setDouble(6, 0.0);
            psInsert.executeUpdate();

            ResultSet rsGen = psInsert.getGeneratedKeys();
            int testClientId = -1;
            if (rsGen.next()) testClientId = rsGen.getInt(1);
            rsGen.close();
            psInsert.close();

            PreparedStatement psRead = conn.prepareStatement("SELECT adresse FROM client WHERE id_client = ?");
            psRead.setInt(1, testClientId);
            ResultSet rsRead = psRead.executeQuery();
            String lue = "";
            if (rsRead.next()) lue = rsRead.getString("adresse");
            rsRead.close();
            psRead.close();

            assertCondition("Encodage UTF-8 sans corruption '?' pour les accents français", testFrenchText.equals(lue));

            // Nettoyage
            if (testClientId > 0) {
                PreparedStatement psDel = conn.prepareStatement("DELETE FROM client WHERE id_client = ?");
                psDel.setInt(1, testClientId);
                psDel.executeUpdate();
                psDel.close();
            }

        } catch (Exception e) {
            fail("Erreur Suite 3 Database & UTF-8: " + e.getMessage());
        }
    }

    // ==========================================================================
    // SUITE 4 : Authentification, Sécurité des rôles & Session
    // ==========================================================================
    private static void runSuite4AuthenticationAndRoles() {
        printHeader("SUITE 4 : Authentification, Sécurité des Rôles & Session");

        if (!dbAvailable) {
            skipSuite("Base MySQL locale non disponible");
            return;
        }

        AuthService authService = new AuthService();
        UtilisateurDAO userDAO = new UtilisateurDAO();
        int testUserId = -1;

        try {
            // Création d'un utilisateur de test actif
            Utilisateur testUser = new Utilisateur();
            testUser.setNom("Benani");
            testUser.setPrenom("Youssef");
            testUser.setLogin("test_youssef_" + System.currentTimeMillis() % 10000);
            testUser.setMotDePasseHash(PasswordUtils.hashPassword("P@ssword2026"));
            testUser.setRole("PHARMACIEN");
            testUser.setStatut("ACTIF");

            boolean inserted = userDAO.insert(testUser);
            testUserId = testUser.getIdUtilisateur();
            assertCondition("Création d'un utilisateur de test", inserted && testUserId > 0);

            // 4.1 Authentification réussie
            Utilisateur connecte = authService.authentifier(testUser.getLogin(), "P@ssword2026");
            assertCondition("AuthService - Authentification valide", connecte != null && connecte.getIdUtilisateur() == testUserId);
            assertCondition("SessionManager - Utilisateur connecté enregistré",
                    SessionManager.getInstance().isConnecte()
                            && SessionManager.getInstance().getUtilisateurConnecte().getIdUtilisateur() == testUserId);
            assertCondition("SessionManager - Rôle non-admin bien identifié", !SessionManager.getInstance().isAdmin());

            // 4.2 Déconnexion
            authService.deconnecter();
            assertCondition("AuthService - Déconnexion vide la session", !SessionManager.getInstance().isConnecte());

            // 4.3 Mauvais mot de passe
            Utilisateur badPass = authService.authentifier(testUser.getLogin(), "FauxMotDePasse");
            assertCondition("AuthService - Rejet si mot de passe erroné", badPass == null);

            // 4.4 Utilisateur Inactif
            testUser.setStatut("INACTIF");
            userDAO.update(testUser);
            Utilisateur inactiveUser = authService.authentifier(testUser.getLogin(), "P@ssword2026");
            assertCondition("AuthService - Rejet d'un utilisateur INACTIF", inactiveUser == null);

        } catch (Exception e) {
            fail("Erreur Suite 4 Authentification: " + e.getMessage());
        } finally {
            if (testUserId > 0) {
                userDAO.delete(testUserId);
            }
            SessionManager.getInstance().fermerSession();
        }
    }

    // ==========================================================================
    // SUITE 5 : Opérations CRUD unifiées, Validations & Intégrité Référentielle
    // ==========================================================================
    private static void runSuite5CRUDAndReferentialIntegrity() {
        printHeader("SUITE 5 : CRUD Unifié, Validations & Intégrité Référentielle");

        if (!dbAvailable) {
            skipSuite("Base MySQL locale non disponible");
            return;
        }

        MedicamentDAO medDAO = new MedicamentDAO();
        ClientDAO clientDAO = new ClientDAO();
        FournisseurDAO fourDAO = new FournisseurDAO();
        ParametresSocieteDAO paramDAO = new ParametresSocieteDAO();

        int medId = -1;
        int clientId = -1;
        int fourId = -1;

        try {
            // 5.1 CRUD Médicament
            Medicament m = new Medicament();
            m.setNomCommercial("Vitamine C Test " + System.currentTimeMillis() % 1000);
            m.setDci("Acide ascorbique");
            m.setDosage("1000mg");
            m.setFormePharmaceutique("COMPRIME");
            m.setLaboratoire("Laprophan");
            m.setPrixVentePublic(25.50);
            m.setSeuilMinimal(5);
            m.setStatut("COMMERCIALISE");
            m.setQuantiteDisponible(20);

            boolean mOk = medDAO.insert(m);
            medId = m.getIdMedicament();
            assertCondition("MedicamentDAO - Insertion", mOk && medId > 0);

            Medicament recup = medDAO.findById(medId);
            assertCondition("MedicamentDAO - Lecture par ID", recup != null && recup.getNomCommercial().equals(m.getNomCommercial()));

            recup.setPrixVentePublic(29.90);
            medDAO.update(recup);
            Medicament recup2 = medDAO.findById(medId);
            assertCondition("MedicamentDAO - Modification du prix", Math.abs(recup2.getPrixVentePublic() - 29.90) < 0.01);

            medDAO.updateQuantite(medId, 35);
            assertCondition("MedicamentDAO - Mise à jour de quantité", medDAO.findById(medId).getQuantiteDisponible() == 35);

            medDAO.updateStatut(medId, "EN_RUPTURE");
            assertCondition("MedicamentDAO - Mise à jour de statut", "EN_RUPTURE".equals(medDAO.findById(medId).getStatut()));

            // 5.2 CRUD Client
            Client c = new Client();
            c.setNom("Benjelloun");
            c.setPrenom("Salma");
            c.setTelephone("0665443322");
            c.setEmail("salma@test.ma");
            c.setAdresse("Rabat Centre");
            c.setCredit(100.0);

            boolean cOk = clientDAO.insert(c);
            clientId = c.getIdClient();
            assertCondition("ClientDAO - Insertion", cOk && clientId > 0);

            clientDAO.updateCredit(clientId, 250.0);
            assertCondition("ClientDAO - Mise à jour crédit", Math.abs(clientDAO.findById(clientId).getCredit() - 250.0) < 0.01);

            // 5.3 CRUD Fournisseur
            Fournisseur f = new Fournisseur();
            f.setRaisonSociale("Fournisseur QA Test " + System.currentTimeMillis() % 1000);
            f.setIce("00998877" + (System.currentTimeMillis() % 10000));
            f.setTelephone("0522000000");
            f.setVille("Tanger");
            f.setStatut("ACTIF");

            boolean fOk = fourDAO.insert(f);
            fourId = f.getIdFournisseur();
            assertCondition("FournisseurDAO - Insertion", fOk && fourId > 0);

            // 5.4 ParametresSocieteDAO
            ParametresSociete p = paramDAO.find();
            assertCondition("ParametresSocieteDAO - Lecture ID 1", p != null && p.getId() == 1);

            String oldTheme = p.getThemeApp();
            paramDAO.updateTheme("SOMBRE");
            assertCondition("ParametresSocieteDAO - updateTheme SOMBRE", "SOMBRE".equalsIgnoreCase(paramDAO.getTheme()));
            paramDAO.updateTheme(oldTheme != null ? oldTheme : "CLAIR");

        } catch (Exception e) {
            fail("Erreur Suite 5 CRUD: " + e.getMessage());
        } finally {
            if (medId > 0) medDAO.delete(medId);
            if (clientId > 0) clientDAO.delete(clientId);
            if (fourId > 0) fourDAO.delete(fourId);
        }
    }

    // ==========================================================================
    // SUITE 6 : Approvisionnements en 2 étapes & Lots
    // ==========================================================================
    private static void runSuite6TwoStepProcurementWorkflow() {
        printHeader("SUITE 6 : Workflow Approvisionnement en 2 étapes & Traçabilité");

        if (!dbAvailable) {
            skipSuite("Base MySQL locale non disponible");
            return;
        }

        ApprovisionnementService approService = new ApprovisionnementService();
        FournisseurDAO fourDAO = new FournisseurDAO();
        MedicamentDAO medDAO = new MedicamentDAO();
        LotDAO lotDAO = new LotDAO();

        int fourId = -1;
        int medId = -1;
        int approId = -1;

        try {
            // Création préalable d'un fournisseur et d'un médicament test
            Fournisseur four = new Fournisseur();
            four.setRaisonSociale("Laboratoire Appro Test " + System.currentTimeMillis() % 1000);
            four.setIce("00112233" + (System.currentTimeMillis() % 10000));
            four.setStatut("ACTIF");
            fourDAO.insert(four);
            fourId = four.getIdFournisseur();

            Medicament med = new Medicament();
            med.setNomCommercial("Antibiotique Test " + System.currentTimeMillis() % 1000);
            med.setPrixVentePublic(60.0);
            med.setQuantiteDisponible(10); // Stock initial = 10
            med.setStatut("COMMERCIALISE");
            medDAO.insert(med);
            medId = med.getIdMedicament();

            // ÉTAPE 1 : Création de la commande fournisseur (statut 'En attente')
            Approvisionnement appro = new Approvisionnement();
            appro.setIdFournisseur(fourId);
            appro.setIdUtilisateur(1); // Admin
            appro.setDateApprovisionnement(LocalDateTime.now());

            List<LigneApprovisionnement> lignesCmd = new ArrayList<>();
            LigneApprovisionnement la = new LigneApprovisionnement();
            la.setIdMedicament(medId);
            la.setQuantiteCommandee(50);
            la.setPrixAchatUnitaire(30.0);
            lignesCmd.add(la);

            boolean cmdOk = approService.creerCommande(appro, lignesCmd);
            approId = appro.getIdApprovisionnement();

            assertCondition("Étape 1 - Création de commande réussie", cmdOk && approId > 0);

            Approvisionnement approEnBase = approService.getParId(approId);
            assertCondition("Étape 1 - Statut 'En attente'", "En attente".equalsIgnoreCase(approEnBase.getStatut()));

            // Vérification : le stock n'a PAS augmenté à l'étape 1
            Medicament medCheck = medDAO.findById(medId);
            assertCondition("Étape 1 - Stock non incrémenté lors de la commande (stock = 10)", medCheck.getQuantiteDisponible() == 10);

            // Vérification : 0 lot créé
            List<Lot> lotsCheck = lotDAO.findByMedicament(medId);
            assertCondition("Étape 1 - Aucun lot créé avant la réception", lotsCheck.isEmpty());

            // ÉTAPE 2 : Réception avec quantité ajustée (ex: 45 reçus au lieu de 50) et création du Lot
            List<LigneApprovisionnement> lignesRecep = new ArrayList<>();
            LigneApprovisionnement laRecep = new LigneApprovisionnement();
            laRecep.setIdMedicament(medId);
            laRecep.setQuantiteCommandee(50);
            laRecep.setQuantiteRecue(45); // Ajustement
            laRecep.setPrixAchatUnitaire(30.0);
            lignesRecep.add(laRecep);

            List<Lot> lotsRecep = new ArrayList<>();
            Lot lotRecep = new Lot();
            lotRecep.setNumeroLot("LOT-TEST-QA-" + System.currentTimeMillis() % 1000);
            lotRecep.setDateExpiration(LocalDate.now().plusMonths(18));
            lotsRecep.add(lotRecep);

            boolean recepOk = approService.validerReception(approId, lignesRecep, lotsRecep);
            assertCondition("Étape 2 - Validation de la réception", recepOk);

            Approvisionnement approRecu = approService.getParId(approId);
            assertCondition("Étape 2 - Statut mis à jour à 'Reçu'", "Reçu".equalsIgnoreCase(approRecu.getStatut()));
            assertCondition("Étape 2 - Montant recalculé (45 x 30 = 1350 DH)", Math.abs(approRecu.getMontantTotal() - 1350.0) < 0.01);

            // Vérification de l'incrémentation exacte du stock (10 + 45 = 55)
            Medicament medApresRecep = medDAO.findById(medId);
            assertCondition("Étape 2 - Stock incrémenté de la quantité reçue (10 + 45 = 55)", medApresRecep.getQuantiteDisponible() == 55);

            // Vérification du lot créé en base
            List<Lot> lotsCrees = lotDAO.findByMedicament(medId);
            assertCondition("Étape 2 - Lot créé avec quantité restante égale à 45",
                    !lotsCrees.isEmpty() && lotsCrees.get(0).getQuantiteRestante() == 45);

        } catch (Exception e) {
            fail("Erreur Suite 6 Approvisionnement: " + e.getMessage());
        } finally {
            // Nettoyage complet
            cleanupApprovisionnementTestData(approId, medId, fourId);
        }
    }

    // ==========================================================================
    // SUITE 7 : Ventes, Stratégie FEFO & Transactions Atomiques
    // ==========================================================================
    private static void runSuite7SalesAndFEFOStockManagement() {
        printHeader("SUITE 7 : Ventes, Algorithme FEFO, Rollback & Crédit");

        if (!dbAvailable) {
            skipSuite("Base MySQL locale non disponible");
            return;
        }

        StockService stockService = new StockService();
        VenteService venteService = new VenteService();
        MedicamentDAO medDAO = new MedicamentDAO();
        LotDAO lotDAO = new LotDAO();
        ClientDAO clientDAO = new ClientDAO();

        int medId = -1;
        int lotId1 = -1;
        int lotId2 = -1;
        int clientId = -1;
        int venteId = -1;

        try {
            // Création d'un médicament test
            Medicament med = new Medicament();
            med.setNomCommercial("Sirop Toux FEFO " + System.currentTimeMillis() % 1000);
            med.setPrixVentePublic(40.0);
            med.setQuantiteDisponible(25);
            med.setStatut("COMMERCIALISE");
            medDAO.insert(med);
            medId = med.getIdMedicament();

            // Création préalable d'un appro fictif pour lier les lots
            int fakeApproId = createFakeApprovisionnement(medId);

            // Lot 1 : expire dans 30 jours, 5 unités disponibles
            Lot lot1 = new Lot();
            lot1.setIdLigneApprovisionnement(fakeApproId);
            lot1.setNumeroLot("LOT-FEFO-PROCHE");
            lot1.setDateExpiration(LocalDate.now().plusDays(30));
            lot1.setQuantiteInitiale(5);
            lot1.setQuantiteRestante(5);
            lotDAO.insert(lot1);
            lotId1 = lot1.getIdLot();

            // Lot 2 : expire dans 365 jours, 20 unités disponibles
            Lot lot2 = new Lot();
            lot2.setIdLigneApprovisionnement(fakeApproId);
            lot2.setNumeroLot("LOT-FEFO-LOINTAIN");
            lot2.setDateExpiration(LocalDate.now().plusDays(365));
            lot2.setQuantiteInitiale(20);
            lot2.setQuantiteRestante(20);
            lotDAO.insert(lot2);
            lotId2 = lot2.getIdLot();

            // 7.1 Test de l'algorithme FEFO : demande de 8 unités
            // Doit consommer : 5 du Lot 1 (qui expire d'abord) + 3 du Lot 2
            List<LigneVente> fefoLines = stockService.decrementerStock(medId, 8, 40.0);
            assertCondition("FEFO - Découpage en 2 lignes de lots distincts", fefoLines.size() == 2);
            assertCondition("FEFO - Lot 1 consommé en premier pour 5 unités",
                    fefoLines.get(0).getIdLot() == lotId1 && fefoLines.get(0).getQuantite() == 5);
            assertCondition("FEFO - Lot 2 consommé pour le reliquat de 3 unités",
                    fefoLines.get(1).getIdLot() == lotId2 && fefoLines.get(1).getQuantite() == 3);

            // 7.2 Enregistrement transactionnel de la vente
            Client c = new Client();
            c.setNom("Tazi");
            c.setPrenom("Mehdi");
            c.setTelephone("0611223344");
            c.setCredit(0.0);
            clientDAO.insert(c);
            clientId = c.getIdClient();

            Vente v = new Vente();
            v.setIdUtilisateur(1);
            v.setIdClient(clientId);
            v.setDateVente(LocalDateTime.now());
            v.setModePaiement("CREDIT");
            v.setStatut("VALIDEE");

            boolean venteOk = venteService.enregistrerVente(v, fefoLines);
            venteId = v.getIdVente();
            assertCondition("VenteService - Transaction de vente enregistrée", venteOk && venteId > 0);

            // Vérification en base des stocks de lots
            Lot lot1Post = lotDAO.findById(lotId1);
            Lot lot2Post = lotDAO.findById(lotId2);
            assertCondition("FEFO en base - Lot 1 épuisé (quantité = 0)", lot1Post.getQuantiteRestante() == 0);
            assertCondition("FEFO en base - Lot 2 décrémenté (20 - 3 = 17)", lot2Post.getQuantiteRestante() == 17);

            // Vérification du stock global médicament (25 - 8 = 17)
            Medicament medPost = medDAO.findById(medId);
            assertCondition("Vente - Stock médicament global mis à jour à 17", medPost.getQuantiteDisponible() == 17);

            // Vérification du crédit client (8 x 40 = 320 DH)
            Client cPost = clientDAO.findById(clientId);
            assertCondition("Vente à crédit - Solde client mis à jour à 320 DH", Math.abs(cPost.getCredit() - 320.0) < 0.01);

            // 7.3 Test de Rollback si stock insuffisant
            boolean exceptionThrown = false;
            try {
                stockService.decrementerStock(medId, 1000, 40.0);
            } catch (RuntimeException ex) {
                exceptionThrown = true;
            }
            assertCondition("Vente - Rejet et exception si quantité supérieure au stock", exceptionThrown);

        } catch (Exception e) {
            fail("Erreur Suite 7 Ventes & FEFO: " + e.getMessage());
        } finally {
            cleanupVenteTestData(venteId, lotId1, lotId2, medId, clientId);
        }
    }

    // ==========================================================================
    // SUITE 8 : KPIs du Tableau de Bord & Alertes
    // ==========================================================================
    private static void runSuite8DashboardKPIsAndAlerts() {
        printHeader("SUITE 8 : KPIs du Tableau de Bord & Alertes");

        if (!dbAvailable) {
            skipSuite("Base MySQL locale non disponible");
            return;
        }

        MedicamentService medService = new MedicamentService();
        VenteService venteService = new VenteService();
        StockService stockService = new StockService();

        try {
            int totalMeds = medService.getNombreTotal();
            assertCondition("Dashboard - Comptage des médicaments (>= 0)", totalMeds >= 0);

            int totalVentes = venteService.getNombreVentes();
            assertCondition("Dashboard - Comptage des ventes (>= 0)", totalVentes >= 0);

            double ca = venteService.getChiffreAffaires();
            assertCondition("Dashboard - Calcul Chiffre d'Affaires (>= 0)", ca >= 0.0);

            List<Medicament> alertesStock = medService.getAlertesStock();
            assertCondition("Dashboard - Liste des alertes de stock faible non nulle", alertesStock != null);

            List<Lot> lotsProches = stockService.getLotsExpirationProche();
            assertCondition("Dashboard - Liste des lots proches d'expiration non nulle", lotsProches != null);

        } catch (Exception e) {
            fail("Erreur Suite 8 KPIs Dashboard: " + e.getMessage());
        }
    }

    // ==========================================================================
    // MÉTHODES UTILITAIRES DE NETTOYAGE & ASSERTIONS
    // ==========================================================================
    private static int createFakeApprovisionnement(int medId) throws SQLException {
        Connection conn = DBConnection.getInstance().getConnection();
        PreparedStatement psA = conn.prepareStatement(
                "INSERT INTO approvisionnement (id_fournisseur, id_utilisateur, date_approvisionnement, montant_total, statut) " +
                "VALUES (1, 1, NOW(), 500.0, 'Reçu')", PreparedStatement.RETURN_GENERATED_KEYS);
        psA.executeUpdate();
        ResultSet rsA = psA.getGeneratedKeys();
        rsA.next();
        int appKey = rsA.getInt(1);
        rsA.close(); psA.close();

        PreparedStatement psL = conn.prepareStatement(
                "INSERT INTO ligne_approvisionnement (id_approvisionnement, id_medicament, date_reception, quantite_commandee, quantite_recue, prix_achat_unitaire) " +
                "VALUES (?, ?, CURDATE(), 25, 25, 20.0)", PreparedStatement.RETURN_GENERATED_KEYS);
        psL.setInt(1, appKey);
        psL.setInt(2, medId);
        psL.executeUpdate();
        ResultSet rsL = psL.getGeneratedKeys();
        rsL.next();
        int laKey = rsL.getInt(1);
        rsL.close(); psL.close();

        return laKey;
    }

    private static void cleanupApprovisionnementTestData(int approId, int medId, int fourId) {
        try {
            Connection conn = DBConnection.getInstance().getConnection();
            if (approId > 0) {
                conn.createStatement().executeUpdate("DELETE FROM lot WHERE id_ligne_approvisionnement IN (SELECT id_ligne_approvisionnement FROM ligne_approvisionnement WHERE id_approvisionnement = " + approId + ")");
                conn.createStatement().executeUpdate("DELETE FROM ligne_approvisionnement WHERE id_approvisionnement = " + approId);
                conn.createStatement().executeUpdate("DELETE FROM approvisionnement WHERE id_approvisionnement = " + approId);
            }
            if (medId > 0) conn.createStatement().executeUpdate("DELETE FROM medicament WHERE id_medicament = " + medId);
            if (fourId > 0) conn.createStatement().executeUpdate("DELETE FROM fournisseur WHERE id_fournisseur = " + fourId);
        } catch (Exception ignored) {}
    }

    private static void cleanupVenteTestData(int venteId, int lotId1, int lotId2, int medId, int clientId) {
        try {
            Connection conn = DBConnection.getInstance().getConnection();
            if (venteId > 0) {
                conn.createStatement().executeUpdate("DELETE FROM ligne_vente WHERE id_vente = " + venteId);
                conn.createStatement().executeUpdate("DELETE FROM vente WHERE id_vente = " + venteId);
            }
            if (lotId1 > 0) conn.createStatement().executeUpdate("DELETE FROM lot WHERE id_lot = " + lotId1);
            if (lotId2 > 0) conn.createStatement().executeUpdate("DELETE FROM lot WHERE id_lot = " + lotId2);
            if (medId > 0) {
                conn.createStatement().executeUpdate("DELETE FROM ligne_approvisionnement WHERE id_medicament = " + medId);
                conn.createStatement().executeUpdate("DELETE FROM medicament WHERE id_medicament = " + medId);
            }
            if (clientId > 0) conn.createStatement().executeUpdate("DELETE FROM client WHERE id_client = " + clientId);
        } catch (Exception ignored) {}
    }

    private static void assertCondition(String testName, boolean condition) {
        totalTests++;
        if (condition) {
            passedTests++;
            System.out.println("  [PASS] " + testName);
        } else {
            failedTests++;
            System.err.println("  [FAIL] " + testName);
        }
    }

    private static void pass(String message) {
        totalTests++;
        passedTests++;
        System.out.println("  [PASS] " + message);
    }

    private static void fail(String message) {
        totalTests++;
        failedTests++;
        System.err.println("  [FAIL] " + message);
    }

    private static void skipSuite(String reason) {
        skippedTests++;
        System.out.println("  [SKIP] Suite ignorée : " + reason);
    }

    private static void printHeader(String title) {
        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println(" " + title);
        System.out.println("--------------------------------------------------------------------------------");
    }

    private static void printFinalReport() {
        System.out.println("\n================================================================================");
        System.out.println("                             RAPPORT FINAL QA");
        System.out.println("================================================================================");
        System.out.println("  Total des assertions testées : " + totalTests);
        System.out.println("  Tests Réussis   [PASS]       : " + passedTests);
        System.out.println("  Tests Échoués   [FAIL]       : " + failedTests);
        System.out.println("  Suites Ignorées [SKIP]       : " + skippedTests);
        System.out.println("--------------------------------------------------------------------------------");
        if (failedTests == 0) {
            System.out.println("  RÉSULTAT GLOBAL : [SUCCÈS] Tous les tests exécutés sont conformes !");
        } else {
            System.err.println("  RÉSULTAT GLOBAL : [ÉCHEC] Des anomalies ont été détectées (" + failedTests + " échecs).");
        }
        System.out.println("================================================================================\n");
    }
}
