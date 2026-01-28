// ========== DATA MANAGEMENT ==========
let farmData = null;

// Load data from data.json
async function loadData() {
    try {
        const response = await fetch('data.json');
        farmData = await response.json();
        console.log('Data loaded:', farmData);
        initializeApp();
    } catch (error) {
        console.error('Error loading data:', error);
        // Fallback inline data if fetch fails
        farmData = {
            "farm": {
                "name": "TINY FARM TD1",
                "owner": "Farmfarmer99"
            },
            "animals": {
                "pre": {
                    "name": "Pré",
                    "icon": "🐄",
                    "type": "Vaches",
                    "stats": [
                        { "label": "Nombre", "value": "15" },
                        { "label": "Santé", "value": "95%" },
                        { "label": "Production Lait", "value": "450L" },
                        { "label": "Âge Moyen", "value": "4.2 ans" }
                    ],
                    "actions": [
                        { "icon": "🍽️", "name": "Nourrir les vaches" },
                        { "icon": "🏥", "name": "Vérifier la santé" },
                        { "icon": "🔍", "name": "Inspecter l'enclos" },
                        { "icon": "📈", "name": "Consulter stats production" }
                    ]
                },
                "poulailler": {
                    "name": "Poulailler",
                    "icon": "🐔",
                    "type": "Poules",
                    "stats": [
                        { "label": "Nombre", "value": "45" },
                        { "label": "Santé", "value": "98%" },
                        { "label": "Production Œufs", "value": "38/jour" },
                        { "label": "Âge Moyen", "value": "2.5 ans" }
                    ],
                    "actions": [
                        { "icon": "🍽️", "name": "Nourrir les poules" },
                        { "icon": "🏥", "name": "Vérifier la santé" },
                        { "icon": "🧹", "name": "Nettoyer le poulailler" },
                        { "icon": "📊", "name": "Consulter production" }
                    ]
                },
                "clapier": {
                    "name": "Clapier",
                    "icon": "🐰",
                    "type": "Lapins",
                    "stats": [
                        { "label": "Nombre", "value": "28" },
                        { "label": "Santé", "value": "92%" },
                        { "label": "Poids Moyen", "value": "2.8 kg" },
                        { "label": "Âge Moyen", "value": "1.8 ans" }
                    ],
                    "actions": [
                        { "icon": "🥬", "name": "Nourrir les lapins" },
                        { "icon": "💧", "name": "Abreuver" },
                        { "icon": "🏥", "name": "Vérifier la santé" },
                        { "icon": "🧽", "name": "Nettoyer les cages" }
                    ]
                }
            },
            "approvisionnement": {
                "name": "Approvisionnement",
                "icon": "📦",
                "supplies": [
                    { "name": "Foin", "quantity": "2400 kg" },
                    { "name": "Céréales", "quantity": "800 kg" },
                    { "name": "Minéraux", "quantity": "150 kg" },
                    { "name": "Vitamines", "quantity": "50 L" },
                    { "name": "Eau Filtrée", "quantity": "5000 L" },
                    { "name": "Litière", "quantity": "300 kg" }
                ]
            },
            "suppliers": {
                "cooperative": {
                    "name": "Coopérative Agricole",
                    "icon": "🏪",
                    "description": "Fournisseur officiel de la coopérative agricole régionale",
                    "benefits": [
                        "Tarifs négociés pour les membres",
                        "Livraison hebdomadaire garantie",
                        "Produits certifiés biologiques",
                        "Support technique inclus"
                    ],
                    "price_level": "Modéré",
                    "delivery_time": "3-5 jours"
                },
                "marche": {
                    "name": "Marché des Éleveurs",
                    "icon": "🛒",
                    "description": "Marché direct entre éleveurs pour partage de ressources",
                    "benefits": [
                        "Tarifs compétitifs entre pairs",
                        "Livraison rapide possible",
                        "Produits frais de qualité",
                        "Réseau communautaire"
                    ],
                    "price_level": "Variable",
                    "delivery_time": "1-3 jours"
                }
            },
            "rankings": [
                { "rank": "1", "name": "Production Lait", "value": "8520L" },
                { "rank": "2", "name": "Production Œufs", "value": "1710/mois" },
                { "rank": "3", "name": "Santé Animaux", "value": "94.3%" },
                { "rank": "4", "name": "Efficacité Coûts", "value": "87%" }
            ]
        };
        initializeApp();
    }
}

