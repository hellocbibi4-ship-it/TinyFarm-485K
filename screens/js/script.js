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

const fallbackStockProducts = [
  { id: "milk", name: "Lait", stock: 32, price: 4 },
  { id: "eggs", name: "Oeufs", stock: 48, price: 2 },
  { id: "wool", name: "Laine", stock: 18, price: 7 },
  { id: "cheese", name: "Fromage", stock: 26, price: 6 }
]

const stockState = {
  products: [],
  selectedProductId: null,
  quantity: 1
}

let farmDataPromise = null

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
    if (stockAvailable) {
      stockAvailable.textContent = "0"
    }

    if (stockTotalUnits) {
      stockTotalUnits.textContent = "0"
    }

    if (stockUnitPrice) {
      stockUnitPrice.textContent = formatEcus(0)
    }

    if (stockTotalPrice) {
      stockTotalPrice.textContent = formatEcus(0)
    }

    if (stockQuantityInput) {
      stockQuantityInput.value = "0"
      stockQuantityInput.disabled = true
    }

    if (stockMinusButton) {
      stockMinusButton.disabled = true
    }

    if (stockPlusButton) {
      stockPlusButton.disabled = true
    }

    if (stockSellButton) {
      stockSellButton.disabled = true
    }

    return
  }

  const minimumQuantity = product.stock > 0 ? 1 : 0
  stockState.quantity = clamp(stockState.quantity, minimumQuantity, product.stock)

  if (stockProductSelect) {
    stockProductSelect.value = product.id
  }

  if (stockTotalUnits) {
    stockTotalUnits.textContent = String(getTotalUnits())
  }

  if (stockAvailable) {
    stockAvailable.textContent = String(product.stock)
  }

  if (stockUnitPrice) {
    stockUnitPrice.textContent = formatEcus(product.price)
  }

  if (stockTotalPrice) {
    stockTotalPrice.textContent = formatEcus(stockState.quantity * product.price)
  }

  if (stockQuantityInput) {
    stockQuantityInput.min = String(minimumQuantity)
    stockQuantityInput.max = String(product.stock)
    stockQuantityInput.value = String(stockState.quantity)
    stockQuantityInput.disabled = product.stock === 0
  }

  if (stockMinusButton) {
    stockMinusButton.disabled = product.stock === 0 || stockState.quantity <= minimumQuantity
  }

  if (stockPlusButton) {
    stockPlusButton.disabled = product.stock === 0 || stockState.quantity >= product.stock
  }

  if (stockSellButton) {
    stockSellButton.disabled = product.stock === 0 || stockState.quantity === 0
  }
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

if (loginBtn && loginScreen && farmScreen) {
  loginBtn.addEventListener("click", () => {
    loginScreen.classList.add("hidden")
    farmScreen.classList.remove("hidden")
  })
}

classement()

if (clsBtn && classementScreen) {
  clsBtn.addEventListener("click", () => {
    classementScreen.classList.toggle("show")
    clsBtn.classList.toggle("trophy2")

    if (classementScreen.classList.contains("show")) {
      classement()
    }
  })
}

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

  document.addEventListener("click", () => {
    setStockPanelOpen(false)
  })

  document.addEventListener("keydown", (event) => {
    if (event.key === "Escape") {
      setStockPanelOpen(false)
    }
  })

  initializeStockPanel()
  updateClock()
  setInterval(updateClock, 1000)
})
