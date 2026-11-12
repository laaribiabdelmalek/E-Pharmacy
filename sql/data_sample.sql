-- ============================================================
-- E-PHARMACIE — Données de test réalistes (contexte marocain)
-- ============================================================

USE e_pharmacie;

-- ============================================================
-- UTILISATEURS
-- Mots de passe : admin123 / pharma123 / emp123
-- ============================================================
INSERT INTO utilisateur (nom, prenom, login, mot_de_passe_hash, role, statut) VALUES
('Benani',    'Youssef', 'admin',      '$2a$10$ITAgMBZiAKbP4Wkym3CWNeVxMYSwUiMvlDSS75ZuCRcz2S8/.iBHq', 'ADMIN',      'ACTIF'),
('El Amrani', 'Sara',    'pharmacien', '$2a$10$fetpbg3Vm4UZacssUp1AgOh5H.MPuKJXqidvrFjJjHHychiy.4O.S', 'PHARMACIEN', 'ACTIF'),
('Tazi',      'Mehdi',   'employe',    '$2a$10$n3tqY0tIJlipZuVmMpAqnuM.gBoowmXCKPIWvJaZkDCXuQjKivpRy', 'EMPLOYE',    'ACTIF');
-- ============================================================
-- CLIENTS
-- ============================================================
INSERT INTO client (nom, prenom, telephone, email, adresse, date_inscription, credit) VALUES
('Alaoui',     'Mohammed',  '0661123456', 'mohammed.alaoui@gmail.com',   '12 Rue Hassan II, Casablanca',          '2024-01-15', 0.00),
('Benkirane',  'Fatima',    '0662234567', 'fatima.benkirane@yahoo.fr',   '45 Bd Mohammed V, Rabat',               '2024-02-20', 150.00),
('El Fassi',   'Ahmed',     '0663345678', 'ahmed.elfassi@hotmail.com',   '8 Rue Allal Ben Abdellah, Fès',         '2024-03-10', 0.00),
('Chraibi',    'Khadija',   '0664456789', 'khadija.chraibi@gmail.com',   '23 Ave des FAR, Marrakech',             '2024-03-25', 75.50),
('Bouazza',    'Youssef',   '0665567890', 'youssef.bouazza@outlook.com', '56 Rue Moulay Ismail, Tanger',          '2024-04-05', 0.00),
('Idrissi',    'Amina',     '0666678901', 'amina.idrissi@gmail.com',     '34 Bd Zerktouni, Casablanca',           '2024-04-18', 200.00),
('Senhaji',    'Omar',      '0667789012', 'omar.senhaji@yahoo.fr',       '17 Rue Ibn Batouta, Meknès',            '2024-05-02', 0.00),
('Berrada',    'Zineb',     '0668890123', 'zineb.berrada@gmail.com',     '9 Ave Mohammed VI, Agadir',             '2024-05-20', 50.00),
('Lahlou',     'Hassan',    '0669901234', 'hassan.lahlou@hotmail.com',   '62 Rue de la Liberté, Kénitra',         '2024-06-10', 0.00),
('Kettani',    'Salma',     '0670012345', 'salma.kettani@gmail.com',     '28 Bd Bir Anzarane, Oujda',             '2024-06-28', 120.00);

-- ============================================================
-- FOURNISSEURS
-- ICE : Identifiant Commun de l'Entreprise (15 chiffres au Maroc)
-- ============================================================
INSERT INTO fournisseur (ice, raison_sociale, nom_commercial, telephone, email, adresse, ville, nom_contact, statut) VALUES
('001475326000048', 'Pharma 5 SA',                 'Pharma 5',      '0522953800', 'contact@pharma5.ma',     'Zone Industrielle, Ouled Salah, Bouskoura',     'Casablanca', 'Rachid Lamrini',    'ACTIF'),
('001623489000015', 'Cooper Pharma SA',             'Cooper Pharma', '0522764500', 'info@cooperpharma.ma',   'Zone Industrielle, Sidi Maarouf',               'Casablanca', 'Nadia Bennani',     'ACTIF'),
('001587234000062', 'Sothema SA',                   'Sothema',       '0522974200', 'contact@sothema.ma',     'Z.I. Bouskoura, BP 1, Nouaceur',                'Casablanca', 'Karim El Harti',    'ACTIF'),
('001298745000031', 'Maphar SA',                    'Maphar',        '0522353600', 'info@maphar.ma',         '21 Rue Ibnou Majid, Ain Sebaâ',                 'Casablanca', 'Leila Tazi',        'ACTIF'),
('001834567000079', 'Laboratoires Laprophan SA',    'Laprophan',     '0522668800', 'contact@laprophan.ma',   'Route de Nouaceur, Zone Industrielle Sapino',   'Casablanca', 'Adil Mansouri',     'ACTIF');

