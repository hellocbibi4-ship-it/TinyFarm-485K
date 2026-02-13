package com.farm.tinyfarm.controller;

import com.farm.tinyfarm.service.FermeService;
import com.farm.tinyfarm.model.Ferme;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api/fermes") // Toutes les URLS vont commencer par ca

public class FermeController{
    private final FermeService fermeService;

    public FermeController(FermeService fermeService){
        this.fermeService = fermeService;
    }

    //Création d'une ferme
    @PostMapping
    public ResponseEntity<Ferme> creerFerme(@RequestBody Ferme ferme){
        Ferme nouvelleFerme = fermeService.create(ferme);
        return new ResponseEntity<>(nouvelleFerme, HttpStatus.CREATED);
    }

    //Suppression d'une ferme
    //Exemple d'utilisatiion : DELETE http://localhost:8080/api/fermes/(id)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimerFerme(@PathVariable Integer id){
        fermeService.delete(id);
        return ResponseEntity.noContent().build(); //Ok si 204
    }

    //Ajouter au score
    //Exemple d'utilisation : PATCH http://localhost:8080/api/fermes/(id)/score?montant=(montant)
    @PatchMapping("/{id}/score")
    public ResponseEntity<String> augmenterScore(@PathVariable Integer id, @RequestParam Integer montant){
        fermeService.ajouterScore(id, montant);
        return ResponseEntity.ok("Score mis à jour");
    }

    //Ajouter des écus
    //Expemple d'utilisation : PATCH http://localhost:8080/api/fermes/(id)/ajout-ecus?montant=(montant)
    @PatchMapping("/{id}/ajout-ecus")
    public ResponseEntity<String> ajouterEcus(@PathVariable Integer id, @RequestParam Integer montant){
        fermeService.ajouterEcus(id, montant);
        return ResponseEntity.ok("SoldeEcus mis à jour");
    }

    //Modifier l'état d'hibernation
    //Exemple d'utilisation : PATCH http://localhost:8080/api/fermes/(id)/hibernation?etat=(etat)
    @PatchMapping("/{id}/hibernation")
    public ResponseEntity<String> changerHibernation(@PathVariable Integer id, @RequestParam boolean etat){
        fermeService.hibernation(id, etat);
        String message = "Etat d'hibernation : " + etat;
        return ResponseEntity.ok(message);

    }

    //Gestion des erreurs
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntime(RuntimeException e) {
        // Si la ferme n'existe pas, on renvoie une 404 au lieu d'une 500
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegal(IllegalArgumentException e) {
        // Si le montant est négatif, on renvoie une 400 (Bad Request)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }

}
