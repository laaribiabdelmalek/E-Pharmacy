-- ============================================================================
-- E-PHARMACIE — Final Clean Database Setup (Schema v3 + Single Admin Account)
-- Target DBMS: MySQL 8.0+ (utf8mb4)
-- ============================================================================

DROP DATABASE IF EXISTS e_pharmacie;
CREATE DATABASE e_pharmacie
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE e_pharmacie;

-- 1. Users & Role-Based Access
CREATE TABLE utilisateur (
    id_utilisateur INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    login VARCHAR(50) NOT NULL UNIQUE,
    mot_de_passe VARCHAR(255) NOT NULL,
    role ENUM('ADMIN', 'PHARMACIEN', 'EMPLOYE') NOT NULL DEFAULT 'EMPLOYE',
    statut ENUM('ACTIF', 'INACTIF') NOT NULL DEFAULT 'ACTIF',
    date_creation DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 2. Company / Pharmacy Settings (for Invoices, Purchase Orders & Receipts)
CREATE TABLE parametres_societe (
    id_parametre INT AUTO_INCREMENT PRIMARY KEY,
    nom_pharmacie VARCHAR(150) NOT NULL,
    adresse VARCHAR(255) NOT NULL,
    telephone VARCHAR(30) NOT NULL,
    email VARCHAR(100),
    ice VARCHAR(30),
    rc VARCHAR(30),
    patente VARCHAR(30),
    if_fiscal VARCHAR(30),
    pied_ticket VARCHAR(255) DEFAULT 'Merci de votre visite et prompt rétablissement !'
) ENGINE=InnoDB;

-- 3. Suppliers
CREATE TABLE fournisseur (
    id_fournisseur INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(150) NOT NULL,
    telephone VARCHAR(30),
    email VARCHAR(100),
    adresse VARCHAR(255),
    statut ENUM('ACTIF', 'INACTIF') NOT NULL DEFAULT 'ACTIF'
) ENGINE=InnoDB;

-- 4. Medications Catalog
CREATE TABLE medicament (
    id_medicament INT AUTO_INCREMENT PRIMARY KEY,
    code_barre VARCHAR(50) UNIQUE,
    nom_commercial VARCHAR(150) NOT NULL,
    dci VARCHAR(150),
    forme VARCHAR(80),
    dosage VARCHAR(80),
    prix_vente DECIMAL(10,2) NOT NULL CHECK (prix_vente >= 0),
    seuil_alerte INT NOT NULL DEFAULT 10 CHECK (seuil_alerte >= 0),
    quantite_disponible INT NOT NULL DEFAULT 0 CHECK (quantite_disponible >= 0),
    ordonnance_requise BOOLEAN NOT NULL DEFAULT FALSE,
    statut ENUM('COMMERCIALISE', 'EN_RUPTURE', 'INACTIF') NOT NULL DEFAULT 'EN_RUPTURE'
) ENGINE=InnoDB;

-- 5. Clients & Credit Limits
CREATE TABLE client (
    id_client INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    telephone VARCHAR(30),
    adresse VARCHAR(255),
    credit DECIMAL(10,2) NOT NULL DEFAULT 0.00 CHECK (credit >= 0),
    plafond_credit DECIMAL(10,2) NOT NULL DEFAULT 1000.00 CHECK (plafond_credit >= 0),
    statut ENUM('ACTIF', 'INACTIF') NOT NULL DEFAULT 'ACTIF'
) ENGINE=InnoDB;

-- 6. Procurement Orders
CREATE TABLE approvisionnement (
    id_approvisionnement INT AUTO_INCREMENT PRIMARY KEY,
    id_fournisseur INT NOT NULL,
    id_utilisateur INT NOT NULL,
    date_commande DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    montant_total DECIMAL(10,2) NOT NULL DEFAULT 0.00 CHECK (montant_total >= 0),
    statut ENUM('En attente', 'Partiellement reçu', 'Reçu', 'Annulé') NOT NULL DEFAULT 'En attente',
    CONSTRAINT fk_appro_fournisseur FOREIGN KEY (id_fournisseur) REFERENCES fournisseur(id_fournisseur),
    CONSTRAINT fk_appro_utilisateur FOREIGN KEY (id_utilisateur) REFERENCES utilisateur(id_utilisateur)
) ENGINE=InnoDB;

-- 7. Procurement Order Lines
CREATE TABLE ligne_approvisionnement (
    id_ligne_approvisionnement INT AUTO_INCREMENT PRIMARY KEY,
    id_approvisionnement INT NOT NULL,
    id_medicament INT NOT NULL,
    quantite_commandee INT NOT NULL CHECK (quantite_commandee > 0),
    quantite_recue INT NOT NULL DEFAULT 0 CHECK (quantite_recue >= 0),
    prix_achat_unitaire DECIMAL(10,2) NOT NULL DEFAULT 0.00 CHECK (prix_achat_unitaire >= 0),
    date_reception DATE NULL,
    CONSTRAINT fk_ligne_appro FOREIGN KEY (id_approvisionnement) REFERENCES approvisionnement(id_approvisionnement) ON DELETE CASCADE,
    CONSTRAINT fk_ligne_appro_med FOREIGN KEY (id_medicament) REFERENCES medicament(id_medicament)
) ENGINE=InnoDB;

-- 8. Stock Batches (FEFO Management)
CREATE TABLE lot (
    id_lot INT AUTO_INCREMENT PRIMARY KEY,
    id_medicament INT NOT NULL,
    id_ligne_approvisionnement INT NULL,
    numero_lot VARCHAR(80) NOT NULL,
    date_expiration DATE NOT NULL,
    quantite_initiale INT NOT NULL CHECK (quantite_initiale > 0),
    quantite_restante INT NOT NULL CHECK (quantite_restante >= 0),
    CONSTRAINT uk_medicament_lot UNIQUE (id_medicament, numero_lot),
    CONSTRAINT fk_lot_medicament FOREIGN KEY (id_medicament) REFERENCES medicament(id_medicament),
    CONSTRAINT fk_lot_ligne_appro FOREIGN KEY (id_ligne_approvisionnement) REFERENCES ligne_approvisionnement(id_ligne_approvisionnement)
) ENGINE=InnoDB;

-- 9. Sales
CREATE TABLE vente (
    id_vente INT AUTO_INCREMENT PRIMARY KEY,
    id_client INT NULL,
    id_utilisateur INT NOT NULL,
    date_vente DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    montant_total DECIMAL(10,2) NOT NULL DEFAULT 0.00 CHECK (montant_total >= 0),
    mode_paiement ENUM('ESPECES', 'CARTE', 'CREDIT') NOT NULL DEFAULT 'ESPECES',
    statut ENUM('VALIDEE', 'ANNULEE') NOT NULL DEFAULT 'VALIDEE',
    CONSTRAINT fk_vente_client FOREIGN KEY (id_client) REFERENCES client(id_client),
    CONSTRAINT fk_vente_utilisateur FOREIGN KEY (id_utilisateur) REFERENCES utilisateur(id_utilisateur)
) ENGINE=InnoDB;

-- 10. Sale Lines
CREATE TABLE ligne_vente (
    id_ligne_vente INT AUTO_INCREMENT PRIMARY KEY,
    id_vente INT NOT NULL,
    id_medicament INT NOT NULL,
    id_lot INT NULL,
    quantite INT NOT NULL CHECK (quantite > 0),
    prix_unitaire DECIMAL(10,2) NOT NULL CHECK (prix_unitaire >= 0),
    sous_total DECIMAL(10,2) NOT NULL CHECK (sous_total >= 0),
    CONSTRAINT fk_lv_vente FOREIGN KEY (id_vente) REFERENCES vente(id_vente) ON DELETE CASCADE,
    CONSTRAINT fk_lv_medicament FOREIGN KEY (id_medicament) REFERENCES medicament(id_medicament),
    CONSTRAINT fk_lv_lot FOREIGN KEY (id_lot) REFERENCES lot(id_lot)
) ENGINE=InnoDB;

-- 11. Client Credit Repayments
CREATE TABLE paiement_client (
    id_paiement INT AUTO_INCREMENT PRIMARY KEY,
    id_client INT NOT NULL,
    id_utilisateur INT NOT NULL,
    montant DECIMAL(10,2) NOT NULL CHECK (montant > 0),
    mode_paiement ENUM('ESPECES', 'CARTE', 'VIREMENT') NOT NULL DEFAULT 'ESPECES',
    date_paiement DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_paiement_client FOREIGN KEY (id_client) REFERENCES client(id_client),
    CONSTRAINT fk_paiement_utilisateur FOREIGN KEY (id_utilisateur) REFERENCES utilisateur(id_utilisateur)
) ENGINE=InnoDB;

-- 12. Security & Pharmaceutical Audit Trail
CREATE TABLE audit_log (
    id_audit INT AUTO_INCREMENT PRIMARY KEY,
    id_utilisateur INT NULL,
    action VARCHAR(50) NOT NULL,
    entite VARCHAR(50) NOT NULL,
    id_entite INT NULL,
    details TEXT,
    date_action DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_audit_utilisateur FOREIGN KEY (id_utilisateur) REFERENCES utilisateur(id_utilisateur) ON DELETE SET NULL
) ENGINE=InnoDB;

-- ============================================================================
-- INITIAL DATA: Default Company Profile + Single Active ADMIN User
-- ============================================================================

INSERT INTO parametres_societe (nom_pharmacie, adresse, telephone, email, ice, rc, patente, if_fiscal, pied_ticket)
VALUES (
    'E-PHARMACIE',
    'Avenue Mohammed V, Marrakech',
    '0524-00-00-00',
    'contact@epharmacie.ma',
    '001234567000089',
    '12345',
    '45123456',
    '87654321',
    'Merci de votre confiance et prompt rétablissement !'
);

-- Single Active Administrator Account
-- Login    : admin
-- Password : password  (Change it inside the Utilisateurs screen after first login)
INSERT INTO utilisateur (nom, prenom, login, mot_de_passe, role, statut)
VALUES (
    'Administrateur',
    'Principal',
    'admin',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'ADMIN',
    'ACTIF'
);