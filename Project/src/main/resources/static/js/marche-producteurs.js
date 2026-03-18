// Catalogue des produits du marche. Chaque produit garde sa categorie, son prix et son stock initial.
const catalogueProduits = {
    lait: { nom: "Lait frais", categorie: "laiterie", prix: 12, stock: 28, producteur: "Ferme Bellevue", image: "vache.svg" },
    fromage: { nom: "Fromage ferme", categorie: "laiterie", prix: 19, stock: 16, producteur: "Ferme Bellevue", image: "vache.svg" },
    oeufs: { nom: "Oeufs fermiers", categorie: "volaille", prix: 9, stock: 40, producteur: "Poulailler Vert", image: "poule.svg" },
    plume: { nom: "Plumes triees", categorie: "volaille", prix: 7, stock: 22, producteur: "Poulailler Vert", image: "poule.svg" },
    laine: { nom: "Fil de laine", categorie: "atelier", prix: 14, stock: 25, producteur: "Atelier du Pre", image: "lapin.svg" },
    panier: { nom: "Panier tresse", categorie: "atelier", prix: 11, stock: 19, producteur: "Atelier du Pre", image: "lapin.svg" }
};

const etatMarche = {
    solde: 260,
    categorieActive: "tous",
    panier: {},
    quantitesSelectionnees: {}
};

const elements = {
    popup: document.getElementById("popup-marche"),
    boutonOuvrir: document.getElementById("btn-ouvrir-marche"),
    boutonFermer: document.getElementById("btn-fermer-marche"),
    boutonValider: document.getElementById("btn-valider-achat"),
    boutonVider: document.getElementById("btn-vider-panier"),
    solde: document.getElementById("solde-joueur"),
    totalPanier: document.getElementById("total-panier"),
    message: document.getElementById("message-marche"),
    grille: document.getElementById("grille-produits"),
    filtres: Array.from(document.querySelectorAll(".btn-filtre"))
};

initialiserEtat();
construireCartes();
brancherEvenements();
mettreAJourInterface();

function initialiserEtat() {
    Object.keys(catalogueProduits).forEach((idProduit) => {
        etatMarche.panier[idProduit] = 0;
        etatMarche.quantitesSelectionnees[idProduit] = 1;
    });
}

function construireCartes() {
    elements.grille.innerHTML = "";

    Object.entries(catalogueProduits).forEach(([idProduit, produit]) => {
        const carte = document.createElement("article");
        carte.className = "carte-produit";
        carte.dataset.produit = idProduit;
        carte.dataset.categorie = produit.categorie;

        carte.innerHTML = `
            <h2>${produit.nom}</h2>
            <p class="producteur">${produit.producteur}</p>
            <div class="visuel-produit">
                <img src="assets/${produit.image}" alt="Produit ${produit.nom.toLowerCase()}">
            </div>
            <p class="meta-produit">
                <span>Prix: ${produit.prix}</span>
                <span data-role="stock">Stock: ${produit.stock}</span>
            </p>
            <div class="controle-quantite">
                <button class="btn-mini" type="button" data-action="moins" data-produit="${idProduit}">-</button>
                <span class="quantite-selection" data-role="quantite">1</span>
                <button class="btn-mini" type="button" data-action="plus" data-produit="${idProduit}">+</button>
            </div>
            <button class="btn-ajouter" type="button" data-action="ajouter" data-produit="${idProduit}">Ajouter</button>
        `;

        elements.grille.appendChild(carte);
    });
}

function brancherEvenements() {
    elements.grille.addEventListener("click", gererActionsProduit);

    elements.filtres.forEach((boutonFiltre) => {
        boutonFiltre.addEventListener("click", () => {
            activerFiltre(boutonFiltre.dataset.categorie);
        });
    });

    elements.boutonValider.addEventListener("click", validerPanier);
    elements.boutonVider.addEventListener("click", viderPanier);
    elements.boutonFermer.addEventListener("click", fermerPopup);
    elements.boutonOuvrir.addEventListener("click", ouvrirPopup);

    window.addEventListener("keydown", (event) => {
        if (event.key === "Escape") {
            fermerPopup();
        }
    });
}

function gererActionsProduit(event) {
    const bouton = event.target.closest("button[data-action]");

    if (!bouton) {
        return;
    }

    const action = bouton.dataset.action;
    const idProduit = bouton.dataset.produit;

    if (action === "plus") {
        changerQuantiteSelection(idProduit, +1);
        return;
    }

    if (action === "moins") {
        changerQuantiteSelection(idProduit, -1);
        return;
    }

    if (action === "ajouter") {
        ajouterAuPanier(idProduit);
    }
}