-- ============================================================
-- MEDICAMENTS (20+ références)
-- Prix en DH (Dirham marocain) — prix réalistes du marché
-- ============================================================
INSERT INTO medicament (nom_commercial, dci, dosage, forme_pharmaceutique, conditionnement, laboratoire, type_medicament, conditions_conservation, categorie, prix_vente_public, seuil_minimal, statut, quantite_disponible) VALUES
('Doliprane',       'Paracétamol',            '1000 mg',  'COMPRIME',    'Boîte de 8',      'Sanofi',         'PRINCEPS',   'Conserver à T° < 25°C',          'Antalgique',              25.00,  20, 'COMMERCIALISE', 150),
('Amoxicilline EG', 'Amoxicilline',           '500 mg',   'GELULE',      'Boîte de 12',     'Cooper Pharma',  'GENERIQUE',  'Conserver à T° < 25°C',          'Antibiotique',            35.00,  15, 'COMMERCIALISE', 100),
('Voltarène',       'Diclofénac',             '50 mg',    'COMPRIME',    'Boîte de 30',     'Novartis',       'PRINCEPS',   'Conserver à T° < 30°C',          'Anti-inflammatoire',      65.00,  10, 'COMMERCIALISE', 80),
('Augmentin',       'Amoxicilline/Ac. clav.', '1g/125mg', 'COMPRIME',    'Boîte de 12',     'GSK',            'PRINCEPS',   'Conserver à T° < 25°C',          'Antibiotique',            85.00,  10, 'COMMERCIALISE', 60),
('Aspégic',         'Acide acétylsalicylique','1000 mg',  'SACHET',      'Boîte de 20',     'Sanofi',         'PRINCEPS',   'Conserver à T° < 25°C',          'Antalgique',              42.00,  15, 'COMMERCIALISE', 90),
('Ventoline',       'Salbutamol',             '100 µg',   'SOLUTION',    'Flacon 200 doses','GSK',            'PRINCEPS',   'Conserver à T° < 30°C',          'Bronchodilatateur',       55.00,  10, 'COMMERCIALISE', 45),
('Clamoxyl',        'Amoxicilline',           '500 mg',   'GELULE',      'Boîte de 12',     'GSK',            'PRINCEPS',   'Conserver à T° < 25°C',          'Antibiotique',            40.00,  10, 'COMMERCIALISE', 70),
('Smecta',          'Diosmectite',            '3 g',      'SACHET',      'Boîte de 30',     'Ipsen',          'PRINCEPS',   'Conserver à T° < 25°C',          'Antidiarrhéique',         55.00,  10, 'COMMERCIALISE', 85),
('Nexium',          'Ésoméprazole',           '20 mg',    'GELULE',      'Boîte de 14',     'AstraZeneca',    'PRINCEPS',   'Conserver à T° < 25°C',          'IPP',                     120.00, 10, 'COMMERCIALISE', 50),
('Levothyrox',      'Lévothyroxine',          '100 µg',   'COMPRIME',    'Boîte de 30',     'Merck',          'PRINCEPS',   'Conserver à T° < 25°C, abri lumière', 'Thyroïde',           38.00,  15, 'COMMERCIALISE', 65),
('Glucophage',      'Metformine',             '850 mg',   'COMPRIME',    'Boîte de 30',     'Merck',          'PRINCEPS',   'Conserver à T° < 25°C',          'Antidiabétique',          30.00,  20, 'COMMERCIALISE', 120),
('Kardégic',        'Acide acétylsalicylique','75 mg',    'SACHET',      'Boîte de 30',     'Sanofi',         'PRINCEPS',   'Conserver à T° < 25°C',          'Antiagrégant',            48.00,  10, 'COMMERCIALISE', 70),
('Efferalgan',      'Paracétamol',            '500 mg',   'COMPRIME',    'Boîte de 16',     'UPSA',           'PRINCEPS',   'Conserver à T° < 25°C',          'Antalgique',              22.00,  20, 'COMMERCIALISE', 130),
('Celestène',       'Bétaméthasone',          '2 mg',     'COMPRIME',    'Boîte de 30',     'MSD',            'PRINCEPS',   'Conserver à T° < 25°C',          'Corticoïde',              45.00,  10, 'COMMERCIALISE', 40),
('Spasfon',         'Phloroglucinol',         '80 mg',    'COMPRIME',    'Boîte de 30',     'Teva',           'PRINCEPS',   'Conserver à T° < 25°C',          'Antispasmodique',         35.00,  15, 'COMMERCIALISE', 95),
('Toplexil',        'Oxomémazine',            '0.33 mg/ml','SIROP',      'Flacon 150 ml',   'Sanofi',         'PRINCEPS',   'Conserver à T° < 25°C',          'Antitussif',              38.00,  10, 'COMMERCIALISE', 55),
('Daflon',          'Diosmine/Hespéridine',   '500 mg',   'COMPRIME',    'Boîte de 30',     'Servier',        'PRINCEPS',   'Conserver à T° < 25°C',          'Veinotonique',            75.00,  10, 'COMMERCIALISE', 60),
('Maalox',          'Hydroxyde Al/Mg',        '400/400 mg','COMPRIME',   'Boîte de 40',     'Sanofi',         'OTC',        'Conserver à T° < 25°C',          'Antiacide',               32.00,  15, 'COMMERCIALISE', 110),
('Betadine',        'Povidone iodée',         '10%',      'SOLUTION',    'Flacon 125 ml',   'Mylan',          'OTC',        'Conserver à T° < 25°C',          'Antiseptique',            28.00,  10, 'COMMERCIALISE', 75),
('Ibuprofène EG',   'Ibuprofène',             '400 mg',   'COMPRIME',    'Boîte de 30',     'Pharma 5',       'GENERIQUE',  'Conserver à T° < 25°C',          'Anti-inflammatoire',      25.00,  15, 'COMMERCIALISE', 100),
('Amoxil',          'Amoxicilline',           '250 mg/5ml','SIROP',      'Flacon 60 ml',    'GSK',            'PRINCEPS',   'Après reconstitution : réfrigérer','Antibiotique',           42.00,  10, 'COMMERCIALISE', 55),
('Xanax',           'Alprazolam',             '0.25 mg',  'COMPRIME',    'Boîte de 30',     'Pfizer',         'PRINCEPS',   'Conserver à T° < 25°C',          'Anxiolytique',            50.00,  10, 'COMMERCIALISE', 30),
('Dermalibour',     'Cuivre/Zinc',            '—',        'POMMADE',     'Tube 15 g',       'A-Derma',        'OTC',        'Conserver à T° < 25°C',          'Dermatologie',            65.00,  10, 'COMMERCIALISE', 35),
('Maxilase',        'Alpha-amylase',          '3000 UI',  'SIROP',       'Flacon 200 ml',   'Sanofi',         'OTC',        'Conserver à T° < 25°C',          'Anti-inflammatoire ORL',  45.00,  10, 'COMMERCIALISE', 50);

