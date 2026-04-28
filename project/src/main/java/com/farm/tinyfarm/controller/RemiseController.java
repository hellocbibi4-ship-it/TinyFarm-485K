/*
 * Contrôleur REST gérant les opérations remise de TinyFarm et exposant les points d'API correspondants.
 */



package com.farm.tinyfarm.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.farm.tinyfarm.model.Remise;
import com.farm.tinyfarm.model.TypeStock;
import com.farm.tinyfarm.service.RemiseService;

@RestController
@RequestMapping("/api/remise") // L'URL de base
public class RemiseController {
    private final RemiseService remiseService;

    //Constructeur
    public RemiseController(RemiseService remiseService) {
        this.remiseService = remiseService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Remise> getRemiseByFermeId(@PathVariable Integer id) {
        Remise remise = remiseService.getById(id);
        return ResponseEntity.ok(remise);
    }

    @PatchMapping("/{id}/ajouter-stock")
    public ResponseEntity<Remise> ajouterStock(@PathVariable Integer id, @RequestParam Integer montant, @RequestParam TypeStock stock){
        remiseService.ajouterStock(id, stock, montant);
        Remise remiseMAJ = remiseService.getById(id);
        return ResponseEntity.ok(remiseMAJ);
    }

    @PatchMapping("/{id}/retirer-stock")
    public ResponseEntity<Remise> retirerStock(@PathVariable Integer id, @RequestParam Integer montant, @RequestParam TypeStock stock){
        remiseService.retirerStock(id, stock, montant);
        Remise remiseMAJ = remiseService.getById(id);
        return ResponseEntity.ok(remiseMAJ);
    }

}//Class