function changerQuantiteSelection(idProduit, delta) {
    const quantiteActuelle = etatMarche.quantitesSelectionnees[idProduit];
    const nouvelleQuantite = Math.max(1, Math.min(99, quantiteActuelle + delta));

    etatMarche.quantitesSelectionnees[idProduit] = nouvelleQuantite;
    mettreAJourCarte(idProduit);
}

function ajouterAuPanier(idProduit) {
    const produit = catalogueProduits[idProduit];
    const quantiteSouhaitee = etatMarche.quantitesSelectionnees[idProduit];
    const stockDisponible = calculerStockDisponible(idProduit);

    if (quantiteSouhaitee > stockDisponible) {
        afficherMessage(`Stock insuffisant pour ${produit.nom}.`, "erreur");
        return;
    }

    etatMarche.panier[idProduit] += quantiteSouhaitee;

    mettreAJourInterface();
    afficherMessage(`${quantiteSouhaitee} x ${produit.nom} ajoute(s) au panier.`, "succes");
}

function validerPanier() {
    const totalPanier = calculerTotalPanier();

    if (totalPanier === 0) {
        afficherMessage("Panier vide: ajoute des produits avant validation.", "erreur");
        return;
    }

    if (totalPanier > etatMarche.solde) {
        afficherMessage("Solde insuffisant pour valider cet achat.", "erreur");
        return;
    }

    etatMarche.solde -= totalPanier;

    Object.keys(catalogueProduits).forEach((idProduit) => {
        catalogueProduits[idProduit].stock -= etatMarche.panier[idProduit];
        etatMarche.panier[idProduit] = 0;
    });

    mettreAJourInterface();
    afficherMessage("Achat confirme au marche des producteurs.", "succes");
}

function viderPanier() {
    const totalPanier = calculerTotalPanier();

    if (totalPanier === 0) {
        afficherMessage("Le panier est deja vide.", "erreur");
        return;
    }

    Object.keys(etatMarche.panier).forEach((idProduit) => {
        etatMarche.panier[idProduit] = 0;
    });

    mettreAJourInterface();
    afficherMessage("Panier vide. Aucun achat valide.", "succes");
}

function activerFiltre(categorie) {
    etatMarche.categorieActive = categorie;

    elements.filtres.forEach((boutonFiltre) => {
        boutonFiltre.classList.toggle("actif", boutonFiltre.dataset.categorie === categorie);
    });

    Array.from(elements.grille.children).forEach((carte) => {
        const doitAfficher = categorie === "tous" || carte.dataset.categorie === categorie;
        carte.classList.toggle("cache", !doitAfficher);
    });
}

function calculerStockDisponible(idProduit) {
    return catalogueProduits[idProduit].stock - etatMarche.panier[idProduit];
}

function calculerTotalPanier() {
    return Object.entries(catalogueProduits).reduce((total, [idProduit, produit]) => {
        return total + produit.prix * etatMarche.panier[idProduit];
    }, 0);
}

function mettreAJourCarte(idProduit) {
    const carte = elements.grille.querySelector(`[data-produit="${idProduit}"]`);

    if (!carte) {
        return;
    }

    const stockDisponible = calculerStockDisponible(idProduit);
    const elementStock = carte.querySelector('[data-role="stock"]');
    const elementQuantite = carte.querySelector('[data-role="quantite"]');

    elementStock.textContent = `Stock: ${stockDisponible}`;
    elementStock.classList.toggle("stock-faible", stockDisponible <= 3);
    elementQuantite.textContent = String(etatMarche.quantitesSelectionnees[idProduit]);
}

function mettreAJourInterface() {
    elements.solde.textContent = String(etatMarche.solde);
    elements.totalPanier.textContent = String(calculerTotalPanier());

    Object.keys(catalogueProduits).forEach((idProduit) => {
        mettreAJourCarte(idProduit);
    });

    activerFiltre(etatMarche.categorieActive);
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
    afficherMessage("Marche ferme. Clique sur 'Ouvrir le marche' pour revenir.", "succes");
}

function ouvrirPopup() {
    elements.popup.classList.remove("cache");
    elements.boutonOuvrir.classList.add("cache");
    afficherMessage("Marche ouvert.", "succes");
}
