const fallbackStockProducts = [
  { id: "milk", name: "Lait", stock: 32, price: 4 },
  { id: "eggs", name: "Oeufs", stock: 48, price: 2 },
  { id: "cheese", name: "Fromage", stock: 26, price: 6 }
]

const fallbackCommunityItems = [
  { id: "feed-bag", label: "Sac de nourriture", price: 19 },
  { id: "straw-bales", label: "Bottes de pailles", price: 19 },
  { id: "syringe", label: "Seringue", price: 19 },
  { id: "water-bucket", label: "Seau d'eau", price: 19 },
  { id: "soap", label: "Savon", price: 19 },
  { id: "collectible", label: "Objet de collection" },
  { id: "buy-animals", label: "Achat animaux", variant: "shortcut" },
  { id: "farmers-market", label: "Marche des producteurs", variant: "shortcut" }
]

const stockState = {
  products: [],
  selectedProductId: null,
  quantity: 1
}

const collectiviteState = {
  items: []
}

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

const poulaillerContainer = document.getElementById("poulailler-container")
const paturageContainer = document.getElementById("paturage-container")
const clapierContainer = document.getElementById("clapier-container")
const animalZoneControllers = Array.from(document.querySelectorAll("[data-zone-controls]"))
  .map((shell) => ({
    shell,
    container: document.getElementById(shell.dataset.zoneControls),
    previousButton: shell.querySelector('[data-zone-nav="previous"]'),
    nextButton: shell.querySelector('[data-zone-nav="next"]')
  }))
  .filter(
    ({ container, previousButton, nextButton }) =>
      Boolean(container) && Boolean(previousButton) && Boolean(nextButton)
  )

const modal = document.getElementById("animal-modal")
const modalName = document.getElementById("animal-name")
const modalType = document.getElementById("animal-type")
const modalPlace = document.getElementById("animal-place")
const modalWeightLabel = document.getElementById("animal-weight-label")
const modalWeight = document.getElementById("animal-weight")
const closeTargets = document.querySelectorAll("[data-close-modal]")
const actionButtons = document.querySelectorAll("[data-action]")
const toast = document.getElementById("toast")

const elementsInterface = {
  popup: document.getElementById("popup-achat"),
  boutonFermer: document.getElementById("bouton-fermer"),
  solde: document.getElementById("solde-ecus"),
  message: document.getElementById("message-action"),
  compteurs: {
    vache: document.getElementById("compteur-vache"),
    poule: document.getElementById("compteur-poule"),
    lapin: document.getElementById("compteur-lapin")
  }
}

let currentFarmModel = null
let collectiviteFeedbackTimeout = null
let toastTimeoutId = null
let activeActionTarget = ""
let activeAnimalId = null
let animalZoneResizeBound = false
let animalZoneRefreshFrame = null

const ANIMATED_ANIMAL_CONFIG = {
  poule: {
    spriteClass: "animal-sprite--chicken",
    sizeClass: "animal-icon--chicken",
    minDuration: 5.4,
    maxDuration: 8.4,
    allowMirror: true
  },
  vache: {
    spriteClass: "animal-sprite--cow",
    sizeClass: "animal-icon--cow",
    minDuration: 7.2,
    maxDuration: 10.2,
    allowMirror: false
  },
  lapin: {
    spriteClass: "animal-sprite--rabbit",
    sizeClass: "animal-icon--rabbit",
    minDuration: 5.6,
    maxDuration: 8.2,
    allowMirror: false
  }
}

function randomBetween(min, max) {
  return Math.random() * (max - min) + min
}

function getAnimatedAnimalConfig(typeKey) {
  return ANIMATED_ANIMAL_CONFIG[typeKey] || null
}

