const loginBtn = document.getElementById("login-btn");
const loginScreen = document.getElementById("login-screen");
const farmScreen = document.getElementById("farm-screen");

loginBtn.addEventListener("click", () => {
  // cacher le login
  loginScreen.classList.add("hidden");

  // afficher la ferme
  farmScreen.classList.remove("hidden");

  // charger les données APRÈS connexion
  loadFarm();
});

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
}
