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
import com.farm.tinyfarm.model.Ferme;
import com.farm.tinyfarm.model.TypeAnimal;
import com.farm.tinyfarm.repository.AnimalRepository;
import com.farm.tinyfarm.repository.FermeRepository;
import com.farm.tinyfarm.service.AnimalService;
import com.farm.tinyfarm.service.FermeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AnimalController {

    private final AnimalRepository animalRepository;
    private final FermeRepository fermeRepository;
    private final AnimalService animalService;
    private final FermeService fermeService;

    private static final Map<TypeAnimal, Integer> PRIX_ACHAT = Map.of(
        TypeAnimal.VACHE, 50,
        TypeAnimal.POULE, 10,
        TypeAnimal.LAPIN, 10
    );

    public AnimalController(AnimalRepository animalRepository, FermeRepository fermeRepository,
                            AnimalService animalService, FermeService fermeService) {
        this.animalRepository = animalRepository;
        this.fermeRepository = fermeRepository;
        this.animalService = animalService;
        this.fermeService = fermeService;
    }

    @GetMapping("/fermes/{fermeId}/animaux")
    public ResponseEntity<List<Animal>> getAnimaux(@PathVariable Integer fermeId) {
        if (!fermeRepository.existsById(fermeId)) {
            return ResponseEntity.notFound().build();
        }
        List<Animal> animaux = animalRepository.findByFerme_IdFerme(fermeId);
        return ResponseEntity.ok(animaux);
    }

    @PostMapping("/fermes/{fermeId}/animaux")
    public ResponseEntity<?> acheterAnimal(@PathVariable Integer fermeId, @RequestBody Animal animal) {
        Ferme ferme = fermeRepository.findById(fermeId).orElse(null);
        if (ferme == null) {
            return ResponseEntity.notFound().build();
        }

        TypeAnimal type = animal.getTypeAnimal();
        if (type == null || !PRIX_ACHAT.containsKey(type)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Type d'animal invalide."));
        }

        int prix = PRIX_ACHAT.get(type);
        if (ferme.getSoldeEcus() < prix) {
            return ResponseEntity.badRequest().body(Map.of("error", "Solde insuffisant."));
        }

        animal.setFerme(ferme);
        animalService.createBaseAnimal(animal);
        fermeService.retirerEcus(fermeId, prix);
        Animal saved = animalRepository.save(animal);

        return ResponseEntity.status(201).body(saved);
    }

    @PatchMapping("/animaux/{id}/nourrir")
    public ResponseEntity<?> nourrir(@PathVariable Integer id) {
        Animal animal = animalRepository.findById(id).orElse(null);
        if (animal == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            switch (animal.getTypeAnimal()) {
                case POULE -> animalService.nourrirPoule(animal);
                case VACHE -> animalService.nourrirHerbe(animal);
                default -> {
                    return ResponseEntity.badRequest().body("Action non disponible pour ce type d'animal.");
                }
            }
            return ResponseEntity.ok(animal);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/animaux/{id}/abreuver")
    public ResponseEntity<?> abreuver(@PathVariable Integer id) {
        Animal animal = animalRepository.findById(id).orElse(null);
        if (animal == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            switch (animal.getTypeAnimal()) {
                case POULE -> animalService.hydraterPoule(animal);
                case VACHE -> animalService.abreuverVache(animal);
                default -> {
                    return ResponseEntity.badRequest().body("Action non disponible pour ce type d'animal.");
                }
            }
            return ResponseEntity.ok(animal);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/animaux/{id}/soigner")
    public ResponseEntity<?> soigner(@PathVariable Integer id) {
        Animal animal = animalRepository.findById(id).orElse(null);
        if (animal == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            switch (animal.getTypeAnimal()) {
                case POULE -> animalService.soignerPoule(animal);
                case VACHE -> animalService.soigner(animal);
                default -> {
                    return ResponseEntity.badRequest().body("Action non disponible pour ce type d'animal.");
                }
            }
            return ResponseEntity.ok(animal);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/animaux/{id}/nettoyer")
    public ResponseEntity<?> nettoyer(@PathVariable Integer id) {
        Animal animal = animalRepository.findById(id).orElse(null);
        if (animal == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            if (animal.getTypeAnimal() != TypeAnimal.VACHE) {
                return ResponseEntity.badRequest().body("Seules les vaches peuvent etre nettoyees.");
            }
            animalService.nettoyer(animal);
            return ResponseEntity.ok(animal);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
