async function loadFarm() {
    try {
        // Récupération des données depuis le fichier JSON
        const response = await fetch('./data/farmData.json');
        const data = await response.json();

        // 1. Mise à jour des compteurs de ressources
        document.getElementById('cash').innerText = data.cash;
        document.getElementById('water').innerText = data.inventory.water;
        document.getElementById('food').innerText = data.inventory.food;
        document.getElementById('straw').innerText = data.inventory.straw;

        // 2. Génération des cartes d'animaux
        const grid = document.getElementById('animal-grid');
        grid.innerHTML = ""; // On vide la grille au cas où

        data.animals.forEach(animal => {
            const card = document.createElement('div');
            card.className = 'card';
            card.innerHTML = `
                <img src="assets/${animal.img}" alt="${animal.type}">
                <h3>${animal.name}</h3>
                <p>${animal.type}</p>
                <p><strong>${animal.weight}kg</strong></p>
            `;
            grid.appendChild(card);
        });

    } catch (error) {
        console.error("Erreur lors du chargement des données :", error);
    }
}

// On lance la fonction dès que la page est chargée
window.onload = loadFarm;