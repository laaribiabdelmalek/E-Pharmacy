# 🏥 E-PHARMACIE — Application de Gestion de Pharmacie (v3.0)

Application Desktop Java d'entreprise pour l'administration et la gestion complète d'une officine pharmaceutique, conçue selon une architecture multicouche stricte (MVC / Service / DAO / Modèle) avec persistance JDBC optimisée, pool de connexions haute performance HikariCP, sécurité RBAC renforcée, journalisation SLF4J/Logback et conformité réglementaire pharmaceutique.

---

## 📋 Table des matières

1. [Prérequis](#-prérequis)
2. [Installation & Migrations de la Base de Données](#-installation--migrations-de-la-base-de-données)
3. [Configuration Externalisée](#-configuration-externalisée)
4. [Compilation et Exécution](#-compilation-et-exécution)
5. [Identifiants & Comptes de Démonstration](#-identifiants--comptes-de-démonstration)
6. [Architecture Technique v3.0](#-architecture-technique-v30)
7. [Modules Fonctionnels & Règles Métier](#-modules-fonctionnels--règles-métier)
8. [Assurance Qualité & Tests Automatisés](#-assurance-qualité--tests-automatisés)
9. [Structure du Projet](#-structure-du-projet)

---

## ✅ Prérequis

| Composant | Version minimale | Rôle |
|---|---|---|
| **JDK** | 17+ (testé sur JDK 17 et JDK 25) | Environnement d'exécution et de compilation Java |
| **Apache Maven** | 3.8+ / 4.0+ | Gestionnaire de dépendances et cycle de build |
| **MySQL Community** | 8.0+ | Serveur de base de données relationnelle |

---

## 🗄 Installation & Migrations de la Base de Données

### Option A : Nouvelle Installation Complète

Exécutez le script du schéma global suivi du jeu de données d'exemple :

```bash
mysql -u root -p < sql/schema.sql
mysql -u root -p e_pharmacie < sql/data_sample.sql
```

### Option B : Migration Idempotente depuis une version existante

Si une version antérieure de la base est déjà déployée, appliquez les scripts de migration successifs dans l'ordre :

```bash
# 1. Migration v2 : Paramètres société, encodage UTF-8 et gestion des thèmes
mysql -u root -p e_pharmacie < sql/migration_v2.sql

# 2. Migration v3 : Ordonnances, plafonds de crédit, statuts logiques, paiement_client et audit_log
mysql -u root -p e_pharmacie < sql/migration_v3.sql
```

---

## ⚙ Configuration Externalisée

L'application supporte une hiérarchie de configuration en 3 niveaux gérée par `DBConnection.java` :

1. **Variables d'environnement système** (priorité maximale, idéal en CI/CD ou production) :
   - `EPHARMACY_DB_URL`
   - `EPHARMACY_DB_USER`
   - `EPHARMACY_DB_PASS`
2. **Fichier `config.properties`** à la racine du projet (exclu de Git via `.gitignore`) :
   Copiez le modèle fourni :
   ```bash
   cp config.properties.example config.properties
   ```
   Puis ajustez vos paramètres locaux :
   ```properties
   db.url=jdbc:mysql://localhost:3306/e_pharmacie?useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
   db.user=root
   db.password=epharmacy123
   ```
3. **Valeurs par défaut sécurisées** intégrées au code en cas d'absence de configuration externe.

---

## 🚀 Compilation et Exécution

### Compilation des sources

```bash
mvn clean test-compile
```

### Construction du Fat JAR exécutable

```bash
mvn clean package
```

### Lancement de l'application

```bash
java -jar target/e-pharmacie-1.0-SNAPSHOT.jar
```

---

## 🔑 Identifiants & Comptes de Démonstration

Les mots de passe sont hachés de manière irréversible via **BCrypt** (`$2a$10$...`) :

| Rôle | Login | Mot de passe | Droits et Périmètre |
|---|---|---|---|
| **ADMIN** | `admin` | `admin123` | Accès total : Utilisateurs, Paramètres société, Annulation ventes, Stocks, POS, Rapports |
| **PHARMACIEN** | `pharmacien` | `pharma123` | Accès officine : Ventes, Approvisionnements, Règlements clients, Modification des prix |
| **EMPLOYE** | `employe` | `emp123` | Point de vente (POS), Consultation du catalogue, Prix unitaire en lecture seule |

---

## 🏛 Architecture Technique v3.0

### 1. Pool de Connexions Haute Performance (HikariCP)
- Pool managé avec 10 connexions max, 2 idle minimum, timeouts de connexion configurés.
- Fermeture automatique propre via un `ShutdownHook` enregistré dans `App.java`.
- Isolation stricte des transactions JDBC : `conn.setAutoCommit(false)` avec rollback automatique en cas d'erreur.

### 2. Typage Monétaire Rigoureux (`BigDecimal`)
- Tous les montants financiers (prix de vente, prix d'achat, totaux, crédits, sous-totaux, règlements) sont gérés avec `BigDecimal` et arrondi bancaire standard (`RoundingMode.HALF_UP` à 2 décimales) pour éliminer toute dérive d'arrondi flottant.

### 3. Contrôle d'Accès par Rôle (RBAC) & Sécurité
- Classe centrale `AccessControl` avec méthode `require("ROLE_A", "ROLE_B")`.
- Protection anti-brute-force : verrouillage temporaire (60 secondes) après 5 tentatives échouées consécutives.
- Politique de mot de passe fort : minimum 8 caractères, 1 majuscule, 1 minuscule, 1 chiffre, 1 caractère spécial.
- Garde-fous administratifs : interdiction pour un admin connecté de rétrograder son propre rôle ; interdiction de supprimer ou désactiver le dernier administrateur actif du système.
- Suppression logique (soft delete) : aucun enregistrement maître n'est effacé physiquement ; bascule de l'état vers `INACTIF`.

### 4. Concurrence & Règles Métier Pharmaceutiques
- Stratégie de prélèvement de stock **FEFO** (*First Expired, First Out*).
- Verrouillage transactionnel pessimiste (`SELECT ... FOR UPDATE`) sur les stocks et les comptes clients pour prévenir les *race conditions*.
- Contrôle strict du plafond de crédit client (`plafond_credit`) avant toute validation de vente à crédit.
- Contrôle de prescription médicale obligatoire (`ordonnance_requise`) avec confirmation explicite de l'opérateur et traçabilité dans `audit_log`.
- Annulation intégrale de vente (`annulerVente`) : restitution automatique des quantités aux lots d'origine, recalcul du stock médicament et réajustement de la balance client.
- Réception d'approvisionnement en 2 temps : création de la commande (`En attente`) puis réception (`Partiellement reçu` ou `Reçu`) avec génération de lots uniques traçables (`LOT-YYYYMMDD-XXXX`).

---

## 🧪 Assurance Qualité & Tests Automatisés

Le projet intègre une double validation de qualité :

1. **Suite de Tests Unitaires JUnit 5** (`src/test/java/com/epharmacy/`) :
   - `AccessControlTest` : vérification des autorisations et exceptions RBAC.
   - `UtilisateurServiceTest` : vérification exhaustive des règles de complexité de mot de passe.
   - `FrenchNumberToWordsTest` : validation de la transcription des montants en lettres françaises (Dirhams et centimes).
   - `AuthServiceLockoutTest` : validation du mécanisme de temporisation anti-brute-force.
2. **Suite d'Intégration et QA Système** (`FullSystemTestRunner.java`) :
   - Tests de connectivité et encodage UTF-8 (caractères accentués français).
   - Tests de rendu headless des impressions graphiques (Ticket 80mm, Facture A4, Bon de commande).
   - Validation de l'intégrité référentielle des 12 tables MySQL.

Pour lancer les tests programmatiques en console :
```powershell
$jars = (Get-ChildItem -Path "$env:USERPROFILE\.m2\repository" -Filter "*.jar" -Recurse | Where-Object { $_.FullName -notmatch "sources|javadoc" }).FullName -join ";"
$cp = "target/classes;target/test-classes;" + $jars
java -cp $cp com.epharmacy.test.JUnitTestRunner
java -cp $cp com.epharmacy.test.FullSystemTestRunner
```

---

## 📁 Structure du Projet

```text
E-Pharmacy/
├── pom.xml                               # Dépendances Maven (HikariCP, FlatLaf, PDFBox, JUnit 5, SLF4J)
├── config.properties.example             # Modèle de configuration JDBC locale
├── sql/
│   ├── schema.sql                        # Schéma complet de la base (12 tables v3.0)
│   ├── data_sample.sql                   # Données de démonstration et comptes initiaux
│   ├── migration_v2.sql                  # Script de migration vers v2
│   └── migration_v3.sql                  # Script de migration idempotente vers v3
├── src/
│   ├── main/
│   │   ├── java/com/epharmacy/
│   │   │   ├── App.java                  # Point d'entrée, LookAndFeel, ShutdownHook
│   │   │   ├── config/                   # DBConnection (HikariCP, résolution d'environnement)
│   │   │   ├── dao/                      # 12 DAOs avec try-with-resources et support transactionnel
│   │   │   ├── exception/                # Exceptions typées (Business, DataAccess, InsufficientStock, Unauthorized)
│   │   │   ├── model/                    # 12 Modèles du domaine (BigDecimal, accesseurs doubles rétrocompatibles)
│   │   │   ├── print/                    # Rendu imprimable (Ticket thermique, Facture A4, Bon de commande)
│   │   │   ├── security/                 # AccessControl (RBAC et vérification des rôles)
│   │   │   ├── service/                  # Services transactionnels et règles métier
│   │   │   ├── ui/                       # LoginFrame, MainFrame (chargement paresseux), Composants, Panneaux
│   │   │   └── util/                     # Formatage, conversions, thèmes, génération PDF
│   │   └── resources/
│   │       └── logback.xml               # Configuration de journalisation SLF4J / Logback
│   └── test/
│       └── java/com/epharmacy/           # Suites de tests JUnit 5 et runner d'intégration FullSystemTestRunner
├── TEST_PLAN.md                          # Cahier de recette et protocole de test détaillé
└── README.md                             # Documentation technique et opérationnelle du projet
```
