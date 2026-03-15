/*
const loginBtn = document.getElementById("login-btn");
const loginScreen = document.getElementById("login-screen");
const clsBtn = document.getElementById("Trophy");
const farmScreen = document.getElementById("farm-screen");
const classementScreen = document.getElementById("Classement");

loginBtn.addEventListener("click", () => {
  // cacher le login
  loginScreen.classList.add("hidden");

  // afficher la ferme
  farmScreen.classList.remove("hidden");

  // charger les données APRÈS connexion
  loadFarm();
});
 classement();
clsBtn.addEventListener("click", () => {
    classementScreen.classList.toggle("show");
    clsBtn.classList.toggle("trophy2");

    if (classementScreen.classList.contains("show")) {
        classement(); // met à jour le tableau si nécessaire
    }
});
const tbody = document.getElementById("classement-body");




async function classement() {
  const response = await fetch('./data/farmData.json');
  const data = await response.json();
  tbody.innerHTML = '';
  if (data.players) {
    data.players.forEach((p, i) => {
      tbody.innerHTML += `
        <tr>
          <td>${i+1}</td>
          <td>${p.name}</td>
          <td>${p.production}</td>
          <td>${p.capacity}</td>
          <td>${p.money}</td>
        </tr>
      `;
});
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
    console.error("Erreur lors du chargement des données :", error);
  }
}*/

const loginScreen      = document.getElementById("login-screen");
const farmScreen       = document.getElementById("farm-screen");
const loginBtn         = document.getElementById("login-btn");
const clsBtn           = document.getElementById("Trophy");
const classementScreen = document.getElementById("Classement");
const tbody            = document.getElementById("classement-body");

// Au chargement : on vérifie si une session est déjà active
window.addEventListener("DOMContentLoaded", async () => {
    try {
        const res = await fetch("/api/me");
        if (res.ok) {
            const user = await res.json();
            afficherFerme(user);
        } else {
            afficherLogin();
        }
    } catch (e) {
        console.error("Impossible de joindre le serveur :", e);
        afficherLogin();
    }
    classement();
});

// Clic login → redirige vers GitHub
loginBtn.addEventListener("click", () => {
    window.location.href = "/oauth2/authorization/github";
});

// Bouton trophée
clsBtn.addEventListener("click", () => {
    classementScreen.classList.toggle("show");
    clsBtn.classList.toggle("trophy2");
    if (classementScreen.classList.contains("show")) {
        classement();
    }
});

function afficherLogin() {
    loginScreen.classList.remove("hidden");
    farmScreen.classList.add("hidden");
}

function afficherFerme(user) {
    loginScreen.classList.add("hidden");
    farmScreen.classList.remove("hidden");
    loadFarm(user);
}

async function loadFarm(user) {
    try {
        const response = await fetch("./data/farmData.json");
        const data = await response.json();

        document.getElementById("cash").innerText  = user?.solde  ?? data.cash;
        document.getElementById("water").innerText = data.inventory.water;
        document.getElementById("food").innerText  = data.inventory.food;
        document.getElementById("straw").innerText = data.inventory.straw;

        const grid = document.getElementById("animal-grid");
        grid.innerHTML = "";

        data.animals.forEach(animal => {
            const card = document.createElement("div");
            card.className = "card";
            card.innerHTML = `
                <img src="assets/${animal.img}" alt="${animal.type}">
                <h3>${animal.name}</h3>
                <p>${animal.type}</p>
                <p><strong>${animal.weight} kg</strong></p>
            `;
            grid.appendChild(card);
        });

    } catch (error) {
        console.error("Erreur lors du chargement des données :", error);
    }
}

async function classement() {
    try {
        const response = await fetch("./data/farmData.json");
        const data = await response.json();
        tbody.innerHTML = "";
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
    } catch (e) {
        console.error("Erreur classement :", e);
    }
}