function createAnimatedSprite(typeKey) {
  const config = getAnimatedAnimalConfig(typeKey)

  if (!config) {
    return null
  }

  const spriteDuration = randomBetween(config.minDuration, config.maxDuration)
  const sprite = document.createElement("span")
  sprite.className = `animal-sprite ${config.spriteClass}`
  sprite.setAttribute("aria-hidden", "true")
  sprite.style.setProperty("--sprite-duration", `${spriteDuration.toFixed(2)}s`)
  sprite.style.setProperty("--sprite-delay", `${(-1 * randomBetween(0, spriteDuration)).toFixed(2)}s`)
  sprite.style.setProperty(
    "--sprite-direction",
    config.allowMirror && Math.random() > 0.45 ? "-1" : "1"
  )
  return sprite
}

function createAnimalVisual(animal) {
  const sprite = createAnimatedSprite(animal.typeKey)

  if (sprite) {
    return sprite
  }

  const image = document.createElement("img")
  image.src = `assets/${animal.img}`
  image.alt = animal.name
  return image
}

function getCurrentProduct() {
  return (
    stockState.products.find((product) => product.id === stockState.selectedProductId) ||
    stockState.products[0] ||
    null
  )
}

function clamp(value, min, max) {
  return Math.min(Math.max(value, min), max)
}

function formatEcus(value) {
  return `${value} ${value > 1 ? "ecus" : "ecu"}`
}

function formatWeight(weight) {
  if (!Number.isFinite(weight)) {
    return "-"
  }

  return `${Number.parseFloat(weight.toFixed(1))} kg`
}

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
    .map((product) => `<option value="${product.id}">${product.name}</option>`)
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
  if (stockTotalUnits) {
    stockTotalUnits.textContent = String(
      stockState.products.reduce((total, item) => total + item.stock, 0)
    )
  }
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

function createAnimalIcon(animal, { groupOnly = false } = {}) {
  const element = document.createElement(groupOnly ? "span" : "button")
  element.className = `animal-icon${groupOnly ? " animal-icon--decorative" : ""}`
  element.title = animal.name

  const spriteConfig = getAnimatedAnimalConfig(animal.typeKey)

  if (spriteConfig?.sizeClass) {
    element.classList.add(spriteConfig.sizeClass)
  }

  if (!groupOnly) {
    element.type = "button"
    element.setAttribute("aria-label", `${animal.name}, ${animal.typeLabel}`)
    element.addEventListener("click", () => openAnimalModal(animal))
  } else {
    element.setAttribute("aria-hidden", "true")
  }

  element.appendChild(createAnimalVisual(animal))
  return element
}

function renderEmptyZone(container, message) {
  const emptyState = document.createElement("p")
  emptyState.className = "animals-empty"
  emptyState.textContent = message
  container.appendChild(emptyState)
}

function hasHorizontalOverflow(container) {
  return Boolean(container) && container.scrollWidth > container.clientWidth + 1
}

function getAnimalZoneMaxScroll(container) {
  return Math.max(container.scrollWidth - container.clientWidth, 0)
}

function getAnimalZoneStep(container) {
  return Math.max(container.clientWidth * 0.5, 1)
}

function updateAnimalZoneNavigation(zoneController) {
  const { shell, container, previousButton, nextButton } = zoneController
  const maxScrollLeft = getAnimalZoneMaxScroll(container)
  const hasOverflow = hasHorizontalOverflow(container)

  if (!hasOverflow && container.scrollLeft !== 0) {
    container.scrollLeft = 0
  } else if (hasOverflow && container.scrollLeft > maxScrollLeft) {
    container.scrollLeft = maxScrollLeft
  }

  shell.classList.toggle("has-overflow", hasOverflow)
  previousButton.disabled = !hasOverflow || container.scrollLeft <= 1
  nextButton.disabled = !hasOverflow || container.scrollLeft >= maxScrollLeft - 1
}

function scheduleAnimalZoneNavigationRefresh() {
  if (animalZoneRefreshFrame !== null) {
    return
  }

  animalZoneRefreshFrame = window.requestAnimationFrame(() => {
    animalZoneRefreshFrame = null
    animalZoneControllers.forEach(updateAnimalZoneNavigation)
  })
}

