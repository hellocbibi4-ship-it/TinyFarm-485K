package com.farm.tinyfarm.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import java.util.List;
import org.springframework.web.bind.annotation.RequestBody;

import com.farm.tinyfarm.model.Remise;
import com.farm.tinyfarm.model.Cooperative;
import com.farm.tinyfarm.model.TypeStock;
import com.farm.tinyfarm.service.CooperativeService;
import com.farm.tinyfarm.service.RemiseService;

@RestController
@RequestMapping("/api/cooperative") // L'URL de base
public class CooperativeController {
    private final CooperativeService cooperativeService;

    //Constructeur
    public CooperativeController(CooperativeService cooperativeService) {
        this.cooperativeService = cooperativeService;
    }

    @PostMapping("/acheter")
    public ResponseEntity<String> acheter(@RequestParam Integer idAcheteur, 
                                          @RequestParam Integer idArticle, 
                                          @RequestParam Integer quantite) {
        try {
            cooperativeService.transaction(idAcheteur, idArticle, quantite);
            return ResponseEntity.ok("Achat réussi ! Les articles ont été ajoutés à votre remise.");
        } catch (IllegalStateException e) {
            // Cas où la coopérative est fermée
            return ResponseEntity.status(403).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @GetMapping("/statut")
    public ResponseEntity<Boolean> estOuvert(){
        return ResponseEntity.ok(cooperativeService.estOuverte());
    }
    @GetMapping("/catalogue")
    public ResponseEntity<List<?>> getCatalogue() {
        if (!cooperativeService.estOuverte()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(cooperativeService.getCatalogue());
    }

   


   

   



    

}//Class

