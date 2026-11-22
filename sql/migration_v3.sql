-- ============================================================
-- E-PHARMACIE — Migration v3 (idempotente)
-- Ajout : ordonnance_requise, plafond_credit, statut client,
--         paiement_client, audit_log, index composite lot
-- ============================================================

USE e_pharmacie;

-- 1. medicament.ordonnance_requise
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = 'e_pharmacie' AND TABLE_NAME = 'medicament' AND COLUMN_NAME = 'ordonnance_requise');
SET @sql = IF(@col_exists = 0,
    'ALTER TABLE medicament ADD COLUMN ordonnance_requise BOOLEAN NOT NULL DEFAULT FALSE',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 2. client.plafond_credit
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = 'e_pharmacie' AND TABLE_NAME = 'client' AND COLUMN_NAME = 'plafond_credit');
SET @sql = IF(@col_exists = 0,
    'ALTER TABLE client ADD COLUMN plafond_credit DECIMAL(10,2) NOT NULL DEFAULT 1000.00',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 3. client.statut
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = 'e_pharmacie' AND TABLE_NAME = 'client' AND COLUMN_NAME = 'statut');
SET @sql = IF(@col_exists = 0,
    'ALTER TABLE client ADD COLUMN statut ENUM(''ACTIF'', ''INACTIF'') NOT NULL DEFAULT ''ACTIF''',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 4. approvisionnement.statut — étendre l'ENUM pour supporter 'Partiellement reçu'
ALTER TABLE approvisionnement MODIFY COLUMN statut
    ENUM('En attente', 'Partiellement reçu', 'Reçu', 'Annulé') NOT NULL DEFAULT 'En attente';

-- 5. Index composite unique sur lot(id_medicament via ligne_approvisionnement, numero_lot)
-- On crée un index fonctionnel via une table dérivée. Approche alternative : index direct.
SET @idx_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = 'e_pharmacie' AND TABLE_NAME = 'lot' AND INDEX_NAME = 'uq_lot_med_numero');
SET @sql = IF(@idx_exists = 0,
    'CREATE UNIQUE INDEX uq_lot_med_numero ON lot (id_ligne_approvisionnement, numero_lot)',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 6. Table paiement_client
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

-- 7. Table audit_log
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

-- 8. Utilisateur INACTIF pour les tests
INSERT IGNORE INTO utilisateur (nom, prenom, login, mot_de_passe_hash, role, statut)
VALUES ('Testeur', 'Inactif', 'inactif_test',
        '$2a$10$ITAgMBZiAKbP4Wkym3CWNeVxMYSwUiMvlDSS75ZuCRcz2S8/.iBHq',
        'EMPLOYE', 'INACTIF');
