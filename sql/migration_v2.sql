-- ============================================================
-- E-PHARMACIE — Migration v2
-- Safe migration for existing e_pharmacie databases
-- ============================================================

SET NAMES utf8mb4;
USE e_pharmacie;

-- ============================================================
-- 1. Update approvisionnement.statut ENUM to match MERISE schema
-- ============================================================
ALTER TABLE approvisionnement
    MODIFY COLUMN statut ENUM('En attente', 'Reçu', 'Annulé', 'EN_COURS', 'VALIDEE', 'ANNULEE')
    NOT NULL DEFAULT 'En attente';

UPDATE approvisionnement SET statut = 'Reçu' WHERE statut = 'VALIDEE';
UPDATE approvisionnement SET statut = 'En attente' WHERE statut = 'EN_COURS';
UPDATE approvisionnement SET statut = 'Annulé' WHERE statut = 'ANNULEE';

ALTER TABLE approvisionnement
    MODIFY COLUMN statut ENUM('En attente', 'Reçu', 'Annulé')
    NOT NULL DEFAULT 'En attente';

-- ============================================================
-- 2. Create parametres_societe table
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