-- ============================================================
-- APPROVISIONNEMENTS (5 commandes)
-- ============================================================
INSERT INTO approvisionnement (id_fournisseur, id_utilisateur, date_approvisionnement, montant_total, statut) VALUES
(1, 1, '2024-06-01 09:30:00',  4500.00, 'VALIDEE'),   -- Pharma 5
(2, 1, '2024-06-10 10:00:00',  6200.00, 'VALIDEE'),   -- Cooper Pharma
(3, 2, '2024-06-20 14:15:00',  3800.00, 'VALIDEE'),   -- Sothema
(4, 1, '2024-07-05 08:45:00',  5100.00, 'VALIDEE'),   -- Maphar
(5, 2, '2024-07-15 11:00:00',  2900.00, 'EN_COURS');   -- Laprophan

-- ============================================================
-- LIGNES D'APPROVISIONNEMENT
-- ============================================================
-- Approvisionnement 1 (Pharma 5)
INSERT INTO ligne_approvisionnement (id_approvisionnement, id_medicament, date_reception, quantite_commandee, quantite_recue, prix_achat_unitaire) VALUES
(1, 1,  '2024-06-03', 200, 200, 15.00),   -- Doliprane
(1, 20, '2024-06-03', 150, 150, 14.00),   -- Ibuprofène EG
(1, 13, '2024-06-03', 100, 100, 12.00);   -- Efferalgan

