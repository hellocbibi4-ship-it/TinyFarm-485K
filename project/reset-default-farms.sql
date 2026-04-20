UPDATE ferme
SET nom = 'f1',
    solde_ecus = 1500,
    hibernation = FALSE,
    score = 0,
    jour_actuel = 1,
    achats_collectivite_restants = 12,
    nb_vaches = 1,
    nb_poules = 4,
    nb_lapins = 8,
    nb_lapins_malades = 0,
    nb_vaches_affamees = 0,
    nb_vaches_assoiffees = 0,
    nb_poule_affamees = 0,
    nb_poule_assoiffees = 0,
    nb_lapins_affames = 0,
    nb_lapins_assoiffes = 0
WHERE id_utilisateur = (
    SELECT id_utilisateur FROM utilisateur WHERE github_username = 'a'
);

UPDATE ferme
SET nom = 'f2',
    solde_ecus = 1499,
    hibernation = FALSE,
    score = 0,
    jour_actuel = 1,
    achats_collectivite_restants = 12,
    nb_vaches = 1,
    nb_poules = 4,
    nb_lapins = 8,
    nb_lapins_malades = 0,
    nb_vaches_affamees = 0,
    nb_vaches_assoiffees = 0,
    nb_poule_affamees = 0,
    nb_poule_assoiffees = 0,
    nb_lapins_affames = 0,
    nb_lapins_assoiffes = 0
WHERE id_utilisateur = (
    SELECT id_utilisateur FROM utilisateur WHERE github_username = 'b'
);

MERGE INTO remise (
    ferme_id_ferme,
    stock_oeuf,
    stock_lait,
    stock_lapin,
    stock_nourriture,
    stock_eau,
    stock_savon,
    stock_seringue,
    stock_paille
)
KEY (ferme_id_ferme)
SELECT id_ferme, 0, 0, 0, 0, 0, 0, 0, 0
FROM ferme
WHERE id_utilisateur = (
    SELECT id_utilisateur FROM utilisateur WHERE github_username = 'a'
);

MERGE INTO remise (
    ferme_id_ferme,
    stock_oeuf,
    stock_lait,
    stock_lapin,
    stock_nourriture,
    stock_eau,
    stock_savon,
    stock_seringue,
    stock_paille
)
KEY (ferme_id_ferme)
SELECT id_ferme, 0, 0, 0, 0, 0, 0, 0, 0
FROM ferme
WHERE id_utilisateur = (
    SELECT id_utilisateur FROM utilisateur WHERE github_username = 'b'
);

DELETE FROM animal
WHERE id_ferme IN (
    SELECT f.id_ferme
    FROM ferme f
    JOIN utilisateur u ON u.id_utilisateur = f.id_utilisateur
    WHERE u.github_username IN ('a', 'b')
);

INSERT INTO animal (
    id_ferme,
    nom,
    type_animal,
    role,
    sexe,
    poids,
    age,
    jauge_sante,
    jauge_faim,
    jauge_proprete,
    jauge_hydratation,
    jours_malade_consecutifs,
    est_malade,
    a_mange,
    a_ete_traite
)
SELECT f.id_ferme,
       animal_name,
       animal_type,
       animal_role,
       animal_sex,
       animal_weight,
       animal_age,
       100,
       100,
       100,
       100,
       0,
       FALSE,
       FALSE,
       FALSE
FROM ferme f
JOIN utilisateur u ON u.id_utilisateur = f.id_utilisateur
JOIN (
    SELECT 'Vache 1' AS animal_name, 2 AS animal_type, 2 AS animal_role, 2 AS animal_sex, 500 AS animal_weight, 0 AS animal_age
    UNION ALL SELECT 'Coq 1', 0, 0, 0, 2.5, 5
    UNION ALL SELECT 'Poule 1', 0, 1, 1, 2.5, 5
    UNION ALL SELECT 'Poule 2', 0, 1, 1, 2.5, 5
    UNION ALL SELECT 'Poule 3', 0, 1, 1, 2.5, 5
    UNION ALL SELECT 'Lapin 1', 1, 2, 2, 2, 0
    UNION ALL SELECT 'Lapin 2', 1, 2, 2, 2, 0
    UNION ALL SELECT 'Lapin 3', 1, 2, 2, 2, 0
    UNION ALL SELECT 'Lapin 4', 1, 2, 2, 2, 0
    UNION ALL SELECT 'Lapin 5', 1, 2, 2, 2, 0
    UNION ALL SELECT 'Lapin 6', 1, 2, 2, 2, 0
    UNION ALL SELECT 'Lapin 7', 1, 2, 2, 2, 0
    UNION ALL SELECT 'Lapin 8', 1, 2, 2, 2, 0
) template
WHERE u.github_username IN ('a', 'b');
