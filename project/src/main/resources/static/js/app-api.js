/*
 * Couche d'acces API du front TinyFarm.
 * Toutes les requetes HTTP sortantes passent par ici pour garder
 * les handlers d'interface plus lisibles.
 */
(function attachTinyFarmApi(global) {
  const shell = global.TinyFarmShell

  async function fetchJsonOrThrow(url, options, fallbackMessage) {
    const response = await fetch(url, options)

    if (!response.ok) {
      throw new Error((await response.text()) || fallbackMessage)
    }

    return response.json()
  }

  async function fetchFarmDataById(farmId) {
    return fetchJsonOrThrow(`/api/fermes/${farmId}/front-data`, undefined, "Impossible de charger la ferme.")
  }

  async function fetchActiveFarmData() {
    if (!shell.state.currentFarmId) {
      throw new Error("Aucune ferme connectee.")
    }

    return fetchFarmDataById(shell.state.currentFarmId)
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

    const fallbackAnimal = shell.state.currentFarmModel?.animals?.find(
      (item) => String(item.id) === String(animalId)
    )

    if (!fallbackAnimal) {
      throw new Error("Animal introuvable.")
    }

    return fallbackAnimal
  }

  async function fetchAnimalTypeStatus(type) {
    if (!shell.state.currentFarmId) {
      throw new Error("Aucune ferme connectee.")
    }

    return fetchJsonOrThrow(
      `/api/fermes/${encodeURIComponent(shell.state.currentFarmId)}/animaux/${encodeURIComponent(type)}/status`,
      undefined,
      "Impossible de charger les stats du type d'animal."
    )
  }

  async function fetchClapierStatus() {
    if (!shell.state.currentFarmId) {
      throw new Error("Aucune ferme connectee.")
    }

    return fetchJsonOrThrow(
      `/api/fermes/${encodeURIComponent(shell.state.currentFarmId)}/animaux/clapier`,
      undefined,
      "Impossible de charger les donnees du clapier."
    )
  }

  async function fetchRankingData() {
    if (shell.state.currentFarmId) {
      return fetchActiveFarmData()
    }

    const candidateFarmIds = [1, 2]

    for (const farmId of candidateFarmIds) {
      try {
        return await fetchFarmDataById(farmId)
      } catch (error) {
        // On tente simplement la ferme suivante.
      }
    }

    throw new Error("Impossible de charger le classement.")
  }

  async function loginLocal(username, password) {
    const response = await fetch("/api/auth/login-local", {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify({ username, password })
    })

    if (!response.ok) {
      throw new Error("erreur de username ou de password")
    }

    return response.json()
  }
  async function loginGit(user) {
  const response = await fetch("/api/auth/login-git", {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify({ login: user.login }) 
  })

  if (!response.ok) {
    throw new Error("erreur de username")
  }

  return response.json()
}

  async function acheterAnimal(typeAnimal) {
    if (!shell.state.currentFarmId) {
      throw new Error("Connecte-toi pour acheter des animaux.")
    }

    return fetchJsonOrThrow(
      `/api/fermes/${shell.state.currentFarmId}/acheter-animal?type=${encodeURIComponent(typeAnimal)}`,
      { method: "POST" },
      "Impossible d'acheter cet animal."
    )
  }

  async function acheterObjetEntretien(apiType) {
    if (!shell.state.currentFarmId) {
      throw new Error("Connecte-toi pour acheter cet objet.")
    }

    return fetchJsonOrThrow(
      `/api/fermes/${shell.state.currentFarmId}/acheter-objet-entretien?type=${encodeURIComponent(apiType)}`,
      { method: "POST" },
      "Achat impossible."
    )
  }

  async function vendreACollectivite(product, quantity) {
    if (!shell.state.currentFarmId) {
      throw new Error("Connecte-toi pour vendre a la collectivite.")
    }

    return fetchJsonOrThrow(
      `/api/fermes/${encodeURIComponent(shell.state.currentFarmId)}/collectivite/vente?produit=${encodeURIComponent(product)}&quantite=${encodeURIComponent(quantity)}`,
      { method: "POST" },
      "Impossible de vendre a la collectivite."
    )
  }

  async function publierOffreMarche(product, quantity, unitPrice) {
    if (!shell.state.currentFarmId) {
      throw new Error("Connecte-toi pour vendre sur le marche.")
    }

    return fetchJsonOrThrow(
      `/api/fermes/${encodeURIComponent(shell.state.currentFarmId)}/marche/offres?produit=${encodeURIComponent(product)}&quantite=${encodeURIComponent(quantity)}&prix=${encodeURIComponent(unitPrice)}`,
      { method: "POST" },
      "Impossible de publier l'offre."
    )
  }

  async function acheterOffreMarche(offerId, quantity) {
    if (!shell.state.currentFarmId) {
      throw new Error("Connecte-toi pour acheter sur le marche.")
    }

    return fetchJsonOrThrow(
      `/api/fermes/${encodeURIComponent(shell.state.currentFarmId)}/marche/achat?idOffre=${encodeURIComponent(offerId)}&quantite=${encodeURIComponent(quantity)}`,
      { method: "POST" },
      "Impossible d'acheter cette offre."
    )
  }

  async function passerJourFerme() {
    if (!shell.state.currentFarmId) {
      throw new Error("Connecte-toi pour passer au jour suivant.")
    }

    return fetchJsonOrThrow(
      `/api/fermes/${encodeURIComponent(shell.state.currentFarmId)}/passer-jour`,
      { method: "POST" },
      "Impossible de passer au jour suivant."
    )
  }

  async function agirSurAnimal(animalType, action, animalId) {
    if (!shell.state.currentFarmId) {
      throw new Error("Pas de ferme connectee.")
    }

    const querySuffix = animalId ? `?animalId=${encodeURIComponent(animalId)}` : ""

    return fetchJsonOrThrow(
      `/api/fermes/${encodeURIComponent(shell.state.currentFarmId)}/animaux/${encodeURIComponent(animalType)}/${action}${querySuffix}`,
      { method: "POST" },
      "Action impossible."
    )
  }

  global.TinyFarmApi = {
    fetchFarmDataById,
    fetchActiveFarmData,
    fetchAnimalDetail,
    fetchAnimalTypeStatus,
    fetchClapierStatus,
    fetchRankingData,
    loginLocal,
    loginGit,
    acheterAnimal,
    acheterObjetEntretien,
    vendreACollectivite,
    publierOffreMarche,
    acheterOffreMarche,
    passerJourFerme,
    agirSurAnimal
  }
})(window)
