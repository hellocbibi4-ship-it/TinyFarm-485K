const fallbackCommunityItems = [
  { id: "feed-bag", label: "Nourriture", price: 5 },
  { id: "straw-bales", label: "Bottes de paille", price: 5 },
  { id: "syringe", label: "Seringue", price: 6 },
  { id: "water-bucket", label: "Seau d'eau", price: 2 },
  { id: "soap", label: "Savon", price: 3 },
  { id: "buy-animals", label: "Achat d'animaux", variant: "shortcut" },
  { id: "farmers-market", label: "Marché des producteurs", variant: "shortcut" }
]

console.log("TinyFarm script loaded")

const stockState = {
  products: [],
  selectedProductId: null,
  quantity: 1
}


const collectiviteState = {
  items: []
}

const CARE_ITEM_IDS = ["feed-bag", "straw-bales", "syringe", "water-bucket", "soap"]
const CARE_ITEM_TO_API_TYPE = {
  "feed-bag": "NOURRITURE",
  "straw-bales": "PAILLE",
  syringe: "SERINGUE",
  "water-bucket": "EAU",
  soap: "SAVON"
}

const loginBtn = document.getElementById("login-btn")
const loginScreen = document.getElementById("login-screen")
const loginModal = document.getElementById("login-modal")
const loginForm = document.getElementById("login-form")
const loginUsernameInput = document.getElementById("login-username")
const loginPasswordInput = document.getElementById("login-password")
const loginFeedback = document.getElementById("login-feedback")
const loginCloseTargets = document.querySelectorAll("[data-close-login-modal]")
const clsBtn = document.getElementById("Trophy")
const farmScreen = document.getElementById("farm-screen")
const classementScreen = document.getElementById("Classement")
const tbody = document.getElementById("classement-body")

const stockToggle = document.getElementById("stock-toggle")
const stockPanel = document.getElementById("stock-panel")
const stockTotalUnits = document.getElementById("stock-total-units")
const stockTableBody = document.getElementById("stock-table-body")
const stockFeedback = document.getElementById("stock-feedback")
const collectiviteList = document.getElementById("collectivite-list")
const collectiviteFeedback = document.getElementById("collectivite-feedback")
const careInventoryCounters = Array.from(document.querySelectorAll("[data-care-count]")).reduce(
  (accumulator, element) => {
    accumulator[element.dataset.careCount] = element
    return accumulator
  },
  {}
)

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
const modalStatus = document.getElementById("animal-status")
const modalHealth = document.getElementById("animal-health")
const modalHunger = document.getElementById("animal-hunger")
const modalHydration = document.getElementById("animal-hydration")
const modalWeightLabel = document.getElementById("animal-weight-label")
const modalWeight = document.getElementById("animal-weight")
const closeTargets = document.querySelectorAll("[data-close-modal]")
const actionButtons = document.querySelectorAll("[data-action]")
const toast = document.getElementById("toast")
const popupMarche = document.getElementById("popup-marche")
const boutonFermerMarche = document.getElementById("bouton-fermer-marche")
const closeMarcheTargets = document.querySelectorAll("[data-close-marche]")
const marcheTabButtons = Array.from(document.querySelectorAll("[data-marche-tab]"))
const marchePanels = Array.from(document.querySelectorAll("[data-marche-panel]"))
const marcheAchatBody = document.getElementById("marche-achat-body")
const marcheAchatFeedback = document.getElementById("marche-achat-feedback")
const marcheVenteFeedback = document.getElementById("marche-vente-feedback")
const marcheVenteProduit = document.getElementById("marche-vente-produit")
const marcheVenteStock = document.getElementById("marche-vente-stock")
const marcheVenteQuantite = document.getElementById("marche-vente-quantite")
const marcheVentePrix = document.getElementById("marche-vente-prix")
const marcheVenteSubmit = document.getElementById("marche-vente-submit")

const poulaillerListModal = document.getElementById("poulailler-list-modal")
const poulaillerListBody = document.getElementById("poulailler-list-body")

const elementsInterface = {
  popup: document.getElementById("popup-achat"),
  boutonFermer: document.getElementById("bouton-fermer"),
  solde: document.getElementById("solde-ecus"),
  message: document.getElementById("message-action"),
  ownerName: document.getElementById("farm-owner-name"),
  ownerBalance: document.getElementById("farm-owner-balance"),
  compteurs: {
    vache: document.getElementById("compteur-vache"),
    poule: document.getElementById("compteur-poule"),
    lapin: document.getElementById("compteur-lapin")
  }
}

let currentFarmModel = null
let currentFarmId = null
let currentUsername = "-"
let latestFarmData = null
let farmClockState = null
let farmRefreshIntervalId = null
let marketSaleState = {
  selectedProduct: "",
  quantity: 1,
  unitPrice: 1
}
let collectiviteFeedbackTimeout = null
let toastTimeoutId = null
let activeActionTarget = ""
let animalZoneResizeBound = false
let animalZoneRefreshFrame = null
let careInventoryState = createDefaultCareInventory()
let currentAnimalAction = {
  farmId: null,
  animalType: null,
  animalName: null
}

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

function createDefaultCareInventory() {
  return CARE_ITEM_IDS.reduce((inventory, itemId) => {
    inventory[itemId] = 0
    return inventory
  }, {})
}

function renderCareInventory() {
  CARE_ITEM_IDS.forEach((itemId) => {
    const counter = careInventoryCounters[itemId]

    if (counter) {
      counter.textContent = String(careInventoryState[itemId] || 0)
    }
  })
}

function applyCareInventory(data) {
  const inventory = data?.careInventory

  if (!inventory || typeof inventory !== "object") {
    careInventoryState = createDefaultCareInventory()
    renderCareInventory()
    return
  }

  careInventoryState = CARE_ITEM_IDS.reduce((normalizedInventory, itemId) => {
    normalizedInventory[itemId] = Math.max(0, Number.parseInt(inventory[itemId], 10) || 0)
    return normalizedInventory
  }, {})

  renderCareInventory()
}

