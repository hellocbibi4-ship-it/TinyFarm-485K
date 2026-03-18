const cowButtonsContainer = document.getElementById("cow-buttons-container")
const cowCountSummary = document.getElementById("cow-count-summary")

function createCowButton(animal) {
  const button = document.createElement("div")
  button.className = "cow-btn"
  button.textContent = animal.name
  return button
}

async function renderCowPage() {
  if (!cowButtonsContainer || !cowCountSummary) {
    return
  }

  try {
    const data = await TinyFarmState.fetchFarmData()
    const model = TinyFarmState.buildFarmModel(data)
    const cows = TinyFarmState.getAnimalsByType(model, "vache")

    cowButtonsContainer.innerHTML = ""

    if (cows.length === 0) {
      const emptyState = document.createElement("p")
      emptyState.className = "cow-empty"
      emptyState.textContent = "Aucune vache pour le moment."
      cowButtonsContainer.appendChild(emptyState)
      cowCountSummary.textContent = "0 vache affichee selon le JSON."
      return
    }

    cows.forEach((animal) => {
      cowButtonsContainer.appendChild(createCowButton(animal))
    })

    cowCountSummary.textContent = `${cows.length} vache${cows.length > 1 ? "s" : ""} affichee${cows.length > 1 ? "s" : ""} selon le JSON et les achats de la session.`
  } catch (error) {
    console.error("Erreur lors du chargement des vaches :", error)
    cowButtonsContainer.innerHTML = ""
    cowCountSummary.textContent = "Impossible de charger les vaches."
  }
}

window.addEventListener("pageshow", renderCowPage)
window.addEventListener("storage", (event) => {
  if (event.key === TinyFarmState.STORAGE_KEY) {
    renderCowPage()
  }
})

renderCowPage()