function scrollAnimalZone(zoneController, direction, event) {
  if (event) {
    event.preventDefault()
    event.stopPropagation()
  }

  const { container } = zoneController
  const maxScrollLeft = getAnimalZoneMaxScroll(container)

  if (maxScrollLeft <= 1) {
    return
  }

  const nextScrollLeft = clamp(
    container.scrollLeft + getAnimalZoneStep(container) * direction,
    0,
    maxScrollLeft
  )

  container.scrollTo({
    left: nextScrollLeft,
    behavior: "smooth"
  })
}

function initializeAnimalZoneNavigation() {
  animalZoneControllers.forEach((zoneController) => {
    if (zoneController.shell.dataset.zoneNavigationBound === "true") {
      return
    }

    zoneController.previousButton.addEventListener("click", (event) => {
      scrollAnimalZone(zoneController, -1, event)
    })

    zoneController.nextButton.addEventListener("click", (event) => {
      scrollAnimalZone(zoneController, 1, event)
    })

    zoneController.container.addEventListener("scroll", () => {
      updateAnimalZoneNavigation(zoneController)
    })

    zoneController.shell.dataset.zoneNavigationBound = "true"
  })

  if (!animalZoneResizeBound) {
    window.addEventListener("resize", scheduleAnimalZoneNavigationRefresh)
    animalZoneResizeBound = true
  }

  scheduleAnimalZoneNavigationRefresh()
}

function renderAnimalZones() {
  if (!currentFarmModel) {
    return
  }

  const cows = TinyFarmState.getAnimalsByType(currentFarmModel, "vache")
  const chickens = TinyFarmState.getAnimalsByType(currentFarmModel, "poule")
  const rabbits = TinyFarmState.getAnimalsByType(currentFarmModel, "lapin")

  if (paturageContainer) {
    paturageContainer.innerHTML = ""

    if (cows.length === 0) {
      renderEmptyZone(paturageContainer, "Aucune vache")
    } else {
      cows.forEach((animal) => {
        paturageContainer.appendChild(createAnimalIcon(animal))
      })
    }
  }

  if (poulaillerContainer) {
    poulaillerContainer.innerHTML = ""

    if (chickens.length === 0) {
      renderEmptyZone(poulaillerContainer, "Aucune poule")
    } else {
      chickens.forEach((animal) => {
        poulaillerContainer.appendChild(createAnimalIcon(animal))
      })
    }
  }

  if (clapierContainer) {
    clapierContainer.innerHTML = ""
    clapierContainer.dataset.groupTooltip = `${rabbits.length} lapin${rabbits.length > 1 ? "s" : ""} - actions de groupe`
    clapierContainer.classList.toggle("is-empty", rabbits.length === 0)

    if (rabbits.length === 0) {
      renderEmptyZone(clapierContainer, "Aucun lapin")
    } else {
      rabbits.forEach((animal) => {
        clapierContainer.appendChild(createAnimalIcon(animal, { groupOnly: true }))
      })
    }
  }

  scheduleAnimalZoneNavigationRefresh()
}

function mettreAJourInterface() {
  if (!currentFarmModel) {
    return
  }

  if (elementsInterface.solde) {
    elementsInterface.solde.textContent = String(currentFarmModel.balance)
  }

  Object.entries(currentFarmModel.counts).forEach(([typeKey, count]) => {
    if (elementsInterface.compteurs[typeKey]) {
      elementsInterface.compteurs[typeKey].textContent = String(count)
    }
  })
}

function afficherMessage(text, type = "") {
  if (!elementsInterface.message) {
    return
  }

  elementsInterface.message.textContent = text
  elementsInterface.message.classList.remove("erreur", "succes")

  if (type) {
    elementsInterface.message.classList.add(type)
  }
}

function estAchatAutorise(typeAnimal) {
  if (!currentFarmModel) {
    return false
  }

  const catalogEntry = TinyFarmState.ANIMAL_CATALOG[typeAnimal]

  if (!catalogEntry) {
    return false
  }

  return currentFarmModel.uiState.level >= catalogEntry.minLevel
}

