/*
 * Construction du modele front TinyFarm.
 * Ce module transforme la reponse backend en donnees simples a rendre
 * et garde un petit etat UI persiste en localStorage.
 */
(function attachTinyFarmState(global) {
  const STORAGE_KEY = "tinyfarm-front-ui-state-v2"
  const DEFAULT_LEVEL = 1
  const BASE_UI_STATE = {
    level: DEFAULT_LEVEL,
    balance: null,
    purchases: {
      vache: 0,
      poule: 0,
      lapin: 0
    }
  }

  const ANIMAL_CATALOG = {
    vache: {
      label: "Vache",
      article: "une",
      home: "Paturage",
      img: "vache.png",
      price: 50,
      minLevel: 2,
      defaultWeight: 500
    },
    poule: {
      label: "Poule",
      article: "une",
      home: "Poulailler",
      img: "poule.png",
      price: 10,
      minLevel: 1,
      defaultWeight: 1.5
    },
    lapin: {
      label: "Lapin",
      article: "un",
      home: "Clapier",
      img: "lapin.png",
      price: 10,
      minLevel: 1,
      defaultWeight: 2
    }
  }

  function cloneUiState(uiState = BASE_UI_STATE) {
    return {
      level: Number.isFinite(uiState.level) ? uiState.level : DEFAULT_LEVEL,
      balance: Number.isFinite(uiState.balance) ? uiState.balance : null,
      purchases: {
        vache: Math.max(0, Number.parseInt(uiState.purchases?.vache, 10) || 0),
        poule: Math.max(0, Number.parseInt(uiState.purchases?.poule, 10) || 0),
        lapin: Math.max(0, Number.parseInt(uiState.purchases?.lapin, 10) || 0)
      }
    }
  }

  function normalizeAnimalType(rawType = "") {
    const normalized = String(rawType).trim().toLowerCase()

    if (["vache", "cow"].includes(normalized)) {
      return "vache"
    }

    if (["poule", "coq", "chicken", "hen", "cock"].includes(normalized)) {
      return "poule"
    }

    if (["lapin", "lapereau", "rabbit", "bunny"].includes(normalized)) {
      return "lapin"
    }

    return null
  }

  function safeParseState(rawValue) {
    if (!rawValue) {
      return null
    }

    try {
      return JSON.parse(rawValue)
    } catch (error) {
      return null
    }
  }

  function readUiState() {
    const parsedState = safeParseState(global.localStorage?.getItem(STORAGE_KEY))

    if (!parsedState) {
      return cloneUiState()
    }

    return cloneUiState(parsedState)
  }

  function writeUiState(uiState) {
    const normalizedState = cloneUiState(uiState)
    global.localStorage?.setItem(STORAGE_KEY, JSON.stringify(normalizedState))
    return normalizedState
  }

  function ensureUiState(data) {
    const uiState = readUiState()

    if (!Number.isFinite(uiState.balance)) {
      uiState.balance = Number.isFinite(data?.cash) ? data.cash : 0
      writeUiState(uiState)
    }

    return uiState
  }

  function createBaseAnimal(rawAnimal, index) {
    const typeKey = normalizeAnimalType(rawAnimal?.type)
    const catalogEntry = ANIMAL_CATALOG[typeKey]

    if (!catalogEntry) {
      return null
    }

    const parsedWeight = Number.parseFloat(rawAnimal?.weight)

    return {
      id: rawAnimal?.id || `base-${typeKey}-${index + 1}`,
      idAnimal: Number.parseInt(rawAnimal?.idAnimal, 10) || null,
      name: rawAnimal?.name || `${catalogEntry.label} ${index + 1}`,
      type: typeKey,
      typeKey,
      typeLabel: catalogEntry.label,
      homeLabel: catalogEntry.home,
      img: rawAnimal?.img || catalogEntry.img,
      weight: Number.isFinite(parsedWeight) ? parsedWeight : catalogEntry.defaultWeight,
      age: Number.parseInt(rawAnimal?.age, 10) || 0,
      stage: rawAnimal?.stage || null,
      isSick: Boolean(rawAnimal?.isSick),
      health: Number.parseInt(rawAnimal?.health, 10) || 100,
      hunger: Number.parseInt(rawAnimal?.hunger, 10) || 100,
      hydration: Number.parseInt(rawAnimal?.hydration, 10) || 100,
      cleanliness: Number.parseInt(rawAnimal?.cleanliness, 10) || 100,
      isPurchased: false
    }
  }

  function buildCounts(animals) {
    return animals.reduce(
      (counts, animal) => {
        counts[animal.typeKey] += 1
        return counts
      },
      { vache: 0, poule: 0, lapin: 0 }
    )
  }

  function buildPurchasedAnimals(uiState, baseCounts) {
    return Object.entries(uiState.purchases).flatMap(([typeKey, purchaseCount]) => {
      const catalogEntry = ANIMAL_CATALOG[typeKey]
      const baseCount = baseCounts[typeKey] || 0

      return Array.from({ length: purchaseCount }, (_, purchaseIndex) => {
        const displayIndex = baseCount + purchaseIndex + 1

        return {
          id: `purchase-${typeKey}-${displayIndex}`,
          name: `${catalogEntry.label} ${displayIndex}`,
          type: typeKey,
          typeKey,
          typeLabel: catalogEntry.label,
          homeLabel: catalogEntry.home,
          img: catalogEntry.img,
          weight: catalogEntry.defaultWeight,
          isPurchased: true
        }
      })
    })
  }

  function buildFarmModel(data, providedUiState) {
    const uiState = providedUiState ? cloneUiState(providedUiState) : ensureUiState(data)
    const baseAnimals = Array.isArray(data?.animals)
      ? data.animals.map(createBaseAnimal).filter(Boolean)
      : []
    const baseCounts = buildCounts(baseAnimals)
    const purchasedAnimals = buildPurchasedAnimals(uiState, baseCounts)
    const animals = [...baseAnimals, ...purchasedAnimals]

    return {
      rawData: data,
      uiState,
      balance: Number.isFinite(uiState.balance) ? uiState.balance : 0,
      animals,
      counts: buildCounts(animals),
      players: Array.isArray(data?.players) ? data.players : [],
      stockProducts: Array.isArray(data?.stockProducts) ? data.stockProducts : [],
      communityItems: Array.isArray(data?.communityItems) ? data.communityItems : []
    }
  }

  function getAnimalsByType(model, typeKey) {
    return model.animals.filter((animal) => animal.typeKey === typeKey)
  }

  global.TinyFarmState = {
    STORAGE_KEY,
    ANIMAL_CATALOG,
    readUiState,
    writeUiState,
    ensureUiState,
    buildFarmModel,
    getAnimalsByType,
    normalizeAnimalType
  }
})(window)