function loadCareInventoryState() {
  careInventoryState = createDefaultCareInventory()
  renderCareInventory()
}

function renderCommunityFromData(data) {
  const items = Array.isArray(data?.communityItems) && data.communityItems.length > 0
    ? data.communityItems
    : fallbackCommunityItems

  renderCollectivitePanel(items)
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
  return null
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

function buildStockRows(data = null) {
  const rows = Array.isArray(data?.stockInventory) ? data.stockInventory : []

  if (rows.length > 0) {
    return rows.map((row) => ({
      category: row.category || "-",
      label: row.label || "-",
      quantity: Math.max(0, Number.parseInt(row.quantity, 10) || 0)
    }))
  }

  return [
    { category: "Produits", label: "Oeufs", quantity: 0 },
    { category: "Produits", label: "Lait", quantity: 0 },
    { category: "Produits", label: "Lapins", quantity: 0 },
    { category: "Entretien", label: "Nourriture", quantity: 0 },
    { category: "Entretien", label: "Seau d'eau", quantity: 0 },
    { category: "Entretien", label: "Bottes de paille", quantity: 0 },
    { category: "Entretien", label: "Savon", quantity: 0 },
    { category: "Entretien", label: "Seringue", quantity: 0 }
  ]
}

function getMarketSellRows(data = null) {
  return buildStockRows(data).filter((row) => {
    const normalizedLabel = normalizeMarketProduct(row.label)
    return Boolean(normalizedLabel)
  })
}

function normalizeMarketProduct(label) {
  const normalizedLabel = String(label || "").trim().toLowerCase()

  switch (normalizedLabel) {
    case "oeuf":
    case "oeufs":
      return "OEUF"
    case "lait":
      return "LAIT"
    case "lapin":
    case "lapins":
      return "LAPIN"
    case "nourriture":
    case "grain":
      return "NOURRITURE"
    case "seau d'eau":
      return "EAU"
    case "bottes de paille":
    case "paille":
      return "PAILLE"
    case "savon":
      return "SAVON"
    case "seringue":
      return "SERINGUE"
    default:
      return ""
  }
}

function formatMarketProductLabel(product) {
  switch (String(product || "").trim().toUpperCase()) {
    case "OEUF":
      return "Oeufs"
    case "LAIT":
      return "Lait"
    case "LAPIN":
      return "Lapins"
    case "NOURRITURE":
    case "GRAIN":
      return "Nourriture"
    case "EAU":
      return "Seau d'eau"
    case "PAILLE":
      return "Bottes de paille"
    case "SAVON":
      return "Savon"
    case "SERINGUE":
      return "Seringue"
    default:
      return String(product || "-")
  }
}

function setMarcheFeedback(element, message = "", type = "") {
  if (!element) {
    return
  }

  element.textContent = message
  element.classList.remove("is-error", "is-success")

  if (type) {
    element.classList.add(type)
  }
}

function renderMarcheAchatRows(data = null) {
  if (!marcheAchatBody) {
    return
  }

  const offers = Array.isArray(data?.marketOffers) ? data.marketOffers : []

  if (offers.length === 0) {
    marcheAchatBody.innerHTML = `
      <tr>
        <td class="popup-marche-empty" colspan="6">Aucune offre disponible pour le moment.</td>
      </tr>
    `
    return
  }

  marcheAchatBody.innerHTML = offers
    .map((offer) => {
      const quantity = Math.max(0, Number.parseInt(offer.quantity, 10) || 0)
      const isOwnOffer = currentFarmId && Number(offer.sellerFarmId) === Number(currentFarmId)
      return `
        <tr>
          <td>${offer.sellerName || "-"}</td>
          <td>${formatMarketProductLabel(offer.product)}</td>
          <td>${quantity}</td>
          <td>${Number.parseInt(offer.unitPrice, 10) || 0}</td>
          <td>
            <input
              class="popup-marche-number"
              type="number"
              min="1"
              max="${Math.max(quantity, 1)}"
              value="1"
              data-market-buy-qty="${offer.id}"
              ${quantity <= 0 || isOwnOffer ? "disabled" : ""}
            >
          </td>
          <td>
            <button
              class="popup-marche-action popup-marche-action--buy"
              type="button"
              data-market-buy="${offer.id}"
              ${quantity <= 0 || isOwnOffer ? "disabled" : ""}
            >
              ${isOwnOffer ? "Ta ferme" : "Acheter"}
            </button>
          </td>
        </tr>
      `
    })
    .join("")
}

function updateMarcheVenteSelection() {
  if (!marcheVenteProduit || !marcheVenteStock || !marcheVenteQuantite || !marcheVentePrix || !marcheVenteSubmit) {
    return
  }

  const selectedOption = marcheVenteProduit.selectedOptions[0]
  const stockQuantity = Math.max(0, Number.parseInt(selectedOption?.dataset.stockQuantity, 10) || 0)
  marketSaleState.selectedProduct = marcheVenteProduit.value

  marcheVenteStock.textContent = String(stockQuantity)
  marcheVenteQuantite.max = String(Math.max(stockQuantity, 1))

  if (stockQuantity <= 0) {
    marcheVenteQuantite.value = "0"
    marketSaleState.quantity = 0
    marcheVenteQuantite.disabled = true
    marcheVentePrix.disabled = true
    marcheVenteSubmit.disabled = true
    return
  }

  const currentQuantity = Math.max(1, Number.parseInt(marcheVenteQuantite.value, 10) || 1)
  marcheVenteQuantite.value = String(Math.min(currentQuantity, stockQuantity))
  marketSaleState.quantity = Number.parseInt(marcheVenteQuantite.value, 10) || 1
  marketSaleState.unitPrice = Math.max(1, Number.parseInt(marcheVentePrix.value, 10) || 1)
  marcheVenteQuantite.disabled = false
  marcheVentePrix.disabled = false
  marcheVenteSubmit.disabled = false
}

function renderMarcheVenteForm(data = null) {
  if (!marcheVenteProduit || !marcheVenteStock || !marcheVenteQuantite || !marcheVenteSubmit) {
    return
  }

  const rows = getMarketSellRows(data)

  if (rows.length === 0) {
    marcheVenteProduit.innerHTML = `<option value="">Aucun produit</option>`
    marcheVenteProduit.disabled = true
    marketSaleState.selectedProduct = ""
    marcheVenteQuantite.value = "0"
    marcheVenteQuantite.disabled = true
    marcheVentePrix.value = "1"
    marcheVentePrix.disabled = true
    marcheVenteSubmit.disabled = true
    marcheVenteStock.textContent = "0"
    return
  }

  marcheVenteProduit.disabled = false
  marcheVentePrix.disabled = false
  marcheVenteProduit.innerHTML = rows
    .map((row) => {
      const product = normalizeMarketProduct(row.label)
      const stockQuantity = Math.max(0, Number.parseInt(row.quantity, 10) || 0)
      return `<option value="${product}" data-stock-quantity="${stockQuantity}">${row.label}</option>`
    })
    .join("")

  const availableProducts = rows.map((row) => normalizeMarketProduct(row.label))
  const preferredProduct = availableProducts.includes(marketSaleState.selectedProduct)
    ? marketSaleState.selectedProduct
    : (rows.find((row) => (Number.parseInt(row.quantity, 10) || 0) > 0)
        ? normalizeMarketProduct(rows.find((row) => (Number.parseInt(row.quantity, 10) || 0) > 0).label)
        : availableProducts[0])

  marcheVenteProduit.value = preferredProduct || availableProducts[0] || ""
  marketSaleState.selectedProduct = marcheVenteProduit.value

  marcheVenteQuantite.value = String(Math.max(1, Number.parseInt(marketSaleState.quantity, 10) || 1))
  marcheVentePrix.value = String(Math.max(1, Number.parseInt(marketSaleState.unitPrice, 10) || 1))

  updateMarcheVenteSelection()
}

function renderMarketPanels(data = null) {
  renderMarcheAchatRows(data)
  renderMarcheVenteForm(data)
}

function renderStockTable(rows) {
  if (!stockTableBody) {
    return
  }

  stockTableBody.innerHTML = rows
    .map(
      (row) => `
        <tr>
          <td>${row.category}</td>
          <td>${row.label}</td>
          <td>${row.quantity}</td>
        </tr>
      `
    )
    .join("")

  if (stockTotalUnits) {
    stockTotalUnits.textContent = String(
      rows.reduce((total, row) => total + (Number.isFinite(row.quantity) ? row.quantity : 0), 0)
    )
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
  return
}

function updateStockPanel() {
  if (stockTotalUnits) {
    stockTotalUnits.textContent = "0"
  }
}

function adjustStockQuantity(delta) {
  return
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

  if (elementsInterface.ownerName) {
    elementsInterface.ownerName.textContent = currentUsername || "-"
  }

  if (elementsInterface.ownerBalance) {
    elementsInterface.ownerBalance.textContent = String(currentFarmModel.balance)
  }

  Object.entries(currentFarmModel.counts).forEach(([typeKey, count]) => {
    if (elementsInterface.compteurs[typeKey]) {
      elementsInterface.compteurs[typeKey].textContent =
        typeKey === "vache" || typeKey === "poule"
          ? `${count}/${count}`
          : String(count)
    }
  })
}

function applyFarmData(data) {
  latestFarmData = data
  applyGameClock(data)
  const uiState = createFreshUiState(data)
  currentFarmModel = TinyFarmState.buildFarmModel(data, uiState)
  console.log("DEBUG: applyFarmData called")
  console.log("DEBUG: data.animals count:", data?.animals?.length)
  console.log("DEBUG: currentFarmModel.animals count:", currentFarmModel?.animals?.length)
  console.log("DEBUG: currentFarmModel.counts:", currentFarmModel?.counts)
  applyCareInventory(data)
  renderCommunityFromData(data)
  renderMarketPanels(data)
  renderAnimalZones()
  mettreAJourInterface()
  renderFarmDataStatus(data)
  mettreAJourDisponibiliteBoutons()
}

function renderFarmDataStatus(data) {
  const statusElement = document.getElementById("farm-data-status")

  if (!statusElement) {
    return
  }

  const animalsCount = Array.isArray(data?.animals) ? data.animals.length : 0
  const cashValue = Number(data?.cash)
  const cashText = Number.isFinite(cashValue) ? `${cashValue} ecus` : "0 ecus"

  statusElement.textContent = `Données chargées : ${animalsCount} animaux, ${cashText}`
}

function applyGameClock(data) {
  const gameTime = data?.gameTime

  if (!gameTime || typeof gameTime !== "object") {
    farmClockState = null
    return
  }

  farmClockState = {
    day: Math.max(1, Number.parseInt(gameTime.day, 10) || 1),
    hours: Math.max(0, Number.parseInt(gameTime.hours, 10) || 0),
    minutes: Math.max(0, Number.parseInt(gameTime.minutes, 10) || 0),
    seconds: Math.max(0, Number.parseInt(gameTime.seconds, 10) || 0),
    realSecondsPerDay: Math.max(1, Number.parseInt(gameTime.realSecondsPerDay, 10) || 60),
    syncedAtMs: Date.now()
  }
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
    afficherMessage("Données animales indisponibles.", "erreur")
    return
  }

  const catalogEntry = TinyFarmState.ANIMAL_CATALOG[typeAnimal]

  if (!catalogEntry) {
    afficherMessage("Animal inconnu, achat annule.", "erreur")
    return
  }

  if (!estAchatAutorise(typeAnimal)) {
    const message = typeAnimal === "vache"
      ? "Niveau 1 : la vache est deja fournie et ne peut pas etre rachetee."
      : `Niveau insuffisant pour acheter ${catalogEntry.article} ${catalogEntry.label.toLowerCase()}.`

    afficherMessage(message, "erreur")
    return
  }

  if (currentFarmModel.balance < catalogEntry.price) {
    afficherMessage(
      `Solde insuffisant pour acheter ${catalogEntry.article} ${catalogEntry.label.toLowerCase()}.`,
      "erreur"
    )
    return
  }

  if (!currentFarmId) {
    afficherMessage("Connecte-toi pour acheter des animaux.", "erreur")
    return
  }

  try {
    const response = await fetch(`/api/fermes/${currentFarmId}/acheter-animal?type=${encodeURIComponent(typeAnimal)}`, {
      method: "POST"
    })

    if (!response.ok) {
      const errorText = await response.text()
      afficherMessage(errorText || "Impossible d'acheter cet animal.", "erreur")
      return
    }

    const farmData = await response.json()
    applyFarmData(farmData)
    afficherMessage(`Achat valide : ${catalogEntry.label}.`, "succes")
  } catch (error) {
    console.error("Erreur lors de l'achat :", error)
    afficherMessage("Impossible d'acheter cet animal.", "erreur")
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

function setMarcheTab(activeTab) {
  marcheTabButtons.forEach((button) => {
    const isActive = button.dataset.marcheTab === activeTab
    button.classList.toggle("is-active", isActive)
    button.setAttribute("aria-selected", String(isActive))
  })

  marchePanels.forEach((panel) => {
    panel.classList.toggle("is-active", panel.dataset.marchePanel === activeTab)
  })
}

async function refreshMarketData() {
  if (!currentFarmId) {
    renderMarketPanels()
    setMarcheFeedback(marcheAchatFeedback, "Connecte-toi pour accéder au marché.", "is-error")
    setMarcheFeedback(marcheVenteFeedback, "Connecte-toi pour accéder au marché.", "is-error")
    return
  }

  try {
    const data = await fetchActiveFarmData()
    applyFarmData(data)
    renderStockTable(buildStockRows(data))
    setStockFeedback()
    setMarcheFeedback(marcheAchatFeedback)
    setMarcheFeedback(marcheVenteFeedback)
  } catch (error) {
    console.error("Erreur lors du chargement du marche :", error)
    renderMarketPanels(latestFarmData)
    setMarcheFeedback(marcheAchatFeedback, "Impossible de charger le marché.", "is-error")
    setMarcheFeedback(marcheVenteFeedback, "Impossible de charger le marché.", "is-error")
  }
}

async function ouvrirPopupMarche() {
  if (!popupMarche || !farmScreen) {
    return
  }

  fermerPopup({ announce: false })
  setMarcheTab("achat")
  popupMarche.classList.remove("hidden")
  popupMarche.setAttribute("aria-hidden", "false")
  farmScreen.classList.add("popup-ouverte")
  await refreshMarketData()
}

function fermerPopupMarche() {
  if (!popupMarche || popupMarche.classList.contains("hidden")) {
    return
  }

  popupMarche.classList.add("hidden")
  popupMarche.setAttribute("aria-hidden", "true")

  if (!elementsInterface.popup || elementsInterface.popup.classList.contains("hidden")) {
    farmScreen.classList.remove("popup-ouverte")
  }
}

function openActionModal({
  name,
  typeLabel,
  homeLabel,
  statusValue,
  healthValue,
  hungerValue,
  hydrationValue,
  weightLabel,
  weightValue,
  targetLabel
}) {
  if (!modal) {
    return
  }

  modalName.textContent = name
  modalType.textContent = typeLabel
  modalPlace.textContent = homeLabel
  modalStatus.textContent = statusValue || "-"
  modalHealth.textContent = healthValue || "-"
  modalHunger.textContent = hungerValue || "-"
  modalHydration.textContent = hydrationValue || "-"
  modalWeightLabel.textContent = weightLabel
  modalWeight.textContent = weightValue
  activeActionTarget = targetLabel
  modal.classList.remove("hidden")
  modal.setAttribute("aria-hidden", "false")
}

function deriveAnimalStatus(animal) {
  if (animal?.status) {
    return animal.status
  }

  if (Number(animal?.health) < 60) {
    return "Fragile"
  }

  if (Number(animal?.hunger) < 60) {
    return "Affame"
  }

  if (Number(animal?.hydration) < 60) {
    return "Detrempe"
  }

  return animal?.stage ? String(animal.stage) : "En bonne sante"
}

function isCattleOrChicken(type) {
  return ["vache", "poule"].includes(String(type || "").toLowerCase())
}

function deriveHungerState(animalDetails, animalStats) {
  if (animalStats && Number.isFinite(animalStats.total) && animalStats.total > 0) {
    return animalStats.hungry > 0 ? "Affamé" : "Rassasié"
  }

  return Number(animalDetails.hunger) < 60 ? "Affamé" : "Rassasié"
}

function deriveHydrationState(animalDetails, animalStats) {
  if (animalStats && Number.isFinite(animalStats.total) && animalStats.total > 0) {
    return animalStats.thirsty > 0 ? "Assoiffé" : "Hydraté"
  }

  return Number(animalDetails.hydration) < 60 ? "Assoiffé" : "Hydraté"
}

async function openAnimalModal(animal) {
  if (!animal || !animal.id) {
    return
  }

  let animalDetails = animal
  let animalStats = null

  try {
    animalDetails = await fetchAnimalDetail(animal.id)
  } catch (error) {
    console.warn("Impossible de charger les donnees detaillees de l'animal :", error)
  }

  if (currentFarmId && animal.type) {
    try {
      const response = await fetch(`/api/fermes/${encodeURIComponent(currentFarmId)}/animaux/${encodeURIComponent(animal.type)}/status`)
      if (response.ok) {
        animalStats = await response.json()
      }
    } catch (error) {
      console.warn("Impossible de charger les stats du type d'animal :", error)
    }
  }

  const hungerValue = isCattleOrChicken(animal.type)
    ? deriveHungerState(animalDetails, animalStats)
    : animalStats
    ? `${animalStats.hungry}/${animalStats.total} affames`
    : `${animalDetails.hunger ?? 100}%`

  const thirstValue = isCattleOrChicken(animal.type)
    ? deriveHydrationState(animalDetails, animalStats)
    : animalStats
    ? `${animalStats.thirsty}/${animalStats.total} assoffes`
    : `${animalDetails.hydration ?? 100}%`

  openActionModal({
    name: animalDetails.name || animal.name,
    typeLabel: animalDetails.typeLabel || animal.type,
    homeLabel: animalDetails.homeLabel || animal.homeLabel || "Ferme",
    statusValue: deriveAnimalStatus(animalDetails),
    healthValue: `${animalDetails.health ?? 100}%`,
    hungerValue,
    hydrationValue: thirstValue,
    weightLabel: "Poids :",
    weightValue: formatWeight(animalDetails.weight),
    targetLabel: animalDetails.name || animal.name
  })

  currentAnimalAction.animalType = animal.type
  currentAnimalAction.farmId = currentFarmId
  currentAnimalAction.animalName = animalDetails.name || animal.name
}

async function openClapierModal() {
  if (!currentFarmModel || !currentFarmId) {
    return
  }

  const rabbits = TinyFarmState.getAnimalsByType(currentFarmModel, "lapin")

  if (rabbits.length === 0) {
    return
  }

  let rabbitHealth = {
    totalLapins: rabbits.length,
    sickLapins: 0,
    hungryLapins: 0,
    thirstyLapins: 0
  }

  try {
    const response = await fetch(`/api/fermes/${encodeURIComponent(currentFarmId)}/animaux/clapier`)
    if (response.ok) {
      rabbitHealth = await response.json()
    }
  } catch (error) {
    console.warn("Impossible de charger les donnees du clapier :", error)
  }

  const rabbitStatusValue = rabbitHealth.sickLapins === 0
    ? "Aucun lapin malade"
    : `${rabbitHealth.sickLapins}/${rabbitHealth.totalLapins} sont malades`

  const rabbitHealthValue = rabbitHealth.sickLapins === 0
    ? "Tous sains"
    : `${Math.max(0, rabbitHealth.totalLapins - rabbitHealth.sickLapins)}/${rabbitHealth.totalLapins} sains`

  const rabbitHungerValue = rabbitHealth.hungryLapins === 0
    ? "Personne n'a faim"
    : `${rabbitHealth.hungryLapins}/${rabbitHealth.totalLapins} ont faim`

  const rabbitHydrationValue = rabbitHealth.thirstyLapins === 0
    ? "Personne n'a soif"
    : `${rabbitHealth.thirstyLapins}/${rabbitHealth.totalLapins} ont soif`

  openActionModal({
    name: "Clapier",
    typeLabel: "Lapins",
    homeLabel: "Clapier",
    statusValue: rabbitStatusValue,
    healthValue: rabbitHealthValue,
    hungerValue: rabbitHungerValue,
    hydrationValue: rabbitHydrationValue,
    weightLabel: "Effectif :",
    weightValue: `${rabbitHealth.totalLapins} lapin${(rabbitHealth.totalLapins || 0) > 1 ? "s" : ""}`,
    targetLabel: "tout le clapier"
  })

  currentAnimalAction.animalType = "lapin"
  currentAnimalAction.farmId = currentFarmId
  currentAnimalAction.animalName = "clapier"
}

async function openPoulaillerListModal() {
  if (!currentFarmModel || !currentFarmId) {
    return
  }

  const chickens = TinyFarmState.getAnimalsByType(currentFarmModel, "poule")

  if (chickens.length === 0) {
    return
  }

  if (!poulaillerListBody) {
    return
  }

  poulaillerListBody.innerHTML = ""

  for (const chicken of chickens) {
    let animalDetails = chicken

    try {
      animalDetails = await fetchAnimalDetail(chicken.id)
    } catch (error) {
      console.warn("Impossible de charger les donnees detaillees de la poule :", error)
    }

    const statusValue = deriveAnimalStatus(animalDetails)
    const healthValue = `${animalDetails.health ?? 100}%`
    const hungerValue = deriveHungerState(animalDetails, null)
    const hydrationValue = deriveHydrationState(animalDetails, null)

    const row = document.createElement("tr")
    row.className = "poulailler-row"
    row.innerHTML = `
      <td>${animalDetails.name || chicken.name}</td>
      <td>${statusValue}</td>
      <td>${healthValue}</td>
      <td>${hungerValue}</td>
      <td>${hydrationValue}</td>
      <td><button type="button" class="action-btn poulailler-view-btn" data-animal-id="${chicken.id}">Voir</button></td>
    `

    // Add event listener to the button
    const viewButton = row.querySelector('.poulailler-view-btn')
    viewButton.addEventListener("click", () => {
      closePoulaillerListModal()
      openAnimalModal(chicken)
    })

    poulaillerListBody.appendChild(row)
  }

  poulaillerListModal.classList.remove("hidden")
  poulaillerListModal.setAttribute("aria-hidden", "false")
}

function closePoulaillerListModal() {
  if (!poulaillerListModal) {
    return
  }

  poulaillerListModal.classList.add("hidden")
  poulaillerListModal.setAttribute("aria-hidden", "true")
}

function closeActionModal() {
  if (!modal || modal.classList.contains("hidden")) {
    return
  }

  modal.classList.add("hidden")
  modal.setAttribute("aria-hidden", "true")
}

function closeAllModals() {
  closeActionModal()
  closePoulaillerListModal()
  fermerPopup()
  fermerPopupMarche()
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
  if (!currentFarmId) {
    currentFarmModel = null
    latestFarmData = null
    farmClockState = null
    loadCareInventoryState()
    renderMarketPanels()
    return
  }

  try {
    const data = await fetchActiveFarmData()
    applyFarmData(data)
  } catch (error) {
    console.error("Erreur lors du chargement des animaux :", error)
    afficherMessage("Impossible de charger les animaux.", "erreur")
  }
}

async function initializeStockPanel() {
  if (!currentFarmId) {
    renderStockTable(buildStockRows())
    setStockFeedback("Connecte-toi pour voir le stock reel.", "is-error")
    return
  }

  try {
    const data = await fetchActiveFarmData()
    renderStockTable(buildStockRows(data))
    setStockFeedback()
  } catch (error) {
    console.error("Erreur lors du chargement du stock :", error)
    renderStockTable(buildStockRows())
    setStockFeedback("Impossible de charger le stock reel.", "is-error")
  }
}

async function refreshFarmTimeAndProduction() {
  if (!currentFarmId || document.hidden) {
    return
  }

  try {
    const data = await fetchActiveFarmData()
    applyFarmData(data)

    if (stockPanel?.classList.contains("open")) {
      renderStockTable(buildStockRows(data))
      setStockFeedback()
    }
  } catch (error) {
    console.error("Erreur lors de l'actualisation du temps de jeu :", error)
  }
}

async function initializeCollectivitePanel() {
  if (!collectiviteList) {
    return
  }

  if (!currentFarmId) {
    renderCollectivitePanel(fallbackCommunityItems)
    return
  }

  try {
    const data = await fetchActiveFarmData()
    renderCommunityFromData(data)
  } catch (error) {
    console.error("Erreur lors du chargement de la collectivite :", error)
    renderCollectivitePanel(fallbackCommunityItems)
    setCollectiviteFeedback("Collectivité chargée avec les données de secours.")
  }
}

async function classement() {
  if (!tbody) {
    return
  }

  try {
    const data = await fetchRankingData()
    const ranking = Array.isArray(data?.ranking) ? data.ranking : []

    tbody.innerHTML = ""

    if (ranking.length === 0) {
      tbody.innerHTML = `
        <tr>
          <td colspan="6">Aucune ferme classee.</td>
        </tr>
      `
      return
    }

    ranking.forEach((player, index) => {
      tbody.innerHTML += `
        <tr>
          <td>${index + 1}</td>
          <td>${player.name}</td>
          <td>${player.money}</td>
          <td>${player.poules}</td>
          <td>${player.vaches}</td>
          <td>${player.lapins}</td>
        </tr>
      `
    })
  } catch (error) {
    console.error("Erreur lors du chargement du classement :", error)
    tbody.innerHTML = `
      <tr>
        <td colspan="6">Impossible de charger le classement.</td>
      </tr>
    `
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

async function fetchFarmDataById(farmId) {
  const response = await fetch(`/api/fermes/${farmId}/front-data`)

  if (!response.ok) {
    throw new Error("Impossible de charger la ferme.")
  }

  return response.json()
}

async function fetchAnimalDetail(animalId) {
  if (!animalId) {
    throw new Error("Animal ID invalide.")
  }

  try {
    const data = await fetchActiveFarmData()
    const foundAnimal = Array.isArray(data?.animals)
      ? data.animals.find((item) => String(item.id) === String(animalId))
      : null

    if (foundAnimal) {
      return foundAnimal
    }
  } catch (error) {
    console.warn("Impossible de recharger les donnees de l'animal :", error)
  }

  const fallbackAnimal = currentFarmModel?.animals?.find((item) => String(item.id) === String(animalId))

  if (!fallbackAnimal) {
    throw new Error("Animal introuvable.")
  }

  return fallbackAnimal
}

async function fetchActiveFarmData() {
  if (!currentFarmId) {
    throw new Error("Aucune ferme connectee.")
  }

  return fetchFarmDataById(currentFarmId)
}

async function fetchRankingData() {
  if (currentFarmId) {
    return fetchActiveFarmData()
  }

  const candidateFarmIds = [1, 2]

  for (const farmId of candidateFarmIds) {
    try {
      return await fetchFarmDataById(farmId)
    } catch (error) {
      // On tente une autre ferme existante si celle-ci n'est pas disponible.
    }
  }

  throw new Error("Impossible de charger le classement.")
}

async function publierOffreMarche(product, quantity, unitPrice) {
  if (!currentFarmId) {
    throw new Error("Connecte-toi pour vendre sur le marché.")
  }

  const response = await fetch(
    `/api/fermes/${encodeURIComponent(currentFarmId)}/marche/offres?produit=${encodeURIComponent(product)}&quantite=${encodeURIComponent(quantity)}&prix=${encodeURIComponent(unitPrice)}`,
    { method: "POST" }
  )

  if (!response.ok) {
    throw new Error((await response.text()) || "Impossible de publier l'offre.")
  }

  return response.json()
}

async function acheterOffreMarche(offerId, quantity) {
  if (!currentFarmId) {
    throw new Error("Connecte-toi pour acheter sur le marché.")
  }

  const response = await fetch(
    `/api/fermes/${encodeURIComponent(currentFarmId)}/marche/achat?idOffre=${encodeURIComponent(offerId)}&quantite=${encodeURIComponent(quantity)}`,
    { method: "POST" }
  )

  if (!response.ok) {
    throw new Error((await response.text()) || "Impossible d'acheter cette offre.")
  }

  return response.json()
}

function createFreshUiState(data) {
  const cashValue = Number(data?.cash)

  return TinyFarmState.writeUiState({
    level: 1,
    balance: Number.isFinite(cashValue) ? cashValue : 0,
    purchases: {
      vache: 0,
      poule: 0,
      lapin: 0
    }
  })
}

function setLoginFeedback(message = "", type = "") {
  if (!loginFeedback) {
    return
  }

  loginFeedback.textContent = message
  loginFeedback.classList.remove("is-error", "is-success")

  if (type) {
    loginFeedback.classList.add(type)
  }
}

function openLoginModal() {
  if (!loginModal) {
    showFarmScreen()
    return
  }

  setLoginFeedback()
  loginModal.classList.remove("hidden")
  loginModal.setAttribute("aria-hidden", "false")

  window.setTimeout(() => {
    loginUsernameInput?.focus()
  }, 0)
}

function closeLoginModal() {
  if (!loginModal) {
    return
  }

  loginModal.classList.add("hidden")
  loginModal.setAttribute("aria-hidden", "true")
  setLoginFeedback()

  if (loginForm) {
    loginForm.reset()
  }
}

function showFarmScreen(prefetchedFarmData = null) {
  if (!loginScreen || !farmScreen) {
    return
  }

  closeLoginModal()
  loginScreen.classList.add("hidden")
  farmScreen.classList.remove("hidden")

  if (prefetchedFarmData) {
    applyFarmData(prefetchedFarmData)
    return
  }

  initializeFarmState()
}

function showLoginScreen() {
  if (!loginScreen || !farmScreen) {
    return
  }

  currentFarmModel = null
  currentFarmId = null
  currentUsername = "-"
  latestFarmData = null
  farmClockState = null
  marketSaleState.selectedProduct = ""
  marketSaleState.quantity = 1
  marketSaleState.unitPrice = 1
  loadCareInventoryState()
  renderStockTable(buildStockRows())
  renderMarketPanels()
  loginScreen.classList.remove("hidden")
  farmScreen.classList.add("hidden")
}

if (loginBtn) {
  loginBtn.addEventListener("click", openLoginModal)
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
  button.addEventListener("click", closeAllModals)
})

loginCloseTargets.forEach((button) => {
  button.addEventListener("click", closeLoginModal)
})

async function loginWithCredentials(username, password) {
  if (!username || !password) {
    setLoginFeedback("Renseigne un username et un password.", "is-error")
    return
  }

  setLoginFeedback()

  try {
    const response = await fetch("/api/auth/login-local", {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify({ username, password })
    })

    if (!response.ok) {
      setLoginFeedback("erreur de username ou de password", "is-error")
      return
    }

    const payload = await response.json()
    currentFarmId = payload.farmId
    currentUsername = username

    const farmData = await fetchFarmDataById(currentFarmId)
    setLoginFeedback("Connexion acceptee.", "is-success")
    showFarmScreen(farmData)
  } catch (error) {
    console.error("Erreur de connexion :", error)
    setLoginFeedback("erreur de username ou de password", "is-error")
  }
}

if (loginForm) {
  loginForm.addEventListener("submit", async (event) => {
    event.preventDefault()

    const username = loginUsernameInput?.value.trim() || ""
    const password = loginPasswordInput?.value.trim() || ""
    await loginWithCredentials(username, password)
  })
}

const demoLoginAButton = document.getElementById("demo-login-a")
const demoLoginBButton = document.getElementById("demo-login-b")
const loginShortcutAButton = document.getElementById("login-shortcut-a")
const loginShortcutBButton = document.getElementById("login-shortcut-b")
if (demoLoginAButton) {
  demoLoginAButton.addEventListener("click", () => loginWithCredentials("a", "a1"))
}
if (demoLoginBButton) {
  demoLoginBButton.addEventListener("click", () => loginWithCredentials("b", "b2"))
}
if (loginShortcutAButton) {
  loginShortcutAButton.addEventListener("click", () => loginWithCredentials("a", "a1"))
}
if (loginShortcutBButton) {
  loginShortcutBButton.addEventListener("click", () => loginWithCredentials("b", "b2"))
}

actionButtons.forEach((button) => {
  button.addEventListener("click", async () => {
    const action = button.dataset.action
    const target = activeActionTarget || "la ferme"

    if (!currentFarmId || !currentAnimalAction.animalType) {
      showToast(`${action} : impossible (pas connecte ou animal invalide)`)
      return
    }

    try {
      const actionMap = {
        "Nourrir": "feed",
        "Abreuver": "water",
        "Soigner": "heal",
        "Nettoyer": "clean"
      }

      const apiAction = actionMap[action]
      if (!apiAction) {
        showToast(`${action} : action inconnue`)
        return
      }

      const response = await fetch(
        `/api/fermes/${encodeURIComponent(currentFarmId)}/animaux/${encodeURIComponent(currentAnimalAction.animalType)}/${apiAction}`,
        { method: "POST" }
      )

      if (!response.ok) {
        const error = await response.text()
        showToast(`${action} : ${error || "impossible"}`)
        return
      }

      const farmData = await response.json()
      applyFarmData(farmData)
      showToast(`${action} : ${target} - OK`)
      closeActionModal()
    } catch (error) {
      console.error(`Erreur lors de ${action} :`, error)
      showToast(`${action} : erreur`)
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

document.addEventListener("DOMContentLoaded", () => {
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
      if (!stockPanel.classList.contains("open")) {
        initializeStockPanel()
      }
      setStockPanelOpen(!stockPanel.classList.contains("open"))
    })

    stockPanel.addEventListener("click", (event) => {
      event.stopPropagation()
    })
  }

  if (collectiviteList) {
    collectiviteList.addEventListener("click", async (event) => {
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

      if (selectedItem.id === "farmers-market") {
        await ouvrirPopupMarche()
        return
      }

      if (!currentFarmId) {
        setCollectiviteFeedback("Connecte-toi pour acheter cet objet.")
        return
      }

      if (currentFarmId && CARE_ITEM_TO_API_TYPE[selectedItem.id]) {
        try {
          const response = await fetch(
            `/api/fermes/${currentFarmId}/acheter-objet-entretien?type=${encodeURIComponent(CARE_ITEM_TO_API_TYPE[selectedItem.id])}`,
            { method: "POST" }
          )

          if (!response.ok) {
            const errorText = await response.text()
            setCollectiviteFeedback(errorText || "Achat impossible.")
            return
          }

          const farmData = await response.json()
          applyFarmData(farmData)
          setCollectiviteFeedback(`${selectedItem.label} ajouté au stock.`)
          return
        } catch (error) {
          console.error("Erreur lors de l'achat d'objet :", error)
          setCollectiviteFeedback("Achat impossible.")
          return
        }
      }

      setCollectiviteFeedback(`${selectedItem.label} sélectionné.`)
    })
  }

  if (elementsInterface.boutonFermer) {
    elementsInterface.boutonFermer.addEventListener("click", () => fermerPopup())
  }

  if (boutonFermerMarche) {
    boutonFermerMarche.addEventListener("click", fermerPopupMarche)
  }

  closeMarcheTargets.forEach((target) => {
    target.addEventListener("click", fermerPopupMarche)
  })

  marcheTabButtons.forEach((button) => {
    button.addEventListener("click", () => {
      setMarcheTab(button.dataset.marcheTab)
    })
  })

  if (popupMarche) {
    popupMarche.addEventListener("click", async (event) => {
      const buyButton = event.target.closest("[data-market-buy]")

      if (buyButton) {
        const offerId = buyButton.dataset.marketBuy
        const input = popupMarche.querySelector(`[data-market-buy-qty="${offerId}"]`)
        const quantity = Math.max(1, Number.parseInt(input?.value, 10) || 1)

        try {
          setMarcheFeedback(marcheAchatFeedback)
          const farmData = await acheterOffreMarche(offerId, quantity)
          applyFarmData(farmData)
          renderStockTable(buildStockRows(farmData))
          setStockFeedback()
          afficherMessage("Achat du marché validé.", "succes")
          setMarcheFeedback(marcheAchatFeedback, "Achat effectué avec succès.", "is-success")
        } catch (error) {
          console.error("Erreur lors de l'achat sur le marche :", error)
          setMarcheFeedback(marcheAchatFeedback, error.message || "Achat impossible.", "is-error")
        }

        return
      }
    })
  }

  if (marcheVenteProduit) {
    marcheVenteProduit.addEventListener("change", () => {
      setMarcheFeedback(marcheVenteFeedback)
      updateMarcheVenteSelection()
    })
  }

  if (marcheVenteQuantite) {
    marcheVenteQuantite.addEventListener("input", () => {
      marketSaleState.quantity = Math.max(0, Number.parseInt(marcheVenteQuantite.value, 10) || 0)
      updateMarcheVenteSelection()
    })
  }

  if (marcheVentePrix) {
    marcheVentePrix.addEventListener("input", () => {
      marketSaleState.unitPrice = Math.max(1, Number.parseInt(marcheVentePrix.value, 10) || 1)
      marcheVentePrix.value = String(marketSaleState.unitPrice)
    })
  }

  if (marcheVenteSubmit) {
    marcheVenteSubmit.addEventListener("click", async () => {
      if (marcheVenteSubmit.disabled) {
        return
      }

      const product = marcheVenteProduit?.value || ""
      const quantity = Math.max(1, Number.parseInt(marcheVenteQuantite?.value, 10) || 1)
      const unitPrice = Math.max(1, Number.parseInt(marcheVentePrix?.value, 10) || 1)
      marketSaleState.selectedProduct = product
      marketSaleState.quantity = quantity
      marketSaleState.unitPrice = unitPrice

      try {
        marcheVenteSubmit.disabled = true
        setMarcheFeedback(marcheVenteFeedback)
        const farmData = await publierOffreMarche(product, quantity, unitPrice)
        applyFarmData(farmData)
        renderStockTable(buildStockRows(farmData))
        setStockFeedback()
        afficherMessage("Offre du marche publiee.", "succes")
        setMarcheFeedback(marcheVenteFeedback, "Offre publiee avec succes.", "is-success")
      } catch (error) {
        console.error("Erreur lors de la mise en vente :", error)
        setMarcheFeedback(marcheVenteFeedback, error.message || "Mise en vente impossible.", "is-error")
      } finally {
        updateMarcheVenteSelection()
      }
    })
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

  if (poulaillerContainer) {
    poulaillerContainer.addEventListener("click", (event) => {
      // Only open list modal if clicking on the background (not on an animal icon)
      if (event.target === poulaillerContainer || event.target.classList.contains("animals-empty")) {
        openPoulaillerListModal()
      }
    })
  }

  document.addEventListener("click", () => {
    setStockPanelOpen(false)
  })

  document.addEventListener("keydown", (event) => {
    if (event.key === "Escape") {
      closeLoginModal()
      setStockPanelOpen(false)
      closeAllModals()
    }
  })

  initializeAnimalZoneNavigation()
  loadCareInventoryState()
  initializeFarmState()
  initializeStockPanel()
  initializeCollectivitePanel()
  updateClock()
  window.setInterval(updateClock, 1000)

  if (farmRefreshIntervalId) {
    window.clearInterval(farmRefreshIntervalId)
  }

  farmRefreshIntervalId = window.setInterval(refreshFarmTimeAndProduction, 10000)
})
