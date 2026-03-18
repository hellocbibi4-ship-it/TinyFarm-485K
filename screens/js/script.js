// ============================================================
// DONNÉES STATIQUES
// ============================================================

const fallbackStockProducts = [
  { id: "milk", name: "Lait", stock: 32, price: 4 },
  { id: "eggs", name: "Oeufs", stock: 48, price: 2 },
  { id: "cheese", name: "Fromage", stock: 26, price: 6 }
]

const fallbackCommunityItems = [
  { id: "feed-bag", label: "Sac de nourriture", icon: "🧺", price: 19 },
  { id: "straw-bales", label: "Bottes de pailles", icon: "🌾", price: 19 },
  { id: "syringe", label: "Seringue", icon: "💉", price: 19 },
  { id: "water-bucket", label: "Seau d'eau", icon: "🪣", price: 19 },
  { id: "soap", label: "Savon", icon: "🧼", price: 19 },
  { id: "collectible", label: "Objet de collection", icon: "💍" },
  { id: "buy-animals", label: "Achat animaux", icon: "🐄", variant: "shortcut" },
  { id: "farmers-market", label: "Marché des producteurs", icon: "🧑‍🌾", variant: "shortcut" }
]

// Catalogue statique de démonstration : à remplacer plus tard par des données backend si besoin.
const catalogueAnimaux = {
  vache: { nom: "Vache", nomMinuscule: "vache", article: "une", prix: 50, niveauMinimumAchat: 2 },
  poule: { nom: "Poule", nomMinuscule: "poule", article: "une", prix: 10 },
  lapin: { nom: "Lapin", nomMinuscule: "lapin", article: "un", prix: 10 }
}

// ============================================================
// ÉTATS
// ============================================================

const stockState = {
  products: [],
  selectedProductId: null,
  quantity: 1
}

const collectiviteState = {
  items: []
}

// État local de la boutique animaux : utile pour tester la popup avant intégration backend.
const etatBoutique = {
  niveauJoueur: 1,
  solde: 120,
  achats: {
    // Contrainte sujet : le joueur possède déjà une vache au niveau 1 (kit de démarrage).
    vache: 1,
    poule: 0,
    lapin: 0
  }
}

let farmDataPromise = null
let collectiviteFeedbackTimeout = null

// ============================================================
// RÉFÉRENCES DOM — ÉCRAN PRINCIPAL
// ============================================================

const loginBtn = document.getElementById("login-btn")
const loginScreen = document.getElementById("login-screen")
const clsBtn = document.getElementById("Trophy")
const farmScreen = document.getElementById("farm-screen")
const classementScreen = document.getElementById("Classement")
const tbody = document.getElementById("classement-body")

const stockToggle = document.getElementById("stock-toggle")
const stockPanel = document.getElementById("stock-panel")
const stockProductSelect = document.getElementById("stock-product")
const stockAvailable = document.getElementById("stock-available")
const stockQuantityInput = document.getElementById("stock-quantity")
const stockMinusButton = document.getElementById("stock-minus")
const stockPlusButton = document.getElementById("stock-plus")
const stockUnitPrice = document.getElementById("stock-unit-price")
const stockTotalPrice = document.getElementById("stock-total-price")
const stockTotalUnits = document.getElementById("stock-total-units")
const stockFeedback = document.getElementById("stock-feedback")
const stockCancelButton = document.getElementById("stock-cancel")
const stockSellButton = document.getElementById("stock-sell")
const collectiviteList = document.getElementById("collectivite-list")
const collectiviteFeedback = document.getElementById("collectivite-feedback")

// ============================================================
// RÉFÉRENCES DOM — POPUP ACHAT ANIMAUX
// ============================================================

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
}

// ============================================================
// UTILITAIRES GÉNÉRAUX
// ============================================================

function getCurrentProduct() {
  return (
    stockState.products.find((product) => product.id === stockState.selectedProductId) ||
    stockState.products[0] ||
    null
  )
}

function getTotalUnits() {
  return stockState.products.reduce((total, product) => total + product.stock, 0)
}

function clamp(value, min, max) {
  return Math.min(Math.max(value, min), max)
}

function formatEcus(value) {
  return `${value} ${value > 1 ? "ecus" : "ecu"}`
}

// ============================================================
// FEEDBACK — STOCK & COLLECTIVITÉ
// ============================================================

function setStockFeedback(message = "", type = "") {
  if (!stockFeedback) {
    return
  }

  stockFeedback.textContent = message
  stockFeedback.classList.remove("is-success", "is-error")

  if (type) {
    stockFeedback.classList.add(type)
  }
}

