/*
 * Point d'entree du front TinyFarm.
 * Ce fichier ne garde que la coordination generale : actions utilisateur,
 * branchement des evenements et initialisation de l'application.
 */
(function bootstrapTinyFarm(global) {
  const shell = global.TinyFarmShell
  const api = global.TinyFarmApi
  const ui = global.TinyFarmUi
  const { dom, state, constants } = shell

  console.log("TinyFarm script loaded")

  async function loginWithCredentials(username, password) {
    // Toute la session front repart de cette connexion locale.
    if (!username || !password) {
      ui.setLoginFeedback("Renseigne un username et un password.", "is-error")
      return
    }

    ui.setLoginFeedback()

    try {
      const payload = await api.loginLocal(username, password)
      state.currentFarmId = payload.farmId
      state.currentUsername = username

      const farmData = await api.fetchFarmDataById(state.currentFarmId)
      ui.setLoginFeedback("Connexion acceptee.", "is-success")
      ui.showFarmScreen(farmData)
    } catch (error) {
      console.error("Erreur de connexion :", error)
      ui.setLoginFeedback("erreur de username ou de password", "is-error")
    }
  }
  async function loginWithGit(username) {
    // Toute la session front repart de cette connexion locale.
    if (!username) {
      ui.setLoginFeedback("Renseigne un username et un password.", "is-error")
      return
    }

    ui.setLoginFeedback()

    try {
      const payload = await api.loginGit(username)
      state.currentFarmId = payload.farmId
      state.currentUsername = username.name

      const farmData = await api.fetchFarmDataById(state.currentFarmId)
      ui.setLoginFeedback("Connexion acceptee.", "is-success")
      ui.showFarmScreen(farmData)
    } catch (error) {
      console.error("Erreur de connexion :", error)
      ui.setLoginFeedback("erreur de connexion git", "is-error")
    }
  }

  async function traiterAchat(typeAnimal) {
    // Les achats d'animaux passent toujours par le backend pour garder
    // le solde, le quota collectivite et le cheptel synchronises.
    if (!state.currentFarmModel) {
      ui.afficherMessage("Donnees animales indisponibles.", "erreur")
      return
    }

    const catalogEntry = TinyFarmState.ANIMAL_CATALOG[typeAnimal]

    if (!catalogEntry) {
      ui.afficherMessage("Animal inconnu, achat annule.", "erreur")
      return
    }

    if (!ui.estAchatAutorise(typeAnimal)) {
      const message = typeAnimal === "vache"
        ? "Impossible d'acheter une deuxieme vache."
        : `Niveau insuffisant pour acheter ${catalogEntry.article} ${catalogEntry.label.toLowerCase()}.`

      ui.afficherMessage(message, "erreur")
      return
    }

    if (state.currentFarmModel.balance < catalogEntry.price) {
      ui.afficherMessage(
        `Solde insuffisant pour acheter ${catalogEntry.article} ${catalogEntry.label.toLowerCase()}.`,
        "erreur"
      )
      return
    }

    if (!state.currentFarmId) {
      ui.afficherMessage("Connecte-toi pour acheter des animaux.", "erreur")
      return
    }

    try {
      const farmData = await api.acheterAnimal(typeAnimal)
      ui.applyFarmData(farmData)
      ui.afficherMessage(`Achat valide : ${catalogEntry.label}.`, "succes")
    } catch (error) {
      console.error("Erreur lors de l'achat :", error)
      ui.afficherMessage(error.message || "Impossible d'acheter cet animal.", "erreur")
    }
  }

  async function handleAnimalAction(actionLabel) {
    const target = state.activeActionTarget || "la ferme"

    if (!state.currentFarmId || !state.currentAnimalAction.animalType) {
      ui.showToast(`${actionLabel} : impossible (pas connecte ou animal invalide)`)
      return
    }

    try {
      const actionMap = {
        Nourrir: "feed",
        Abreuver: "water",
        Soigner: "heal",
        Nettoyer: "clean"
      }

      const apiAction = actionMap[actionLabel]
      if (!apiAction) {
        ui.showToast(`${actionLabel} : action inconnue`)
        return
      }

      const farmData = await api.agirSurAnimal(
        state.currentAnimalAction.animalType,
        apiAction,
        state.currentAnimalAction.animalId
      )

      // On reapplique tout le front-data renvoye par l'API pour rester
      // aligne avec l'etat reel de la ferme apres l'action.
      ui.applyFarmData(farmData)
      ui.showToast(`${actionLabel} : ${target} - OK`)

      // Le clapier est gere comme une entite de groupe, donc on rouvre
      // directement sa popup au lieu d'une fiche individuelle.
      if (state.currentAnimalAction.animalType === "lapin") {
        await ui.openClapierModal()
        return
      }

      // Pour les poules/vaches, on garde la modale ouverte et on recharge
      // la fiche du meme animal afin de permettre plusieurs soins d'affilee.
      const refreshedAnimal = state.currentFarmModel?.animals?.find(
        (animal) => Number(animal.idAnimal) === Number(state.currentAnimalAction.animalId)
      )

      if (refreshedAnimal) {
        await ui.openAnimalModal(refreshedAnimal)
        return
      }

      ui.closeActionModal()
    } catch (error) {
      console.error(`Erreur lors de ${actionLabel} :`, error)
      ui.showToast(`${actionLabel} : ${error.message || "erreur"}`)
    }
  }

  function bindStaticEvents() {
    // Ces evenements existent avant meme que l'ecran ferme soit visible.
    if (dom.loginBtn) {
      dom.loginBtn.addEventListener("click", () => {
    window.location.href = "/oauth2/authorization/github"
  })
    }
    //dom.loginBtn.addEventListener("click", ui.openLoginModal)}

    if (dom.clsBtn && dom.classementScreen) {
      dom.clsBtn.addEventListener("click", () => {
        dom.classementScreen.classList.toggle("show")
        dom.clsBtn.classList.toggle("trophy2")

        if (dom.classementScreen.classList.contains("show")) {
          ui.classement()
        }
      })
    }

    dom.closeTargets.forEach((button) => {
      button.addEventListener("click", ui.closeAllModals)
    })

    dom.loginCloseTargets.forEach((button) => {
      button.addEventListener("click", ui.closeLoginModal)
    })

    dom.actionButtons.forEach((button) => {
      button.addEventListener("click", async () => {
        await handleAnimalAction(button.dataset.action)
      })
    })

    if (dom.loginForm) {
      dom.loginForm.addEventListener("submit", async (event) => {
        event.preventDefault()

        const username = dom.loginUsernameInput?.value.trim() || ""
        const password = dom.loginPasswordInput?.value.trim() || ""
        await loginWithCredentials(username, password)
      })
    }

    if (dom.demoLoginAButton) {
      dom.demoLoginAButton.addEventListener("click", () => loginWithCredentials("a", "a1"))
    }
    if (dom.demoLoginBButton) {
      dom.demoLoginBButton.addEventListener("click", () => loginWithCredentials("b", "b2"))
    }
    if (dom.loginShortcutAButton) {
      dom.loginShortcutAButton.addEventListener("click", () => loginWithCredentials("a", "a1"))
    }
    if (dom.loginShortcutBButton) {
      dom.loginShortcutBButton.addEventListener("click", () => loginWithCredentials("b", "b2"))
    }

    window.addEventListener("focus", () => {
      ui.initializeFarmState()
    })

    window.addEventListener("storage", (event) => {
      if (event.key === TinyFarmState.STORAGE_KEY) {
        ui.initializeFarmState()
      }
    })
  }

  function bindDomContentLoadedEvents() {
    // Cette partie branche surtout l'interface du jeu une fois le DOM pret.
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
        button.nextElementSibling?.classList.toggle("open")
      })
    })

    document.querySelectorAll(".logout-btn").forEach((button) => {
      button.addEventListener("click", (event) => {
        event.preventDefault()
        event.stopPropagation()
        ui.closeActionModal()
        ui.fermerPopup({ announce: false })
        ui.setStockPanelOpen(false)
        ui.showLoginScreen()

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
        button.nextElementSibling?.classList.toggle("open")
      })
    })

    if (dom.stockToggle && dom.stockPanel) {
      dom.stockToggle.addEventListener("click", (event) => {
        event.stopPropagation()
        if (!dom.stockPanel.classList.contains("open")) {
          ui.initializeStockPanel()
        }
        ui.setStockPanelOpen(!dom.stockPanel.classList.contains("open"))
      })

      dom.stockPanel.addEventListener("click", (event) => {
        event.stopPropagation()
      })
    }

    if (dom.collectiviteList) {
      dom.collectiviteList.addEventListener("click", async (event) => {
        const button = event.target.closest("[data-collectivite-id]")

        if (!button) {
          return
        }

        const selectedItem = state.collectiviteState.items.find((item) => item.id === button.dataset.collectiviteId)
        if (!selectedItem) {
          return
        }

        if (selectedItem.id === "buy-animals") {
          ui.ouvrirPopup()
          return
        }

        if (selectedItem.id === "farmers-market") {
          await ui.ouvrirPopupMarche()
          return
        }

        if (!state.currentFarmId) {
          ui.setCollectiviteFeedback("Connecte-toi pour acheter cet objet.")
          return
        }

        if (constants.CARE_ITEM_TO_API_TYPE[selectedItem.id]) {
          try {
            const farmData = await api.acheterObjetEntretien(constants.CARE_ITEM_TO_API_TYPE[selectedItem.id])
            ui.applyFarmData(farmData)
            ui.setCollectiviteFeedback(`${selectedItem.label} ajoute au stock.`)
            return
          } catch (error) {
            console.error("Erreur lors de l'achat d'objet :", error)
            ui.setCollectiviteFeedback(error.message || "Achat impossible.")
            return
          }
        }

        ui.setCollectiviteFeedback(`${selectedItem.label} selectionne.`)
      })
    }

    if (dom.passDayButton) {
      dom.passDayButton.addEventListener("click", async () => {
        if (dom.passDayButton.disabled) {
          return
        }

        try {
          dom.passDayButton.disabled = true
          const farmData = await api.passerJourFerme()
          ui.applyFarmData(farmData)
          ui.renderStockTable(ui.buildStockRows(farmData))
          ui.setStockFeedback()
          ui.setCollectiviteFeedback("Le jour suivant a ete applique.")
          ui.afficherMessage("Jour suivant applique.", "succes")
        } catch (error) {
          console.error("Erreur lors du passage au jour suivant :", error)
          ui.setCollectiviteFeedback(error.message || "Impossible de passer au jour suivant.")
          ui.afficherMessage("Impossible de passer au jour suivant.", "erreur")
        } finally {
          dom.passDayButton.disabled = false
        }
      })
    }

    if (dom.elementsInterface.boutonFermer) {
      dom.elementsInterface.boutonFermer.addEventListener("click", () => ui.fermerPopup())
    }

    if (dom.boutonFermerMarche) {
      dom.boutonFermerMarche.addEventListener("click", ui.fermerPopupMarche)
    }

    dom.closeMarcheTargets.forEach((target) => {
      target.addEventListener("click", ui.fermerPopupMarche)
    })

    dom.marcheTabButtons.forEach((button) => {
      button.addEventListener("click", () => {
        ui.setMarcheTab(button.dataset.marcheTab)
      })
    })

    if (dom.popupMarche) {
      dom.popupMarche.addEventListener("click", async (event) => {
        const buyButton = event.target.closest("[data-market-buy]")
        const communitySellButton = event.target.closest("[data-community-sell]")

        if (communitySellButton) {
          const product = communitySellButton.dataset.communitySell

          try {
            ui.setMarcheFeedback(dom.marcheCollectiviteFeedback)
            const farmData = await api.vendreACollectivite(product, 1)
            ui.applyFarmData(farmData)
            ui.renderStockTable(ui.buildStockRows(farmData))
            ui.setStockFeedback()
            ui.afficherMessage("Vente a la collectivite validee.", "succes")
            ui.setMarcheFeedback(dom.marcheCollectiviteFeedback, "Vente effectuee avec succes.", "is-success")
          } catch (error) {
            console.error("Erreur lors de la vente a la collectivite :", error)
            ui.setMarcheFeedback(dom.marcheCollectiviteFeedback, error.message || "Vente impossible.", "is-error")
          }

          return
        }

        if (!buyButton) {
          return
        }

        const offerId = buyButton.dataset.marketBuy
        const input = dom.popupMarche.querySelector(`[data-market-buy-qty="${offerId}"]`)
        const quantity = Math.max(1, Number.parseInt(input?.value, 10) || 1)

        try {
          ui.setMarcheFeedback(dom.marcheAchatFeedback)
          const farmData = await api.acheterOffreMarche(offerId, quantity)
          ui.applyFarmData(farmData)
          ui.renderStockTable(ui.buildStockRows(farmData))
          ui.setStockFeedback()
          ui.afficherMessage("Achat du marche valide.", "succes")
          ui.setMarcheFeedback(dom.marcheAchatFeedback, "Achat effectue avec succes.", "is-success")
        } catch (error) {
          console.error("Erreur lors de l'achat sur le marche :", error)
          ui.setMarcheFeedback(dom.marcheAchatFeedback, error.message || "Achat impossible.", "is-error")
        }
      })
    }

    if (dom.marcheVenteProduit) {
      dom.marcheVenteProduit.addEventListener("change", () => {
        ui.setMarcheFeedback(dom.marcheVenteFeedback)
        ui.updateMarcheVenteSelection()
      })
    }

    if (dom.marcheVenteQuantite) {
      dom.marcheVenteQuantite.addEventListener("input", () => {
        state.marketSaleState.quantity = Math.max(0, Number.parseInt(dom.marcheVenteQuantite.value, 10) || 0)
        ui.updateMarcheVenteSelection()
      })
    }

    if (dom.marcheVentePrix) {
      dom.marcheVentePrix.addEventListener("input", () => {
        state.marketSaleState.unitPrice = Math.max(1, Number.parseInt(dom.marcheVentePrix.value, 10) || 1)
        dom.marcheVentePrix.value = String(state.marketSaleState.unitPrice)
      })
    }

    if (dom.marcheVenteSubmit) {
      dom.marcheVenteSubmit.addEventListener("click", async () => {
        if (dom.marcheVenteSubmit.disabled) {
          return
        }

        const product = dom.marcheVenteProduit?.value || ""
        const quantity = Math.max(1, Number.parseInt(dom.marcheVenteQuantite?.value, 10) || 1)
        const unitPrice = Math.max(1, Number.parseInt(dom.marcheVentePrix?.value, 10) || 1)

        state.marketSaleState.selectedProduct = product
        state.marketSaleState.quantity = quantity
        state.marketSaleState.unitPrice = unitPrice

        try {
          dom.marcheVenteSubmit.disabled = true
          ui.setMarcheFeedback(dom.marcheVenteFeedback)
          const farmData = await api.publierOffreMarche(product, quantity, unitPrice)
          ui.applyFarmData(farmData)
          ui.renderStockTable(ui.buildStockRows(farmData))
          ui.setStockFeedback()
          ui.afficherMessage("Offre du marche publiee.", "succes")
          ui.setMarcheFeedback(dom.marcheVenteFeedback, "Offre publiee avec succes.", "is-success")
        } catch (error) {
          console.error("Erreur lors de la mise en vente :", error)
          ui.setMarcheFeedback(dom.marcheVenteFeedback, error.message || "Mise en vente impossible.", "is-error")
        } finally {
          ui.updateMarcheVenteSelection()
        }
      })
    }

    if (dom.marcheCollectiviteProduit) {
      dom.marcheCollectiviteProduit.addEventListener("change", () => {
        ui.setMarcheFeedback(dom.marcheCollectiviteFeedback)
        ui.updateMarcheCollectiviteSelection()
      })
    }

    if (dom.marcheCollectiviteQuantite) {
      dom.marcheCollectiviteQuantite.addEventListener("input", () => {
        state.communitySaleState.quantity = Math.max(0, Number.parseInt(dom.marcheCollectiviteQuantite.value, 10) || 0)
        ui.updateMarcheCollectiviteSelection()
      })
    }

    if (dom.marcheCollectiviteSubmit) {
      dom.marcheCollectiviteSubmit.addEventListener("click", async () => {
        if (dom.marcheCollectiviteSubmit.disabled) {
          return
        }

        const product = dom.marcheCollectiviteProduit?.value || ""
        const quantity = Math.max(1, Number.parseInt(dom.marcheCollectiviteQuantite?.value, 10) || 1)

        state.communitySaleState.selectedProduct = product
        state.communitySaleState.quantity = quantity

        try {
          dom.marcheCollectiviteSubmit.disabled = true
          ui.setMarcheFeedback(dom.marcheCollectiviteFeedback)
          // La collectivite ne cree pas d'offre : la vente est immediate.
          const farmData = await api.vendreACollectivite(product, quantity)
          ui.applyFarmData(farmData)
          ui.renderStockTable(ui.buildStockRows(farmData))
          ui.setStockFeedback()
          ui.afficherMessage("Vente a la collectivite validee.", "succes")
          ui.setMarcheFeedback(dom.marcheCollectiviteFeedback, "Vente effectuee avec succes.", "is-success")
        } catch (error) {
          console.error("Erreur lors de la vente a la collectivite :", error)
          ui.setMarcheFeedback(dom.marcheCollectiviteFeedback, error.message || "Vente impossible.", "is-error")
        } finally {
          ui.updateMarcheCollectiviteSelection()
        }
      })
    }

    document.querySelectorAll(".btn-acheter").forEach((button) => {
      button.addEventListener("click", () => {
        traiterAchat(button.dataset.animal)
      })
    })

    if (dom.clapierContainer) {
      dom.clapierContainer.addEventListener("click", ui.openClapierModal)
      dom.clapierContainer.addEventListener("keydown", (event) => {
        if (event.key === "Enter" || event.key === " ") {
          event.preventDefault()
          ui.openClapierModal()
        }
      })
    }

    if (dom.poulaillerContainer) {
      dom.poulaillerContainer.addEventListener("click", (event) => {
        if (event.target === dom.poulaillerContainer || event.target.classList.contains("animals-empty")) {
          ui.openPoulaillerListModal()
        }
      })
    }

    document.addEventListener("click", () => {
      ui.setStockPanelOpen(false)
    })

    document.addEventListener("keydown", (event) => {
      if (event.key === "Escape") {
        ui.closeLoginModal()
        ui.setStockPanelOpen(false)
        ui.closeAllModals()
      }
    })

    ui.initializeAnimalZoneNavigation()
    ui.loadCareInventoryState()
    ui.initializeFarmState()
    ui.initializeStockPanel()
    ui.initializeCollectivitePanel()
    ui.updateClock()
    window.setInterval(ui.updateClock, 1000)

    if (state.farmRefreshIntervalId) {
      window.clearInterval(state.farmRefreshIntervalId)
    }

    state.farmRefreshIntervalId = window.setInterval(ui.refreshFarmTimeAndProduction, 10000)
  }

  bindStaticEvents()
  ui.classement()
  window.addEventListener("DOMContentLoaded", async () => {
    try {
        const res = await fetch("/api/me");
        if (res.ok) {
            const user = await res.json();
            await loginWithGit(user)
        }
        else{
          ui.showLoginScreen()
        }
    } catch (e) {
        console.error("Impossible de joindre le serveur :", e);
        ui.showLoginScreen()
    }
    ui.classement;
  });
  document.addEventListener("DOMContentLoaded", bindDomContentLoadedEvents)
})(window)
