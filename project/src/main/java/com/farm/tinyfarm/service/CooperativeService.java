package com.farm.tinyfarm.service;

import com.farm.tinyfarm.model.Ferme;
import com.farm.tinyfarm.model.Remise;
import com.farm.tinyfarm.model.Cooperative;
import com.farm.tinyfarm.repository.FermeRepository;
import com.farm.tinyfarm.repository.RemiseRepository;
import com.farm.tinyfarm.repository.CooperativeRepository;
import com.farm.tinyfarm.model.TypeStock;
import java.util.List;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.springframework.stereotype.Service;

@Service
public class CooperativeService {
    
    private final FermeRepository fermeRepository;
    private final RemiseRepository remiseRepository;
    private final CooperativeRepository cooperativeRepository;
    private final RemiseService remiseService;

    //Constructeur
    public CooperativeService(FermeRepository fermeRepository, RemiseRepository remiseRepository, CooperativeRepository cooperativeRepository){
        this.fermeRepository = fermeRepository;
        this.remiseRepository = remiseRepository;
        this.cooperativeRepository = cooperativeRepository;
        this.remiseService = new RemiseService(remiseRepository, fermeRepository);
    }


    //Fonction de création d'une ferme
    public Cooperative create(String produit, Integer quantite, Integer prix){
        
        Cooperative cooperative = new Cooperative();



        if(quantite <= 0) {
            throw new IllegalArgumentException("Impossible de proposer cette offre, vous devez proposer une quantité supérieur à 0");
            }
        if( prix < 0) {
            throw new IllegalArgumentException("Impossible de proposer un prix négatif.");
            }
        Cooperative article = new Cooperative();
        article.setProduit(produit);
        article.setQuantite(quantite);
        article.setPrix(prix);

        return cooperativeRepository.save(article);
    }
    public Cooperative getById(Integer id){
    return cooperativeRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Offre introuvable"));
    }
    public boolean estOuverte() {
        LocalDateTime now = LocalDateTime.now();
        LocalTime heure = now.toLocalTime();
        int jour = now.getDayOfWeek().getValue(); 

        if (jour <= 5) { // Semaine (Lundi à Vendredi)
            return (heure.isAfter(LocalTime.of(5, 0)) && heure.isBefore(LocalTime.of(14, 0))) ||
                   (heure.isAfter(LocalTime.of(17, 0)) && heure.isBefore(LocalTime.of(20, 0))) ||
                   (heure.isAfter(LocalTime.of(22, 0)) || heure.isBefore(LocalTime.of(3, 0)));
        } else { // Week-end
            return (heure.isAfter(LocalTime.of(9, 0)) && heure.isBefore(LocalTime.of(14, 0))) ||
                   (heure.isAfter(LocalTime.of(19, 0)) || heure.isBefore(LocalTime.of(3, 0)));
        }
    }

    /*//Procédure de retrait d'écus à la ferme qui achète
    @Transactional
    public void retirerEcus(Integer idFerme, Cooperative cooperative){
        Ferme ferme = fermeRepository.findById(idFerme)
            .orElseThrow(() -> new RuntimeException("Impossible d'ajouter les écus: la ferme n'existe pas"));
        
        if (cooperative.getPrix() < 0){
            throw new IllegalArgumentException("Le montant d'écus à retirer doit être positif");
        }

        int montantTotal = ferme.getSoldeEcus() - cooperative.getPrix();
        ferme.setSoldeEcus(montantTotal);
    }*/

    @Transactional
    public void transaction(Integer idAcheteur, Integer idArticle,Integer quantite){
        if (!estOuverte()){
            throw new IllegalArgumentException("La Coopérative n'est pas ouverte. Revenez dans les horaires d'ouvertures.");
        }
        Cooperative article = cooperativeRepository.findById(idArticle)
            .orElseThrow(() -> new RuntimeException("Article introuvable en magasin."));
        if (quantite<=0){
            throw new IllegalArgumentException("La quantité doit être supérieur à 0.");
        }
        if (quantite>article.getQuantite()){
            throw new IllegalArgumentException("La quantité ne peux pas être supérieur à la disponibilité.");
        }
        Ferme acheteur = fermeRepository.findById(idAcheteur)
        .orElseThrow(() -> new RuntimeException("Acheteur introuvable"));

        Remise remiseAcheteur = remiseRepository.findById(idAcheteur)
        .orElseThrow(() -> new RuntimeException("Remise introuvable"));

        int coutTotal = article.getPrix()*quantite;
        if (acheteur.getSoldeEcus() < coutTotal) {
        throw new IllegalArgumentException("Solde d'écus insuffisant.");
        }
        acheteur.setSoldeEcus(acheteur.getSoldeEcus()-coutTotal);
        fermeRepository.save(acheteur);
        
        article.setQuantite(article.getQuantite()-quantite);
        cooperativeRepository.save(article);

        TypeStock type = TypeStock.valueOf(article.getProduit());
        remiseService.ajouterStock(idAcheteur,type,quantite);
    }
    public Cooperative getArticleById(Integer id) {
        return cooperativeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Article introuvable"));
    }
    public List<Cooperative> getCatalogue() {
        return cooperativeRepository.findAll();
    }
}
