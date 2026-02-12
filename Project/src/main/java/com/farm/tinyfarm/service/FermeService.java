package com.farm.tinyfarm.service;

import com.farm.tinyfarm.model.Ferme;
import com.farm.tinyfarm.repository.FermeRepository;
import com.farm.tinyfarm.outils.Utilitaires;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;

@Service
public class FermeService {
    
    private final FermeRepository fermeRepository;
    private Utilitaires utilitaire; //Sert à appeler des fonctions de la classe Utilitaires

    public FermeService(FermeRepository fermeRepository){
        this.fermeRepository = fermeRepository;
    }

    

    //Fonction de création d'une ferme
    public Ferme create(Ferme ferme){
        utilitaire.validationNom(ferme.getNom());
        ferme.setScore(0);
        ferme.setSoldeEcus(1500);
        ferme.setHibernation(false);
        ferme.setDateCreation(LocalDateTime.now());
        return fermeRepository.save(ferme);
    }

    //Procédure de suppression d'une ferme
    public void delete(Integer id){
        fermeRepository.deleteById(id);
    }

    //Procédure d'ajout d'écus à la ferme
    @Transactional
    public void ajouterEcus(Integer idFerme, int montant){
        Ferme ferme = fermeRepository.findById(idFerme)
            .orElseThrow(() -> new RuntimeException("Impossible d'ajouter les écus: la ferme n'existe pas"));
        
        if (montant < 0){
            throw new IllegalArgumentException("Le montant d'écus à ajouter doit être positif");
        }

        int montantTotal = ferme.getSoldeEcus() + montant;
        ferme.setSoldeEcus(montantTotal);
    }

    //Procédure d'augmentation de score de la ferme
    @Transactional
    public void ajouterScore(Integer idFerme, int montant){
        Ferme ferme = fermeRepository.findById(idFerme)
            .orElseThrow(() -> new RuntimeException("Impossible d'ajouter le score: la ferme n'existe pas"));
        
        if (montant < 0){
            throw new IllegalArgumentException("Le score à ajouter doit être positif");
        }

        int montantTotal = ferme.getScore() + montant;
        ferme.setScore(montantTotal);
    }

    @Transactional
    public void hibernation(Integer idFerme, boolean bool){
        Ferme ferme = fermeRepository.findById(idFerme)
            .orElseThrow(() -> new RuntimeException("Impossible de modifier l'état d'hibernation: la ferme n'existe pas"));

        ferme.setHibernation(bool);
    }



}
