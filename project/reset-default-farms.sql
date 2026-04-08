UPDATE ferme
SET nom = 'f1',
    solde_ecus = 1500,
    hibernation = FALSE,
    score = 0,
    nb_vaches = 1,
    nb_poules = 3,
    nb_lapins = 2
WHERE id_utilisateur = (
    SELECT id_utilisateur FROM utilisateur WHERE github_username = 'a'
);

UPDATE ferme
SET nom = 'f2',
    solde_ecus = 1500,
    hibernation = FALSE,
    score = 0,
    nb_vaches = 1,
    nb_poules = 3,
    nb_lapins = 2
WHERE id_utilisateur = (
    SELECT id_utilisateur FROM utilisateur WHERE github_username = 'b'
);

MERGE INTO remise (ferme_id_ferme, stock_oeuf, stock_nourriture, stock_eau, stock_savon, stock_seringue, stock_paille)
KEY (ferme_id_ferme)
SELECT id_ferme, 0, 0, 0, 0, 0, 0
FROM ferme
WHERE id_utilisateur = (
    SELECT id_utilisateur FROM utilisateur WHERE github_username = 'a'
);

MERGE INTO remise (ferme_id_ferme, stock_oeuf, stock_nourriture, stock_eau, stock_savon, stock_seringue, stock_paille)
KEY (ferme_id_ferme)
SELECT id_ferme, 0, 0, 0, 0, 0, 0
FROM ferme
WHERE id_utilisateur = (
    SELECT id_utilisateur FROM utilisateur WHERE github_username = 'b'
);
