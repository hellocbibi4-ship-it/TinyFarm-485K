/*
 * Contrôleur REST gérant les opérations collectivité de TinyFarm et exposant les points d'API correspondants.
 */



package com.farm.tinyfarm.controller;

import com.farm.tinyfarm.model.TypeStock;
import com.farm.tinyfarm.service.CooperativeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cooperative")
public class CooperativeController {

    private final CooperativeService cooperativeService;

    public CooperativeController(CooperativeService cooperativeService) {
        this.cooperativeService = cooperativeService;
    }

    /**
     * Vérifier si la coopérative est ouverte
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        return ResponseEntity.ok(Map.of(
            "ouverte", cooperativeService.isOuverte()
        ));
    }

    /**
     * Acheter des articles à la coopérative
     * @param fermeId ID de la ferme
     * @param article Type d'article 
     * @param quantite Quantité à acheter
     */
    @PostMapping("/fermes/{fermeId}/acheter")
    public ResponseEntity<?> acheterArticle(
            @PathVariable Integer fermeId,
            @RequestParam TypeStock article,
            @RequestParam int quantite) {
        try {
            cooperativeService.acheterArticle(fermeId, article, quantite);
            return ResponseEntity.ok(Map.of(
                "message", "Achat réussi",
                "article", article,
                "quantite", quantite
            ));
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Vendre de la production à la coopérative
     * @param fermeId ID de la ferme
     * @param produit Type de produit à vendre
     * @param quantite Quantité à vendre
     */
    @PostMapping("/fermes/{fermeId}/vendre")
    public ResponseEntity<?> vendreProduction(
            @PathVariable Integer fermeId,
            @RequestParam TypeStock produit,
            @RequestParam int quantite) {
        try {
            cooperativeService.vendreProduction(fermeId, produit, quantite);
            return ResponseEntity.ok(Map.of(
                "message", "Vente réussie",
                "produit", produit,
                "quantite", quantite
            ));
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}
