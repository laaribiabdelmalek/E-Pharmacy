-- ============================================================
-- E-PHARMACIE — Schéma complet de la base de données (v3.0)
-- MySQL 8+  |  Charset : utf8mb4  |  Collation : utf8mb4_unicode_ci
-- ============================================================

-- Création de la base
CREATE DATABASE IF NOT EXISTS e_pharmacie
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE e_pharmacie;

-- ============================================================
-- 1. UTILISATEUR — Comptes utilisateurs de l'application
-- ============================================================
CREATE TABLE IF NOT EXISTS utilisateur (
    id_utilisateur      INT             AUTO_INCREMENT PRIMARY KEY,
    nom                 VARCHAR(100)    NOT NULL,
    prenom              VARCHAR(100)    NOT NULL,
    login               VARCHAR(50)     NOT NULL UNIQUE,
    mot_de_passe_hash   VARCHAR(255)    NOT NULL,
    role                ENUM('ADMIN', 'PHARMACIEN', 'EMPLOYE') NOT NULL,
    statut              ENUM('ACTIF', 'INACTIF') NOT NULL DEFAULT 'ACTIF',
    date_creation       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_utilisateur_role   (role),
    INDEX idx_utilisateur_statut (statut)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 2. CLIENT — Clients de la pharmacie
-- ============================================================
CREATE TABLE IF NOT EXISTS client (
    id_client           INT             AUTO_INCREMENT PRIMARY KEY,
    nom                 VARCHAR(100)    NOT NULL,
    prenom              VARCHAR(100)    NOT NULL,
    telephone           VARCHAR(20),
    email               VARCHAR(150),
    adresse             TEXT,
    date_inscription    DATE            NOT NULL DEFAULT (CURRENT_DATE),
    credit              DECIMAL(10,2)   NOT NULL DEFAULT 0.00,
    plafond_credit      DECIMAL(10,2)   NOT NULL DEFAULT 1000.00,
    statut              ENUM('ACTIF', 'INACTIF') NOT NULL DEFAULT 'ACTIF',

    INDEX idx_client_nom     (nom, prenom),
    INDEX idx_client_tel     (telephone),
    INDEX idx_client_statut  (statut)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 3. FOURNISSEUR — Fournisseurs de médicaments
-- ============================================================
CREATE TABLE IF NOT EXISTS fournisseur (
    id_fournisseur      INT             AUTO_INCREMENT PRIMARY KEY,
    ice                 VARCHAR(20)     UNIQUE,
    raison_sociale      VARCHAR(200)    NOT NULL,
    nom_commercial      VARCHAR(200),
    telephone           VARCHAR(20),
    email               VARCHAR(150),
    adresse             TEXT,
    ville               VARCHAR(100),
    nom_contact         VARCHAR(150),
    statut              ENUM('ACTIF', 'INACTIF') NOT NULL DEFAULT 'ACTIF',

    INDEX idx_fournisseur_ville  (ville),
    INDEX idx_fournisseur_statut (statut)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 4. MEDICAMENT — Catalogue de médicaments
-- ============================================================
CREATE TABLE IF NOT EXISTS medicament (
    id_medicament           INT             AUTO_INCREMENT PRIMARY KEY,
    nom_commercial          VARCHAR(200)    NOT NULL,
    dci                     VARCHAR(200),
    dosage                  VARCHAR(100),
    forme_pharmaceutique    ENUM(
        'COMPRIME', 'GELULE', 'SIROP', 'INJECTABLE', 'POMMADE',
        'SUPPOSITOIRE', 'COLLYRE', 'SACHET', 'SOLUTION', 'AUTRE'
    ) NOT NULL DEFAULT 'COMPRIME',
    conditionnement         VARCHAR(100),
    laboratoire             VARCHAR(200),
    type_medicament         ENUM('PRINCEPS', 'GENERIQUE', 'OTC') NOT NULL DEFAULT 'PRINCEPS',
    conditions_conservation VARCHAR(200),
    categorie               VARCHAR(100),
    prix_vente_public       DECIMAL(10,2)   NOT NULL,
    seuil_minimal           INT             NOT NULL DEFAULT 10,
    statut                  ENUM('COMMERCIALISE', 'NON_COMMERCIALISE', 'EN_RUPTURE', 'INACTIF') NOT NULL DEFAULT 'COMMERCIALISE',
    quantite_disponible     INT             NOT NULL DEFAULT 0,
    ordonnance_requise      BOOLEAN         NOT NULL DEFAULT FALSE,

    CONSTRAINT chk_prix_vente          CHECK (prix_vente_public >= 0),
    CONSTRAINT chk_seuil_minimal       CHECK (seuil_minimal >= 0),
    CONSTRAINT chk_quantite_disponible CHECK (quantite_disponible >= 0),

    INDEX idx_med_nom           (nom_commercial),
    INDEX idx_med_dci           (dci),
    INDEX idx_med_forme         (forme_pharmaceutique),
    INDEX idx_med_type          (type_medicament),
    INDEX idx_med_categorie     (categorie),
    INDEX idx_med_statut        (statut),
    INDEX idx_med_laboratoire   (laboratoire)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 5. APPROVISIONNEMENT — Commandes fournisseurs
-- ============================================================
CREATE TABLE IF NOT EXISTS approvisionnement (
    id_approvisionnement    INT             AUTO_INCREMENT PRIMARY KEY,
    id_fournisseur          INT             NOT NULL,
    id_utilisateur          INT             NOT NULL,
    date_approvisionnement  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    montant_total           DECIMAL(12,2)   NOT NULL DEFAULT 0.00,
    statut                  ENUM('En attente', 'Partiellement reçu', 'Reçu', 'Annulé') NOT NULL DEFAULT 'En attente',

    CONSTRAINT fk_appro_fournisseur
        FOREIGN KEY (id_fournisseur) REFERENCES fournisseur(id_fournisseur)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_appro_utilisateur
        FOREIGN KEY (id_utilisateur) REFERENCES utilisateur(id_utilisateur)
        ON UPDATE CASCADE ON DELETE RESTRICT,

    INDEX idx_appro_date   (date_approvisionnement),
    INDEX idx_appro_statut (statut)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 6. LIGNE_APPROVISIONNEMENT — Détail de chaque commande
-- ============================================================
CREATE TABLE IF NOT EXISTS ligne_approvisionnement (
    id_ligne_approvisionnement  INT             AUTO_INCREMENT PRIMARY KEY,
    id_approvisionnement        INT             NOT NULL,
    id_medicament               INT             NOT NULL,
    date_reception              DATE,
    quantite_commandee          INT             NOT NULL,
    quantite_recue              INT             NOT NULL DEFAULT 0,
    prix_achat_unitaire         DECIMAL(10,2)   NOT NULL,

    CONSTRAINT chk_la_qte_cmd   CHECK (quantite_commandee > 0),
    CONSTRAINT chk_la_qte_recu  CHECK (quantite_recue >= 0),
    CONSTRAINT chk_la_prix      CHECK (prix_achat_unitaire >= 0),

    CONSTRAINT fk_la_approvisionnement
        FOREIGN KEY (id_approvisionnement) REFERENCES approvisionnement(id_approvisionnement)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_la_medicament
        FOREIGN KEY (id_medicament) REFERENCES medicament(id_medicament)
        ON UPDATE CASCADE ON DELETE RESTRICT,

    INDEX idx_la_medicament (id_medicament)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 7. LOT — Lots de médicaments avec traçabilité
-- ============================================================
CREATE TABLE IF NOT EXISTS lot (
    id_lot                      INT             AUTO_INCREMENT PRIMARY KEY,
    id_ligne_approvisionnement  INT             NOT NULL,
    numero_lot                  VARCHAR(50)     NOT NULL,
    date_expiration             DATE            NOT NULL,
    quantite_initiale           INT             NOT NULL,
    quantite_restante           INT             NOT NULL,

    CONSTRAINT chk_lot_qte_init  CHECK (quantite_initiale > 0),
    CONSTRAINT chk_lot_qte_rest  CHECK (quantite_restante >= 0),

    CONSTRAINT fk_lot_ligne_appro
        FOREIGN KEY (id_ligne_approvisionnement) REFERENCES ligne_approvisionnement(id_ligne_approvisionnement)
        ON UPDATE CASCADE ON DELETE CASCADE,

    INDEX idx_lot_expiration (date_expiration),
    INDEX idx_lot_numero     (numero_lot)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 8. VENTE — Ventes effectuées au comptoir
-- ============================================================
CREATE TABLE IF NOT EXISTS vente (
    id_vente        INT             AUTO_INCREMENT PRIMARY KEY,
    id_utilisateur  INT             NOT NULL,
    id_client       INT             NULL,
    date_vente      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    montant_total   DECIMAL(12,2)   NOT NULL DEFAULT 0.00,
    mode_paiement   ENUM('ESPECES', 'CARTE', 'CREDIT') NOT NULL DEFAULT 'ESPECES',
    statut          ENUM('VALIDEE', 'ANNULEE') NOT NULL DEFAULT 'VALIDEE',

    CONSTRAINT fk_vente_utilisateur
        FOREIGN KEY (id_utilisateur) REFERENCES utilisateur(id_utilisateur)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_vente_client
        FOREIGN KEY (id_client) REFERENCES client(id_client)
        ON UPDATE CASCADE ON DELETE SET NULL,

    INDEX idx_vente_date    (date_vente),
    INDEX idx_vente_statut  (statut),
    INDEX idx_vente_client  (id_client)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 9. LIGNE_VENTE — Détail de chaque vente
-- ============================================================
CREATE TABLE IF NOT EXISTS ligne_vente (
    id_ligne_vente      INT             AUTO_INCREMENT PRIMARY KEY,
    id_vente            INT             NOT NULL,
    id_lot              INT             NOT NULL,
    quantite            INT             NOT NULL,
    prix_unitaire_vente DECIMAL(10,2)   NOT NULL,
    sous_total          DECIMAL(10,2)   NOT NULL,

    CONSTRAINT chk_lv_quantite   CHECK (quantite > 0),
    CONSTRAINT chk_lv_prix       CHECK (prix_unitaire_vente >= 0),
    CONSTRAINT chk_lv_sous_total CHECK (sous_total >= 0),

    CONSTRAINT fk_lv_vente
        FOREIGN KEY (id_vente) REFERENCES vente(id_vente)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_lv_lot
        FOREIGN KEY (id_lot) REFERENCES lot(id_lot)
        ON UPDATE CASCADE ON DELETE RESTRICT,

    INDEX idx_lv_lot (id_lot)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 10. PARAMETRES_SOCIETE — Paramètres de la pharmacie
-- ============================================================
CREATE TABLE IF NOT EXISTS parametres_societe (
    id INT PRIMARY KEY DEFAULT 1,
    nom_pharmacie VARCHAR(100) NOT NULL,
    ice VARCHAR(20),
    identifiant_fiscal VARCHAR(20),
    rc VARCHAR(30),
    patente VARCHAR(30),
    telephone VARCHAR(20),
    email VARCHAR(100),
    adresse TEXT,
    ville VARCHAR(50),
    message_ticket VARCHAR(255),
    theme_app ENUM('CLAIR', 'SOMBRE', 'SYSTEME') DEFAULT 'CLAIR'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT IGNORE INTO parametres_societe
    (id, nom_pharmacie, ice, identifiant_fiscal, rc, patente, telephone, email, adresse, ville, message_ticket, theme_app)
VALUES
    (1, 'Pharmacie E-Pharmacie', '001524896000045', '45896321', 'RC 14520', '45218963', '05 24 43 00 00', 'contact@epharmacie.ma', 'Avenue Mohammed V, Guéliz', 'Marrakech', 'Merci de votre visite. Prompt rétablissement !', 'CLAIR');

-- ============================================================
-- 11. PAIEMENT_CLIENT — Règlements de crédit client
-- ============================================================
CREATE TABLE IF NOT EXISTS paiement_client (
    id_paiement     INT             AUTO_INCREMENT PRIMARY KEY,
    id_client       INT             NOT NULL,
    id_utilisateur  INT             NOT NULL,
    montant         DECIMAL(10,2)   NOT NULL,
    mode_paiement   ENUM('ESPECES', 'CARTE', 'VIREMENT') NOT NULL DEFAULT 'ESPECES',
    date_paiement   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_paiement_client
        FOREIGN KEY (id_client) REFERENCES client(id_client)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_paiement_utilisateur
        FOREIGN KEY (id_utilisateur) REFERENCES utilisateur(id_utilisateur)
        ON UPDATE CASCADE ON DELETE RESTRICT,

    CONSTRAINT chk_paiement_montant CHECK (montant > 0),

    INDEX idx_paiement_client (id_client),
    INDEX idx_paiement_date   (date_paiement)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 12. AUDIT_LOG — Journal d'audit des opérations sensibles
-- ============================================================
CREATE TABLE IF NOT EXISTS audit_log (
    id_audit        INT             AUTO_INCREMENT PRIMARY KEY,
    id_utilisateur  INT,
    action          VARCHAR(50)     NOT NULL,
    entite          VARCHAR(50)     NOT NULL,
    id_entite       INT,
    details         TEXT,
    date_action     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_audit_utilisateur (id_utilisateur),
    INDEX idx_audit_action      (action),
    INDEX idx_audit_entite      (entite),
    INDEX idx_audit_date        (date_action)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
