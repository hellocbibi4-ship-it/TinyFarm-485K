package com.farm.tinyfarm.service;

import com.farm.tinyfarm.model.Ferme;
import com.farm.tinyfarm.model.Habitat;
import com.farm.tinyfarm.model.Remise;
import com.farm.tinyfarm.model.TypeHabitat;
import com.farm.tinyfarm.repository.FermeRepository;
import com.farm.tinyfarm.repository.HabitatRepository;
import com.farm.tinyfarm.repository.RemiseRepository;
import com.farm.tinyfarm.outils.Utilitaires;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Service;

@Service
public class FermeService {
    
    private final FermeRepository fermeRepository;
    private final RemiseRepository remiseRepository;
    private final HabitatRepository habitatRepository;

    //Constructeur
    public FermeService(FermeRepository fermeRepository,
                        RemiseRepository remiseRepository,
                        HabitatRepository habitatRepository) {
        this.fermeRepository = fermeRepository;
        this.remiseRepository = remiseRepository;
        this.habitatRepository = habitatRepository;
    }


    //Fonction de création d'une ferme
    public Ferme create(Ferme ferme){
        Utilitaires.validationNom(ferme.getNom());
        ferme.setScore(0);
        ferme.setSoldeEcus(1500);
        ferme.setHibernation(false);
        ferme.setDateCreation(LocalDateTime.now());
        
        // Sauvegarde de la ferme d'abord pour avoir un id
        Ferme fermeSauvee = fermeRepository.save(ferme);

        //Création de la remise.
        Remise remise = new Remise();
        remise.setFerme(fermeSauvee);
        remiseRepository.save(remise);

        // Création des habitats par défaut (PRE, POULLAILLER, CLAPIER)
        initialiserHabitats(fermeSauvee);

        return fermeSauvee;
    }

    private void initialiserHabitats(Ferme ferme) {
        Habitat pre = new Habitat(1, TypeHabitat.PRE);
        pre.setFerme(ferme);
        habitatRepository.save(pre);

        Habitat poulailler = new Habitat(20, TypeHabitat.POULLAILLER);
        poulailler.setFerme(ferme);
        habitatRepository.save(poulailler);

        Habitat clapier = new Habitat(15, TypeHabitat.CLAPIER);
        clapier.setFerme(ferme);
        habitatRepository.save(clapier);
    }

    //Procédure de suppression d'une ferme
    public void deleteById(Integer id){
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

    //Procédure de retrait d'écus à la ferme
    @Transactional
    public void retirerEcus(Integer idFerme, int montant){
        Ferme ferme = fermeRepository.findById(idFerme)
            .orElseThrow(() -> new RuntimeException("Impossible d'ajouter les écus: la ferme n'existe pas"));
        
        if (montant < 0){
            throw new IllegalArgumentException("Le montant d'écus à retirer doit être positif");
        }

        int montantTotal = ferme.getSoldeEcus() - montant;
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

    @Transactional
    public void mettreAJourferme(Ferme ferme){
        Integer fermeId = ferme.getIdFerme();
        //Calcule le nombre de jours depuis lesquels le joueur ne s'est pas connecté
        long joursAbsence = ChronoUnit.DAYS.between(ferme.getDerniereCo(), LocalDateTime.now());

        if (joursAbsence <= 0) {
            return;
        }

        if (joursAbsence >= 3) {
            hibernation(fermeId, true);
        }

        if(joursAbsence >= 50) {
            deleteById(fermeId);
        }
        //TODO
        //Faire la fonction qui fait passer un jour  si la ferme n'est pas en hibernation
        //et inglige des dégâts aux animaux selon leur niveau de santé/hydratation/faim
        //et les faire vieillir si tout va bien
        ferme.setDerniereCo(LocalDateTime.now());
        fermeRepository.save(ferme);
    }


    public Ferme getById(Integer id) {
        return fermeRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Ferme introuvable"));
    }


}
