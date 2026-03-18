// Donnees de collection: rarete, cout en jetons et petit code visuel pour la medaille.
const catalogueObjets = {
    ruban_or: { nom: "Ruban d'or", rarete: "epique", cout: 70, code: "RO", description: "Bonus prestige +12%" },
    cloche_argent: { nom: "Cloche d'argent", rarete: "rare", cout: 42, code: "CA", description: "Recolte acceleree +8%" },
    plume_bleue: { nom: "Plume bleue", rarete: "rare", cout: 38, code: "PB", description: "Vitesse atelier +6%" },
    medaille_foin: { nom: "Medaille du foin", rarete: "commun", cout: 18, code: "MF", description: "Stock paille +10" },
    sceau_lait: { nom: "Sceau du lait", rarete: "commun", cout: 16, code: "SL", description: "Lait quotidien +1" },
    cachet_marche: { nom: "Cachet du marche", rarete: "epique", cout: 64, code: "CM", description: "Prix de vente +10%" }
};

const etatCollection = {
    jetons: 180,
    filtreActif: "tous",
    objetEquipe: null,
    progression: {}
};

const elements = {
    popup: document.getElementById("popup-collection"),
    boutonOuvrir: document.getElementById("btn-ouvrir-collection"),
    boutonFermer: document.getElementById("btn-fermer-collection"),
    boutonRetirerEquipement: document.getElementById("btn-reinitialiser-equipement"),
    grille: document.getElementById("grille-objets"),
    jetons: document.getElementById("jetons-joueur"),
    compteurDebloques: document.getElementById("compteur-debloques"),
    compteurTotal: document.getElementById("compteur-total"),
    objetEquipe: document.getElementById("objet-equipe"),
    message: document.getElementById("message-collection"),
    filtres: Array.from(document.querySelectorAll(".btn-filtre"))
};

initialiserEtat();
construireCartes();
brancherEvenements();
mettreAJourInterface();

function initialiserEtat() {
    Object.keys(catalogueObjets).forEach((idObjet) => {
        etatCollection.progression[idObjet] = {
            debloque: false,
            equipe: false
        };
    });

    elements.compteurTotal.textContent = String(Object.keys(catalogueObjets).length);
}

function construireCartes() {
    elements.grille.innerHTML = "";

    Object.entries(catalogueObjets).forEach(([idObjet, objet]) => {
        const carte = document.createElement("article");
        carte.className = "carte-objet";
        carte.dataset.objet = idObjet;
        carte.dataset.rarete = objet.rarete;

        carte.innerHTML = `
            <h2>${objet.nom}</h2>
            <div class="badges">
                <span class="badge ${objet.rarete}">${objet.rarete}</span>
                <span class="badge">${objet.cout} jetons</span>
            </div>
            <div class="medaille">${objet.code}</div>
            <p class="description">${objet.description}</p>
            <p class="meta">
                <span data-role="statut">Statut: verrouille</span>
                <span data-role="equipement">-</span>
            </p>
            <button class="btn-objet debloquer" type="button" data-action="action-objet" data-objet="${idObjet}">Debloquer</button>
        `;

        elements.grille.appendChild(carte);
    });
}

function brancherEvenements() {
    elements.grille.addEventListener("click", gererActionObjet);

    elements.filtres.forEach((boutonFiltre) => {
        boutonFiltre.addEventListener("click", () => {
            activerFiltre(boutonFiltre.dataset.filtre);
        });
    });

    elements.boutonRetirerEquipement.addEventListener("click", retirerEquipement);
    elements.boutonFermer.addEventListener("click", fermerPopup);
    elements.boutonOuvrir.addEventListener("click", ouvrirPopup);

    window.addEventListener("keydown", (event) => {
        if (event.key === "Escape") {
            fermerPopup();
        }
    });
}

function gererActionObjet(event) {
    const bouton = event.target.closest('button[data-action="action-objet"]');

    if (!bouton) {
        return;
    }

    const idObjet = bouton.dataset.objet;
    const progression = etatCollection.progression[idObjet];

    if (!progression.debloque) {
        debloquerObjet(idObjet);
        return;
    }

    basculerEquipement(idObjet);
}

function debloquerObjet(idObjet) {
    const objet = catalogueObjets[idObjet];

    if (etatCollection.jetons < objet.cout) {
        afficherMessage(`Jetons insuffisants pour ${objet.nom}.`, "erreur");
        return;
    }

    etatCollection.jetons -= objet.cout;
    etatCollection.progression[idObjet].debloque = true;

    mettreAJourInterface();
    afficherMessage(`${objet.nom} debloque. Tu peux maintenant l'equiper.`, "succes");
}

