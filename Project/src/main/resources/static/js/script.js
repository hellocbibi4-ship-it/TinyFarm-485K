// --- SÉLECTEURS DOM ---
const loginBtn = document.getElementById("login-btn");
const loginScreen = document.getElementById("login-screen");
const clsBtn = document.getElementById("Trophy");
const farmScreen = document.getElementById("farm-screen");
const classementScreen = document.getElementById("Classement");
const tbody = document.getElementById("classement-body");

// --- LOGIQUE D'AUTHENTIFICATION ---

/**
 * Affiche l'écran de la ferme et charge les données
 */
function showFarm() {
    loginScreen.classList.add("hidden");
    farmScreen.classList.remove("hidden");
    loadFarm();
}

/**
 * Vérifie si l'utilisateur est déjà authentifié auprès du backend
 */
async function checkAuth() {
    try {
        // Appelle l'endpoint du UserController pour voir si une session existe
        const response = await fetch('/user');
        
        if (response.ok) {
            const userData = await response.json();
            console.log("Utilisateur authentifié :", userData.username);
            // Si on reçoit une réponse positive, on affiche directement la ferme
            showFarm();
        } else {
            console.log("Utilisateur non connecté (Statut : " + response.status + ")");
        }
    } catch (error) {
        console.error("Erreur lors de la vérification de l'auth :", error);
    }
}

// Lancement automatique de la vérification au chargement
checkAuth();

// --- ÉVÉNEMENTS ---

// Bouton de connexion GitHub
loginBtn.addEventListener("click", () => {
    // Redirection vers le point d'entrée OAuth2 de ton backend Spring
    window.location.href = '/oauth2/authorization/github';
});

// Gestion du classement
clsBtn.addEventListener("click", () => {
    classementScreen.classList.toggle("show");
    clsBtn.classList.toggle("trophy2");

    if (classementScreen.classList.contains("show")) {
        classement(); 
    }
});

// --- FONCTIONS DE CHARGEMENT ---

async function classement() {
    try {
        const response = await fetch('./data/farmData.json');
        const data = await response.json();
        tbody.innerHTML = '';
        if (data.players) {
            data.players.forEach((p, i) => {
                tbody.innerHTML += `
                    <tr>
                        <td>${i + 1}</td>
                        <td>${p.name}</td>
                        <td>${p.production}</td>
                        <td>${p.capacity}</td>
                        <td>${p.money}</td>
                    </tr>
                `;
            });
        }
    } catch (error) {
        console.error("Erreur chargement classement :", error);
    }
}

async function loadFarm() {
    try {
        const response = await fetch('./data/farmData.json');
        const data = await response.json();

        document.getElementById('cash').innerText = data.cash;
        document.getElementById('water').innerText = data.inventory.water;
        document.getElementById('food').innerText = data.inventory.food;
        document.getElementById('straw').innerText = data.inventory.straw;

        const grid = document.getElementById('animal-grid');
        grid.innerHTML = "";

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
        console.error("Erreur lors du chargement des données de la ferme :", error);
    }
}