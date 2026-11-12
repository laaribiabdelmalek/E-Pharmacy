# 🏥 E-PHARMACIE — Application de Gestion de Pharmacie

Application Desktop Java professionnelle pour l'administration et la gestion complète d'une pharmacie.

---

## 📋 Table des matières

1. [Prérequis](#-prérequis)
2. [Installation de la base de données](#-installation-de-la-base-de-données)
3. [Configuration](#-configuration)
4. [Compilation et exécution](#-compilation-et-exécution)
5. [Identifiants par défaut](#-identifiants-par-défaut)
6. [Modules fonctionnels](#-modules-fonctionnels)
7. [Architecture technique](#-architecture-technique)
8. [Structure du projet](#-structure-du-projet)

---

## ✅ Prérequis

| Composant | Version minimale | Téléchargement |
|-----------|-----------------|----------------|
| **JDK** | 17+ | [Oracle JDK](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html) ou [OpenJDK](https://adoptium.net/) |
| **Maven** | 3.8+ | [Apache Maven](https://maven.apache.org/download.cgi) |
| **MySQL** | 8.0+ | [MySQL Community](https://dev.mysql.com/downloads/mysql/) |

---

## 🗄 Installation de la base de données

### Étape 1 : Créer la base et les tables

```bash
mysql -u root -p < sql/schema.sql
```

### Étape 2 : Insérer les données de test

```bash
mysql -u root -p e_pharmacie < sql/data_sample.sql
```

> **Note :** Les données de test incluent des médicaments, clients, fournisseurs et ventes réalistes adaptés au contexte pharmaceutique marocain.

---

## ⚙ Configuration

La connexion à la base de données est configurée dans le fichier :

📁 `src/main/java/com/epharmacy/config/DBConnection.java`

```java
private static final String URL = "jdbc:mysql://localhost:3306/e_pharmacie?useSSL=false&serverTimezone=UTC";
private static final String USER = "root";
private static final String PASSWORD = "";  // ← Modifier selon votre configuration
```

> **Important :** Modifiez le mot de passe MySQL si nécessaire avant la compilation.

---

## 🚀 Compilation et exécution

### Compilation

```bash
mvn clean compile
```

### Construction du JAR exécutable (Fat JAR)

```bash
mvn clean package
```

### Exécution

```bash
java -jar target/e-pharmacie-1.0-SNAPSHOT.jar
```

> Le JAR généré contient toutes les dépendances (shade plugin). Aucune bibliothèque externe n'est nécessaire à l'exécution.

---

## 🔑 Identifiants par défaut

| Rôle | Login | Mot de passe |
|------|-------|-------------|
| **Administrateur** | `admin` | `admin123` |
| **Pharmacien** | `pharmacien` | `pharma123` |
| **Employé** | `employe` | `emp123` |

---

## 📦 Modules fonctionnels

### Module 1 — Authentification & Sécurité
- Connexion sécurisée (Login/Password avec hachage BCrypt)
- Gestion des rôles : ADMIN, PHARMACIEN, EMPLOYE
- Session utilisateur active avec déconnexion
- Accès restreint aux fonctions d'administration

### Module 2 — Gestion du Catalogue Médicaments
- Data Grid avec filtres par statut (Commercialisé, Non Commercialisé, En rupture)
- Recherche multi-critères (Nom commercial, DCI, Catégorie)
- Formulaire modal (Ajout / Modification) avec validation
- Suppression sécurisée

### Module 3 — Gestion du Stock & Traçabilité des Lots
- Suivi précis par lot (Numéro de lot + Date d'expiration)
- Décrémentation automatique FEFO (First Expired, First Out)
- Alertes visuelles pour stock bas et péremption

### Module 4 — Gestion des Tiers (Clients & Fournisseurs)
- Répertoire des Clients avec suivi du crédit
- Répertoire des Fournisseurs avec ICE, téléphone, ville

### Module 5 — Module Caisse & Ventes (POS)
- Interface point de vente interactive
- Panier multi-articles avec calcul automatique
- Transactions JDBC (commit / rollback)
- Impression de facture PDF

### Module 6 — Approvisionnements
- Réception de commandes fournisseur
- Création automatique des lots
- Incrémentation automatique du stock

### Module 7 — Tableau de Bord (Dashboard)
- Indicateurs KPI (Total Médicaments, Clients, Ventes, CA)
- Alertes de stock faible
- Toggle Mode Sombre / Mode Clair (FlatLaf)

---

## 🏗 Architecture technique

### Stack technologique

| Composant | Technologie |
|-----------|------------|
| Langage | Java 17+ |
| GUI | Java Swing + FlatLaf + MigLayout |
| Base de données | MySQL 8.0+ |
| Accès données | JDBC natif (PreparedStatement) |
| Graphiques PDF | Apache PDFBox 2.0 |
| Hachage | jBCrypt |

### Patterns & Principes

- **MVC** (Model-View-Controller)
- **DAO** (Data Access Object) pour l'accès aux données
- **Singleton** pour la connexion DB (`DBConnection`)
- **SOLID** — Responsabilité unique, Open/Closed, etc.
- **FEFO** (First Expired, First Out) pour la gestion des lots

### Conception MERISE

Le Modèle Conceptuel de Données (MCD) comprend **9 entités** :

```
UTILISATEUR ──┬── VENTE ──── LIGNE_VENTE ──── LOT
              │                                 │
CLIENT ───────┘                    LIGNE_APPROVISIONNEMENT
                                        │
FOURNISSEUR ──── APPROVISIONNEMENT ─────┘
                                        
MEDICAMENT ──────────────────────────────┘
```

---

## 📂 Structure du projet

```
E-Pharmacy/
├── pom.xml                          # Configuration Maven
├── README.md                        # Ce fichier
├── sql/
│   ├── schema.sql                   # Script de création de la BDD
│   └── data_sample.sql              # Données de test
└── src/main/java/com/epharmacy/
    ├── App.java                     # Point d'entrée principal
    ├── config/
    │   └── DBConnection.java        # Connexion Singleton JDBC
    ├── model/                       # Entités POJO (9 classes)
    │   ├── Utilisateur.java
    │   ├── Client.java
    │   ├── Fournisseur.java
    │   ├── Medicament.java
    │   ├── Approvisionnement.java
    │   ├── LigneApprovisionnement.java
    │   ├── Lot.java
    │   ├── Vente.java
    │   └── LigneVente.java
    ├── dao/                         # Data Access Objects (9 classes)
    │   ├── UtilisateurDAO.java
    │   ├── ClientDAO.java
    │   ├── FournisseurDAO.java
    │   ├── MedicamentDAO.java
    │   ├── ApprovisionnementDAO.java
    │   ├── LigneApprovisionnementDAO.java
    │   ├── LotDAO.java
    │   ├── VenteDAO.java
    │   └── LigneVenteDAO.java
    ├── service/                     # Couche métier (5 classes)
    │   ├── AuthService.java
    │   ├── MedicamentService.java
    │   ├── StockService.java
    │   ├── VenteService.java
    │   └── ApprovisionnementService.java
    ├── util/                        # Utilitaires (5 classes)
    │   ├── SessionManager.java
    │   ├── PasswordUtils.java
    │   ├── ValidationUtils.java
    │   ├── PDFGenerator.java
    │   └── ThemeManager.java
    └── ui/                          # Interface graphique
        ├── LoginFrame.java          # Fenêtre de connexion
        ├── MainFrame.java           # Fenêtre principale
        ├── components/              # Composants réutilisables (8)
        │   ├── Sidebar.java
        │   ├── StyledTable.java
        │   ├── FormDialog.java
        │   ├── KPICard.java
        │   ├── StatusBadge.java
        │   ├── HeaderBar.java
        │   ├── PaginationPanel.java
        │   └── ActionPopupMenu.java
        └── panels/                  # Panneaux de contenu (8)
            ├── DashboardPanel.java
            ├── MedicamentPanel.java
            ├── ClientPanel.java
            ├── FournisseurPanel.java
            ├── StockLotPanel.java
            ├── VentePanel.java
            ├── ApprovisionnementPanel.java
            └── UtilisateurPanel.java
```

---

## 📝 Licence

Projet académique — E-PHARMACIE © 2024
