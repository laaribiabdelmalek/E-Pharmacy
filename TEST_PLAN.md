# CAHIER DE RECETTE, PLAN DE TEST & GUIDE DE DÉMO JURY
## APPLICATION DE GESTION PHARMACEUTIQUE — E-PHARMACIE (v2.0)

---

## 1. ENVIRONNEMENT & PRÉ-REQUIS TECHNIQUES

### 1.1 Spécifications de la plateforme
- **Langage / Runtime** : Java 17+ (LTS)
- **Interface Graphique** : Java Swing, Look & Feel FlatLaf 3.2+ (Clair, Sombre, Système), MigLayout 11.4+
- **Base de Données** : MySQL 8.0+ avec encodage `utf8mb4` et collation `utf8mb4_unicode_ci`
- **Pilote JDBC** : `mysql-connector-j` 8.0.33 avec `useUnicode=true&characterEncoding=UTF-8`
- **Sécurité** : Hachage de mots de passe par sel aléatoire BCrypt
- **Architecture** : MVC / DAO / Service transactionnel avec gestion stricte de rollback

### 1.2 Comptes Utilisateurs de Démonstration (Jeu d'essai)
| Login | Mot de passe | Rôle | Statut | Permissions & Périmètre |
| :--- | :--- | :--- | :--- | :--- |
| `admin` | `admin123` | `ADMIN` | `ACTIF` | Accès total (KPIs, Catalogue, Ventes, Appro, Utilisateurs, Paramètres) |
| `pharmacien` | `pharm123` | `PHARMACIEN` | `ACTIF` | Catalogue, Stocks & Lots, Ventes POS, Approvisionnements |
| `employe` | `emp123` | `EMPLOYE` | `ACTIF` | Ventes POS, Consultation stocks |
| `bloque` | `test123` | `EMPLOYE` | `INACTIF` | Compte désactivé (Test de rejet à la connexion) |

---

## 2. SUITE AUTOMATISÉE D'INTÉGRATION & DE VÉRIFICATION

L'application dispose d'un exécuteur de tests programmatiques automatisés :
`com.epharmacy.test.FullSystemTestRunner`

### 2.1 Commande d'exécution du runner :
```bash
# Compilation du projet
mvn clean compile

# Exécution du test runner automatisé
mvn exec:java -Dexec.mainClass="com.epharmacy.test.FullSystemTestRunner"
```

### 2.2 Couverture des 8 Suites Automatisées :
1. **Suite 1 : Utilitaires & Conversions** — Test de `FrenchNumberToWords` (Dirhams et Centimes), hachage BCrypt, expressions régulières `ValidationUtils` (Téléphone marocain, Email, formats positifs), et détection de thème `ThemeManager`.
2. **Suite 2 : Moteurs d'impression Headless** — Rendu sur `Graphics2D` / `BufferedImage` du Ticket de caisse 80mm (`TicketPrintable`), de la Facture A4 (`FacturePrintable`) et du Bon de Commande (`BonCommandePrintable`).
3. **Suite 3 : Base de Données & UTF-8** — Vérification de la connexion singleton, intégrité des 10 tables SQL et préservation des accents français sans corruption `?`.
4. **Suite 4 : Authentification & Sécurité** — Validation login/password, blocage des comptes inactifs, session utilisateur.
5. **Suite 5 : Opérations CRUD unifiées** — Insertion, modification, recherche, filtres et protection contre les suppressions d'entités avec contraintes d'intégrité.
6. **Suite 6 : Workflow Approvisionnement en 2 étapes** — Création commande 'En attente' (sans impact stock/lot), puis validation réception 'Reçu' (création lot et incrémentation de stock).
7. **Suite 7 : Ventes, FEFO & Rollback** — Déduction First Expired, First Out entre lots multiples, transaction atomique, vente à crédit et rollback sur stock insuffisant.
8. **Suite 8 : KPIs Dashboard & Alertes** — Détection des ruptures de seuil et lots à péremption imminente (<= 90 jours).

---

## 3. CHECKLIST DÉTAILLÉE DES TESTS MANUELS (UI & BDD)

### MODULE 1 : AUTHENTIFICATION & SÉCURITÉ DE SESSION

#### TC-AUTH-01 : Connexion Administrateur valide
- **Priorité** : Critique
- **Pré-condition** : Application lancée sur l'écran `LoginFrame`.
- **Étapes** :
  1. Saisir Login : `admin`
  2. Saisir Mot de passe : `admin123`
  3. Cliquer sur le bouton *"Se connecter"* (ou touche Entrée).