// ========== NAVIGATION FUNCTIONS ==========

// Navigate between main screens
function redirectToScreen(screenNumber) {
    // Hide all screens
    const screens = document.querySelectorAll('.screen');
    screens.forEach(screen => screen.classList.add('hidden'));
    
    // Show selected screen
    const targetScreen = document.getElementById(`screen-${screenNumber}`);
    if (targetScreen) {
        targetScreen.classList.remove('hidden');
        
        // Initialize content based on screen
        if (screenNumber === 2) {
            loadRankings();
        }
    }
}

// Navigate to animal detail page
function navigateToAnimal(animalType) {
    // Hide screen 2, show screen 3
    redirectToScreen(3);
    
    // Hide all animal views
    const animalViews = document.querySelectorAll('.animal-view');
    animalViews.forEach(view => view.classList.add('hidden'));
    
    // Show selected animal view
    const targetView = document.getElementById(`animal-${animalType}`);
    if (targetView) {
        targetView.classList.remove('hidden');
    }
    
    // Update title
    const title = document.getElementById('animal-title');
    if (animalType === 'approvisionnement') {
        title.textContent = 'Approvisionnement';
        loadSupplies();
    } else if (farmData && farmData.animals && farmData.animals[animalType]) {
        const animal = farmData.animals[animalType];
        title.textContent = `${animal.icon} ${animal.name}`;
        loadAnimalStats(animalType);
        loadAnimalActions(animalType);
    }
}

// Navigate to supplier page
function navigateToSupplier(supplierType) {
    redirectToScreen(4);
    
    // Hide all supplier views
    const supplierViews = document.querySelectorAll('.supplier-view');
    supplierViews.forEach(view => view.classList.add('hidden'));
    
    // Show selected supplier view
    const targetView = document.getElementById(`${supplierType}-view`);
    if (targetView) {
        targetView.classList.remove('hidden');
    }
    
    // Load supplier details
    loadSupplierDetails(supplierType);
}

// ========== DATA LOADING FUNCTIONS ==========

// Load rankings in dashboard
function loadRankings() {
    const rankingsList = document.getElementById('rankings-list');
    if (!rankingsList || !farmData || !farmData.rankings) return;
    
    rankingsList.innerHTML = '';
    
    farmData.rankings.forEach(ranking => {
        const rankingItem = document.createElement('div');
        rankingItem.className = 'ranking-item';
        rankingItem.innerHTML = `
            <div class="ranking-badge">#${ranking.rank}</div>
            <div class="ranking-info">
                <div class="ranking-name">${ranking.name}</div>
                <div class="ranking-value">${ranking.value}</div>
            </div>
        `;
        rankingsList.appendChild(rankingItem);
    });
}

// Load animal statistics
function loadAnimalStats(animalType) {
    if (!farmData || !farmData.animals || !farmData.animals[animalType]) return;
    
    const animal = farmData.animals[animalType];
    const statsContainer = document.getElementById(`${animalType}-stats`);
    
    if (!statsContainer) return;
    
    statsContainer.innerHTML = '';
    
    animal.stats.forEach(stat => {
        const statCard = document.createElement('div');
        statCard.className = 'stat-card';
        statCard.innerHTML = `
            <div class="stat-label">${stat.label}</div>
            <div class="stat-value">${stat.value}</div>
        `;
        statsContainer.appendChild(statCard);
    });
}

// Load animal actions
function loadAnimalActions(animalType) {
    if (!farmData || !farmData.animals || !farmData.animals[animalType]) return;
    
    const animal = farmData.animals[animalType];
    const actionsContainer = document.getElementById(`${animalType}-actions`);
    
    if (!actionsContainer) return;
    
    actionsContainer.innerHTML = '';
    
    animal.actions.forEach(action => {
        const actionButton = document.createElement('button');
        actionButton.className = 'btn btn-action';
        actionButton.innerHTML = `${action.icon} ${action.name}`;
        actionButton.onclick = () => performAction(animalType, action.name);
        actionsContainer.appendChild(actionButton);
    });
}