-- Approvisionnement 2 (Cooper Pharma)
INSERT INTO ligne_approvisionnement (id_approvisionnement, id_medicament, date_reception, quantite_commandee, quantite_recue, prix_achat_unitaire) VALUES
(2, 2,  '2024-06-12', 120, 120, 20.00),   -- Amoxicilline EG
(2, 7,  '2024-06-12', 100, 100, 24.00),   -- Clamoxyl
(2, 4,  '2024-06-12', 80,  80,  52.00);   -- Augmentin

-- Approvisionnement 3 (Sothema)
INSERT INTO ligne_approvisionnement (id_approvisionnement, id_medicament, date_reception, quantite_commandee, quantite_recue, prix_achat_unitaire) VALUES
(3, 11, '2024-06-22', 150, 150, 18.00),   -- Glucophage
(3, 10, '2024-06-22', 100, 100, 22.00);   -- Levothyrox

-- Approvisionnement 4 (Maphar)
INSERT INTO ligne_approvisionnement (id_approvisionnement, id_medicament, date_reception, quantite_commandee, quantite_recue, prix_achat_unitaire) VALUES
(4, 8,  '2024-07-07', 100, 100, 32.00),   -- Smecta
(4, 15, '2024-07-07', 120, 120, 20.00),   -- Spasfon
(4, 18, '2024-07-07', 130, 130, 18.00);   -- Maalox

-- Approvisionnement 5 (Laprophan - en cours)
INSERT INTO ligne_approvisionnement (id_approvisionnement, id_medicament, date_reception, quantite_commandee, quantite_recue, prix_achat_unitaire) VALUES
(5, 9,  NULL, 60, 0, 72.00),   -- Nexium (pas encore reçu)
(5, 17, NULL, 80, 0, 45.00);   -- Daflon (pas encore reçu)

-- ============================================================
-- LOTS — Traçabilité des médicaments reçus
-- Quantités restantes = quantité initiale - quantités vendues
-- ============================================================
-- Lots pour Approvisionnement 1
INSERT INTO lot (id_ligne_approvisionnement, numero_lot, date_expiration, quantite_initiale, quantite_restante) VALUES
(1, 'DOL-2024-A001', '2026-06-30', 200, 150),   -- Doliprane (50 vendus)
(2, 'IBU-2024-A002', '2026-09-30', 150, 100),   -- Ibuprofène EG (50 vendus)
(3, 'EFF-2024-A003', '2026-12-31', 100, 130);    -- Efferalgan (corrigé ci-dessous → 70 restants seront ajustés)

-- Correction : Efferalgan → lot 3 : 100 initiales, vendus 0 (on ajuste plus tard)
UPDATE lot SET quantite_restante = 100 WHERE id_lot = 3;

-- Lots pour Approvisionnement 2
INSERT INTO lot (id_ligne_approvisionnement, numero_lot, date_expiration, quantite_initiale, quantite_restante) VALUES
(4, 'AMX-2024-B001', '2025-12-31', 120, 100),   -- Amoxicilline EG (20 vendus)
(5, 'CLM-2024-B002', '2026-03-31', 100, 70),    -- Clamoxyl (30 vendus)
(6, 'AUG-2024-B003', '2026-06-30', 80,  60);    -- Augmentin (20 vendus)

-- Lots pour Approvisionnement 3
INSERT INTO lot (id_ligne_approvisionnement, numero_lot, date_expiration, quantite_initiale, quantite_restante) VALUES
(7, 'GLC-2024-C001', '2026-08-31', 150, 120),   -- Glucophage (30 vendus)
(8, 'LVT-2024-C002', '2027-01-31', 100, 65);    -- Levothyrox (35 vendus)

-- Lots pour Approvisionnement 4
INSERT INTO lot (id_ligne_approvisionnement, numero_lot, date_expiration, quantite_initiale, quantite_restante) VALUES
(9,  'SMC-2024-D001', '2026-11-30', 100, 85),   -- Smecta (15 vendus)
(10, 'SPF-2024-D002', '2027-04-30', 120, 95),   -- Spasfon (25 vendus)
(11, 'MLX-2024-D003', '2026-10-31', 130, 110);  -- Maalox (20 vendus)

