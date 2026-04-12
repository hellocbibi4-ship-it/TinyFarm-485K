package com.farm.tinyfarm.controller;

import com.farm.tinyfarm.model.Animal;
import com.farm.tinyfarm.model.Ferme;
import com.farm.tinyfarm.model.Remise;
import com.farm.tinyfarm.model.TypeAnimal;
import com.farm.tinyfarm.model.TypeStock;
import com.farm.tinyfarm.repository.FermeRepository;
import com.farm.tinyfarm.service.FermeService;
import com.farm.tinyfarm.service.MarcheService;
import com.farm.tinyfarm.service.RemiseService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/fermes")
public class FermeController {
    private final FermeService fermeService;
    private final RemiseService remiseService;
    private final MarcheService marcheService;
    private final FermeRepository fermeRepository;

    public FermeController(
        FermeService fermeService,
        RemiseService remiseService,
        MarcheService marcheService,
        FermeRepository fermeRepository
    ) {
        this.fermeService = fermeService;
        this.remiseService = remiseService;
        this.marcheService = marcheService;
        this.fermeRepository = fermeRepository;
    }

    @PostMapping
    public ResponseEntity<Ferme> creerFerme(@RequestBody Ferme ferme) {
        Ferme nouvelleFerme = fermeService.create(ferme);
        return new ResponseEntity<>(nouvelleFerme, HttpStatus.CREATED);
    }

