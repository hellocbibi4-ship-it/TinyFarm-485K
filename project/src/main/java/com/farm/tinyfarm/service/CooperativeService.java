/*
 * Couche métier gérant la logique de collectivité pour TinyFarm.
 */



package com.farm.tinyfarm.service;

import com.farm.tinyfarm.model.Ferme;
import com.farm.tinyfarm.model.TypeStock;
import jakarta.transaction.Transactional;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;

@Service
public class CooperativeService {
    
    private final FermeService fermeService;
    private final RemiseService remiseService;

    // Constantes basées sur les regles du projet
    private static final int MAX_ACHATS_JOUR = 12; // Limite niveau 1
    private static final int PRIX_OEUF = 8;        // 
    private static final int PRIX_LAIT = 2;        // 2 écus par litre 
    private static final int PRIX_LAPIN = 25;      // 

    public CooperativeService(FermeService fermeService, RemiseService remiseService) {
        this.fermeService = fermeService;
        this.remiseService = remiseService;
    }

    /**
     * Vérifie les horaires d'ouverture 
     */
    public boolean isOuverte() {
        LocalDateTime now = LocalDateTime.now();
        int h = now.getHour();
        DayOfWeek day = now.getDayOfWeek();

        if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
            // Week-end : 9h-14h et 19h-3h 
            return (h >= 9 && h < 14) || (h >= 19 || h < 3);
        } else {
            // Semaine : 5h-14h, 17h-20h, 22h-3h 
            return (h >= 5 && h < 14) || (h >= 17 && h < 20) || (h >= 22 || h < 3);
        }
    }

    /**
     * Prix d'achat des articles pour le fermier 
     */
    private int getPrixArticle(TypeStock article) {
        return switch (article) {
            case SAVON -> 3;      // 
            case SERINGUE -> 6;   // 
            case PAILLE -> 3;     // Botte de paille 
            case EAU -> 2;        // Seau d'eau 
            case NOURRITURE -> 5; // Sac de nourriture 
            default -> throw new IllegalArgumentException("Cet article n'est pas vendu à la coopérative.");
        };
    }

    @Transactional
    public void acheterArticle(Integer idFerme, TypeStock article, int quantite) {
        if (!isOuverte()) {
            throw new IllegalStateException("La coopérative est fermée.");
        }

        if (quantite <= 0) {
            throw new IllegalArgumentException("La quantité doit être positive.");
        }

        Ferme ferme = fermeService.getById(idFerme);
        int prixTotal = quantite * getPrixArticle(article);

        if (ferme.getSoldeEcus() < prixTotal) {
            throw new IllegalStateException("Fonds insuffisants.");
        }

        // La limite de 12 est commune Coopérative + Marché 
        int achatsDuJour = ferme.getAchatsJour(); 
        if (achatsDuJour + quantite > MAX_ACHATS_JOUR) {
            throw new IllegalStateException("Limite d'achats journaliers dépassée (Max 12).");
        }

        fermeService.retirerEcus(idFerme, prixTotal);
        remiseService.ajouterStock(idFerme, article, quantite);
        fermeService.ajouterAchats(idFerme, quantite);
    }

    @Transactional
    public void vendreProduction(Integer idFerme, TypeStock produit, int quantite) {
        // Optionnel : vérifier l'ouverture pour la vente aussi
        if (!isOuverte()) {
            throw new IllegalStateException("La coopérative est fermée.");
        }

        if (quantite <= 0) {
            throw new IllegalArgumentException("La quantité doit être positive.");
        }

        int prixUnitaire = switch (produit) {
            case OEUF -> PRIX_OEUF;
            case LAIT -> PRIX_LAIT;
            case LAPIN -> PRIX_LAPIN;
            default -> throw new IllegalArgumentException("La coopérative ne rachète pas ce produit.");
        };

        int prixTotal = quantite * prixUnitaire;

        remiseService.retirerStock(idFerme, produit, quantite);
        fermeService.ajouterEcus(idFerme, prixTotal);
    }
}