function mettreAJourDisponibiliteBoutons() {
  const boutonsAchat = document.querySelectorAll(".btn-acheter")

  boutonsAchat.forEach((bouton) => {
    const typeAnimal = bouton.dataset.animal
    const autorise = estAchatAutorise(typeAnimal)
    const alreadyOwnsCow = currentFarmModel && currentFarmModel.counts.vache > 0

    bouton.disabled = !autorise

    if (!autorise && typeAnimal === "vache") {
      bouton.textContent = alreadyOwnsCow ? "Deja possedee" : "Niveau 2 requis"
      bouton.title = alreadyOwnsCow
        ? "La vache est deja presente dans la ferme au niveau 1."
        : "La vache devient achetable a partir du niveau 2."
      return
    }

    bouton.textContent = "Acheter"
    bouton.title = ""
  })
}

function updateFarmOverlayState(uiState) {
  const normalizedState = TinyFarmState.writeUiState(uiState)
  currentFarmModel = TinyFarmState.buildFarmModel(currentFarmModel.rawData, normalizedState)
  renderAnimalZones()
  mettreAJourInterface()
  mettreAJourDisponibiliteBoutons()
}

async function traiterAchat(typeAnimal) {
  if (!currentFarmModel) {
    afficherMessage("Donnees animaux indisponibles.", "erreur")
    return
  }

  const catalogEntry = TinyFarmState.ANIMAL_CATALOG[typeAnimal]

  if (!catalogEntry) {
    afficherMessage("Animal inconnu, achat annule.", "erreur")
    return
  }

  if (currentFarmModel.balance < catalogEntry.price) {
    afficherMessage(
      `Solde insuffisant pour acheter ${catalogEntry.article} ${catalogEntry.label.toLowerCase()}.`,
      "erreur"
    )
    return
  }

  const fermeId = TinyFarmState.getFermeId()
  if (!fermeId) {
    afficherMessage("Ferme non trouvee.", "erreur")
    return
  }

  try {
    const displayIndex = (currentFarmModel.counts[typeAnimal] || 0) + 1
    const response = await fetch(`/api/fermes/${fermeId}/animaux`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        nom: `${catalogEntry.label}-${displayIndex}`,
        typeAnimal: typeAnimal.toUpperCase()
      })
    })

    if (!response.ok) {
      const err = await response.json().catch(() => ({ error: "Erreur serveur" }))
      afficherMessage(err.error || "Achat echoue.", "erreur")
      return
    }

    TinyFarmState.invalidateCache()
    await initializeFarmState()
    afficherMessage(`Achat valide : ${catalogEntry.label}.`, "succes")
  } catch (e) {
    afficherMessage("Erreur reseau lors de l'achat.", "erreur")
  }
}

function setAnimalShopOpen(isOpen, { announce = true } = {}) {
  if (!elementsInterface.popup || !farmScreen) {
    return
  }

  elementsInterface.popup.classList.toggle("hidden", !isOpen)
  farmScreen.classList.toggle("popup-ouverte", isOpen)

  if (announce) {
    afficherMessage(isOpen ? "Boutique ouverte." : "Popup fermee.", "succes")
  }
}

function ouvrirPopup() {
  mettreAJourInterface()
  mettreAJourDisponibiliteBoutons()
  setAnimalShopOpen(true)
}

function fermerPopup({ announce = true } = {}) {
  if (!elementsInterface.popup || elementsInterface.popup.classList.contains("hidden")) {
    return
  }

  setAnimalShopOpen(false, { announce })
}

function openActionModal({ name, typeLabel, homeLabel, weightLabel, weightValue, targetLabel }) {
  if (!modal) {
    return
  }

  modalName.textContent = name
  modalType.textContent = typeLabel
  modalPlace.textContent = homeLabel
  modalWeightLabel.textContent = weightLabel
  modalWeight.textContent = weightValue
  activeActionTarget = targetLabel
  modal.classList.remove("hidden")
  modal.setAttribute("aria-hidden", "false")
}

function openAnimalModal(animal) {
  activeAnimalId = animal.id || null
  openActionModal({
    name: animal.name,
    typeLabel: animal.typeLabel,
    homeLabel: animal.homeLabel,
    weightLabel: "Poids :",
    weightValue: formatWeight(animal.weight),
    targetLabel: animal.name
  })
}