function setCollectiviteFeedback(message = "") {
  if (!collectiviteFeedback) {
    return
  }

  collectiviteFeedback.textContent = message
  collectiviteFeedback.classList.toggle("has-message", Boolean(message))

  if (collectiviteFeedbackTimeout) {
    window.clearTimeout(collectiviteFeedbackTimeout)
    collectiviteFeedbackTimeout = null
  }

  if (message) {
    collectiviteFeedbackTimeout = window.setTimeout(() => {
      collectiviteFeedback.textContent = ""
      collectiviteFeedback.classList.remove("has-message")
      collectiviteFeedbackTimeout = null
    }, 2200)
  }
}

// ============================================================
// PANEL STOCK
// ============================================================

function setStockPanelOpen(isOpen) {
  if (!stockPanel || !stockToggle) {
    return
  }

  stockPanel.classList.toggle("open", isOpen)
  stockToggle.setAttribute("aria-expanded", String(isOpen))
  stockPanel.setAttribute("aria-hidden", String(!isOpen))
}

function renderStockOptions() {
  if (!stockProductSelect) {
    return
  }

  stockProductSelect.innerHTML = stockState.products
    .map(
      (product) =>
        `<option value="${product.id}">${product.name}</option>`
    )
    .join("")

  if (stockState.selectedProductId) {
    stockProductSelect.value = stockState.selectedProductId
  }
}

function updateStockPanel() {
  const product = getCurrentProduct()

  if (!product) {
    if (stockAvailable) stockAvailable.textContent = "0"
    if (stockTotalUnits) stockTotalUnits.textContent = "0"
    if (stockUnitPrice) stockUnitPrice.textContent = formatEcus(0)
    if (stockTotalPrice) stockTotalPrice.textContent = formatEcus(0)

    if (stockQuantityInput) {
      stockQuantityInput.value = "0"
      stockQuantityInput.disabled = true
    }

    if (stockMinusButton) stockMinusButton.disabled = true
    if (stockPlusButton) stockPlusButton.disabled = true
    if (stockSellButton) stockSellButton.disabled = true

    return
  }

  const minimumQuantity = product.stock > 0 ? 1 : 0
  stockState.quantity = clamp(stockState.quantity, minimumQuantity, product.stock)

  if (stockProductSelect) stockProductSelect.value = product.id
  if (stockTotalUnits) stockTotalUnits.textContent = String(getTotalUnits())
  if (stockAvailable) stockAvailable.textContent = String(product.stock)
  if (stockUnitPrice) stockUnitPrice.textContent = formatEcus(product.price)
  if (stockTotalPrice) stockTotalPrice.textContent = formatEcus(stockState.quantity * product.price)

  if (stockQuantityInput) {
    stockQuantityInput.min = String(minimumQuantity)
    stockQuantityInput.max = String(product.stock)
    stockQuantityInput.value = String(stockState.quantity)
    stockQuantityInput.disabled = product.stock === 0
  }

  if (stockMinusButton) stockMinusButton.disabled = product.stock === 0 || stockState.quantity <= minimumQuantity
  if (stockPlusButton) stockPlusButton.disabled = product.stock === 0 || stockState.quantity >= product.stock
  if (stockSellButton) stockSellButton.disabled = product.stock === 0 || stockState.quantity === 0
}

function adjustStockQuantity(delta) {
  const product = getCurrentProduct()

  if (!product || product.stock === 0) {
    return
  }

  stockState.quantity = clamp(stockState.quantity + delta, 1, product.stock)
  updateStockPanel()
  setStockFeedback()
}

// ============================================================
// PANEL COLLECTIVITÉ
// ============================================================

function renderCollectivitePrice(price) {
  if (!Number.isFinite(price)) {
    return ""
  }

  return `
    <span class="collectivite-item-price" aria-hidden="true">
      <span class="collectivite-item-price-value">${price}</span>
      <span class="collectivite-item-price-coin">&#164;</span>
    </span>
  `
}

function renderCollectivitePanel(items) {
  if (!collectiviteList) {
    return
  }

  collectiviteState.items = items.map((item) => ({ ...item }))

  collectiviteList.innerHTML = collectiviteState.items
    .map((item) => {
      const hasPrice = Number.isFinite(item.price)
      const itemClasses = ["collectivite-item", `collectivite-item--${item.id}`]

      if (item.variant === "shortcut") {
        itemClasses.push("collectivite-item-shortcut")
      }

      return `
        <button
          class="${itemClasses.join(" ")}"
          type="button"
          data-collectivite-id="${item.id}"
          aria-label="${item.label}${hasPrice ? `, ${item.price} ecus` : ""}"
        >
          <span class="collectivite-item-icon" aria-hidden="true"></span>
          <span class="collectivite-item-label">${item.label}</span>
          ${renderCollectivitePrice(item.price)}
        </button>
      `
    })
    .join("")
}

