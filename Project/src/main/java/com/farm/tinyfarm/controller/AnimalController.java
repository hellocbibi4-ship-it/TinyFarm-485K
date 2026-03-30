package com.farm.tinyfarm.controller;

import com.farm.tinyfarm.model.Animal;
import com.farm.tinyfarm.service.AnimalService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/animaux")
public class AnimalController {

    private final AnimalService animalService;

    public AnimalController(AnimalService animalService) {
        this.animalService = animalService;
    }

    @PostMapping
    public ResponseEntity<Animal> creerAnimal(@RequestBody Animal animal) {
        Animal nouvelAnimal = animalService.create(animal);
        return new ResponseEntity<>(nouvelAnimal, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Animal> getById(@PathVariable Integer id) {
        Animal animal = animalService.getById(id);
        return ResponseEntity.ok(animal);
    }

    @PatchMapping("/{id}/poule/nourrir")
    public ResponseEntity<Animal> nourrirPoule(@PathVariable Integer id) {
        Animal animalMAJ = animalService.nourrirPoule(id);
        return ResponseEntity.ok(animalMAJ);
    }

    @PatchMapping("/{id}/poule/hydrater")
    public ResponseEntity<Animal> hydraterPoule(@PathVariable Integer id) {
        Animal animalMAJ = animalService.hydraterPoule(id);
        return ResponseEntity.ok(animalMAJ);
    }

    @PatchMapping("/{id}/poule/soigner")
    public ResponseEntity<Animal> soignerPoule(@PathVariable Integer id) {
        Animal animalMAJ = animalService.soignerPoule(id);
        return ResponseEntity.ok(animalMAJ);
    }

    @PatchMapping("/{id}/vache/nettoyer")
    public ResponseEntity<Animal> nettoyerVache(@PathVariable Integer id) {
        Animal animalMAJ = animalService.nettoyer(id);
        return ResponseEntity.ok(animalMAJ);
    }

    @PatchMapping("/{id}/vache/nourrir-herbe")
    public ResponseEntity<Animal> nourrirHerbe(@PathVariable Integer id) {
        Animal animalMAJ = animalService.nourrirHerbe(id);
        return ResponseEntity.ok(animalMAJ);
    }

    @PatchMapping("/{id}/vache/nourrir-paille")
    public ResponseEntity<Animal> nourrirPaille(@PathVariable Integer id) {
        Animal animalMAJ = animalService.nourrirPaille(id);
        return ResponseEntity.ok(animalMAJ);
    }

    @PatchMapping("/{id}/vache/abreuver")
    public ResponseEntity<Animal> abreuverVache(@PathVariable Integer id) {
        Animal animalMAJ = animalService.abreuverVache(id);
        return ResponseEntity.ok(animalMAJ);
    }

    @PatchMapping("/{id}/vache/soigner")
    public ResponseEntity<Animal> soignerVache(@PathVariable Integer id) {
        Animal animalMAJ = animalService.soigner(id);
        return ResponseEntity.ok(animalMAJ);
    }

    @PatchMapping("/{id}/vache/produire-lait")
    public ResponseEntity<String> produireLait(@PathVariable Integer id) {
        int litres = animalService.produireLait(id);
        return ResponseEntity.ok("Lait produit : " + litres + " L");
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class, IllegalCallerException.class})
    public ResponseEntity<String> handleBadRequest(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntime(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }
}
