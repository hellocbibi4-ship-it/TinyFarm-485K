package com.farm.tinyfarm.service;

import com.farm.tinyfarm.model.Ferme;
import com.farm.tinyfarm.model.Remise;
import com.farm.tinyfarm.model.Marche;
import com.farm.tinyfarm.repository.FermeRepository;
import com.farm.tinyfarm.repository.RemiseRepository;
import com.farm.tinyfarm.repository.MarcheRepository;
import com.farm.tinyfarm.model.TypeStock;
import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

@Service
public class MarcheService {
    
    private final FermeRepository fermeRepository;
    private final RemiseRepository remiseRepository;
    private final MarcheRepository marcheRepository;
    private final RemiseService remiseService;

    //Constructeur
    public MarcheService(FermeRepository fermeRepository, RemiseRepository remiseRepository, MarcheRepository marcheRepository){
        this.fermeRepository = fermeRepository;
        this.remiseRepository = remiseRepository;
        this.marcheRepository = marcheRepository;
        this.remiseService = new RemiseService(remiseRepository, fermeRepository);
    }


    //Fonction de création d'une ferme
    public Marche create(Integer fermeId, String produit, Integer quantite, Integer prix){
        Ferme ferme = fermeRepository.findById(fermeId)
            .orElseThrow(() -> new RuntimeException("Ferme non trouvée"));
        
        Marche marche = new Marche();
        marche.setFerme(ferme); // @MapsId récupère l'ID



        if(quantite <= 0) {
            throw new IllegalArgumentException("Impossible de proposer cette offre, vous devez proposer une quantité supérieur à 0");
            }
        if( prix < 0) {
            throw new IllegalArgumentException("Impossible de proposer un prix négatif.");
            }
        marche.setProduit(produit);
        marche.setQuantite(quantite);
        marche.setPrix(prix);

        return marcheRepository.save(marche);
    }
    public Marche getById(Integer id){
    return marcheRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Offre introuvable"));
    }

    //Procédure d'ajout d'écus à la ferme qui vend
    @Transactional
    public void ajouterEcus(Integer idFerme, Marche marche){
        Ferme ferme = fermeRepository.findById(idFerme)
            .orElseThrow(() -> new RuntimeException("Impossible d'ajouter les écus: la ferme n'existe pas"));
        
        if (marche.getPrix() < 0){
            throw new IllegalArgumentException("Le montant d'écus à ajouter doit être positif");
        }

        int montantTotal = ferme.getSoldeEcus() + marche.getPrix();
        ferme.setSoldeEcus(montantTotal);
    }

    //Procédure de retrait d'écus à la ferme qui achète
    @Transactional
    public void retirerEcus(Integer idFerme, Marche marche){
        Ferme ferme = fermeRepository.findById(idFerme)
            .orElseThrow(() -> new RuntimeException("Impossible d'ajouter les écus: la ferme n'existe pas"));
        
        if (marche.getPrix() < 0){
            throw new IllegalArgumentException("Le montant d'écus à retirer doit être positif");
        }

        int montantTotal = ferme.getSoldeEcus() - marche.getPrix();
        ferme.setSoldeEcus(montantTotal);
    }
    /*public Ferme getFerme(){
        return marcheRepository.ferme
        .orElseThrow(() -> new RuntimeException("Ferme introuvable"));
    }*/

    @Transactional
    public void transaction(Integer idAcheteur, Marche marche,Integer quantite){
        if (quantite<=0){
            throw new IllegalArgumentException("La quantité doit être supérieur à 0.");
        }
        if (quantite>marche.getQuantite()){
            throw new IllegalArgumentException("La quantité ne peux pas être supérieur à la disponibilité.");
        }
        Ferme acheteur = fermeRepository.findById(idAcheteur)
        .orElseThrow(() -> new RuntimeException("Acheteur introuvable"));
        Remise remiseAcheteur = remiseRepository.findById(idAcheteur)
        .orElseThrow(() -> new RuntimeException("Remise introuvable"));
        if (acheteur.getSoldeEcus() < marche.getPrix() * quantite) {
        throw new IllegalArgumentException("Solde d'écus insuffisant.");
        }
        for (int i =0; i < quantite; i++) {
        
        TypeStock typeStock = TypeStock.valueOf(marche.getProduit().toUpperCase());

        retirerEcus(idAcheteur,marche);
        ajouterEcus(marche.getFerme().getIdFerme(),marche);

        //Remise remise_ach = marche.getFerme().getRemise();
        remiseService.ajouterStock(idAcheteur, typeStock, 1);
        remiseService.retirerStock(marche.getFerme().getIdFerme(), typeStock, 1);
        }
        marche.setQuantite(marche.getQuantite()- quantite);
    }
}