    @GetMapping("/classement")
    public ResponseEntity<Map<String, Object>> getClassement() {
        Map<String, Object> response = new HashMap<>();
        response.put("ranking", fermeService.getClassementData());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/front-data")
    public ResponseEntity<Map<String, Object>> getFrontData(@PathVariable Integer id) {
        // Endpoint principal du front : l'interface se reconstruit presque
        // entierement a partir de cet unique payload agrege.
        Ferme ferme = fermeService.getById(id);
        return ResponseEntity.ok(buildFrontData(ferme));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimerFerme(@PathVariable Integer id) {
        fermeService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/score")
    public ResponseEntity<Ferme> augmenterScore(@PathVariable Integer id, @RequestParam Integer montant) {
        fermeService.ajouterScore(id, montant);
        return ResponseEntity.ok(fermeService.getById(id));
    }

    @PatchMapping("/{id}/ajout-ecus")
    public ResponseEntity<Ferme> ajouterEcus(@PathVariable Integer id, @RequestParam Integer montant) {
        fermeService.ajouterEcus(id, montant);
        return ResponseEntity.ok(fermeService.getById(id));
    }

    @PatchMapping("/{id}/retirer-ecus")
    public ResponseEntity<Ferme> retirerEcus(@PathVariable Integer id, @RequestParam Integer montant) {
        fermeService.retirerEcus(id, montant);
        return ResponseEntity.ok(fermeService.getById(id));
    }

    @PostMapping("/{id}/acheter-animal")
    public ResponseEntity<Map<String, Object>> acheterAnimal(@PathVariable Integer id, @RequestParam String type) {
        Ferme fermeMAJ = fermeService.acheterAnimal(id, type);
        return ResponseEntity.ok(buildFrontData(fermeMAJ));
    }

    @PostMapping("/{id}/passer-jour")
    public ResponseEntity<Map<String, Object>> passerJour(@PathVariable Integer id) {
        Ferme fermeMAJ = fermeService.passerJour(id);
        return ResponseEntity.ok(buildFrontData(fermeMAJ));
    }

    @PostMapping("/{id}/acheter-objet-entretien")
    public ResponseEntity<Map<String, Object>> acheterObjetEntretien(@PathVariable Integer id, @RequestParam TypeStock type) {
        remiseService.acheterObjetEntretien(id, type);
        return ResponseEntity.ok(buildFrontData(fermeService.getById(id)));
    }

    @PostMapping("/{id}/collectivite/vente")
    public ResponseEntity<Map<String, Object>> vendreACollectivite(
        @PathVariable Integer id,
        @RequestParam String produit,
        @RequestParam Integer quantite
    ) {
        Ferme fermeMAJ = fermeService.vendreStockACollectivite(id, produit, quantite);
        return ResponseEntity.ok(buildFrontData(fermeMAJ));
    }

    @PostMapping("/{id}/marche/offres")
    public ResponseEntity<Map<String, Object>> creerOffreMarche(
        @PathVariable Integer id,
        @RequestParam String produit,
        @RequestParam Integer quantite,
        @RequestParam Integer prix
    ) {
        marcheService.create(id, produit, quantite, prix);
        return ResponseEntity.ok(buildFrontData(fermeService.getById(id)));
    }

    @PostMapping("/{id}/marche/achat")
    public ResponseEntity<Map<String, Object>> acheterDepuisMarche(
        @PathVariable Integer id,
        @RequestParam Integer idOffre,
        @RequestParam Integer quantite
    ) {
        marcheService.transaction(id, idOffre, quantite);
        return ResponseEntity.ok(buildFrontData(fermeService.getById(id)));
    }

    @PatchMapping("/{id}/hibernation")
    public ResponseEntity<String> changerHibernation(@PathVariable Integer id, @RequestParam boolean etat) {
        fermeService.hibernation(id, etat);
        return ResponseEntity.ok("Etat d'hibernation : " + etat);
    }

    @GetMapping("/{id}/animaux/clapier")
    public ResponseEntity<Map<String, Integer>> getClapierStatus(@PathVariable Integer id) {
        // Le clapier expose des stats de groupe au lieu de details animal par animal.
        Ferme ferme = fermeService.getById(id);
        Map<String, Integer> response = new HashMap<>();
        response.put("totalLapins", ferme.getNbLapins() == null ? 0 : ferme.getNbLapins());
        response.put("sickLapins", ferme.getNbLapinsMalades() == null ? 0 : ferme.getNbLapinsMalades());
        response.put("hungryLapins", ferme.getNbLapinsAffames() == null ? 0 : ferme.getNbLapinsAffames());
        response.put("thirstyLapins", ferme.getNbLapinsAssoiffes() == null ? 0 : ferme.getNbLapinsAssoiffes());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/animaux/{type}/status")
    public ResponseEntity<Map<String, Integer>> getAnimalTypeStatus(@PathVariable Integer id, @PathVariable String type) {
        Ferme ferme = fermeService.getById(id);
        Map<String, Integer> response = new HashMap<>();
        String normalized = type.toLowerCase().trim();

        switch (normalized) {
            case "vache":
            case "vaches":
                response.put("total", ferme.getNbVaches() == null ? 0 : ferme.getNbVaches());
                response.put("hungry", ferme.getNbVachesAffamees() == null ? 0 : ferme.getNbVachesAffamees());
                response.put("thirsty", ferme.getNbVachesAssoiffees() == null ? 0 : ferme.getNbVachesAssoiffees());
                break;
            case "poule":
            case "poules":
                response.put("total", ferme.getNbPoules() == null ? 0 : ferme.getNbPoules());
                response.put("hungry", ferme.getNbPouleAffamees() == null ? 0 : ferme.getNbPouleAffamees());
                response.put("thirsty", ferme.getNbPouleAssoiffees() == null ? 0 : ferme.getNbPouleAssoiffees());
                break;
            case "lapin":
            case "lapins":
                response.put("total", ferme.getNbLapins() == null ? 0 : ferme.getNbLapins());
                response.put("hungry", ferme.getNbLapinsAffames() == null ? 0 : ferme.getNbLapinsAffames());
                response.put("thirsty", ferme.getNbLapinsAssoiffes() == null ? 0 : ferme.getNbLapinsAssoiffes());
                break;
            default:
                return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/animaux/{type}/feed")
    public ResponseEntity<Map<String, Object>> feedAnimals(
        @PathVariable Integer id,
        @PathVariable String type,
        @RequestParam(required = false) Integer animalId
    ) {
        String normalized = type.toLowerCase().trim();

        // Les lapins sont soignes en groupe, contrairement aux poules/vaches.
        if ("lapin".equals(normalized) || "lapins".equals(normalized)) {
            List<Animal> lapins = animauxParType(id, TypeAnimal.LAPIN);
            if (lapins.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Aucun lapin dans le clapier"));
            }
            fermeService.payerActionAnimale(id, normalized, "feed");
            remiseService.retirerStock(id, TypeStock.NOURRITURE, 1);
            lapins.forEach(animal -> animal.setJaugeFaim(100));
            return ResponseEntity.ok(buildFrontData(fermeService.sauvegarderApresActionsAnimaux(id, lapins)));
        }

        Animal animal = requireIndividualAnimal(id, normalized, animalId);
        fermeService.payerActionAnimale(id, normalized, "feed");
        remiseService.retirerStock(id, "vache".equals(normalized) || "vaches".equals(normalized) ? TypeStock.PAILLE : TypeStock.NOURRITURE, 1);
        animal.setJaugeFaim(100);
        return ResponseEntity.ok(buildFrontData(fermeService.sauvegarderApresActionsAnimaux(id, List.of(animal))));
    }

    @PostMapping("/{id}/animaux/{type}/water")
    public ResponseEntity<Map<String, Object>> waterAnimals(
        @PathVariable Integer id,
        @PathVariable String type,
        @RequestParam(required = false) Integer animalId
    ) {
        String normalized = type.toLowerCase().trim();

        if ("lapin".equals(normalized) || "lapins".equals(normalized)) {
            List<Animal> lapins = animauxParType(id, TypeAnimal.LAPIN);
            if (lapins.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Aucun lapin dans le clapier"));
            }
            fermeService.payerActionAnimale(id, normalized, "water");
            remiseService.retirerStock(id, TypeStock.EAU, 1);
            lapins.forEach(animal -> animal.setJaugeHydratation(100));
            return ResponseEntity.ok(buildFrontData(fermeService.sauvegarderApresActionsAnimaux(id, lapins)));
        }

        Animal animal = requireIndividualAnimal(id, normalized, animalId);
        fermeService.payerActionAnimale(id, normalized, "water");
        remiseService.retirerStock(id, TypeStock.EAU, 1);
        animal.setJaugeHydratation(100);
        return ResponseEntity.ok(buildFrontData(fermeService.sauvegarderApresActionsAnimaux(id, List.of(animal))));
    }

    @PostMapping("/{id}/animaux/{type}/heal")
    public ResponseEntity<Map<String, Object>> healAnimals(
        @PathVariable Integer id,
        @PathVariable String type,
        @RequestParam(required = false) Integer animalId
    ) {
        String normalized = type.toLowerCase().trim();

        if ("lapin".equals(normalized) || "lapins".equals(normalized)) {
            List<Animal> lapins = animauxParType(id, TypeAnimal.LAPIN);
            if (lapins.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Aucun lapin dans le clapier"));
            }
            fermeService.payerActionAnimale(id, normalized, "heal");
            remiseService.retirerStock(id, TypeStock.SERINGUE, 1);
            lapins.forEach(animal -> {
                animal.setJaugeSante(100);
                animal.setEstMalade(false);
                animal.setJoursMaladeConsecutifs(0);
            });
            return ResponseEntity.ok(buildFrontData(fermeService.sauvegarderApresActionsAnimaux(id, lapins)));
        }

        Animal animal = requireIndividualAnimal(id, normalized, animalId);
        fermeService.payerActionAnimale(id, normalized, "heal");
        remiseService.retirerStock(id, TypeStock.SERINGUE, 1);
        animal.setJaugeSante(100);
        animal.setEstMalade(false);
        animal.setJoursMaladeConsecutifs(0);
        return ResponseEntity.ok(buildFrontData(fermeService.sauvegarderApresActionsAnimaux(id, List.of(animal))));
    }

    @PostMapping("/{id}/animaux/{type}/clean")
    public ResponseEntity<Map<String, Object>> cleanAnimals(
        @PathVariable Integer id,
        @PathVariable String type,
        @RequestParam(required = false) Integer animalId
    ) {
        String normalized = type.toLowerCase().trim();

        if ("lapin".equals(normalized) || "lapins".equals(normalized)) {
            List<Animal> lapins = animauxParType(id, TypeAnimal.LAPIN);
            if (lapins.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Aucun lapin dans le clapier"));
            }
            fermeService.payerActionAnimale(id, normalized, "clean");
            remiseService.retirerStock(id, TypeStock.SAVON, 1);
            lapins.forEach(animal -> animal.setJaugeProprete(100));
            return ResponseEntity.ok(buildFrontData(fermeService.sauvegarderApresActionsAnimaux(id, lapins)));
        }

        Animal animal = requireIndividualAnimal(id, normalized, animalId);
        fermeService.payerActionAnimale(id, normalized, "clean");
        remiseService.retirerStock(id, TypeStock.SAVON, 1);
        animal.setJaugeProprete(100);
        return ResponseEntity.ok(buildFrontData(fermeService.sauvegarderApresActionsAnimaux(id, List.of(animal))));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntime(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegal(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }

    private List<Animal> animauxParType(Integer farmId, TypeAnimal typeAnimal) {
        return fermeService.getAnimaux(farmId).stream()
            .filter(animal -> animal.getTypeAnimal() == typeAnimal)
            .toList();
    }

    private Animal requireIndividualAnimal(Integer farmId, String normalizedType, Integer animalId) {
        if (animalId == null) {
            throw new IllegalArgumentException("animalId requis pour cette action");
        }

        Animal animal = fermeService.getAnimalDeFerme(farmId, animalId);
        TypeAnimal expectedType = switch (normalizedType) {
            case "vache", "vaches" -> TypeAnimal.VACHE;
            case "poule", "poules" -> TypeAnimal.POULE;
            default -> throw new IllegalArgumentException("Type d'animal inconnu");
        };

        if (animal.getTypeAnimal() != expectedType) {
            throw new IllegalArgumentException("Cet animal ne correspond pas au type demande");
        }

        return animal;
    }

    private List<Map<String, Object>> buildAnimals(Ferme ferme) {
        List<Map<String, Object>> animals = new ArrayList<>();
        for (Animal animal : fermeService.getAnimaux(ferme.getIdFerme())) {
            animals.add(createAnimalData(animal));
        }
        return animals;
    }

    private Map<String, Object> createAnimalData(Animal sourceAnimal) {
        // On transforme ici l'entite JPA en structure simple, stable
        // et orientee UI pour limiter la logique de mapping dans le front.
        Map<String, Object> animal = new HashMap<>();
        animal.put("id", "animal-" + sourceAnimal.getIdAnimal());
        animal.put("idAnimal", sourceAnimal.getIdAnimal());
        animal.put("name", sourceAnimal.getNom());
        animal.put("type", normalizeType(sourceAnimal.getTypeAnimal()));
        animal.put("typeLabel", typeLabel(sourceAnimal.getTypeAnimal()));
        animal.put("homeLabel", homeLabel(sourceAnimal.getTypeAnimal()));
        animal.put("img", imageName(sourceAnimal.getTypeAnimal()));
        animal.put("weight", sourceAnimal.getPoids());
        animal.put("age", sourceAnimal.getAge());
        animal.put("stage", sourceAnimal.getStade() == null ? "Adulte" : sourceAnimal.getStade().name());
        animal.put("isSick", sourceAnimal.estMalade());
        animal.put("health", sourceAnimal.getJaugeSante());
        animal.put("hunger", sourceAnimal.getJaugeFaim());
        animal.put("hydration", sourceAnimal.getJaugeHydratation());
        animal.put("cleanliness", sourceAnimal.getJaugeProprete());
        return animal;
    }

    private String normalizeType(TypeAnimal typeAnimal) {
        return switch (typeAnimal) {
            case VACHE -> "vache";
            case POULE -> "poule";
            case LAPIN -> "lapin";
        };
    }

    private String typeLabel(TypeAnimal typeAnimal) {
        return switch (typeAnimal) {
            case VACHE -> "Vache";
            case POULE -> "Poule";
            case LAPIN -> "Lapin";
        };
    }

    private String homeLabel(TypeAnimal typeAnimal) {
        return switch (typeAnimal) {
            case VACHE -> "Paturage";
            case POULE -> "Poulailler";
            case LAPIN -> "Clapier";
        };
    }

    private String imageName(TypeAnimal typeAnimal) {
        return switch (typeAnimal) {
            case VACHE -> "vache.png";
            case POULE -> "poule.png";
            case LAPIN -> "lapin.png";
        };
    }

    private Map<String, Object> buildFrontData(Ferme ferme) {
        // Ce payload regroupe en une seule reponse tout ce dont le front
        // a besoin apres une action utilisateur.
        Remise remise = remiseService.getById(ferme.getIdFerme());
        Map<String, Object> response = new HashMap<>();
        response.put("farmId", ferme.getIdFerme());
        response.put("farmName", ferme.getNom());
        response.put("cash", ferme.getSoldeEcus());
        response.put("score", ferme.getScore());
        response.put("animals", buildAnimals(ferme));
        response.put("stockInventory", buildStockInventory(ferme, remise));
        response.put("careInventory", buildCareInventory(remise));
        response.put("gameTime", buildGameTime(ferme));
        response.put("rabbitHealth", Map.of(
            "totalLapins", ferme.getNbLapins() == null ? 0 : ferme.getNbLapins(),
            "sickLapins", ferme.getNbLapinsMalades() == null ? 0 : ferme.getNbLapinsMalades()
        ));
        response.put("communityItems", buildCommunityItems());
        response.put("communityPurchases", buildCommunityPurchases(ferme));
        response.put("ranking", fermeService.getClassementData());
        response.put("marketOffers", marcheService.getOffresPourFront());
        return response;
    }

    private Map<String, Object> buildGameTime(Ferme ferme) {
        Map<String, Object> gameTime = new HashMap<>();
        gameTime.put("day", ferme.getJourActuel() == null ? 1 : ferme.getJourActuel());
        gameTime.put("hours", 0);
        gameTime.put("minutes", 0);
        gameTime.put("seconds", 0);
        gameTime.put("realSecondsPerDay", FermeService.GAME_DAY_DURATION_SECONDS);
        return gameTime;
    }

    private Map<String, Object> buildCommunityPurchases(Ferme ferme) {
        Map<String, Object> communityPurchases = new HashMap<>();
        int remaining = ferme.getAchatsCollectiviteRestants() == null
            ? FermeService.DAILY_COMMUNITY_PURCHASE_LIMIT
            : ferme.getAchatsCollectiviteRestants();
        communityPurchases.put("remaining", remaining);
        communityPurchases.put("maxPerDay", FermeService.DAILY_COMMUNITY_PURCHASE_LIMIT);
        return communityPurchases;
    }

    private List<Map<String, Object>> buildStockInventory(Ferme ferme, Remise remise) {
        List<Map<String, Object>> rows = new ArrayList<>();
        rows.add(createStockRow("Produits", "Oeufs", remise.getStockOeuf()));
        rows.add(createStockRow("Produits", "Lait", remise.getStockLait()));
        rows.add(createStockRow("Produits", "Lapins", ferme.getNbLapins()));
        rows.add(createStockRow("Entretien", "Nourriture", remise.getStockNourriture()));
        rows.add(createStockRow("Entretien", "Seau d'eau", remise.getStockEau()));
        rows.add(createStockRow("Entretien", "Bottes de paille", remise.getStockPaille()));
        rows.add(createStockRow("Entretien", "Savon", remise.getStockSavon()));
        rows.add(createStockRow("Entretien", "Seringue", remise.getStockSeringue()));
        return rows;
    }

    private Map<String, Integer> buildCareInventory(Remise remise) {
        Map<String, Integer> careInventory = new HashMap<>();
        careInventory.put("feed-bag", remise.getStockNourriture());
        careInventory.put("straw-bales", remise.getStockPaille());
        careInventory.put("syringe", remise.getStockSeringue());
        careInventory.put("water-bucket", remise.getStockEau());
        careInventory.put("soap", remise.getStockSavon());
        return careInventory;
    }

    private List<Map<String, Object>> buildCommunityItems() {
        List<Map<String, Object>> items = new ArrayList<>();
        items.add(createCommunityItem("feed-bag", "Nourriture", remiseService.getCout(TypeStock.NOURRITURE)));
        items.add(createCommunityItem("straw-bales", "Bottes de paille", remiseService.getCout(TypeStock.PAILLE)));
        items.add(createCommunityItem("syringe", "Seringue", remiseService.getCout(TypeStock.SERINGUE)));
        items.add(createCommunityItem("water-bucket", "Seau d'eau", remiseService.getCout(TypeStock.EAU)));
        items.add(createCommunityItem("soap", "Savon", remiseService.getCout(TypeStock.SAVON)));
        items.add(createShortcutItem("farmers-market", "Marche des producteurs"));
        items.add(createShortcutItem("buy-animals", "Achat d'animaux"));
        return items;
    }

    private Map<String, Object> createCommunityItem(String id, String label, Integer price) {
        Map<String, Object> item = new HashMap<>();
        item.put("id", id);
        item.put("label", label);
        item.put("price", price);
        return item;
    }

    private Map<String, Object> createShortcutItem(String id, String label) {
        Map<String, Object> item = createCommunityItem(id, label, null);
        item.put("variant", "shortcut");
        return item;
    }

    private Map<String, Object> createStockRow(String category, String label, Integer quantity) {
        Map<String, Object> row = new HashMap<>();
        row.put("category", category);
        row.put("label", label);
        row.put("quantity", quantity == null ? 0 : quantity);
        return row;
    }
}
