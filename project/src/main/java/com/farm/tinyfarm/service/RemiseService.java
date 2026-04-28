/*
 * Couche métier gérant la logique de remise pour TinyFarm.
 */



package com.farm.tinyfarm.service;

import org.springframework.stereotype.Service;

import com.farm.tinyfarm.model.Ferme;
import com.farm.tinyfarm.model.Remise;
import com.farm.tinyfarm.model.TypeStock;
import com.farm.tinyfarm.repository.FermeRepository;
import com.farm.tinyfarm.repository.RemiseRepository;

import jakarta.transaction.Transactional;

@Service
public class RemiseService {

    private static final int COUT_NOURRITURE = 5;
    private static final int COUT_PAILLE = 5;
    private static final int COUT_EAU = 2;
    private static final int COUT_SAVON = 3;
    private static final int COUT_SERINGUE = 6;

    private final RemiseRepository remiseRepository;
    private final FermeRepository fermeRepository;
    private final FermeService fermeService;

    public RemiseService(RemiseRepository remiseRepository, FermeRepository fermeRepository, FermeService fermeService) {
        this.remiseRepository = remiseRepository;
        this.fermeRepository = fermeRepository;
        this.fermeService = fermeService;
    }

    public Remise createRemise(Integer fermeId) {
        Ferme ferme = fermeRepository.findById(fermeId)
            .orElseThrow(() -> new RuntimeException("Ferme non trouvee"));

        Remise remise = new Remise();
        remise.setFerme(ferme);
        remise.setStockOeuf(0);
        remise.setStockLait(0);
        remise.setStockLapin(0);
        remise.setStockNourriture(0);
        remise.setStockEau(0);
        remise.setStockSavon(0);
        remise.setStockSeringue(0);
        remise.setStockPaille (0);
        remise.setStockPaille(0);
        return remiseRepository.save(remise);
    }

    public Remise getOrCreateByFermeId(Integer idFerme) {
        return remiseRepository.findById(idFerme).orElseGet(() -> createRemise(idFerme));
    }

    @Transactional
    public void ajouterStock(Integer idRemise, TypeStock typeStock, int montant) {
        Remise remise = getOrCreateByFermeId(idRemise);

        if (montant <= 0) {
            throw new IllegalArgumentException("Impossible d'ajouter du stock, le montant doit etre au moins de 1");
        }

        switch (typeStock) {
            case OEUF:
                remise.setStockOeuf(remise.getStockOeuf() + montant);
                break;
            case EAU :
                int totalEau = remise.getStockEau() + montant;
                remise.setStockEau(totalEau);
                break;
            case NOURRITURE :
                int totalNourriture = remise.getStockNourriture() + montant;
                remise.setStockNourriture(totalNourriture);
                break;
            case LAIT :
                int totalLait = remise.getStockLait() + montant;
                remise.setStockLait(totalLait);
                break;
            case LAPIN :
                int totalLapin = remise.getStockLapin() + montant;
                remise.setStockLapin(totalLapin);
                break;
            case SAVON :
                remise.setStockSavon(remise.getStockSavon() + montant);
                break;
            case PAILLE:
                remise.setStockPaille(remise.getStockPaille() + montant);
                break;
            case SERINGUE:
                remise.setStockSeringue(remise.getStockSeringue() + montant);
                break;
            default:
                throw new IllegalArgumentException("Type de stock inconnu");
        }
    }

    @Transactional
    public void retirerStock(Integer idRemise, TypeStock typeStock, int montant) {
        Remise remise = getOrCreateByFermeId(idRemise);
        int montantTotal;
        if (montant <= 0) {
            throw new IllegalArgumentException("Impossible de retirer du stock, le montant doit etre au moins de 1");
        }

        switch (typeStock) {
            case OEUF:
                verifierStock(remise.getStockOeuf(), montant, "oeufs");
                remise.setStockOeuf(remise.getStockOeuf() - montant);
                break;
            case EAU :
                int totalEau = remise.getStockEau() - montant;
                remise.setStockEau(totalEau);
                break;
            case NOURRITURE :
                int totalNourriture = remise.getStockNourriture() - montant;
                remise.setStockNourriture(totalNourriture);
                break;
            case LAPIN :
                int totalLapin = remise.getStockLapin() - montant;
                remise.setStockLapin(totalLapin);
                break;
            case SAVON :
                remise.setStockSavon(remise.getStockSavon() - montant);
                break;
            case LAIT:
                verifierStock(remise.getStockLait(), montant, "lait");
                remise.setStockLait(remise.getStockLait() - montant);
                break;
            case PAILLE:
                verifierStock(remise.getStockPaille(), montant, "paille");
                remise.setStockPaille(remise.getStockPaille() - montant);
                break;
            case SERINGUE:
                verifierStock(remise.getStockSeringue(), montant, "seringues");
                remise.setStockSeringue(remise.getStockSeringue() - montant);
                break;
            default:
                throw new IllegalArgumentException("Type de stock inconnu");
        }
    }

    public Remise getById(Integer id) {
        return getOrCreateByFermeId(id);
    }

    @Transactional
    public Remise acheterObjetEntretien(Integer idFerme, TypeStock typeStock) {
        Ferme ferme = fermeRepository.findById(idFerme)
            .orElseThrow(() -> new RuntimeException("Ferme non trouvee"));
        int cout = getCout(typeStock);

        if (ferme.getSoldeEcus() < cout) {
            throw new IllegalArgumentException("Solde insuffisant");
        }

        fermeService.consommerAchatCollectivite(idFerme);
        ferme.setSoldeEcus(ferme.getSoldeEcus() - cout);
        ajouterStock(idFerme, typeStock, 1);
        fermeRepository.save(ferme);
        return getOrCreateByFermeId(idFerme);
    }

    public int getCout(TypeStock typeStock) {
        switch (typeStock) {
            case NOURRITURE:
                return COUT_NOURRITURE;
            case PAILLE:
                return COUT_PAILLE;
            case EAU:
                return COUT_EAU;
            case SAVON:
                return COUT_SAVON;
            case SERINGUE:
                return COUT_SERINGUE;
            default:
                throw new IllegalArgumentException("Aucun cout defini pour ce type de stock");
        }
    }

    public TypeStock fromProduitMarche(String produit) {
        if (produit == null || produit.isBlank()) {
            throw new IllegalArgumentException("Produit invalide");
        }

        String normalized = produit.trim().toUpperCase();
        switch (normalized) {
            case "OEUF":
            case "OEUFS":
                return TypeStock.OEUF;
            case "LAIT":
                return TypeStock.LAIT;
            case "LAPIN":
            case "LAPINS":
                return TypeStock.LAPIN;
            case "NOURRITURE":
            case "GRAIN":
                return TypeStock.NOURRITURE;
            case "EAU":
                return TypeStock.EAU;
            case "SAVON":
                return TypeStock.SAVON;
            case "SERINGUE":
                return TypeStock.SERINGUE;
            case "PAILLE":
                return TypeStock.PAILLE;
            default:
                throw new IllegalArgumentException("Produit de marche inconnu");
        }
    }

    private void verifierStock(int stockDisponible, int montant, String libelle) {
        if (stockDisponible < montant) {
            throw new IllegalArgumentException("Stock insuffisant pour " + libelle);
        }
    }
}