- **Résultat attendu (UI)** :
  - La fenêtre `LoginFrame` se ferme instantanément.
  - La fenêtre principale `MainFrame` s'ouvre sur le *"Tableau de bord"*.
  - La barre latérale affiche toutes les sections, y compris *"ADMINISTRATION"* (*Utilisateurs* et *Paramètres*).
- **Contrôle BDD** : `SessionManager` contient l'ID utilisateur, rôle `ADMIN`.

#### TC-AUTH-02 : Rejet mot de passe incorrect
- **Priorité** : Critique
- **Étapes** : Saisir `admin` et mot de passe `fauxpass`. Cliquer sur *"Se connecter"*.
- **Résultat attendu (UI)** : Message d'alerte en français : *"Identifiants invalides ou compte inactif"*. La session reste fermée.

#### TC-AUTH-03 : Rejet compte inactif
- **Priorité** : Majeure
- **Étapes** : Saisir le login d'un utilisateur ayant `statut = 'INACTIF'`.
- **Résultat attendu (UI)** : Refus d'accès avec message d'erreur clair.

#### TC-AUTH-04 : Déconnexion et purge de session
- **Priorité** : Majeure
- **Étapes** : Dans la barre latérale, cliquer sur *"🚪 Déconnexion"*. Confirmer la boîte de dialogue.
- **Résultat attendu (UI)** : `MainFrame` est détruit, `LoginFrame` réapparaît. Aucune donnée résiduelle en session.

---

### MODULE 2 : CATALOGUE MÉDICAMENTS & FORMULAIRE RÉTRACTABLE (`hidemode 3`)

#### TC-MED-01 : Redimensionnement dynamique & affichage 100% hauteur
- **Priorité** : Critique (Ergonomie & UX)
- **Étapes** : Ouvrir la vue *"Médicaments"*.
- **Résultat attendu (UI)** :
  - Le formulaire d'édition est masqué par défaut.
  - Le tableau `StyledTable` occupe **100%** de la hauteur restante jusqu'au bas de l'écran sans aucun espace blanc vide.
  - La grille et le fond de table remplissent entièrement le viewport (`setFillsViewportHeight(true)`).

#### TC-MED-02 : Ouverture et fermeture du formulaire inline
- **Priorité** : Critique
- **Étapes** :
  1. Cliquer sur `+ Ajouter Médicament`.
  2. Observer le glissement vers le bas du tableau pour laisser place au formulaire.
  3. Cliquer sur `Annuler`.
- **Résultat attendu (UI)** :
  - Le formulaire se déplie avec le titre *"Nouveau Médicament"*.
  - En cliquant sur `Annuler`, le formulaire disparaît complètement (`hidemode 3`) et le tableau remonte instantanément pour recoller sous la barre de filtres, avec zéro pixel d'espace mort.

#### TC-MED-03 : Ajout d'un nouveau médicament valide
- **Données de test** :
  - Nom commercial : `Amoxicilline Laprophan 500mg`
  - DCI : `Amoxicilline`
  - Dosage : `500 mg`
  - Forme : `GELULE`
  - Laboratoire : `Laprophan`
  - Prix Vente (DH) : `42.50`
  - Seuil Minimal : `15`
- **Étapes** : Remplir les champs et cliquer sur `Enregistrer`.
- **Résultat attendu (UI)** : Message de confirmation, repli du formulaire, apparition immédiate de la nouvelle ligne dans le tableau avec le statut *"COMMERCIALISE"*.
- **Contrôle BDD** :
  ```sql
  SELECT * FROM medicament WHERE nom_commercial LIKE 'Amoxicilline Laprophan%';
  ```

#### TC-MED-04 : Contrôle de saisie & validations obligatoires
- **Étapes** : Tenter d'enregistrer avec un Nom vide ou un Prix négatif (`-5`).
- **Résultat attendu (UI)** : Dialogue d'erreur bloquant, focus remis sur le champ en anomalie.

#### TC-MED-05 : Recherche instantanée et filtrage
- **Étapes** : Saisir `"Amox"` dans la barre de recherche ; tester le filtre statut `"EN_RUPTURE"`.
- **Résultat attendu (UI)** : Filtrage en temps réel dans le tableau sans scintillement.

---

