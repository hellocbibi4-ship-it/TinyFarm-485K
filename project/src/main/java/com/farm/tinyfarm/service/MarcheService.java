package com.farm.tinyfarm.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.farm.tinyfarm.model.Ferme;
import com.farm.tinyfarm.model.Marche;
import com.farm.tinyfarm.model.TypeStock;
import com.farm.tinyfarm.repository.FermeRepository;
import com.farm.tinyfarm.repository.MarcheRepository;

import jakarta.transaction.Transactional;

@Service
public class MarcheService {

    private final FermeRepository fermeRepository;
    private final MarcheRepository marcheRepository;
    private final RemiseService remiseService;

    public MarcheService(
        FermeRepository fermeRepository,
        MarcheRepository marcheRepository,
        RemiseService remiseService
    ) {
        this.fermeRepository = fermeRepository;
        this.marcheRepository = marcheRepository;
        this.remiseService = remiseService;
    }

    @Transactional
    public Marche create(Integer fermeId, String produit, Integer quantite, Integer prix) {
        Ferme ferme = fermeRepository.findById(fermeId)
            .orElseThrow(() -> new RuntimeException("Ferme non trouvee"));

        if (quantite == null || quantite <= 0) {
            throw new IllegalArgumentException("Impossible de proposer cette offre, la quantite doit etre superieure a 0");
        }

        if (prix == null || prix <= 0) {
            throw new IllegalArgumentException("Impossible de proposer un prix inferieur ou egal a 0");
        }

        TypeStock typeStock = remiseService.fromProduitMarche(produit);
        if (typeStock == TypeStock.LAPIN) {
            int lapinsDisponibles = ferme.getNbLapins() == null ? 0 : ferme.getNbLapins();

            if (lapinsDisponibles < quantite) {
                throw new IllegalArgumentException("Stock insuffisant pour lapins");
            }

            ferme.setNbLapins(lapinsDisponibles - quantite);
        } else {
            remiseService.retirerStock(fermeId, typeStock, quantite);
        }

        String produitNormalise = toMarcheProductValue(typeStock);
        Marche marche = marcheRepository
            .findByFerme_IdFermeAndProduitAndPrixUnitaire(fermeId, produitNormalise, prix)
            .orElseGet(() -> {
                Marche nouvelleOffre = new Marche();
                nouvelleOffre.setFerme(ferme);
                nouvelleOffre.setProduit(produitNormalise);
                nouvelleOffre.setPrix(prix);
                nouvelleOffre.setQuantite(0);
                return nouvelleOffre;
            });

        marche.setQuantite((marche.getQuantite() == null ? 0 : marche.getQuantite()) + quantite);
        return marcheRepository.save(marche);
    }

    public Marche getById(Integer id) {
        return marcheRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Offre introuvable"));
    }

    @Transactional
    public void reset() {
        marcheRepository.deleteAll();
    }

    @Transactional
    public List<Map<String, Object>> getOffresPourFront() {
        return marcheRepository.findAllByOrderByPrixUnitaireAscIdOffreAsc().stream()
            .map(offre -> Map.<String, Object>of(
                "id", offre.getIdOffre(),
                "sellerFarmId", offre.getFerme().getIdFerme(),
                "sellerName", offre.getFerme().getUtilisateur() != null && offre.getFerme().getUtilisateur().getGithubUsername() != null
                    ? offre.getFerme().getUtilisateur().getGithubUsername()
                    : offre.getFerme().getNom(),
                "product", offre.getProduit(),
                "quantity", offre.getQuantite(),
                "unitPrice", offre.getPrix()
            ))
            .toList();
    }

    @Transactional
    public void transaction(Integer idAcheteur, Integer idOffre, Integer quantite) {
        Marche marche = marcheRepository.findById(idOffre)
            .orElseThrow(() -> new RuntimeException("Offre introuvable"));

        if (quantite == null || quantite <= 0) {
            throw new IllegalArgumentException("La quantite doit etre superieure a 0.");
        }

        if (quantite > marche.getQuantite()) {
            throw new IllegalArgumentException("La quantite ne peut pas etre superieure a la disponibilite.");
        }

        Ferme acheteur = fermeRepository.findById(idAcheteur)
            .orElseThrow(() -> new RuntimeException("Acheteur introuvable"));

        if (acheteur.getIdFerme().equals(marche.getFerme().getIdFerme())) {
            throw new IllegalArgumentException("Impossible d'acheter sa propre offre.");
        }

        int montantTotal = marche.getPrix() * quantite;
        if (acheteur.getSoldeEcus() < montantTotal) {
            throw new IllegalArgumentException("Solde d'ecus insuffisant.");
        }

        TypeStock typeStock = remiseService.fromProduitMarche(marche.getProduit());
        acheteur.setSoldeEcus(acheteur.getSoldeEcus() - montantTotal);

        // Incrémenter BElev pour l'acheteur (achats sur le marché)
        acheteur.setBElev((acheteur.getBElev() != null ? acheteur.getBElev() : 0) + quantite);

        Ferme vendeur = marche.getFerme();
        vendeur.setSoldeEcus(vendeur.getSoldeEcus() + montantTotal);

        // Incrémenter VElev pour le vendeur (ventes sur le marché)
        vendeur.setVElev((vendeur.getVElev() != null ? vendeur.getVElev() : 0) + quantite);

        fermeRepository.save(acheteur);
        fermeRepository.save(vendeur);

        if (typeStock == TypeStock.LAPIN) {
            int lapinsAcheteur = acheteur.getNbLapins() == null ? 0 : acheteur.getNbLapins();
            acheteur.setNbLapins(lapinsAcheteur + quantite);
        } else {
            remiseService.ajouterStock(idAcheteur, typeStock, quantite);
        }

        int quantiteRestante = marche.getQuantite() - quantite;
        if (quantiteRestante <= 0) {
            marcheRepository.delete(marche);
        } else {
            marche.setQuantite(quantiteRestante);
            marcheRepository.save(marche);
        }
    }

    private String toMarcheProductValue(TypeStock typeStock) {
        switch (typeStock) {
            case OEUF:
                return "oeuf";
            case LAIT:
                return "lait";
            case LAPIN:
                return "lapin";
            case NOURRITURE:
                return "grain";
            case EAU:
                return "eau";
            case SAVON:
                return "savon";
            case SERINGUE:
                return "seringue";
            case PAILLE:
                return "paille";
            default:
                throw new IllegalArgumentException("Produit de marche inconnu");
        }
    }
}