function openClapierModal() {
  if (!currentFarmModel) {
    return
  }

  const rabbits = TinyFarmState.getAnimalsByType(currentFarmModel, "lapin")

  if (rabbits.length === 0) {
    return
  }

  openActionModal({
    name: "Clapier",
    typeLabel: "Lapins",
    homeLabel: "Clapier",
    weightLabel: "Effectif :",
    weightValue: `${rabbits.length} lapin${rabbits.length > 1 ? "s" : ""}`,
    targetLabel: "tout le clapier"
  })
}

function closeActionModal() {
  if (!modal || modal.classList.contains("hidden")) {
    return
  }

  modal.classList.add("hidden")
  modal.setAttribute("aria-hidden", "true")
}

function showToast(message) {
  if (!toast) {
    return
  }

  toast.textContent = message
  toast.classList.remove("hidden")
  toast.classList.add("show")

  if (toastTimeoutId) {
    window.clearTimeout(toastTimeoutId)
  }

  toastTimeoutId = window.setTimeout(() => {
    toast.classList.remove("show")
    toast.classList.add("hidden")
    toastTimeoutId = null
  }, 1600)
}

async function initializeFarmState() {
  try {
    const data = await TinyFarmState.fetchFarmData()
    currentFarmModel = TinyFarmState.buildFarmModel(data)
    renderAnimalZones()
    mettreAJourInterface()
    mettreAJourDisponibiliteBoutons()
  } catch (error) {
    console.error("Erreur lors du chargement des animaux :", error)
    afficherMessage("Impossible de charger les animaux.", "erreur")
  }
}

async function initializeStockPanel() {
  try {
    const data = await TinyFarmState.fetchFarmData()
    const products = data.stockProducts.length > 0 ? data.stockProducts : fallbackStockProducts

    stockState.products = products.map((product) => ({ ...product }))
    stockState.selectedProductId = stockState.products[0] ? stockState.products[0].id : null
    stockState.quantity = stockState.products[0] && stockState.products[0].stock > 0 ? 1 : 0

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
    const data = await TinyFarmState.fetchFarmData()
    const items = data.communityItems.length > 0 ? data.communityItems : fallbackCommunityItems
    renderCollectivitePanel(items)
  } catch (error) {
    console.error("Erreur lors du chargement de la collectivite :", error)
    renderCollectivitePanel(fallbackCommunityItems)
    setCollectiviteFeedback("Collectivite chargee avec les donnees de secours.")
  }
}

async function classement() {
  if (!tbody) {
    return
  }

  try {
    const response = await fetch("/api/fermes/classement")
    if (!response.ok) {
      throw new Error("Classement indisponible")
    }
    const players = await response.json()
    tbody.innerHTML = ""

    players.forEach((player, index) => {
      const row = document.createElement("tr")
      row.innerHTML = `
        <td>${index + 1}</td>
        <td>${player.name}</td>
        <td>${player.score || 0}</td>
        <td>${player.capacity || 0}</td>
        <td>${player.money || 0}</td>
      `
      tbody.appendChild(row)
    })
  } catch (error) {
    console.error("Erreur lors du chargement du classement :", error)
  }
}

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

function showFarmScreen() {
  if (!loginScreen || !farmScreen) {
    return
  }

  loginScreen.classList.add("hidden")
  farmScreen.classList.remove("hidden")
  initializeFarmState()
}

function showLoginScreen() {
  if (!loginScreen || !farmScreen) {
    return
  }

  loginScreen.classList.remove("hidden")
  farmScreen.classList.add("hidden")
}
//Pour implementer le login github, decommenter, et faites tout le chemin du README.md du tp5
/*
const loginScreen      = document.getElementById("login-screen");
const farmScreen       = document.getElementById("farm-screen");
const loginBtn         = document.getElementById("login-btn");
const clsBtn           = document.getElementById("Trophy");
const classementScreen = document.getElementById("Classement");
const tbody            = document.getElementById("classement-body");

//vérifie si une session est deja active
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

// Clic login redirige vers GitHub
loginBtn.addEventListener("click", () => {
    window.location.href = "/oauth2/authorization/github";
});

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
    */