### MODULE 3 : GESTION DES TIERS (CLIENTS & FOURNISSEURS)

#### TC-CLI-01 : Création et modification client
- **Données de test** : Nom : `El Fassi`, Prénom : `Nadia`, Tél : `0661998877`, Email : `nadia.elfassi@gmail.com`, Adresse : `12 Rue de la Liberté, Guéliz`, Crédit : `0.00`.
- **Étapes** : Naviguer vers *"Clients"*, cliquer sur `+ Ajouter Client`, valider. Puis double-cliquer sur la ligne pour modifier l'adresse.
- **Résultat attendu (UI)** : Le formulaire s'ouvre avec les données existantes sous le titre *"Modifier Client — ID X"*. La mise à jour est immédiatement répercutée.

#### TC-FOUR-01 : Création fournisseur avec identifiants fiscaux
- **Données de test** : Raison Sociale : `Cooper Pharma Maroc`, ICE : `001599887700012`, Tél : `0522334455`, Ville : `Casablanca`, Statut : `ACTIF`.
- **Résultat attendu (UI)** : Enregistrement réussi, table rafraîchie, fournisseur disponible dans les listes déroulantes d'approvisionnement.

#### TC-SEC-INT : Protection contre la suppression avec contrainte SQL
- **Étapes** : Tenter de supprimer un client ou un fournisseur lié à des commandes ou ventes existantes.
- **Résultat attendu (UI)** : Interception propre de l'exception `SQLIntegrityConstraintViolationException` avec message informatif en français : *"Impossible de supprimer : cet enregistrement est référencé par des documents existants."*

---

### MODULE 4 : APPROVISIONNEMENT EN 2 ÉTAPES & TRAÇABILITÉ DES LOTS

#### TC-APP-01 : Étape 1 — Émission d'un Bon de Commande ('En attente')
- **Données de test** :
  - Fournisseur : `Cooper Pharma Maroc`
  - Médicament : `Doliprane 1000mg`
  - Quantité commandée : `100` unités
  - P.U Achat : `12.50 DH`
- **Étapes** :
  1. Dans *"Approvisionnements"*, cliquer sur `+ Nouvel Approvisionnement`.
  2. Sélectionner le fournisseur et ajouter la ligne de médicament.
  3. Cliquer sur `Enregistrer la Commande (En attente)`.
- **Résultat attendu (UI & Métier)** :
  - Commande créée avec le statut **"En attente"**.
  - **Vérification cruciale** : Le stock du médicament en catalogue n'a **PAS** augmenté.
  - **Vérification cruciale** : Aucun lot n'a été créé dans la table `lot`.
  - Une boîte de dialogue propose d'imprimer immédiatement le Bon de Commande.
- **Contrôle BDD** :
  ```sql
  SELECT statut, montant_total FROM approvisionnement ORDER BY id_approvisionnement DESC LIMIT 1;
  -- Doit afficher 'En attente'
  ```

#### TC-APP-02 : Impression / Prévisualisation du Bon de Commande A4
- **Étapes** : Sélectionner la commande 'En attente' et cliquer sur `Imprimer Bon de Commande`.
- **Résultat attendu (UI)** :
  - Ouverture de `PrintPreviewDialog` au format A4 (595 x 842 pt).
  - En-tête officiel de la pharmacie, cadre fournisseur, désignation, quantité commandée, montant total estimé en chiffres et en lettres françaises.

#### TC-APP-03 : Étape 2 — Réception livraison avec ajustement des quantités et saisie des Lots
- **Données de test** :
  - Quantité reçue : `90` (au lieu de 100 commandées - livraison partielle)
  - N° de Lot : `LOT-DP-2026-X1`
  - Date d'expiration : `2027-11-30` (valide dans le futur)
- **Étapes** :
  1. Sélectionner la commande 'En attente', cliquer sur `Réceptionner la livraison`.
  2. Ajuster la quantité reçue à 90.
  3. Saisir le N° de lot et la date d'expiration.
  4. Cliquer sur `Valider la Réception & Mettre à jour les stocks`.
- **Résultat attendu (UI & Métier)** :
  - Statut de l'approvisionnement mis à jour à **"Reçu"**.
  - Montant recalculé à `90 * 12.50 = 1125.00 DH`.
  - Le stock disponible du médicament augmente exactement de `+90`.
  - Le lot `LOT-DP-2026-X1` apparaît dans l'écran *"Stock & Lots"* avec 90 unités disponibles.
