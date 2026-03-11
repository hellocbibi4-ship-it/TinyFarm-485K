// Catalogue statique de démonstration: à remplacer plus tard par des données backend si besoin.
const catalogueAnimaux = {
    vache: { nom: "Vache", nomMinuscule: "vache", article: "une", prix: 50 },
    poule: { nom: "Poule", nomMinuscule: "poule", article: "une", prix: 10 },
    lapin: { nom: "Lapin", nomMinuscule: "lapin", article: "un", prix: 10 }
};

// Etat local de la boutique: utile pour tester la popup avant intégration backend.
const etatBoutique = {
    solde: 120,
    achats: {
        vache: 0,
        poule: 0,
        lapin: 0
    }
};

const elementsInterface = {
    popup: document.getElementById("popup-achat"),
    boutonFermer: document.getElementById("bouton-fermer"),
    boutonOuvrir: document.getElementById("bouton-ouvrir"),
    solde: document.getElementById("solde-ecus"),
    message: document.getElementById("message-action"),
    compteurs: {
        vache: document.getElementById("compteur-vache"),
        poule: document.getElementById("compteur-poule"),
        lapin: document.getElementById("compteur-lapin")
    }
};

// Tous les boutons d'achat partagent la même logique, pilotée par data-animal.
const boutonsAchat = document.querySelectorAll(".btn-acheter");

// Chaque bouton "Acheter" lit son data-animal pour déclencher le bon traitement.
boutonsAchat.forEach((bouton) => {
    bouton.addEventListener("click", () => {
        const typeAnimal = bouton.dataset.animal;
        traiterAchat(typeAnimal);
    });
});

elementsInterface.boutonFermer.addEventListener("click", fermerPopup);
elementsInterface.boutonOuvrir.addEventListener("click", ouvrirPopup);

// Touche de confort utilisateur: Echap ferme la popup.
window.addEventListener("keydown", (event) => {
    if (event.key === "Escape") {
        fermerPopup();
    }
});

// Synchronisation initiale de l'interface (solde + compteurs).
mettreAJourInterface();

// Traite une tentative d'achat: valide le type, vérifie le solde, met à jour l'état.
function traiterAchat(typeAnimal) {
    const animal = catalogueAnimaux[typeAnimal];

    if (!animal) {
        afficherMessage("Animal inconnu, achat annulé.", "erreur");
        return;
    }

    if (etatBoutique.solde < animal.prix) {
        afficherMessage(`Solde insuffisant pour acheter ${animal.article} ${animal.nomMinuscule}.`, "erreur");
        return;
    }

    etatBoutique.solde -= animal.prix;
    etatBoutique.achats[typeAnimal] += 1;

    mettreAJourInterface();
    afficherMessage(`Achat validé : ${animal.nom}.`, "succes");
}

// Répercute l'état courant (solde + quantités achetées) dans le DOM.
function mettreAJourInterface() {
    elementsInterface.solde.textContent = etatBoutique.solde;

    Object.entries(etatBoutique.achats).forEach(([typeAnimal, quantite]) => {
        elementsInterface.compteurs[typeAnimal].textContent = quantite;
    });
}

// Affiche un message utilisateur avec la couleur correspondant au type.
function afficherMessage(texte, type) {
    elementsInterface.message.textContent = texte;
    elementsInterface.message.classList.remove("erreur", "succes");
    elementsInterface.message.classList.add(type);
}

// Masque la popup et révèle le bouton de réouverture.
function fermerPopup() {
    if (elementsInterface.popup.classList.contains("cache")) {
        return;
    }

    elementsInterface.popup.classList.add("cache");
    elementsInterface.boutonOuvrir.classList.remove("cache");
    afficherMessage("Popup fermée. Clique sur \"Ouvrir la boutique\" pour revenir.", "succes");
}

// Réaffiche la popup et remasque le bouton d'ouverture.
function ouvrirPopup() {
    elementsInterface.popup.classList.remove("cache");
    elementsInterface.boutonOuvrir.classList.add("cache");
    afficherMessage("Boutique ouverte.", "succes");
}
