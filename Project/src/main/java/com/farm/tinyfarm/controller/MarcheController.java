package com.farm.tinyfarm.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

import com.farm.tinyfarm.model.Remise;
import com.farm.tinyfarm.model.Marche;
import com.farm.tinyfarm.model.TypeStock;
import com.farm.tinyfarm.service.MarcheService;
import com.farm.tinyfarm.service.RemiseService;

@RestController
@RequestMapping("/api/marche") // L'URL de base
public class MarcheController {
    private final MarcheService marcheService;

    //Constructeur
    public MarcheController(MarcheService marcheService) {
        this.marcheService = marcheService;
    }

    @PostMapping
    public ResponseEntity<Marche> creeroffre(
        @RequestParam Integer fermeId,
        @RequestParam String produit,
        @RequestParam Integer quantite,
        @RequestParam Integer prix){
        Marche nv_offre = marcheService.create(fermeId, produit, quantite, prix);
        return ResponseEntity.ok(nv_offre);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Marche> getById(@PathVariable Integer id) {
        Marche marche = marcheService.getById(id);
        return ResponseEntity.ok(marche);
    }

    @PatchMapping("/{idFerme}/ajouter-ecus2")
    public ResponseEntity<String> ajouterEcus(@PathVariable Integer idFerme, @RequestBody Marche Offre){
        marcheService.ajouterEcus(idFerme, Offre);
        /*Marche marcheMAJ = marcheService.getById(Offre);
        marcheService.ajouterEcus(idFerme, marcheMAJ);*/
        return ResponseEntity.ok("Écus ajoutés avec succès.");
    }

    @PatchMapping("/{idFerme}/retirer-ecus2")
    public ResponseEntity<String> retirerEcus(@PathVariable Integer idFerme, @RequestBody Marche Offre){
        marcheService.retirerEcus(idFerme, Offre);
        /*Marche marcheMAJ = marcheService.getById(Offre);
        marcheService.retirerEcus(idFerme, marcheMAJ);*/
        return ResponseEntity.ok("Écus retirés avec succès.");
    }

    @PostMapping("/transaction")
    public ResponseEntity<String> Transaction(@RequestParam Integer idFerme, @RequestParam Integer idOffre, @RequestParam Integer quantite){
        Marche marcheMaj = marcheService.getById(idOffre);
        marcheService.transaction(idFerme, marcheMaj, quantite);
        return ResponseEntity.ok("Transaction effectué avec succès");
    }



    

}//Class