- **Contrôle BDD** :
  ```sql
  SELECT * FROM lot WHERE numero_lot = 'LOT-DP-2026-X1';
  -- quantite_initiale = 90, quantite_restante = 90
  ```

#### TC-APP-04 : Contrôle de saisie sur la réception
- **Étapes** : Tenter de valider avec une date d'expiration passée (ex: `2020-01-01`) ou un numéro de lot vide.
- **Résultat attendu (UI)** : Validation bloquée avec alerte explicite : *"La date d'expiration doit être strictement supérieure à aujourd'hui."*

---

### MODULE 5 : VENTES (POS), FEFO & IMPRESSION TICKETS / FACTURES

#### TC-VNT-01 : Déduction FEFO (First Expired, First Out)
- **Pré-condition** : Un médicament possède deux lots actifs :
  - Lot A : 5 unités expirant le `2026-12-01` (prioritaire)
  - Lot B : 20 unités expirant le `2027-12-01`
- **Étapes** :
  1. Dans le Point de Vente (*"Ventes"*), rechercher le médicament.
  2. Ajouter une quantité de `8` unités au panier.
- **Résultat attendu (UI)** :
  - Le panier décompose automatiquement l'article en 2 lignes selon la règle FEFO :
    - 5 unités issues du Lot A
    - 3 unités issues du Lot B
- **Étapes de validation** : Choisir mode de paiement `ESPECES`, cliquer sur `Valider la Vente`.
- **Contrôle BDD après vente** :
  - Lot A : `quantite_restante = 0`
  - Lot B : `quantite_restante = 17`
  - Stock global médicament décrémenté de 8.

#### TC-VNT-02 : Dialogue post-vente & Impression Ticket thermique 80mm
- **Étapes** : À la confirmation de vente, cliquer sur *"Imprimer Ticket de Caisse"*.
- **Résultat attendu (UI)** :
  - `PrintPreviewDialog` s'ouvre avec la mise en page ticket thermique 80mm (largeur 226 pt).
  - Présence du nom de la pharmacie, coordonnées, Ticket N° `VNT-XXXX`, liste des médicaments avec P.U, Total TTC, Mode de paiement et message de remerciement.