-- ============================================================
-- VENTES (10 ventes)
-- ============================================================
INSERT INTO vente (id_utilisateur, id_client, date_vente, montant_total, mode_paiement, statut) VALUES
(2, 1,    '2024-07-01 09:15:00',  175.00,  'ESPECES', 'VALIDEE'),   -- V1
(2, 2,    '2024-07-01 10:30:00',  255.00,  'CARTE',   'VALIDEE'),   -- V2
(3, 3,    '2024-07-02 11:00:00',  120.00,  'ESPECES', 'VALIDEE'),   -- V3
(2, NULL, '2024-07-02 14:20:00',   85.00,  'ESPECES', 'VALIDEE'),   -- V4 client anonyme
(3, 4,    '2024-07-03 09:45:00',  340.00,  'CREDIT',  'VALIDEE'),   -- V5
(2, 5,    '2024-07-03 15:00:00',  160.00,  'ESPECES', 'VALIDEE'),   -- V6
(3, 6,    '2024-07-04 10:10:00',  210.00,  'CARTE',   'VALIDEE'),   -- V7
(2, 7,    '2024-07-05 08:30:00',   95.00,  'ESPECES', 'VALIDEE'),   -- V8
(3, 8,    '2024-07-05 16:45:00',  385.00,  'CARTE',   'VALIDEE'),   -- V9
(2, 10,   '2024-07-06 11:20:00',  150.00,  'ESPECES', 'VALIDEE');   -- V10

-- ============================================================
-- LIGNES DE VENTE
-- sous_total = quantite × prix_unitaire_vente
-- Les quantités sont déduites des lots ci-dessus
-- ============================================================

-- Vente 1 : Doliprane ×2 + Ibuprofène ×5 = 50 + 125 = 175 DH
INSERT INTO ligne_vente (id_vente, id_lot, quantite, prix_unitaire_vente, sous_total) VALUES
(1, 1, 2, 25.00, 50.00),    -- Doliprane lot 1
(1, 2, 5, 25.00, 125.00);   -- Ibuprofène lot 2

-- Vente 2 : Augmentin ×2 + Clamoxyl ×2 = 170 + 80 = 250 DH (arrondi 255)
INSERT INTO ligne_vente (id_vente, id_lot, quantite, prix_unitaire_vente, sous_total) VALUES
(2, 6, 2, 85.00, 170.00),   -- Augmentin lot 6
(2, 5, 2, 40.00, 80.00);    -- Clamoxyl lot 5
-- Note: montant_total vente = 250, corrigeons
UPDATE vente SET montant_total = 250.00 WHERE id_vente = 2;

-- Vente 3 : Glucophage ×4 = 120 DH
INSERT INTO ligne_vente (id_vente, id_lot, quantite, prix_unitaire_vente, sous_total) VALUES
(3, 7, 4, 30.00, 120.00);   -- Glucophage lot 7

-- Vente 4 : Augmentin ×1 = 85 DH
INSERT INTO ligne_vente (id_vente, id_lot, quantite, prix_unitaire_vente, sous_total) VALUES
(4, 6, 1, 85.00, 85.00);    -- Augmentin lot 6

-- Vente 5 : Levothyrox ×5 + Clamoxyl ×3 + Doliprane ×2 = 190+120+50 = 360 DH
INSERT INTO ligne_vente (id_vente, id_lot, quantite, prix_unitaire_vente, sous_total) VALUES
(5, 8, 5,  38.00, 190.00),  -- Levothyrox lot 8
(5, 5, 3,  40.00, 120.00),  -- Clamoxyl lot 5
(5, 1, 2,  25.00, 50.00);   -- Doliprane lot 1
UPDATE vente SET montant_total = 360.00 WHERE id_vente = 5;

-- Vente 6 : Smecta ×2 + Spasfon ×2 = 110+70 = 180 DH
INSERT INTO ligne_vente (id_vente, id_lot, quantite, prix_unitaire_vente, sous_total) VALUES
(6, 9,  2, 55.00, 110.00),  -- Smecta lot 9
(6, 10, 2, 35.00, 70.00);   -- Spasfon lot 10
UPDATE vente SET montant_total = 180.00 WHERE id_vente = 6;

-- Vente 7 : Amoxicilline ×5 + Maalox ×2 = 175+64 = 239 DH
INSERT INTO ligne_vente (id_vente, id_lot, quantite, prix_unitaire_vente, sous_total) VALUES
(7, 4, 5,  35.00, 175.00),  -- Amoxicilline lot 4
(7, 11, 2, 32.00, 64.00);   -- Maalox lot 11
UPDATE vente SET montant_total = 239.00 WHERE id_vente = 7;

-- Vente 8 : Spasfon ×1 + Glucophage ×2 = 35+60 = 95 DH
INSERT INTO ligne_vente (id_vente, id_lot, quantite, prix_unitaire_vente, sous_total) VALUES
(8, 10, 1, 35.00, 35.00),   -- Spasfon lot 10
(8, 7,  2, 30.00, 60.00);   -- Glucophage lot 7