// ============================================================
// POPUP ACHAT ANIMAUX
// ============================================================

// Répercute l'état courant (solde + quantités achetées) dans le DOM.
function mettreAJourInterface() {
  if (elementsInterface.solde) {
    elementsInterface.solde.textContent = etatBoutique.solde
  }

  Object.entries(etatBoutique.achats).forEach(([typeAnimal, quantite]) => {
    if (elementsInterface.compteurs[typeAnimal]) {
      elementsInterface.compteurs[typeAnimal].textContent = quantite
    }
  })
}

// Active/désactive visuellement les boutons selon les règles de progression.
function mettreAJourDisponibiliteBoutons() {
  const boutonsAchat = document.querySelectorAll(".btn-acheter")

  boutonsAchat.forEach((bouton) => {
    const typeAnimal = bouton.dataset.animal
    const achatAutorise = estAchatAutorise(typeAnimal)

    bouton.disabled = !achatAutorise

    if (!achatAutorise && typeAnimal === "vache") {
      bouton.textContent = "Déjà possédée"
      bouton.title = "La vache est incluse au niveau 1."
      return
    }

    bouton.textContent = "Acheter"
    bouton.title = ""
  })
}

function estAchatAutorise(typeAnimal) {
  const animal = catalogueAnimaux[typeAnimal]

  if (!animal) {
    return false
  }

  if (!animal.niveauMinimumAchat) {
    return true
  }

  return etatBoutique.niveauJoueur >= animal.niveauMinimumAchat
}

// Traite une tentative d'achat : valide le type, vérifie le solde, met à jour l'état.
function traiterAchat(typeAnimal) {
  const animal = catalogueAnimaux[typeAnimal]

  if (!animal) {
    afficherMessage("Animal inconnu, achat annulé.", "erreur")
    return
  }

  if (!estAchatAutorise(typeAnimal)) {
    afficherMessage(
      "Niveau 1 : la vache est déjà fournie dans le kit de démarrage et ne peut pas être rachetée.",
      "erreur"
    )
    return
  }

  if (etatBoutique.solde < animal.prix) {
    afficherMessage(`Solde insuffisant pour acheter ${animal.article} ${animal.nomMinuscule}.`, "erreur")
    return
  }

  etatBoutique.solde -= animal.prix
  etatBoutique.achats[typeAnimal] += 1

  mettreAJourInterface()
  afficherMessage(`Achat validé : ${animal.nom}.`, "succes")
}

// Affiche un message utilisateur avec la couleur correspondant au type.
function afficherMessage(texte, type) {
  if (!elementsInterface.message) {
    return
  }

  elementsInterface.message.textContent = texte
  elementsInterface.message.classList.remove("erreur", "succes")
  elementsInterface.message.classList.add(type)
}

// Masque la popup et révèle le bouton de réouverture.
function fermerPopup() {
  if (!elementsInterface.popup || elementsInterface.popup.classList.contains("hidden")) {
    return
  }

  elementsInterface.popup.classList.add("hidden")

  if (elementsInterface.boutonOuvrir) {
    elementsInterface.boutonOuvrir.classList.remove("hidden")
  }

  afficherMessage('Popup fermée. Clique sur "Ouvrir la boutique" pour revenir.', "succes")
}

// Réaffiche la popup et remasque le bouton d'ouverture.
function ouvrirPopup() {
  if (!elementsInterface.popup) {
    return
  }

  elementsInterface.popup.classList.remove("hidden")

  if (elementsInterface.boutonOuvrir) {
    elementsInterface.boutonOuvrir.classList.add("hidden")
  }

  afficherMessage("Boutique ouverte.", "succes")
}

// ============================================================
// CHARGEMENT DES DONNÉES
// ============================================================

function fetchFarmData() {
  if (!farmDataPromise) {
    farmDataPromise = fetch("./data/farmData.json").then((response) => {
      if (!response.ok) {
        throw new Error("Impossible de charger les donnees de la ferme.")
      }

      return response.json()
    })
  }

  return farmDataPromise
}

