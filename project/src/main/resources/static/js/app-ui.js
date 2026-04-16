/*
 * Couche de rendu du front TinyFarm.
 * Ce fichier regroupe le rendu de l'interface, les popups et les helpers
 * visuels afin que le point d'entree reste centre sur les interactions.
 */
(function attachTinyFarmUi(global) {
  const shell = global.TinyFarmShell
  const api = global.TinyFarmApi
  const { dom, state, constants, helpers } = shell

  function getCurrentProduct() {
    return state.stockState.products.find((product) => product.id === state.stockState.selectedProductId) || null
  }

  function renderCareInventory() {
    constants.CARE_ITEM_IDS.forEach((itemId) => {
      const counter = dom.careInventoryCounters[itemId]

      if (counter) {
        counter.textContent = String(state.careInventoryState[itemId] || 0)
      }
    })
  }

  function applyCareInventory(data) {
    const inventory = data?.careInventory

    if (!inventory || typeof inventory !== "object") {
      state.careInventoryState = helpers.createDefaultCareInventory()
      renderCareInventory()
      return
    }

    state.careInventoryState = constants.CARE_ITEM_IDS.reduce((normalizedInventory, itemId) => {
      normalizedInventory[itemId] = Math.max(0, Number.parseInt(inventory[itemId], 10) || 0)
      return normalizedInventory
    }, {})

    renderCareInventory()
  }

  function loadCareInventoryState() {
    state.careInventoryState = helpers.createDefaultCareInventory()
    renderCareInventory()
  }

  function renderCommunityPurchaseCounter(data = null) {
    if (!dom.collectiviteCounter) {
      return
    }

    const parsedRemaining = Number.parseInt(data?.communityPurchases?.remaining, 10)
    const parsedTotal = Number.parseInt(data?.communityPurchases?.maxPerDay ?? data?.communityPurchases?.total, 10)
    const remaining = Math.max(0, Number.isFinite(parsedRemaining) ? parsedRemaining : 12)
    const total = Math.max(1, Number.isFinite(parsedTotal) ? parsedTotal : 12)
    dom.collectiviteCounter.textContent = `${remaining}/${total}`
  }

  function renderCommunityFromData(data) {
    // Le backend pilote en priorite la collectivite ; on garde un fallback
    // local uniquement pour eviter un panneau vide si l'API est absente.
    const items = Array.isArray(data?.communityItems) && data.communityItems.length > 0
      ? data.communityItems
      : constants.fallbackCommunityItems

    renderCollectivitePanel(items)
    renderCommunityPurchaseCounter(data)
  }

  function createAnimatedSprite(typeKey) {
    const config = helpers.getAnimatedAnimalConfig(typeKey)

    if (!config) {
      return null
    }

    const sprite = document.createElement("span")
    const duration = helpers.randomBetween(config.minDuration, config.maxDuration)

    sprite.className = `animal-sprite ${config.spriteClass}`
    sprite.style.setProperty("--animal-float-duration", `${duration.toFixed(2)}s`)

    if (config.allowMirror && Math.random() >= 0.5) {
      sprite.classList.add("is-mirrored")
    }

    return sprite
  }

  function createAnimalVisual(animal) {
    const icon = document.createElement("div")
    const config = helpers.getAnimatedAnimalConfig(animal.typeKey)

    icon.className = `animal-icon ${config?.sizeClass || ""}`.trim()
    icon.dataset.animalId = animal.id
    icon.dataset.animalType = animal.type
    icon.dataset.animalName = animal.name
    icon.setAttribute("role", "button")
    icon.setAttribute("tabindex", "0")
    icon.setAttribute("aria-label", `Ouvrir la fiche de ${animal.name}`)

    const sprite = createAnimatedSprite(animal.typeKey)
    if (sprite) {
      icon.appendChild(sprite)
    }

    return icon
  }

  function setStockFeedback(message = "", type = "") {
    if (!dom.stockFeedback) {
      return
    }

    dom.stockFeedback.textContent = message
    dom.stockFeedback.classList.remove("is-error", "is-success")

    if (type) {
      dom.stockFeedback.classList.add(type)
    }
  }

  function buildStockRows(data = null) {
    const stockInventory = Array.isArray(data?.stockInventory) ? data.stockInventory : []

    return stockInventory.map((row, index) => ({
      id: `stock-${index}`,
      label: row.label || "Produit",
      quantity: Math.max(0, Number.parseInt(row.quantity, 10) || 0)
    }))
  }

  function getMarketSellRows(data = null) {
    return Array.isArray(data?.stockInventory)
      ? data.stockInventory.filter((row) => ["Oeufs", "Lait", "Lapins"].includes(row.label))
      : []
  }

  function getCommunityBuybackPrice(product) {
    return constants.COMMUNITY_BUYBACK_PRICES[String(product || "").trim().toUpperCase()] || 0
  }

  function getActionCostLabel(animalType, actionLabel) {
    const normalizedType = String(animalType || "").toLowerCase()
    const cost = constants.ANIMAL_ACTION_COSTS?.[normalizedType]?.[actionLabel]
    return Number.isFinite(cost) ? `${actionLabel} (${cost} ecus)` : actionLabel
  }

  function normalizeMarketProduct(label) {
    const normalized = String(label || "").trim().toLowerCase()

    if (["oeuf", "oeufs", "egg", "eggs"].includes(normalized)) {
      return "OEUF"
    }

    if (["lait", "milk"].includes(normalized)) {
      return "LAIT"
    }

    if (["lapin", "lapins", "rabbit", "rabbits"].includes(normalized)) {
      return "LAPIN"
    }

    return ""
  }

  function formatMarketProductLabel(product) {
    const normalized = String(product || "").trim().toUpperCase()

    switch (normalized) {
      case "OEUF":
        return "Oeufs"
      case "LAIT":
        return "Lait"
      case "LAPIN":
        return "Lapins"
      default:
        return product || "-"
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
    if (!dom.marcheAchatBody) {
      return
    }

    // L'onglet achat est entierement regenere a partir des offres courantes.
    const offers = Array.isArray(data?.marketOffers) ? data.marketOffers : []
    dom.marcheAchatBody.innerHTML = ""

    if (offers.length === 0) {
      dom.marcheAchatBody.innerHTML = `
        <tr>
          <td colspan="6">Aucune offre disponible.</td>
        </tr>
      `
      return
    }

    offers.forEach((offer) => {
      const row = document.createElement("tr")
      row.innerHTML = `
        <td>${offer.sellerName || "Ferme"}</td>
        <td>${formatMarketProductLabel(offer.product)}</td>
        <td>${Math.max(0, Number.parseInt(offer.quantity, 10) || 0)}</td>
        <td>${Math.max(1, Number.parseInt(offer.unitPrice, 10) || 1)}</td>
        <td>
          <input
            type="number"
            min="1"
            max="${Math.max(1, Number.parseInt(offer.quantity, 10) || 1)}"
            value="1"
            data-market-buy-qty="${offer.id}"
          >
        </td>
        <td>
          <button type="button" data-market-buy="${offer.id}">Acheter</button>
        </td>
      `
      dom.marcheAchatBody.appendChild(row)
    })
  }

  function updateMarcheVenteSelection() {
    if (!dom.marcheVenteProduit || !dom.marcheVenteQuantite || !dom.marcheVentePrix || !dom.marcheVenteStock || !dom.marcheVenteSubmit) {
      return
    }

    // On borne toujours la quantite vendue sur le stock reel recu du backend.
    const selectedProduct = dom.marcheVenteProduit.value || ""
    const sellRows = getMarketSellRows(state.latestFarmData)
    const selectedRow = sellRows.find((row) => normalizeMarketProduct(row.label) === selectedProduct) || null
    const stockQuantity = Math.max(0, Number.parseInt(selectedRow?.quantity, 10) || 0)

    state.marketSaleState.selectedProduct = selectedProduct
    state.marketSaleState.quantity = Math.min(
      Math.max(1, Number.parseInt(dom.marcheVenteQuantite.value, 10) || 1),
      Math.max(1, stockQuantity)
    )
    state.marketSaleState.unitPrice = Math.max(1, Number.parseInt(dom.marcheVentePrix.value, 10) || 1)

    dom.marcheVenteStock.textContent = stockQuantity > 0 ? `${stockQuantity} disponible(s)` : "0 disponible"
    dom.marcheVenteQuantite.max = String(Math.max(1, stockQuantity))
    dom.marcheVenteQuantite.value = String(Math.min(state.marketSaleState.quantity, Math.max(1, stockQuantity)))
    dom.marcheVentePrix.value = String(state.marketSaleState.unitPrice)
    dom.marcheVenteSubmit.disabled = !selectedProduct || stockQuantity <= 0
  }

  function updateMarcheCollectiviteSelection() {
    if (!dom.marcheCollectiviteProduit
      || !dom.marcheCollectiviteQuantite
      || !dom.marcheCollectiviteStock
      || !dom.marcheCollectivitePrix
      || !dom.marcheCollectiviteSubmit) {
      return
    }

    // La collectivite reprend le meme stock vendable que le marche joueur,
    // mais avec un prix fixe defini par le projet.
    const selectedProduct = dom.marcheCollectiviteProduit.value || ""
    const sellRows = getMarketSellRows(state.latestFarmData)
    const selectedRow = sellRows.find((row) => normalizeMarketProduct(row.label) === selectedProduct) || null
    const stockQuantity = Math.max(0, Number.parseInt(selectedRow?.quantity, 10) || 0)
    const fixedPrice = getCommunityBuybackPrice(selectedProduct)

    state.communitySaleState.selectedProduct = selectedProduct
    state.communitySaleState.quantity = Math.min(
      Math.max(1, Number.parseInt(dom.marcheCollectiviteQuantite.value, 10) || 1),
      Math.max(1, stockQuantity)
    )

    dom.marcheCollectiviteStock.textContent = stockQuantity > 0 ? `${stockQuantity} disponible(s)` : "0 disponible"
    dom.marcheCollectivitePrix.textContent = fixedPrice > 0 ? `${fixedPrice} ecus / unite` : "-"
    dom.marcheCollectiviteQuantite.max = String(Math.max(1, stockQuantity))
    dom.marcheCollectiviteQuantite.value = String(Math.min(state.communitySaleState.quantity, Math.max(1, stockQuantity)))
    dom.marcheCollectiviteSubmit.disabled = !selectedProduct || stockQuantity <= 0 || fixedPrice <= 0
  }

  function renderMarcheCollectiviteGrid(data = null) {
    if (!dom.marcheCollectiviteGrid) {
      return
    }

    const sellRows = getMarketSellRows(data)
    const products = [
      { id: "OEUF", label: "Oeufs", visual: "OEUFS" },
      { id: "LAIT", label: "Lait", visual: "LAIT" },
      { id: "LAPIN", label: "Lapin", visual: "LAPIN" }
    ]

    dom.marcheCollectiviteGrid.innerHTML = ""

    products.forEach((product) => {
      const stockRow = sellRows.find((row) => normalizeMarketProduct(row.label) === product.id)
      const stockQuantity = Math.max(0, Number.parseInt(stockRow?.quantity, 10) || 0)
      const price = getCommunityBuybackPrice(product.id)
      const card = document.createElement("article")
      card.className = "carte-animal carte-animal--collectivite"

      card.innerHTML = `
        <h3>${product.label}</h3>
        <div class="zone-image zone-image--collectivite">
          <span class="market-product-badge market-product-badge--${product.id.toLowerCase()}">${product.visual}</span>
        </div>
        <p class="prix">${price} <span class="piece" aria-hidden="true"></span></p>
        <p class="popup-marche-stock-chip">${stockQuantity} disponible(s)</p>
        <label class="popup-marche-form-label" for="black-market-qty-${product.id}">Quantite</label>
        <input
          id="black-market-qty-${product.id}"
          class="popup-marche-number"
          type="number"
          min="1"
          max="${Math.max(1, stockQuantity)}"
          value="${stockQuantity > 0 ? 1 : 0}"
          data-community-sell-qty="${product.id}"
          ${stockQuantity <= 0 ? "disabled" : ""}
        >
        <button class="btn-vendre btn-acheter--collectivite" type="button" data-community-sell="${product.id}" ${stockQuantity <= 0 ? "disabled" : ""}>Vendre</button>
      `

      dom.marcheCollectiviteGrid.appendChild(card)
    })
  }

  function renderMarcheVenteForm(data = null) {
    if (!dom.marcheVenteProduit) {
      return
    }

    const sellRows = getMarketSellRows(data)
    const options = ['<option value="">Choisir un produit</option>']

    sellRows.forEach((row) => {
      const product = normalizeMarketProduct(row.label)
      options.push(`<option value="${product}">${row.label}</option>`)
    })

    dom.marcheVenteProduit.innerHTML = options.join("")

    if (sellRows.some((row) => normalizeMarketProduct(row.label) === state.marketSaleState.selectedProduct)) {
      dom.marcheVenteProduit.value = state.marketSaleState.selectedProduct
    }

    if (dom.marcheVenteQuantite) {
      dom.marcheVenteQuantite.value = String(state.marketSaleState.quantity)
    }

    if (dom.marcheVentePrix) {
      dom.marcheVentePrix.value = String(state.marketSaleState.unitPrice)
    }

    updateMarcheVenteSelection()
  }

  function renderMarcheCollectiviteForm(data = null) {
    if (!dom.marcheCollectiviteProduit) {
      return
    }

    // Le formulaire du marche noir reste volontairement simple :
    // choix du produit, quantite, puis prix fixe en lecture seule.
    const sellRows = getMarketSellRows(data)
    const options = ['<option value="">Choisir un produit</option>']

    sellRows.forEach((row) => {
      const product = normalizeMarketProduct(row.label)
      options.push(`<option value="${product}">${row.label}</option>`)
    })

    dom.marcheCollectiviteProduit.innerHTML = options.join("")

    if (sellRows.some((row) => normalizeMarketProduct(row.label) === state.communitySaleState.selectedProduct)) {
      dom.marcheCollectiviteProduit.value = state.communitySaleState.selectedProduct
    }

    if (dom.marcheCollectiviteQuantite) {
      dom.marcheCollectiviteQuantite.value = String(state.communitySaleState.quantity)
    }

    updateMarcheCollectiviteSelection()
  }

  function renderMarketPanels(data = null) {
    renderMarcheAchatRows(data)
    renderMarcheVenteForm(data)
    renderMarcheCollectiviteForm(data)
    renderMarcheCollectiviteGrid(data)

    const blackMarketTab = document.getElementById("marche-tab-collectivite")
    if (blackMarketTab) {
      blackMarketTab.textContent = "Marche noir"
    }

    const blackMarketText = document.querySelector('[data-marche-panel="collectivite"] .popup-marche-texte')
    if (blackMarketText) {
      blackMarketText.textContent = "Vends directement au marche noir au prix fixe."
    }

    if (dom.marcheCollectiviteSubmit) {
      dom.marcheCollectiviteSubmit.textContent = "Vendre au marche noir"
    }

    const blackMarketForm = document.querySelector('[data-marche-panel="collectivite"] .popup-marche-vente-form')
    if (blackMarketForm) {
      blackMarketForm.hidden = true
    }

    document.querySelectorAll('[data-marche-panel="collectivite"] [data-community-sell]').forEach((button) => {
      button.textContent = "Vendre"
    })
  }

  function renderStockTable(rows) {
    if (!dom.stockTableBody) {
      return
    }

    const safeRows = Array.isArray(rows) ? rows : []
    dom.stockTableBody.innerHTML = ""

    if (safeRows.length === 0) {
      dom.stockTableBody.innerHTML = `
        <tr>
          <td colspan="2">Aucun stock disponible.</td>
        </tr>
      `
      if (dom.stockTotalUnits) {
        dom.stockTotalUnits.textContent = "0"
      }
      return
    }

    const totalUnits = safeRows.reduce((sum, row) => sum + row.quantity, 0)

    safeRows.forEach((row) => {
      const tr = document.createElement("tr")
      tr.innerHTML = `
        <td>${row.label}</td>
        <td>${row.quantity}</td>
      `
      dom.stockTableBody.appendChild(tr)
    })

    if (dom.stockTotalUnits) {
      dom.stockTotalUnits.textContent = String(totalUnits)
    }
  }

  function setCollectiviteFeedback(message = "") {
    if (!dom.collectiviteFeedback) {
      return
    }

    dom.collectiviteFeedback.textContent = message

    if (state.collectiviteFeedbackTimeout) {
      window.clearTimeout(state.collectiviteFeedbackTimeout)
    }

    if (!message) {
      state.collectiviteFeedbackTimeout = null
      return
    }

    state.collectiviteFeedbackTimeout = window.setTimeout(() => {
      dom.collectiviteFeedback.textContent = ""
      state.collectiviteFeedbackTimeout = null
    }, 2200)
  }

  function setStockPanelOpen(isOpen) {
    if (!dom.stockToggle || !dom.stockPanel) {
      return
    }

    dom.stockPanel.classList.toggle("open", isOpen)
    dom.stockToggle.setAttribute("aria-expanded", String(isOpen))
  }

  function renderCollectivitePrice(price) {
    if (!Number.isFinite(price)) {
      return ""
    }

    return `
      <span class="collectivite-item-price">
        <span>${price}</span>
        <span class="collectivite-item-price-coin" aria-hidden="true"></span>
      </span>
    `
  }

  function renderCollectivitePanel(items) {
    if (!dom.collectiviteList) {
      return
    }

    state.collectiviteState.items = Array.isArray(items) ? items : []
    dom.collectiviteList.innerHTML = ""

    state.collectiviteState.items.forEach((item) => {
      const button = document.createElement("button")
      button.type = "button"
      button.className = [
        "collectivite-item",
        item.variant === "shortcut" ? "collectivite-item-shortcut" : "",
        `collectivite-item--${item.id}`
      ].filter(Boolean).join(" ")
      button.dataset.collectiviteId = item.id
      button.innerHTML = `
        <span class="collectivite-item-icon" aria-hidden="true"></span>
        <span class="collectivite-item-label">${item.label}</span>
        ${renderCollectivitePrice(item.price)}
      `
      dom.collectiviteList.appendChild(button)
    })
  }

  function createAnimalIcon(animal, { groupOnly = false } = {}) {
    const icon = createAnimalVisual(animal)

    icon.addEventListener("click", () => {
      if (groupOnly) {
        openClapierModal()
        return
      }

      openAnimalModal(animal)
    })

    icon.addEventListener("keydown", (event) => {
      if (event.key === "Enter" || event.key === " ") {
        event.preventDefault()

        if (groupOnly) {
          openClapierModal()
          return
        }

        openAnimalModal(animal)
      }
    })

    return icon
  }

  function renderEmptyZone(container, message) {
    const emptyState = document.createElement("p")
    emptyState.className = "animals-empty"
    emptyState.textContent = message
    container.appendChild(emptyState)
  }

  function hasHorizontalOverflow(container) {
    return container.scrollWidth - container.clientWidth > 1
  }

  function getAnimalZoneMaxScroll(container) {
    return Math.max(0, container.scrollWidth - container.clientWidth)
  }

  function getAnimalZoneStep(container) {
    return Math.max(container.clientWidth * 0.72, 160)
  }

  function updateAnimalZoneNavigation(zoneController) {
    const { shell: zoneShell, container, previousButton, nextButton } = zoneController
    const maxScrollLeft = getAnimalZoneMaxScroll(container)
    const hasOverflow = hasHorizontalOverflow(container)

    zoneShell.classList.toggle("has-overflow", hasOverflow)
    previousButton.disabled = !hasOverflow || container.scrollLeft <= 1
    nextButton.disabled = !hasOverflow || container.scrollLeft >= maxScrollLeft - 1
  }

  function scheduleAnimalZoneNavigationRefresh() {
    if (state.animalZoneRefreshFrame !== null) {
      return
    }

    state.animalZoneRefreshFrame = window.requestAnimationFrame(() => {
      state.animalZoneRefreshFrame = null
      dom.animalZoneControllers.forEach(updateAnimalZoneNavigation)
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

    const nextScrollLeft = helpers.clamp(
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
    dom.animalZoneControllers.forEach((zoneController) => {
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

    if (!state.animalZoneResizeBound) {
      window.addEventListener("resize", scheduleAnimalZoneNavigationRefresh)
      state.animalZoneResizeBound = true
    }

    scheduleAnimalZoneNavigationRefresh()
  }

  function renderAnimalZones() {
    if (!state.currentFarmModel) {
      return
    }

    // Chaque zone est rerendue a partir du modele courant pour eviter
    // les desynchronisations entre les modales, les compteurs et la scene.
    const cows = TinyFarmState.getAnimalsByType(state.currentFarmModel, "vache")
    const chickens = TinyFarmState.getAnimalsByType(state.currentFarmModel, "poule")
    const rabbits = TinyFarmState.getAnimalsByType(state.currentFarmModel, "lapin")

    if (dom.paturageContainer) {
      dom.paturageContainer.innerHTML = ""
      if (cows.length === 0) {
        renderEmptyZone(dom.paturageContainer, "Aucune vache")
      } else {
        cows.forEach((animal) => dom.paturageContainer.appendChild(createAnimalIcon(animal)))
      }
    }

    if (dom.poulaillerContainer) {
      dom.poulaillerContainer.innerHTML = ""
      if (chickens.length === 0) {
        renderEmptyZone(dom.poulaillerContainer, "Aucune poule")
      } else {
        chickens.forEach((animal) => dom.poulaillerContainer.appendChild(createAnimalIcon(animal)))
      }
    }

    if (dom.clapierContainer) {
      dom.clapierContainer.innerHTML = ""
      dom.clapierContainer.dataset.groupTooltip = `${rabbits.length} lapin${rabbits.length > 1 ? "s" : ""} - actions de groupe`
      dom.clapierContainer.classList.toggle("is-empty", rabbits.length === 0)

      if (rabbits.length === 0) {
        renderEmptyZone(dom.clapierContainer, "Aucun lapin")
      } else {
        rabbits.forEach((animal) => dom.clapierContainer.appendChild(createAnimalIcon(animal, { groupOnly: true })))
      }
    }

    scheduleAnimalZoneNavigationRefresh()
  }

  function mettreAJourInterface() {
    if (!state.currentFarmModel) {
      return
    }

    if (dom.elementsInterface.solde) {
      dom.elementsInterface.solde.textContent = String(state.currentFarmModel.balance)
    }

    if (dom.elementsInterface.ownerName) {
      dom.elementsInterface.ownerName.textContent = state.currentUsername || "-"
    }

    if (dom.elementsInterface.ownerBalance) {
      dom.elementsInterface.ownerBalance.textContent = String(state.currentFarmModel.balance)
    }

    Object.entries(state.currentFarmModel.counts).forEach(([typeKey, count]) => {
      if (dom.elementsInterface.compteurs[typeKey]) {
        dom.elementsInterface.compteurs[typeKey].textContent =
          typeKey === "vache" || typeKey === "poule"
            ? `${count}/${count}`
            : String(count)
      }
    })
  }

  function applyGameClock(data) {
    const gameTime = data?.gameTime

    if (!gameTime || typeof gameTime !== "object") {
      state.farmClockState = null
      return
    }

    state.farmClockState = {
      day: Math.max(1, Number.parseInt(gameTime.day, 10) || 1),
      hours: Math.max(0, Number.parseInt(gameTime.hours, 10) || 0),
      minutes: Math.max(0, Number.parseInt(gameTime.minutes, 10) || 0),
      seconds: Math.max(0, Number.parseInt(gameTime.seconds, 10) || 0),
      realSecondsPerDay: Math.max(1, Number.parseInt(gameTime.realSecondsPerDay, 10) || 60),
      syncedAtMs: Date.now()
    }
  }

  function renderFarmDataStatus(data) {
    void data
  }

  function applyFarmData(data) {
    // Cette fonction est le point d'entree principal des reponses backend :
    // on y rehydrate tous les sous-panneaux de l'interface.
    state.latestFarmData = data
    applyGameClock(data)
    const uiState = createFreshUiState(data)
    state.currentFarmModel = TinyFarmState.buildFarmModel(data, uiState)
    applyCareInventory(data)
    renderCommunityFromData(data)
    renderMarketPanels(data)
    renderAnimalZones()
    mettreAJourInterface()
    renderFarmDataStatus(data)
    mettreAJourDisponibiliteBoutons()
    updateClock()
  }

  function afficherMessage(text, type = "") {
    if (!dom.elementsInterface.message) {
      return
    }

    dom.elementsInterface.message.textContent = text
    dom.elementsInterface.message.classList.remove("erreur", "succes")

    if (type) {
      dom.elementsInterface.message.classList.add(type)
    }
  }

  function estAchatAutorise(typeAnimal) {
    if (!state.currentFarmModel) {
      return false
    }

    const catalogEntry = TinyFarmState.ANIMAL_CATALOG[typeAnimal]

    if (!catalogEntry) {
      return false
    }

    if (typeAnimal === "vache") {
      return state.currentFarmModel.counts.vache === 0
    }

    return state.currentFarmModel.uiState.level >= catalogEntry.minLevel
  }

  function mettreAJourDisponibiliteBoutons() {
    const boutonsAchat = document.querySelectorAll(".btn-acheter")

    boutonsAchat.forEach((bouton) => {
      const typeAnimal = bouton.dataset.animal
      const autorise = estAchatAutorise(typeAnimal)
      const alreadyOwnsCow = state.currentFarmModel && state.currentFarmModel.counts.vache > 0

      bouton.disabled = !autorise

      if (typeAnimal === "vache") {
        bouton.textContent = autorise ? "Acheter" : "Deja possedee"
        bouton.title = autorise
          ? "Acheter une vache si la ferme n'en possede aucune."
          : "La ferme possede deja une vache."
        return
      }

      bouton.textContent = "Acheter"
      bouton.title = ""
    })
  }

  function updateFarmOverlayState(uiState) {
    const normalizedState = TinyFarmState.writeUiState(uiState)
    state.currentFarmModel = TinyFarmState.buildFarmModel(state.currentFarmModel.rawData, normalizedState)
    renderAnimalZones()
    mettreAJourInterface()
    mettreAJourDisponibiliteBoutons()
  }

  function setAnimalShopOpen(isOpen, { announce = true } = {}) {
    if (!dom.elementsInterface.popup || !dom.farmScreen) {
      return
    }

    dom.elementsInterface.popup.classList.toggle("hidden", !isOpen)
    dom.farmScreen.classList.toggle("popup-ouverte", isOpen)

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
    if (!dom.elementsInterface.popup || dom.elementsInterface.popup.classList.contains("hidden")) {
      return
    }

    setAnimalShopOpen(false, { announce })
  }

  function setMarcheTab(activeTab) {
    dom.marcheTabButtons.forEach((button) => {
      const isActive = button.dataset.marcheTab === activeTab
      button.classList.toggle("is-active", isActive)
      button.setAttribute("aria-selected", String(isActive))
    })

    dom.marchePanels.forEach((panel) => {
      panel.classList.toggle("is-active", panel.dataset.marchePanel === activeTab)
    })
  }

  async function refreshMarketData() {
    if (!state.currentFarmId) {
      renderMarketPanels()
      setMarcheFeedback(dom.marcheAchatFeedback, "Connecte-toi pour acceder au marche.", "is-error")
      setMarcheFeedback(dom.marcheVenteFeedback, "Connecte-toi pour acceder au marche.", "is-error")
      setMarcheFeedback(dom.marcheCollectiviteFeedback, "Connecte-toi pour acceder au marche noir.", "is-error")
      return
    }

    try {
      const data = await api.fetchActiveFarmData()
      applyFarmData(data)
      renderStockTable(buildStockRows(data))
      setStockFeedback()
      setMarcheFeedback(dom.marcheAchatFeedback)
      setMarcheFeedback(dom.marcheVenteFeedback)
      setMarcheFeedback(dom.marcheCollectiviteFeedback)
    } catch (error) {
      console.error("Erreur lors du chargement du marche :", error)
      renderMarketPanels(state.latestFarmData)
      setMarcheFeedback(dom.marcheAchatFeedback, "Impossible de charger le marche.", "is-error")
      setMarcheFeedback(dom.marcheVenteFeedback, "Impossible de charger le marche.", "is-error")
      setMarcheFeedback(dom.marcheCollectiviteFeedback, "Impossible de charger le marche noir.", "is-error")
    }
  }

  async function ouvrirPopupMarche(activeTab = "achat") {
    if (!dom.popupMarche || !dom.farmScreen) {
      return
    }

    fermerPopup({ announce: false })
    setMarcheTab(activeTab)
    dom.popupMarche.classList.remove("hidden")
    dom.popupMarche.setAttribute("aria-hidden", "false")
    dom.farmScreen.classList.add("popup-ouverte")
    await refreshMarketData()
  }

  function fermerPopupMarche() {
    if (!dom.popupMarche || dom.popupMarche.classList.contains("hidden")) {
      return
    }

    dom.popupMarche.classList.add("hidden")
    dom.popupMarche.setAttribute("aria-hidden", "true")

    if (!dom.elementsInterface.popup || dom.elementsInterface.popup.classList.contains("hidden")) {
      dom.farmScreen.classList.remove("popup-ouverte")
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
    sexValue,
    roleValue,
    weightLabel,
    weightValue,
    targetLabel
  }) {
    if (!dom.modal) {
      return
    }

    dom.modalName.textContent = name
    dom.modalType.textContent = typeLabel
    dom.modalPlace.textContent = homeLabel
    dom.modalStatus.textContent = statusValue || "-"
    dom.modalHealth.textContent = healthValue || "-"
    dom.modalHunger.textContent = hungerValue || "-"
    dom.modalHydration.textContent = hydrationValue || "-"
    if (dom.modalSex) {
      dom.modalSex.textContent = sexValue || "-"
    }
    if (dom.modalRole) {
      dom.modalRole.textContent = roleValue || "-"
    }
    dom.modalWeightLabel.textContent = weightLabel
    dom.modalWeight.textContent = weightValue
    dom.actionButtons.forEach((button) => {
      button.textContent = getActionCostLabel(state.currentAnimalAction.animalType, button.dataset.action)
    })
    state.activeActionTarget = targetLabel
    dom.modal.classList.remove("hidden")
    dom.modal.setAttribute("aria-hidden", "false")
  }

  function deriveAnimalHealthState(animal) {
    return animal?.isSick ? "Malade" : "Saine"
  }

  function deriveCleanlinessState(animal) {
    return Number(animal?.cleanliness) < 60 ? "Sale" : "Propre"
  }

  function isCattleOrChicken(type) {
    return ["vache", "poule"].includes(String(type || "").toLowerCase())
  }

  function deriveHungerState(animalDetails, animalStats) {
    if (animalStats && Number.isFinite(animalStats.total) && animalStats.total > 0) {
      return animalStats.hungry > 0 ? "Affame" : "Rassasie"
    }

    return Number(animalDetails.hunger) < 60 ? "Affame" : "Rassasie"
  }

  function deriveHydrationState(animalDetails, animalStats) {
    if (animalStats && Number.isFinite(animalStats.total) && animalStats.total > 0) {
      return animalStats.thirsty > 0 ? "Assoiffe" : "Hydrate"
    }

    return Number(animalDetails.hydration) < 60 ? "Assoiffe" : "Hydrate"
  }

  function getCareTargetLabel(type, animalName = "") {
    switch (String(type || "").toLowerCase()) {
      case "poule":
        return animalName || "la poule"
      case "vache":
        return animalName || "la vache"
      case "lapin":
        return "tout le clapier"
      default:
        return "la ferme"
    }
  }

  async function openAnimalModal(animal) {
    if (!animal || !animal.id) {
      return
    }

    // On tente d'afficher les donnees les plus recentes de l'animal,
    // meme si l'utilisateur a ouvert la modale depuis une scene deja rendue.
    let animalDetails = animal
    let animalStats = null

    try {
      animalDetails = await api.fetchAnimalDetail(animal.id)
    } catch (error) {
      console.warn("Impossible de charger les donnees detaillees de l'animal :", error)
    }

    if (state.currentFarmId && animal.type) {
      try {
        animalStats = await api.fetchAnimalTypeStatus(animal.type)
      } catch (error) {
        console.warn("Impossible de charger les stats du type d'animal :", error)
      }
    }

    const hungerValue = isCattleOrChicken(animal.type)
      ? deriveHungerState(animalDetails, null)
      : animalStats
      ? `${animalStats.hungry}/${animalStats.total} affames`
      : `${animalDetails.hunger ?? 100}%`

    const thirstValue = isCattleOrChicken(animal.type)
      ? deriveHydrationState(animalDetails, null)
      : animalStats
      ? `${animalStats.thirsty}/${animalStats.total} assoiffes`
      : `${animalDetails.hydration ?? 100}%`

    openActionModal({
      name: animalDetails.name || animal.name,
      typeLabel: animalDetails.typeLabel || animal.type,
      homeLabel: animalDetails.homeLabel || animal.homeLabel || "Ferme",
      statusValue: deriveCleanlinessState(animalDetails),
      healthValue: deriveAnimalHealthState(animalDetails),
      hungerValue,
      hydrationValue: thirstValue,
      sexValue: animalDetails.sex || "-",
      roleValue: animalDetails.role || "-",
      weightLabel: "Poids :",
      weightValue: helpers.formatWeight(animalDetails.weight),
      targetLabel: getCareTargetLabel(animal.type, animalDetails.name || animal.name)
    })

    state.currentAnimalAction.animalType = animal.type
    state.currentAnimalAction.farmId = state.currentFarmId
    state.currentAnimalAction.animalName = animalDetails.name || animal.name
    state.currentAnimalAction.animalId = Number.parseInt(animalDetails.idAnimal, 10) || null
    dom.actionButtons.forEach((button) => {
      button.textContent = getActionCostLabel(animal.type, button.dataset.action)
    })
  }

  async function openClapierModal() {
    if (!state.currentFarmModel || !state.currentFarmId) {
      return
    }

    // Le clapier se comporte comme une cible de groupe : on agrege donc
    // les informations des lapins avant d'afficher les actions.
    const rabbits = TinyFarmState.getAnimalsByType(state.currentFarmModel, "lapin")

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
      rabbitHealth = await api.fetchClapierStatus()
    } catch (error) {
      console.warn("Impossible de charger les donnees du clapier :", error)
    }

    const dirtyRabbits = rabbits.filter((rabbit) => Number(rabbit.cleanliness) < 60).length
    const rabbitStatusValue = dirtyRabbits === 0
      ? "Propres"
      : `${dirtyRabbits}/${rabbitHealth.totalLapins} sont sales`
    const rabbitHealthValue = rabbitHealth.sickLapins === 0 ? "Sains" : "Malades"
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
      sexValue: "-",
      roleValue: "-",
      weightLabel: "Effectif :",
      weightValue: `${rabbitHealth.totalLapins} lapin${(rabbitHealth.totalLapins || 0) > 1 ? "s" : ""}`,
      targetLabel: "tout le clapier"
    })

    state.currentAnimalAction.animalType = "lapin"
    state.currentAnimalAction.farmId = state.currentFarmId
    state.currentAnimalAction.animalName = "clapier"
    state.currentAnimalAction.animalId = null
    dom.actionButtons.forEach((button) => {
      button.textContent = getActionCostLabel("lapin", button.dataset.action)
    })
  }

  async function openPoulaillerListModal() {
    if (!state.currentFarmModel || !state.currentFarmId || !dom.poulaillerListBody) {
      return
    }

    const chickens = TinyFarmState.getAnimalsByType(state.currentFarmModel, "poule")

    if (chickens.length === 0) {
      return
    }

    dom.poulaillerListBody.innerHTML = ""

    for (const chicken of chickens) {
      let animalDetails = chicken

      try {
        animalDetails = await api.fetchAnimalDetail(chicken.id)
      } catch (error) {
        console.warn("Impossible de charger les donnees detaillees de la poule :", error)
      }

      const row = document.createElement("tr")
      row.className = "poulailler-row"
      row.innerHTML = `
        <td>${animalDetails.name || chicken.name}</td>
        <td>${deriveCleanlinessState(animalDetails)}</td>
        <td>${deriveAnimalHealthState(animalDetails)}</td>
        <td>${deriveHungerState(animalDetails, null)}</td>
        <td>${deriveHydrationState(animalDetails, null)}</td>
        <td><button type="button" class="action-btn poulailler-view-btn" data-animal-id="${chicken.id}">Voir</button></td>
      `

      row.querySelector(".poulailler-view-btn")?.addEventListener("click", () => {
        closePoulaillerListModal()
        openAnimalModal(chicken)
      })

      dom.poulaillerListBody.appendChild(row)
    }

    dom.poulaillerListModal.classList.remove("hidden")
    dom.poulaillerListModal.setAttribute("aria-hidden", "false")
  }

  function closePoulaillerListModal() {
    if (!dom.poulaillerListModal) {
      return
    }

    dom.poulaillerListModal.classList.add("hidden")
    dom.poulaillerListModal.setAttribute("aria-hidden", "true")
  }

  function closeActionModal() {
    if (!dom.modal || dom.modal.classList.contains("hidden")) {
      return
    }

    dom.modal.classList.add("hidden")
    dom.modal.setAttribute("aria-hidden", "true")
  }

  function closeAllModals() {
    closeActionModal()
    closePoulaillerListModal()
    fermerPopup()
    fermerPopupMarche()
  }

  function showToast(message) {
    if (!dom.toast) {
      return
    }

    dom.toast.textContent = message
    dom.toast.classList.remove("hidden")
    dom.toast.classList.add("show")

    if (state.toastTimeoutId) {
      window.clearTimeout(state.toastTimeoutId)
    }

    state.toastTimeoutId = window.setTimeout(() => {
      dom.toast.classList.remove("show")
      dom.toast.classList.add("hidden")
      state.toastTimeoutId = null
    }, 1600)
  }

  async function initializeFarmState() {
    if (!state.currentFarmId) {
      state.currentFarmModel = null
      state.latestFarmData = null
      state.farmClockState = null
      loadCareInventoryState()
      renderMarketPanels()
      return
    }

    try {
      const data = await api.fetchActiveFarmData()
      applyFarmData(data)
    } catch (error) {
      console.error("Erreur lors du chargement des animaux :", error)
      afficherMessage("Impossible de charger les animaux.", "erreur")
    }
  }

  async function initializeStockPanel() {
    if (!state.currentFarmId) {
      renderStockTable(buildStockRows())
      setStockFeedback("Connecte-toi pour voir le stock reel.", "is-error")
      return
    }

    try {
      const data = await api.fetchActiveFarmData()
      renderStockTable(buildStockRows(data))
      setStockFeedback()
    } catch (error) {
      console.error("Erreur lors du chargement du stock :", error)
      renderStockTable(buildStockRows())
      setStockFeedback("Impossible de charger le stock reel.", "is-error")
    }
  }

  async function refreshFarmTimeAndProduction() {
    if (!state.currentFarmId || document.hidden) {
      return
    }

    try {
      const data = await api.fetchActiveFarmData()
      applyFarmData(data)

      if (dom.stockPanel?.classList.contains("open")) {
        renderStockTable(buildStockRows(data))
        setStockFeedback()
      }
    } catch (error) {
      console.error("Erreur lors de l'actualisation du temps de jeu :", error)
    }
  }

  async function initializeCollectivitePanel() {
    if (!dom.collectiviteList) {
      return
    }

    if (!state.currentFarmId) {
      renderCollectivitePanel(constants.fallbackCommunityItems)
      renderCommunityPurchaseCounter()
      return
    }

    try {
      const data = await api.fetchActiveFarmData()
      renderCommunityFromData(data)
    } catch (error) {
      console.error("Erreur lors du chargement de la collectivite :", error)
      renderCollectivitePanel(constants.fallbackCommunityItems)
      renderCommunityPurchaseCounter()
      setCollectiviteFeedback("Collectivite chargee avec les donnees de secours.")
    }
  }

  async function classement() {
    if (!dom.tbody) {
      return
    }

    try {
      const data = await api.fetchRankingData()
      const ranking = Array.isArray(data?.ranking) ? data.ranking : []

      dom.tbody.innerHTML = ""

      if (ranking.length === 0) {
        dom.tbody.innerHTML = `
          <tr>
            <td colspan="6">Aucune ferme classee.</td>
          </tr>
        `
        return
      }

      ranking.forEach((player, index) => {
        dom.tbody.innerHTML += `
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
      dom.tbody.innerHTML = `
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
    if (!dom.loginFeedback) {
      return
    }

    dom.loginFeedback.textContent = message
    dom.loginFeedback.classList.remove("is-error", "is-success")

    if (type) {
      dom.loginFeedback.classList.add(type)
    }
  }

  function openLoginModal() {
    if (!dom.loginModal) {
      showLoginScreen()
      return
    }

    setLoginFeedback()
    dom.loginModal.classList.remove("hidden")
    dom.loginModal.setAttribute("aria-hidden", "false")
  }

  function closeLoginModal() {
    if (!dom.loginModal) {
      return
    }

    dom.loginModal.classList.add("hidden")
    dom.loginModal.setAttribute("aria-hidden", "true")
    setLoginFeedback()

  }

  function showFarmScreen(prefetchedFarmData = null) {
    if (!dom.loginScreen || !dom.farmScreen) {
      return
    }
      
    closeLoginModal()
    dom.loginScreen.classList.add("hidden")
    dom.farmScreen.classList.remove("hidden")

    if (prefetchedFarmData) {
      applyFarmData(prefetchedFarmData)
      return
    }

    initializeFarmState()
  }

  function showLoginScreen() {
    if (!dom.loginScreen || !dom.farmScreen) {
      return
    }

    state.currentFarmModel = null
    state.currentFarmId = null
    state.currentUsername = "-"
    state.latestFarmData = null
    state.farmClockState = null
    state.marketSaleState.selectedProduct = ""
    state.marketSaleState.quantity = 1
    state.marketSaleState.unitPrice = 1
    loadCareInventoryState()
    renderCommunityPurchaseCounter()
    renderStockTable(buildStockRows())
    renderMarketPanels()
    updateClock()
    dom.loginScreen.classList.remove("hidden")
    dom.farmScreen.classList.add("hidden")
  }

  global.TinyFarmUi = {
    buildStockRows,
    renderStockTable,
    setStockFeedback,
    setCollectiviteFeedback,
    setMarcheFeedback,
    renderMarketPanels,
    updateMarcheVenteSelection,
    updateMarcheCollectiviteSelection,
    renderCommunityFromData,
    renderCommunityPurchaseCounter,
    initializeAnimalZoneNavigation,
    initializeFarmState,
    initializeStockPanel,
    initializeCollectivitePanel,
    refreshFarmTimeAndProduction,
    applyFarmData,
    afficherMessage,
    estAchatAutorise,
    mettreAJourDisponibiliteBoutons,
    updateFarmOverlayState,
    ouvrirPopup,
    fermerPopup,
    setStockPanelOpen,
    ouvrirPopupMarche,
    fermerPopupMarche,
    setMarcheTab,
    closeActionModal,
    closePoulaillerListModal,
    closeAllModals,
    openAnimalModal,
    openClapierModal,
    openPoulaillerListModal,
    showToast,
    updateClock,
    setLoginFeedback,
    openLoginModal,
    closeLoginModal,
    showFarmScreen,
    showLoginScreen,
    classement,
    loadCareInventoryState
  }
})(window)