#### TC-VNT-03 : Impression Facture Officielle A4 & Montant en lettres
- **Étapes** : À la confirmation de vente (ou depuis l'Historique des ventes), cliquer sur *"Imprimer Facture"*.
- **Résultat attendu (UI)** :
  - `PrintPreviewDialog` affiche la facture au format officiel A4.
  - Bloc fiscal complet : ICE, IF, RC, Patente.
  - Cadre client (ou *"Client Comptoir"*).
  - Tableau borduré avec colonne **Dosage**, quantités et sous-totaux.
  - Montant arrêté en toutes lettres françaises (ex: *"Arrêtée la présente facture à la somme de : Deux cent cinquante Dirhams et cinquante Centimes"*).
  - Cadre officiel *"Cachet et Signature"*.

#### TC-VNT-04 : Vente à crédit & Suivi du compte client
- **Étapes** : Sélectionner un client dans la liste, mode de paiement `CREDIT`, valider une vente de 150 DH.
- **Résultat attendu (UI & BDD)** :
  - Le solde du client dans le panneau *"Clients"* augmente exactement de `+150.00 DH`.
  - La tentative de vente à crédit avec "Vente comptoir" est automatiquement refusée par l'application.

---

### MODULE 6 : STOCK & ALERTES DE PÉREMPTION

#### TC-STK-01 : Filtre des lots proches de péremption (<= 90 jours)
- **Étapes** : Ouvrir *"Stock & Lots"*, cocher la case *"Expiration proche (90j)"*.
- **Résultat attendu (UI)** : Seuls les lots expirant dans les 90 prochains jours s'affichent, avec le badge statut orange *"Bientôt expiré"*.

#### TC-STK-02 : Filtre des lots expirés
- **Étapes** : Cocher *"Lots expirés"*.
- **Résultat attendu (UI)** : Seuls les lots dont la date d'expiration est antérieure à aujourd'hui s'affichent avec le statut rouge *"Expiré"*.

---

### MODULE 7 : TABLEAU DE BORD (DASHBOARD) TEMPS RÉEL

#### TC-DSH-01 : KPIs et alertes dynamiques
- **Étapes** : Consulter la vue *"Tableau de bord"*.
- **Résultat attendu (UI)** :
  - 4 cartes KPI : Total Médicaments, Total Clients, Total Ventes, Chiffre d'Affaires.
  - Les valeurs reflètent fidèlement les opérations réalisées dans les autres modules.
  - La section *"Alertes Stock Faible"* liste tous les médicaments dont `quantite_disponible <= seuil_minimal`.
  - La table inférieure liste les lots critiques proches de péremption et remplit 100% de la hauteur.

---

### MODULE 8 : PARAMÈTRES DE LA PHARMACIE & GESTION DES THÈMES

#### TC-PAR-01 : Personnalisation de l'identité pharmaceutique
- **Étapes** :
  1. Aller dans *"Paramètres"* (compte Admin).
  2. Modifier le Nom de la Pharmacie (ex: `Grande Pharmacie Centrale`), l'ICE, l'adresse et le message de pied de ticket.
  3. Cliquer sur `Enregistrer les paramètres`.
- **Résultat attendu (UI & Impression)** :
  - Sauvegarde immédiate en base dans la table `parametres_societe`.
  - Ré-émettre un ticket ou une facture : les nouvelles coordonnées apparaissent instantanément sur les impressions.

#### TC-PAR-02 : Basculement dynamique de thème (Clair / Sombre / Système)
- **Étapes** :
  1. Dans les paramètres, sélectionner le bouton radio *"Mode Sombre (🌙)"*.
  2. Sélectionner *"Mode Clair (☀)"*.
  3. Sélectionner *"Système (Détection Windows 💻)"*.
- **Résultat attendu (UI)** :
  - Application immédiate du thème sur toutes les fenêtres actives sans redémarrage (`FlatDarkLaf` ou `FlatLightLaf`).
  - Synchronisation en direct du bouton de bascule en bas de la barre latérale.
  - En mode Système, détection automatique de la valeur du registre Windows `AppsUseLightTheme`.
  - Persistance du choix en base pour le prochain lancement de l'application.

---

## 4. SCÉNARIO IDÉAL DE DÉMONSTRATION DEVANT LE JURY (5 À 7 MINUTES)

Ce scénario fluide permet de présenter 100% des exigences sans temps mort :

| Temps | Écran / Action | Ce que le jury voit & retient |
| :--- | :--- | :--- |
| **00:00** | **Login** (`admin` / `admin123`) | Interface moderne FlatLaf, sécurité par mot de passe chiffré BCrypt. |
| **00:45** | **Dashboard** | Vue synthétique : KPIs d'activité (CA, Ventes), alertes de rupture et alertes péremption. |
| **01:30** | **Médicaments** | Démonstration du **formulaire rétractable `hidemode 3`** : clic sur `+ Ajouter`, remplissage express, validation, le tableau s'étire à 100% sans blanc résiduel. |
| **02:30** | **Approvisionnement (Étape 1)** | Création d'une commande fournisseur 'En attente'. Visualisation du **Bon de Commande officiel A4** avec cachet. Démonstration que le stock n'a pas bougé. |
| **03:30** | **Réception (Étape 2)** | Réception de la livraison avec ajustement de quantité, attribution du numéro de lot et de la date d'expiration. Confirmation : les stocks augmentent automatiquement et le lot est créé. |
| **04:30** | **Point de Vente (Ventes)** | Vente au comptoir. Démonstration de l'algorithme **FEFO** (le système choisit automatiquement le lot le plus proche de la péremption). |
| **05:15** | **Impression Documents** | Popup de fin de vente : aperçu du **Ticket thermique 80mm**, puis de la **Facture officielle A4** (avec montant transcrit en lettres françaises, cadre fiscal et dosage). |
| **06:00** | **Paramètres & Thème** | Basculement en direct en **Mode Sombre**, puis **Détection Système Windows**. Édition des coordonnées de la pharmacie. |
| **06:45** | **Déconnexion** | Clôture propre de la session et retour sur l'écran d'accueil sécurisé. |

---

## 5. BILAN DE QUALITÉ & CONFORMITÉ

- **Zéro Régression** : Les modules originaux (`LoginFrame`, `DashboardPanel`, `StockLotPanel`) conservent leur intégrité logique.
- **Régularité Graphique** : Respect absolu de la charte graphique et des icônes d'origine.
- **Qualité de code** : 100% compilable sans avertissement bloquant (`mvn clean test-compile`), gestion transactionnelle ACID sur toutes les écritures critiques.