// Load supply options
function loadSupplies() {
    if (!farmData || !farmData.approvisionnement) return;
    
    const supplyOptions = document.getElementById('supply-options');
    if (!supplyOptions) return;
    
    supplyOptions.innerHTML = '';
    
    farmData.approvisionnement.supplies.forEach(supply => {
        const supplyCard = document.createElement('div');
        supplyCard.className = 'supply-card';
        supplyCard.innerHTML = `
            <div class="supply-name">${supply.name}</div>
            <div class="supply-quantity">${supply.quantity}</div>
        `;
        supplyOptions.appendChild(supplyCard);
    });
}

// Load supplier details
function loadSupplierDetails(supplierType) {
    if (!farmData || !farmData.suppliers || !farmData.suppliers[supplierType]) return;
    
    const supplier = farmData.suppliers[supplierType];
    const detailsContainer = document.getElementById(`${supplierType}-details`);
    
    if (!detailsContainer) return;
    
    detailsContainer.innerHTML = `
        <p class="supplier-description">${supplier.description}</p>
        <div class="supplier-info">
            <div class="info-item">
                <strong>Prix:</strong> ${supplier.price_level}
            </div>
            <div class="info-item">
                <strong>Délai de livraison:</strong> ${supplier.delivery_time}
            </div>
        </div>
        <div class="supplier-benefits">
            <h4>Avantages:</h4>
            <ul>
                ${supplier.benefits.map(benefit => `<li>${benefit}</li>`).join('')}
            </ul>
        </div>
    `;
}

// ========== ACTION HANDLERS ==========

// Perform an action on animals
function performAction(animalType, actionName) {
    const messages = {
        'Nourrir les vaches': 'Les vaches ont été nourries avec succès! 🐄',
        'Vérifier la santé': 'Contrôle de santé effectué. Tous les animaux sont en bonne santé! ✅',
        'Inspecter l\'enclos': 'Inspection terminée. L\'enclos est en bon état. 🔍',
        'Consulter stats production': 'Rapport de production généré avec succès! 📊',
        'Nourrir les poules': 'Les poules ont été nourries! 🐔',
        'Nettoyer le poulailler': 'Poulailler nettoyé avec succès! ✨',
        'Consulter production': 'Production d\'œufs: tout est optimal! 📈',
        'Nourrir les lapins': 'Les lapins ont été nourris! 🐰',
        'Abreuver': 'Abreuvoirs remplis d\'eau fraîche! 💧',
        'Nettoyer les cages': 'Cages nettoyées avec succès! 🧽'
    };
    
    const message = messages[actionName] || `Action "${actionName}" effectuée avec succès!`;
    
    // Show notification
    showNotification(message);
}

// Select a supplier and place order
function selectSupplier(supplierType) {
    const supplierNames = {
        'cooperative': 'Coopérative Agricole',
        'marche': 'Marché des Éleveurs'
    };
    
    const supplierName = supplierNames[supplierType];
    showNotification(`Commande passée auprès de ${supplierName}! 📦`);
}

// Show notification
function showNotification(message) {
    // Create notification element
    const notification = document.createElement('div');
    notification.className = 'notification';
    notification.textContent = message;
    
    // Add to body
    document.body.appendChild(notification);
    
    // Trigger animation
    setTimeout(() => {
        notification.classList.add('show');
    }, 10);
    
    // Remove after 3 seconds
    setTimeout(() => {
        notification.classList.remove('show');
        setTimeout(() => {
            notification.remove();
        }, 300);
    }, 3000);
}

// ========== INITIALIZATION ==========

function initializeApp() {
    console.log('Initializing app...');
    
    // Load rankings on dashboard
    loadRankings();
    
    // Show first screen (login)
    redirectToScreen(1);
}

// Load data when page loads
document.addEventListener('DOMContentLoaded', () => {
    loadData();
});
