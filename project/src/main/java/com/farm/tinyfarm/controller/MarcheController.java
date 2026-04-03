package com.farm.tinyfarm.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.farm.tinyfarm.model.Marche;
import com.farm.tinyfarm.service.MarcheService;

@RestController
@RequestMapping("/api/marche")
public class MarcheController {
    private final MarcheService marcheService;

    public MarcheController(MarcheService marcheService) {
        this.marcheService = marcheService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> creeroffre(
        @RequestParam Integer fermeId,
        @RequestParam String produit,
        @RequestParam Integer quantite,
        @RequestParam Integer prix
    ) {
        Marche nvOffre = marcheService.create(fermeId, produit, quantite, prix);
        Map<String, Object> response = new HashMap<>();
        response.put("idOffre", nvOffre.getIdOffre());
        response.put("produit", nvOffre.getProduit());
        response.put("quantite", nvOffre.getQuantite());
        response.put("prixUnitaire", nvOffre.getPrix());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Marche> getById(@PathVariable Integer id) {
        Marche marche = marcheService.getById(id);
        return ResponseEntity.ok(marche);
    }

    @PostMapping("/transaction")
    public ResponseEntity<String> transaction(
        @RequestParam Integer idFerme,
        @RequestParam Integer idOffre,
        @RequestParam Integer quantite
    ) {
        marcheService.transaction(idFerme, idOffre, quantite);
        return ResponseEntity.ok("Transaction effectuee avec succes");
    }
}