function basculerEquipement(idObjet) {
    const progression = etatCollection.progression[idObjet];

    if (!progression.debloque) {
        afficherMessage("Objet verrouille.", "erreur");
        return;
    }

    if (progression.equipe) {
        rembourserObjetEtReverrouiller(idObjet);
        return;
    }

    Object.values(etatCollection.progression).forEach((etatObjet) => {
        etatObjet.equipe = false;
    });

    progression.equipe = true;
    etatCollection.objetEquipe = idObjet;

    mettreAJourInterface();
    afficherMessage(`${catalogueObjets[idObjet].nom} equipe.`, "succes");
}

function retirerEquipement() {
    if (!etatCollection.objetEquipe) {
        afficherMessage("Aucun objet equipe a retirer.", "erreur");
        return;
    }

    rembourserObjetEtReverrouiller(etatCollection.objetEquipe);
}

function activerFiltre(filtre) {
    etatCollection.filtreActif = filtre;

    elements.filtres.forEach((boutonFiltre) => {
        boutonFiltre.classList.toggle("actif", boutonFiltre.dataset.filtre === filtre);
    });

    Array.from(elements.grille.children).forEach((carte) => {
        const doitAfficher = filtre === "tous" || carte.dataset.rarete === filtre;
        carte.classList.toggle("cache", !doitAfficher);
    });
}

function calculerNombreDebloques() {
    return Object.values(etatCollection.progression).filter((etatObjet) => etatObjet.debloque).length;
}

function mettreAJourCarte(idObjet) {
    const carte = elements.grille.querySelector(`[data-objet="${idObjet}"]`);

    if (!carte) {
        return;
    }

    const objet = catalogueObjets[idObjet];
    const progression = etatCollection.progression[idObjet];

    const elementStatut = carte.querySelector('[data-role="statut"]');
    const elementEquipement = carte.querySelector('[data-role="equipement"]');
    const boutonAction = carte.querySelector('button[data-action="action-objet"]');

    if (!progression.debloque) {
        elementStatut.textContent = "Statut: verrouille";
        elementEquipement.textContent = "-";
        boutonAction.textContent = "Debloquer";
        boutonAction.classList.remove("equiper", "equipe");
        boutonAction.classList.add("debloquer");
        return;
    }

    elementStatut.textContent = "Statut: debloque";
    elementEquipement.textContent = progression.equipe ? "Equipe" : "Disponible";

    if (progression.equipe) {
        boutonAction.textContent = "Desequiper";
        boutonAction.classList.remove("debloquer", "equiper");
        boutonAction.classList.add("equipe");
    } else {
        boutonAction.textContent = "Equiper";
        boutonAction.classList.remove("debloquer", "equipe");
        boutonAction.classList.add("equiper");
    }

    if (objet.rarete === "epique" && progression.equipe) {
        elementEquipement.textContent = "Equipe (epique)";
    }
}

function mettreAJourInterface() {
    elements.jetons.textContent = String(etatCollection.jetons);
    elements.compteurDebloques.textContent = String(calculerNombreDebloques());

    const nomObjetEquipe = etatCollection.objetEquipe
        ? catalogueObjets[etatCollection.objetEquipe].nom
        : "Aucun";

    elements.objetEquipe.textContent = nomObjetEquipe;

    Object.keys(catalogueObjets).forEach((idObjet) => {
        mettreAJourCarte(idObjet);
    });

    activerFiltre(etatCollection.filtreActif);
}

// Quand un objet equipe est retire, on rembourse son cout et on le remet verrouille.
function rembourserObjetEtReverrouiller(idObjet) {
    const objet = catalogueObjets[idObjet];
    const progression = etatCollection.progression[idObjet];

    if (!progression.debloque) {
        afficherMessage("Objet deja verrouille.", "erreur");
        return;
    }

    progression.equipe = false;
    progression.debloque = false;

    if (etatCollection.objetEquipe === idObjet) {
        etatCollection.objetEquipe = null;
    }

    etatCollection.jetons += objet.cout;

    mettreAJourInterface();
    afficherMessage(`${objet.nom} retire. +${objet.cout} jetons rembourses.`, "succes");
}

function afficherMessage(texte, type) {
    elements.message.textContent = texte;
    elements.message.classList.remove("succes", "erreur");
    elements.message.classList.add(type);
}

function fermerPopup() {
    if (elements.popup.classList.contains("cache")) {
        return;
    }

    elements.popup.classList.add("cache");
    elements.boutonOuvrir.classList.remove("cache");
    afficherMessage("Collection fermee. Clique sur 'Ouvrir la collection' pour revenir.", "succes");
}

function ouvrirPopup() {
    elements.popup.classList.remove("cache");
    elements.boutonOuvrir.classList.add("cache");
    afficherMessage("Collection ouverte.", "succes");
}