async function initializeStockPanel() {
  if (!stockToggle || !stockPanel) {
    return
  }

  try {
    const data = await fetchFarmData()
    const products = Array.isArray(data.stockProducts) && data.stockProducts.length > 0
      ? data.stockProducts
      : fallbackStockProducts

    stockState.products = products.map((product) => ({ ...product }))
    stockState.selectedProductId = stockState.products[0]?.id || null
    stockState.quantity = stockState.products[0]?.stock > 0 ? 1 : 0

    renderStockOptions()
    updateStockPanel()
  } catch (error) {
    console.error("Erreur lors du chargement du stock :", error)
    stockState.products = fallbackStockProducts.map((product) => ({ ...product }))
    stockState.selectedProductId = stockState.products[0].id
    stockState.quantity = 1

    renderStockOptions()
    updateStockPanel()
    setStockFeedback("Stock charge avec les donnees de secours.", "is-error")
  }
}

async function initializeCollectivitePanel() {
  if (!collectiviteList) {
    return
  }

  try {
    const data = await fetchFarmData()
    const items = Array.isArray(data.communityItems) && data.communityItems.length > 0
      ? data.communityItems
      : fallbackCommunityItems

    renderCollectivitePanel(items)
  } catch (error) {
    console.error("Erreur lors du chargement de la collectivite :", error)
    renderCollectivitePanel(fallbackCommunityItems)
    setCollectiviteFeedback("Collectivite chargee avec les donnees de secours.")
  }
}

// ============================================================
// CLASSEMENT
// ============================================================

async function classement() {
  if (!tbody) {
    return
  }

  try {
    const data = await fetchFarmData()

    tbody.innerHTML = ""

    if (data.players) {
      data.players.forEach((player, index) => {
        tbody.innerHTML += `
          <tr>
            <td>${index + 1}</td>
            <td>${player.name}</td>
            <td>${player.production}</td>
            <td>${player.capacity}</td>
            <td>${player.money}</td>
          </tr>
        `
      })
    }
  } catch (error) {
    console.error("Erreur lors du chargement du classement :", error)
  }
}

// ============================================================
// HORLOGE
// ============================================================

function updateClock() {
  const clock = document.getElementById("clock")

  if (!clock) {
    return
  }

  const now = new Date()
  const hours = String(now.getHours()).padStart(2, "0")
  const minutes = String(now.getMinutes()).padStart(2, "0")
  const seconds = String(now.getSeconds()).padStart(2, "0")

  clock.textContent = `${hours}:${minutes}:${seconds}`
}

// ============================================================
// ÉCOUTEURS HORS DOMContentLoaded
// ============================================================

if (loginBtn && loginScreen && farmScreen) {
  loginBtn.addEventListener("click", () => {
    loginScreen.classList.add("hidden")
    farmScreen.classList.remove("hidden")
  })
}

if (clsBtn && classementScreen) {
  clsBtn.addEventListener("click", () => {
    classementScreen.classList.toggle("show")
    clsBtn.classList.toggle("trophy2")

    if (classementScreen.classList.contains("show")) {
      classement()
    }
  })
}

classement()

// ============================================================
// INITIALISATION AU CHARGEMENT DU DOM
// ============================================================