if (loginBtn) {
  loginBtn.addEventListener("click", () => {
    window.location.href = "/oauth2/authorization/github"
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

closeTargets.forEach((button) => {
  button.addEventListener("click", closeActionModal)
})

actionButtons.forEach((button) => {
  button.addEventListener("click", async () => {
    const action = button.dataset.action
    const target = activeActionTarget || "la ferme"

    if (!activeAnimalId) {
      showToast(`${action} : ${target} - pas d'animal`)
      return
    }

    const actionMap = {
      "Nourrir": "nourrir",
      "Abreuver": "abreuver",
      "Soigner": "soigner",
      "Nettoyer": "nettoyer"
    }

    const endpoint = actionMap[action]
    if (!endpoint) {
      showToast(`${action} : action inconnue`)
      return
    }

    try {
      const response = await fetch(`/api/animaux/${activeAnimalId}/${endpoint}`, {
        method: "PATCH"
      })

      if (!response.ok) {
        const errText = await response.text()
        showToast(`Echec : ${errText}`)
        return
      }

      showToast(`${action} : ${target} - OK`)
      TinyFarmState.invalidateCache()
      await initializeFarmState()
    } catch (e) {
      showToast(`Erreur reseau pour ${action}`)
    }
  })
})

window.addEventListener("focus", () => {
  initializeFarmState()
})

window.addEventListener("storage", (event) => {
  if (event.key === TinyFarmState.STORAGE_KEY) {
    initializeFarmState()
  }
})

classement()

document.addEventListener("DOMContentLoaded", async () => {
  // Verification de session OAuth au chargement
  try {
    const res = await fetch("/api/me")
    if (res.ok) {
      showFarmScreen()
    }
  } catch (e) {
    console.error("Impossible de joindre le serveur :", e)
  }

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
      closeActionModal()
      fermerPopup({ announce: false })
      setStockPanelOpen(false)
      showLoginScreen()

      document.querySelectorAll(".settings-btn").forEach((currentButton) => currentButton.classList.remove("open"))
      document.querySelectorAll(".settings-panel").forEach((panel) => panel.classList.remove("open"))
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
      stockState.quantity = getCurrentProduct() && getCurrentProduct().stock > 0 ? 1 : 0
      updateStockPanel()
      setStockFeedback()
    })
  }

  if (stockMinusButton) {
    stockMinusButton.addEventListener("click", () => adjustStockQuantity(-1))
  }

  if (stockPlusButton) {
    stockPlusButton.addEventListener("click", () => adjustStockQuantity(1))
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
      stockState.quantity = getCurrentProduct() && getCurrentProduct().stock > 0 ? 1 : 0
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

      if (selectedItem.id === "buy-animals") {
        ouvrirPopup()
        return
      }

      setCollectiviteFeedback(`${selectedItem.label} selectionne.`)
    })
  }

  if (elementsInterface.boutonFermer) {
    elementsInterface.boutonFermer.addEventListener("click", () => fermerPopup())
  }

  document.querySelectorAll(".btn-acheter").forEach((button) => {
    button.addEventListener("click", () => {
      traiterAchat(button.dataset.animal)
    })
  })

  if (clapierContainer) {
    clapierContainer.addEventListener("click", openClapierModal)
    clapierContainer.addEventListener("keydown", (event) => {
      if (event.key === "Enter" || event.key === " ") {
        event.preventDefault()
        openClapierModal()
      }
    })
  }

  document.addEventListener("click", () => {
    setStockPanelOpen(false)
  })

  document.addEventListener("keydown", (event) => {
    if (event.key === "Escape") {
      setStockPanelOpen(false)
      fermerPopup({ announce: false })
      closeActionModal()
    }
  })

  initializeAnimalZoneNavigation()
  initializeFarmState()
  initializeStockPanel()
  initializeCollectivitePanel()
  updateClock()
  window.setInterval(updateClock, 1000)
})
