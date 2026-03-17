const loginBtn = document.getElementById("login-btn")
const loginScreen = document.getElementById("login-screen")
const clsBtn = document.getElementById("Trophy")
const farmScreen = document.getElementById("farm-screen")
const classementScreen = document.getElementById("Classement")
const tbody = document.getElementById("classement-body")
const collectiviteList = document.getElementById("collectivite-list")
const collectiviteFeedback = document.getElementById("collectivite-feedback")

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

const collectiviteState = {
  items: []
}

let farmDataPromise = null
let collectiviteFeedbackTimeout = null

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

function renderCollectivitePanel(items) {
  if (!collectiviteList) {
    return
  }

  collectiviteState.items = items.map((item) => ({ ...item }))

  collectiviteList.innerHTML = collectiviteState.items
    .map((item) => {
      const hasPrice = Number.isFinite(item.price)
      const itemClasses = ["collectivite-item"]

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
          <span class="collectivite-item-icon" aria-hidden="true">${item.icon || "•"}</span>
          <span class="collectivite-item-label">${item.label}</span>
          ${hasPrice
            ? `
              <span class="collectivite-item-price">
                <span>${item.price}</span>
                <span class="collectivite-coin" aria-hidden="true"></span>
              </span>
            `
            : ""}
        </button>
      `
    })
    .join("")
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

    setCollectiviteFeedback(`${selectedItem.label} selectionne.`)
  })
}

document.addEventListener("DOMContentLoaded", () => {
  initializeCollectivitePanel()
  updateClock()
  setInterval(updateClock, 1000)
})
