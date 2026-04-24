"""
Rapport de contributions du projet.

Lance toute la chaine en une seule commande :
    python rapport.py

Lit l'historique Git de TOUTES les branches, consolide les doublons
d'identite, calcule un score de participation pondere sur plusieurs
criteres, et produit un CSV propre pour Excel.

Sortie :
    contributions_final.csv

Colonnes :
    Auteur, Emails, Commits, %_Commits,
    Lignes_ajoutees, Lignes_supprimees, Net, %_Net,
    Fichiers_touches, %_Fichiers,
    Jours_actifs, Premier_commit, Dernier_commit,
    Score, Participation

Aucune dependance externe (Python 3 standard suffit).
"""

import csv
import subprocess
from collections import defaultdict
from datetime import date


# ---------------------------------------------------------------------------
# Regroupement des doublons d'identite Git
# ---------------------------------------------------------------------------
# Cle = nom canonique a afficher, valeurs = variantes a fusionner.
ALIAS = {
    "Raphael Soudant": ["raphael soudant", "Raphael soudant"],
    "Fanatur": ["Fanatur-git"],
    # Ajoute d'autres regroupements au besoin, ex :
    # "Thomas Diatta": ["Thomas DIATTA", "tdiatta"],
}


def canonical_name(raw):
    for canon, variants in ALIAS.items():
        if raw == canon or raw in variants:
            return canon
    return raw


# ---------------------------------------------------------------------------
# Calcul du score et du niveau
# ---------------------------------------------------------------------------
# Ponderation du score composite. Les 3 metriques sont normalisees
# entre 0 et 100 (chacune en % de son max dans l'equipe) puis sommees
# avec ces coefficients. La somme des coefficients doit faire 1.0.
POIDS = {
    "commits": 0.35,   # regularite / investissement dans la coordination
    "net": 0.40,       # volume de code reellement apporte
    "fichiers": 0.25,  # ampleur / transversalite du travail
}

# Seuils de niveau bases sur le score composite (0 a 100).
def niveau(score):
    if score >= 40:  return "Tres eleve"
    if score >= 15:  return "Eleve"
    if score >= 5:   return "Moyen"
    if score >= 1:   return "Faible"
    return "Tres faible"


# ---------------------------------------------------------------------------
# Lecture de git log pour toutes les branches
# ---------------------------------------------------------------------------
SEPARATOR = "==COMMIT=="
cmd = [
    "git", "log", "--all",
    f"--format={SEPARATOR}%n%aN|%aE|%ad",
    "--date=short",
    "--numstat",
]
result = subprocess.run(cmd, capture_output=True, text=True, check=True)
lines = result.stdout.splitlines()

stats = defaultdict(lambda: {
    "emails": set(),
    "commits": 0,
    "adds": 0,
    "dels": 0,
    "fichiers": set(),
    "jours": set(),
})

i = 0
while i < len(lines):
    if lines[i] == SEPARATOR:
        i += 1
        if i >= len(lines):
            break
        try:
            name, email, datestr = lines[i].split("|", 2)
        except ValueError:
            i += 1
            continue
        auteur = canonical_name(name.strip())
        stats[auteur]["commits"] += 1
        stats[auteur]["emails"].add(email.strip())
        stats[auteur]["jours"].add(datestr.strip())
        i += 1
        while i < len(lines) and lines[i] != SEPARATOR:
            parts = lines[i].split("\t")
            if len(parts) == 3:
                add_s, del_s, fichier = parts
                try:
                    stats[auteur]["adds"] += int(add_s)
                except ValueError:
                    pass
                try:
                    stats[auteur]["dels"] += int(del_s)
                except ValueError:
                    pass
                if fichier:
                    stats[auteur]["fichiers"].add(fichier)
            i += 1
    else:
        i += 1


# ---------------------------------------------------------------------------
# Calcul des totaux et des maxima pour la normalisation
# ---------------------------------------------------------------------------
total_commits = sum(s["commits"] for s in stats.values())
total_adds = sum(s["adds"] for s in stats.values())
total_dels = sum(s["dels"] for s in stats.values())
total_net = sum(max(0, s["adds"] - s["dels"]) for s in stats.values())
total_fichiers = sum(len(s["fichiers"]) for s in stats.values())

max_commits = max((s["commits"] for s in stats.values()), default=1)
max_net = max((max(0, s["adds"] - s["dels"]) for s in stats.values()), default=1)
max_fichiers = max((len(s["fichiers"]) for s in stats.values()), default=1)


# ---------------------------------------------------------------------------
# Construction du rapport final
# ---------------------------------------------------------------------------
lignes = []
for auteur, s in stats.items():
    net = s["adds"] - s["dels"]
    net_positif = max(0, net)
    nb_fichiers = len(s["fichiers"])
    nb_jours = len(s["jours"])
    jours_tries = sorted(s["jours"])

    # Normalisation sur 100 et score pondere
    score_commits = (s["commits"] / max_commits * 100) if max_commits else 0
    score_net = (net_positif / max_net * 100) if max_net else 0
    score_fichiers = (nb_fichiers / max_fichiers * 100) if max_fichiers else 0
    score = (
        POIDS["commits"] * score_commits
        + POIDS["net"] * score_net
        + POIDS["fichiers"] * score_fichiers
    )

    lignes.append({
        "Auteur": auteur,
        "Emails": " ; ".join(sorted(s["emails"])),
        "Commits": s["commits"],
        "%_Commits": round(s["commits"] / total_commits * 100, 1) if total_commits else 0,
        "Lignes_ajoutees": s["adds"],
        "Lignes_supprimees": s["dels"],
        "Net": net,
        "%_Net": round(net_positif / total_net * 100, 1) if total_net else 0,
        "Fichiers_touches": nb_fichiers,
        "%_Fichiers": round(nb_fichiers / total_fichiers * 100, 1) if total_fichiers else 0,
        "Jours_actifs": nb_jours,
        "Premier_commit": jours_tries[0] if jours_tries else "",
        "Dernier_commit": jours_tries[-1] if jours_tries else "",
        "Score": round(score, 1),
        "Participation": niveau(score),
    })

lignes.sort(key=lambda r: r["Score"], reverse=True)


# ---------------------------------------------------------------------------
# Ecriture CSV
# ---------------------------------------------------------------------------
with open("contributions_final.csv", "w", newline="", encoding="utf-8") as f:
    writer = csv.DictWriter(f, fieldnames=list(lignes[0].keys()))
    writer.writeheader()
    writer.writerows(lignes)


# ---------------------------------------------------------------------------
# Affichage terminal
# ---------------------------------------------------------------------------
print()
print(f"{'Auteur':<28} {'Commits':>7} {'Net':>8} {'Fichiers':>9} {'Jours':>6} {'Score':>6}  {'Niveau':<12}")
print("-" * 82)
for l in lignes:
    print(
        f"{l['Auteur']:<28} "
        f"{l['Commits']:>7} "
        f"{l['Net']:>8} "
        f"{l['Fichiers_touches']:>9} "
        f"{l['Jours_actifs']:>6} "
        f"{l['Score']:>6} "
        f" {l['Participation']:<12}"
    )
print("-" * 82)
print(
    f"Total : {total_commits} commits | "
    f"+{total_adds} / -{total_dels} lignes | "
    f"{len(lignes)} contributeurs"
)
print(f"Ponderation du score : "
      f"commits {int(POIDS['commits']*100)}% + "
      f"lignes nettes {int(POIDS['net']*100)}% + "
      f"fichiers {int(POIDS['fichiers']*100)}%")
print(f"Rapport ecrit dans contributions_final.csv")