package com.farm.tinyfarm.controller;

import com.farm.tinyfarm.model.Ferme;
import com.farm.tinyfarm.model.Remise;
import com.farm.tinyfarm.model.TypeStock;
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

    public FermeController(FermeService fermeService, RemiseService remiseService, MarcheService marcheService) {
        this.fermeService = fermeService;
        this.remiseService = remiseService;
        this.marcheService = marcheService;
    }

    @PostMapping
    public ResponseEntity<Ferme> creerFerme(@RequestBody Ferme ferme) {
        Ferme nouvelleFerme = fermeService.create(ferme);
        return new ResponseEntity<>(nouvelleFerme, HttpStatus.CREATED);
    }

    @GetMapping("/{id}/front-data")
    public ResponseEntity<Map<String, Object>> getFrontData(@PathVariable Integer id) {
        Ferme ferme = fermeService.mettreAJourTempsEtPonte(id);
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
        Ferme fermeMAJ = fermeService.getById(id);
        return ResponseEntity.ok(fermeMAJ);
    }

    @PatchMapping("/{id}/ajout-ecus")
    public ResponseEntity<Ferme> ajouterEcus(@PathVariable Integer id, @RequestParam Integer montant) {
        fermeService.ajouterEcus(id, montant);
        Ferme fermeMAJ = fermeService.getById(id);
        return ResponseEntity.ok(fermeMAJ);
    }

    @PatchMapping("/{id}/retirer-ecus")
    public ResponseEntity<Ferme> retirerEcus(@PathVariable Integer id, @RequestParam Integer montant) {
        fermeService.retirerEcus(id, montant);
        Ferme fermeMAJ = fermeService.getById(id);
        return ResponseEntity.ok(fermeMAJ);
    }

    @PostMapping("/{id}/acheter-animal")
    public ResponseEntity<Map<String, Object>> acheterAnimal(@PathVariable Integer id, @RequestParam String type) {
        Ferme fermeMAJ = fermeService.acheterAnimal(id, type);
        return ResponseEntity.ok(buildFrontData(fermeMAJ));
    }

    @PostMapping("/{id}/acheter-objet-entretien")
    public ResponseEntity<Map<String, Object>> acheterObjetEntretien(@PathVariable Integer id, @RequestParam TypeStock type) {
        remiseService.acheterObjetEntretien(id, type);
        Ferme fermeMAJ = fermeService.getById(id);
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
        Ferme fermeMAJ = fermeService.getById(id);
        return ResponseEntity.ok(buildFrontData(fermeMAJ));
    }

    @PostMapping("/{id}/marche/achat")
    public ResponseEntity<Map<String, Object>> acheterDepuisMarche(
        @PathVariable Integer id,
        @RequestParam Integer idOffre,
        @RequestParam Integer quantite
    ) {
        marcheService.transaction(id, idOffre, quantite);
        Ferme fermeMAJ = fermeService.getById(id);
        return ResponseEntity.ok(buildFrontData(fermeMAJ));
    }

    @PatchMapping("/{id}/hibernation")
    public ResponseEntity<String> changerHibernation(@PathVariable Integer id, @RequestParam boolean etat) {
        fermeService.hibernation(id, etat);
        String message = "Etat d'hibernation : " + etat;
        return ResponseEntity.ok(message);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntime(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegal(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }

    private List<Map<String, Object>> buildAnimals(Ferme ferme) {
        List<Map<String, Object>> animals = new ArrayList<>();
        addAnimals(animals, "Vache", "vache.png", 520, ferme.getNbVaches(), "Bella");
        addAnimals(animals, "Poule", "poule.png", 1.5, ferme.getNbPoules(), "Poule");
        addAnimals(animals, "Lapin", "lapin.png", 2.0, ferme.getNbLapins(), "Lapin");
        return animals;
    }

    private Map<String, Object> buildFrontData(Ferme ferme) {
        ferme = fermeService.mettreAJourTempsEtPonte(ferme.getIdFerme());
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
        response.put("communityItems", buildCommunityItems());
        response.put("ranking", fermeService.getClassementData());
        response.put("marketOffers", marcheService.getOffresPourFront());
        return response;
    }

    private Map<String, Object> buildGameTime(Ferme ferme) {
        Map<String, Object> gameTime = new HashMap<>();
        long elapsedSeconds = java.time.Duration.between(
            ferme.getDateCreation() == null ? java.time.LocalDateTime.now() : ferme.getDateCreation(),
            java.time.LocalDateTime.now()
        ).getSeconds();
        long dayIndex = (elapsedSeconds / FermeService.GAME_DAY_DURATION_SECONDS) + 1;
        long secondsInDay = elapsedSeconds % FermeService.GAME_DAY_DURATION_SECONDS;
        long totalGameSeconds = secondsInDay * (86400 / FermeService.GAME_DAY_DURATION_SECONDS);
        long hours = totalGameSeconds / 3600;
        long minutes = (totalGameSeconds % 3600) / 60;
        long seconds = totalGameSeconds % 60;

        gameTime.put("day", dayIndex);
        gameTime.put("hours", hours);
        gameTime.put("minutes", minutes);
        gameTime.put("seconds", seconds);
        gameTime.put("realSecondsPerDay", FermeService.GAME_DAY_DURATION_SECONDS);
        gameTime.put("eggsPerDay", ferme.getNbPoules() == null ? 0 : ferme.getNbPoules());
        return gameTime;
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
        items.add(createShortcutItem("buy-animals", "Achat animaux"));
        items.add(createShortcutItem("farmers-market", "Marche des producteurs"));
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

    private void addAnimals(
        List<Map<String, Object>> animals,
        String type,
        String img,
        double weight,
        Integer count,
        String namePrefix
    ) {
        int safeCount = count == null ? 0 : count;
        for (int index = 0; index < safeCount; index++) {
            Map<String, Object> animal = new HashMap<>();
            animal.put("name", safeCount == 1 ? namePrefix : namePrefix + " " + (index + 1));
            animal.put("type", type);
            animal.put("img", img);
            animal.put("weight", weight);
            animals.add(animal);
        }
    }
}