document.addEventListener("DOMContentLoaded", () => {

  // --- Paramètres / langue ---

  const settingsButtons = document.querySelectorAll(".settings-btn")
  const settingsPanels = document.querySelectorAll(".settings-panel")

  settingsButtons.forEach((button, index) => {
    button.addEventListener("click", (event) => {
      event.stopPropagation()

      const isOpen = button.classList.contains("open")

      settingsButtons.forEach((currentButton) => currentButton.classList.remove("open"))
      settingsPanels.forEach((panel) => panel.classList.remove("open"))

      if (!isOpen) {
        button.classList.add("open")
        settingsPanels[index].classList.add("open")
      }
    })
  })

  document.querySelectorAll(".language-btn").forEach((button) => {
    button.addEventListener("click", (event) => {
      event.stopPropagation()
      const panel = button.nextElementSibling
      panel.classList.toggle("open")
    })
  })

  document.querySelectorAll(".logout-btn").forEach((button) => {
    button.addEventListener("click", () => {
      if (farmScreen && loginScreen) {
        farmScreen.classList.add("hidden")
        loginScreen.classList.remove("hidden")
      }

      document.querySelectorAll(".settings-btn").forEach((currentButton) => currentButton.classList.remove("open"))
      document.querySelectorAll(".settings-panel").forEach((panel) => panel.classList.remove("open"))
      setStockPanelOpen(false)
    })
  })

  const loginSettingsBtn = document.querySelector(".settings-login-btn")
  const loginSettingsPanel = document.querySelector(".settings-login-panel")

  if (loginSettingsBtn && loginSettingsPanel) {
    loginSettingsBtn.addEventListener("click", () => {
      loginSettingsPanel.classList.toggle("open")
      loginSettingsBtn.classList.toggle("open")
    })
  }

  document.querySelectorAll(".login-language-btn").forEach((button) => {
    button.addEventListener("click", (event) => {
      event.stopPropagation()
      const panel = button.nextElementSibling
      panel.classList.toggle("open")
    })
  })

  // --- Panel stock ---

  if (stockToggle && stockPanel) {
    stockToggle.addEventListener("click", (event) => {
      event.stopPropagation()
      setStockPanelOpen(!stockPanel.classList.contains("open"))
    })

    stockPanel.addEventListener("click", (event) => {
      event.stopPropagation()
    })
  }

  if (stockProductSelect) {
    stockProductSelect.addEventListener("change", () => {
      stockState.selectedProductId = stockProductSelect.value
      stockState.quantity = getCurrentProduct()?.stock > 0 ? 1 : 0
      updateStockPanel()
      setStockFeedback()
    })
  }

  if (stockMinusButton) {
    stockMinusButton.addEventListener("click", () => {
      adjustStockQuantity(-1)
    })
  }

  if (stockPlusButton) {
    stockPlusButton.addEventListener("click", () => {
      adjustStockQuantity(1)
    })
  }

  if (stockQuantityInput) {
    stockQuantityInput.addEventListener("input", () => {
      const product = getCurrentProduct()

      if (!product) {
        return
      }

      const parsedValue = Number.parseInt(stockQuantityInput.value, 10)
      const fallbackValue = product.stock > 0 ? 1 : 0

      stockState.quantity = Number.isNaN(parsedValue) ? fallbackValue : parsedValue
      updateStockPanel()
      setStockFeedback()
    })
  }

  if (stockCancelButton) {
    stockCancelButton.addEventListener("click", () => {
      stockState.quantity = getCurrentProduct()?.stock > 0 ? 1 : 0
      updateStockPanel()
      setStockFeedback()
      setStockPanelOpen(false)
    })
  }

  if (stockSellButton) {
    stockSellButton.addEventListener("click", () => {
      const product = getCurrentProduct()

      if (!product || product.stock === 0 || stockState.quantity === 0) {
        setStockFeedback("Aucun stock disponible pour cette vente.", "is-error")
        updateStockPanel()
        return
      }

      const soldQuantity = stockState.quantity
      product.stock -= soldQuantity
      stockState.quantity = product.stock > 0 ? 1 : 0

      updateStockPanel()
      setStockFeedback(
        `${soldQuantity} ${soldQuantity > 1 ? "unites vendues" : "unite vendue"} de ${product.name}.`,
        "is-success"
      )
    })
  }

  // --- Panel collectivité ---

  if (collectiviteList) {
    collectiviteList.addEventListener("click", (event) => {
      const button = event.target.closest("[data-collectivite-id]")

      if (!button) {
        return
      }

      const selectedItem = collectiviteState.items.find((item) => item.id === button.dataset.collectiviteId)

      if (!selectedItem) {
        return
      }

      // Le raccourci "Achat animaux" ouvre directement la popup.
      if (selectedItem.id === "buy-animals") {
        ouvrirPopup()
        return
      }

      setCollectiviteFeedback(`${selectedItem.label} selectionne.`)
    })
  }

  // --- Popup achat animaux ---

  const boutonsAchat = document.querySelectorAll(".btn-acheter")

  // Tous les boutons d'achat partagent la même logique, pilotée par data-animal.
  boutonsAchat.forEach((bouton) => {
    bouton.addEventListener("click", () => {
      const typeAnimal = bouton.dataset.animal
      traiterAchat(typeAnimal)
    })
  })

  if (elementsInterface.boutonFermer) {
    elementsInterface.boutonFermer.addEventListener("click", fermerPopup)
  }

  if (elementsInterface.boutonOuvrir) {
    elementsInterface.boutonOuvrir.addEventListener("click", ouvrirPopup)
  }

  // Synchronisation initiale de l'interface (solde + compteurs).
  mettreAJourInterface()
  mettreAJourDisponibiliteBoutons()

  // --- Fermeture globale au clic hors panels & touche Échap ---

  document.addEventListener("click", () => {
    setStockPanelOpen(false)
  })

  // Touche de confort : Échap ferme le panel stock ET la popup animaux.
  document.addEventListener("keydown", (event) => {
    if (event.key === "Escape") {
      setStockPanelOpen(false)
      fermerPopup()
    }
  })

  // --- Initialisation asynchrone & horloge ---

  initializeStockPanel()
  initializeCollectivitePanel()
  updateClock()
  setInterval(updateClock, 1000)
})