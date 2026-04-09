/*
 * Noyau front TinyFarm.
 * Ce fichier centralise les constantes de l'interface, les references DOM
 * et l'etat partage entre les autres modules du front.
 */
(function attachTinyFarmShell(global) {
  const fallbackCommunityItems = [
    { id: "feed-bag", label: "Nourriture", price: 5 },
    { id: "straw-bales", label: "Bottes de paille", price: 5 },
    { id: "syringe", label: "Seringue", price: 6 },
    { id: "water-bucket", label: "Seau d'eau", price: 2 },
    { id: "soap", label: "Savon", price: 3 },
    { id: "farmers-market", label: "Marche des producteurs", variant: "shortcut" },
    { id: "buy-animals", label: "Achat d'animaux", variant: "shortcut" }
  ]

  const COMMUNITY_BUYBACK_PRICES = {
    OEUF: 8,
    LAIT: 2,
    LAPIN: 25
  }

  const ANIMAL_ACTION_COSTS = {
    poule: { Nourrir: 3, Abreuver: 1, Nettoyer: 3, Soigner: 6 },
    lapin: { Nourrir: 5, Abreuver: 2, Nettoyer: 3, Soigner: 6 },
    vache: { Nourrir: 5, Abreuver: 2, Nettoyer: 3, Soigner: 6 }
  }

  const CARE_ITEM_IDS = ["feed-bag", "straw-bales", "syringe", "water-bucket", "soap"]
  const CARE_ITEM_TO_API_TYPE = {
    "feed-bag": "NOURRITURE",
    "straw-bales": "PAILLE",
    syringe: "SERINGUE",
    "water-bucket": "EAU",
    soap: "SAVON"
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

  function createDefaultCareInventory() {
    return CARE_ITEM_IDS.reduce((inventory, itemId) => {
      inventory[itemId] = 0
      return inventory
    }, {})
  }

  const dom = {
    loginBtn: document.getElementById("login-btn"),
    loginScreen: document.getElementById("login-screen"),
    loginModal: document.getElementById("login-modal"),
    loginForm: document.getElementById("login-form"),
    loginUsernameInput: document.getElementById("login-username"),
    loginPasswordInput: document.getElementById("login-password"),
    loginFeedback: document.getElementById("login-feedback"),
    loginCloseTargets: document.querySelectorAll("[data-close-login-modal]"),
    clsBtn: document.getElementById("Trophy"),
    farmScreen: document.getElementById("farm-screen"),
    classementScreen: document.getElementById("Classement"),
    tbody: document.getElementById("classement-body"),
    stockToggle: document.getElementById("stock-toggle"),
    stockPanel: document.getElementById("stock-panel"),
    stockTotalUnits: document.getElementById("stock-total-units"),
    stockTableBody: document.getElementById("stock-table-body"),
    stockFeedback: document.getElementById("stock-feedback"),
    collectiviteList: document.getElementById("collectivite-list"),
    collectiviteFeedback: document.getElementById("collectivite-feedback"),
    collectiviteCounter: document.getElementById("collectivite-counter"),
    passDayButton: document.getElementById("pass-day-button"),
    careInventoryCounters: Array.from(document.querySelectorAll("[data-care-count]")).reduce(
      (accumulator, element) => {
        accumulator[element.dataset.careCount] = element
        return accumulator
      },
      {}
    ),
    poulaillerContainer: document.getElementById("poulailler-container"),
    paturageContainer: document.getElementById("paturage-container"),
    clapierContainer: document.getElementById("clapier-container"),
    animalZoneControllers: Array.from(document.querySelectorAll("[data-zone-controls]"))
      .map((shell) => ({
        shell,
        container: document.getElementById(shell.dataset.zoneControls),
        previousButton: shell.querySelector('[data-zone-nav="previous"]'),
        nextButton: shell.querySelector('[data-zone-nav="next"]')
      }))
      .filter(
        ({ container, previousButton, nextButton }) =>
          Boolean(container) && Boolean(previousButton) && Boolean(nextButton)
      ),
    modal: document.getElementById("animal-modal"),
    modalName: document.getElementById("animal-name"),
    modalType: document.getElementById("animal-type"),
    modalPlace: document.getElementById("animal-place"),
    modalStatus: document.getElementById("animal-status"),
    modalHealth: document.getElementById("animal-health"),
    modalHunger: document.getElementById("animal-hunger"),
    modalHydration: document.getElementById("animal-hydration"),
    modalWeightLabel: document.getElementById("animal-weight-label"),
    modalWeight: document.getElementById("animal-weight"),
    closeTargets: document.querySelectorAll("[data-close-modal]"),
    actionButtons: document.querySelectorAll("[data-action]"),
    toast: document.getElementById("toast"),
    popupMarche: document.getElementById("popup-marche"),
    boutonFermerMarche: document.getElementById("bouton-fermer-marche"),
    closeMarcheTargets: document.querySelectorAll("[data-close-marche]"),
    marcheTabButtons: Array.from(document.querySelectorAll("[data-marche-tab]")),
    marchePanels: Array.from(document.querySelectorAll("[data-marche-panel]")),
    marcheAchatBody: document.getElementById("marche-achat-body"),
    marcheAchatFeedback: document.getElementById("marche-achat-feedback"),
    marcheVenteFeedback: document.getElementById("marche-vente-feedback"),
    marcheVenteProduit: document.getElementById("marche-vente-produit"),
    marcheVenteStock: document.getElementById("marche-vente-stock"),
    marcheVenteQuantite: document.getElementById("marche-vente-quantite"),
    marcheVentePrix: document.getElementById("marche-vente-prix"),
    marcheVenteSubmit: document.getElementById("marche-vente-submit"),
    marcheCollectiviteFeedback: document.getElementById("marche-collectivite-feedback"),
    marcheCollectiviteGrid: document.getElementById("marche-collectivite-grid"),
    marcheCollectiviteProduit: document.getElementById("marche-collectivite-produit"),
    marcheCollectiviteStock: document.getElementById("marche-collectivite-stock"),
    marcheCollectiviteQuantite: document.getElementById("marche-collectivite-quantite"),
    marcheCollectivitePrix: document.getElementById("marche-collectivite-prix"),
    marcheCollectiviteSubmit: document.getElementById("marche-collectivite-submit"),
    poulaillerListModal: document.getElementById("poulailler-list-modal"),
    poulaillerListBody: document.getElementById("poulailler-list-body"),
    demoLoginAButton: document.getElementById("demo-login-a"),
    demoLoginBButton: document.getElementById("demo-login-b"),
    loginShortcutAButton: document.getElementById("login-shortcut-a"),
    loginShortcutBButton: document.getElementById("login-shortcut-b"),
    elementsInterface: {
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
  }

  const state = {
    stockState: {
      products: [],
      selectedProductId: null,
      quantity: 1
    },
    collectiviteState: {
      items: []
    },
    currentFarmModel: null,
    currentFarmId: null,
    currentUsername: "-",
    latestFarmData: null,
    farmClockState: null,
    farmRefreshIntervalId: null,
    marketSaleState: {
      selectedProduct: "",
      quantity: 1,
      unitPrice: 1
    },
    communitySaleState: {
      selectedProduct: "",
      quantity: 1
    },
    collectiviteFeedbackTimeout: null,
    toastTimeoutId: null,
    activeActionTarget: "",
    animalZoneResizeBound: false,
    animalZoneRefreshFrame: null,
    careInventoryState: createDefaultCareInventory(),
    currentAnimalAction: {
      farmId: null,
      animalType: null,
      animalName: null,
      animalId: null
    }
  }

  function randomBetween(min, max) {
    return Math.random() * (max - min) + min
  }

  function getAnimatedAnimalConfig(typeKey) {
    return ANIMATED_ANIMAL_CONFIG[typeKey] || null
  }

  function clamp(value, min, max) {
    return Math.min(Math.max(value, min), max)
  }

  function formatEcus(value) {
    return `${Number.parseInt(value, 10) || 0} ecus`
  }

  function formatWeight(weight) {
    const numericWeight = Number.parseFloat(weight)
    return Number.isFinite(numericWeight) ? `${numericWeight.toFixed(1)} kg` : "-"
  }

  global.TinyFarmShell = {
    constants: {
      fallbackCommunityItems,
      COMMUNITY_BUYBACK_PRICES,
      ANIMAL_ACTION_COSTS,
      CARE_ITEM_IDS,
      CARE_ITEM_TO_API_TYPE,
      ANIMATED_ANIMAL_CONFIG
    },
    dom,
    state,
    helpers: {
      randomBetween,
      getAnimatedAnimalConfig,
      createDefaultCareInventory,
      clamp,
      formatEcus,
      formatWeight
    }
  }
})(window)
