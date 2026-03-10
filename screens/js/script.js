const loginBtn = document.getElementById("login-btn");
const loginScreen = document.getElementById("login-screen");
const clsBtn = document.getElementById("Trophy");
const farmScreen = document.getElementById("farm-screen");
const classementScreen = document.getElementById("Classement");
const mainCowButtonsContainer = document.getElementById("main-cow-buttons-container");
const tbody = document.getElementById("classement-body");

const COW_COUNT_KEY = "tinyfarmCowCount";
const LOGIN_SESSION_KEY = "tinyfarmIsLoggedIn";

function setLoginSession(isLoggedIn) {
  if (isLoggedIn) {
    sessionStorage.setItem(LOGIN_SESSION_KEY, "1");
    return;
  }

  sessionStorage.removeItem(LOGIN_SESSION_KEY);
}

function hasLoginSession() {
  return sessionStorage.getItem(LOGIN_SESSION_KEY) === "1";
}

function showFarmScreen() {
  loginScreen.classList.add("hidden");
  farmScreen.classList.remove("hidden");
  loadFarm();
  syncMainCowButtons();
}

function showLoginScreen() {
  loginScreen.classList.remove("hidden");
  farmScreen.classList.add("hidden");
}

loginBtn.addEventListener("click", () => {
  setLoginSession(true);
  showFarmScreen();
});

if (hasLoginSession()) {
  showFarmScreen();
} else {
  showLoginScreen();
}

classement();

clsBtn.addEventListener("click", () => {
  classementScreen.classList.toggle("show");
  clsBtn.classList.toggle("trophy2");

  if (classementScreen.classList.contains("show")) {
    classement();
  }
});

function createMainCowButton(index) {
  const button = document.createElement("button");
  button.type = "button";
  button.className = "cow-btn";
  button.textContent = `Vache ${index}`;
  return button;
}

function getStoredCowCount() {
  const rawCount = localStorage.getItem(COW_COUNT_KEY);
  return Math.max(0, Number.parseInt(rawCount, 10) || 0);
}

function renderMainCowButtons(count) {
  if (!mainCowButtonsContainer) return;

  const targetCount = Math.max(0, Number.parseInt(count, 10) || 0);
  mainCowButtonsContainer.innerHTML = "";

  for (let index = 1; index <= targetCount; index += 1) {
    mainCowButtonsContainer.appendChild(createMainCowButton(index));
  }
}

function syncMainCowButtons() {
  renderMainCowButtons(getStoredCowCount());
}

window.addEventListener("focus", syncMainCowButtons);
window.addEventListener("pageshow", syncMainCowButtons);
syncMainCowButtons();

async function classement() {
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
}

async function loadFarm() {
  try {
    const response = await fetch("./data/farmData.json");
    const data = await response.json();

    document.getElementById("cash").innerText = data.cash;
    document.getElementById("water").innerText = data.inventory.water;
    document.getElementById("food").innerText = data.inventory.food;
    document.getElementById("straw").innerText = data.inventory.straw;

    const grid = document.getElementById("animal-grid");
    grid.innerHTML = "";

    data.animals.forEach((animal) => {
      const card = document.createElement("div");
      card.className = "card";
      card.innerHTML = `
        <img src="assets/${animal.img}" alt="${animal.type}">
        <h3>${animal.name}</h3>
        <p>${animal.type}</p>
        <p><strong>${animal.weight}kg</strong></p>
      `;
      grid.appendChild(card);
    });
  } catch (error) {
    console.error("Erreur lors du chargement des donnees:", error);
  }
}