-- Vente 9 : Doliprane ×6 + Levothyrox ×5 + Clamoxyl ×5 = 150+190+200 = 540 DH
INSERT INTO ligne_vente (id_vente, id_lot, quantite, prix_unitaire_vente, sous_total) VALUES
(9, 1, 6,  25.00, 150.00),  -- Doliprane lot 1
(9, 8, 5,  38.00, 190.00),  -- Levothyrox lot 8
(9, 5, 5,  40.00, 200.00);  -- Clamoxyl lot 5
UPDATE vente SET montant_total = 540.00 WHERE id_vente = 9;

-- Vente 10 : Ibuprofène ×4 + Doliprane ×2 = 100+50 = 150 DH
INSERT INTO ligne_vente (id_vente, id_lot, quantite, prix_unitaire_vente, sous_total) VALUES
(10, 2, 4, 25.00, 100.00),  -- Ibuprofène lot 2
(10, 1, 2, 25.00, 50.00);   -- Doliprane lot 1

-- ============================================================
-- VÉRIFICATION DES QUANTITÉS RESTANTES DANS LES LOTS
-- Lot 1 (Doliprane)    : 200 - (2+2+6+2) = 200 - 12 = 188
-- Lot 2 (Ibuprofène)   : 150 - (5+4)     = 150 - 9  = 141
-- Lot 3 (Efferalgan)   : 100 - 0         = 100
-- Lot 4 (Amoxicilline) : 120 - 5         = 115
-- Lot 5 (Clamoxyl)     : 100 - (2+3+5)   = 100 - 10 = 90
-- Lot 6 (Augmentin)    : 80  - (2+1)     = 80  - 3  = 77
-- Lot 7 (Glucophage)   : 150 - (4+2)     = 150 - 6  = 144
-- Lot 8 (Levothyrox)   : 100 - (5+5)     = 100 - 10 = 90
-- Lot 9 (Smecta)       : 100 - 2         = 98
-- Lot 10 (Spasfon)     : 120 - (2+1)     = 120 - 3  = 117
-- Lot 11 (Maalox)      : 130 - 2         = 128
-- ============================================================
UPDATE lot SET quantite_restante = 188 WHERE id_lot = 1;
UPDATE lot SET quantite_restante = 141 WHERE id_lot = 2;
UPDATE lot SET quantite_restante = 100 WHERE id_lot = 3;
UPDATE lot SET quantite_restante = 115 WHERE id_lot = 4;
UPDATE lot SET quantite_restante = 90  WHERE id_lot = 5;
UPDATE lot SET quantite_restante = 77  WHERE id_lot = 6;
UPDATE lot SET quantite_restante = 144 WHERE id_lot = 7;
UPDATE lot SET quantite_restante = 90  WHERE id_lot = 8;
UPDATE lot SET quantite_restante = 98  WHERE id_lot = 9;
UPDATE lot SET quantite_restante = 117 WHERE id_lot = 10;
UPDATE lot SET quantite_restante = 128 WHERE id_lot = 11;

-- ============================================================
-- MISE À JOUR DES QUANTITÉS DISPONIBLES DANS MEDICAMENT
-- (somme des quantité_restante de tous les lots par médicament)
-- ============================================================
UPDATE medicament SET quantite_disponible = 188 WHERE id_medicament = 1;   -- Doliprane
UPDATE medicament SET quantite_disponible = 115 WHERE id_medicament = 2;   -- Amoxicilline EG
UPDATE medicament SET quantite_disponible = 77  WHERE id_medicament = 4;   -- Augmentin
UPDATE medicament SET quantite_disponible = 90  WHERE id_medicament = 7;   -- Clamoxyl
UPDATE medicament SET quantite_disponible = 98  WHERE id_medicament = 8;   -- Smecta
UPDATE medicament SET quantite_disponible = 90  WHERE id_medicament = 10;  -- Levothyrox
UPDATE medicament SET quantite_disponible = 144 WHERE id_medicament = 11;  -- Glucophage
UPDATE medicament SET quantite_disponible = 100 WHERE id_medicament = 13;  -- Efferalgan
UPDATE medicament SET quantite_disponible = 117 WHERE id_medicament = 15;  -- Spasfon
UPDATE medicament SET quantite_disponible = 128 WHERE id_medicament = 18;  -- Maalox
UPDATE medicament SET quantite_disponible = 141 WHERE id_medicament = 20;  -- Ibuprofène EG
