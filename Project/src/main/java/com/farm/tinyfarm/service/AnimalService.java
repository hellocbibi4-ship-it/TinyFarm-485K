package com.farm.tinyfarm.service;

import com.farm.tinyfarm.repository.AnimalRepository;

import jakarta.transaction.Transactional;


import com.farm.tinyfarm.model.Animal;
import com.farm.tinyfarm.model.TypeAnimal;
import com.farm.tinyfarm.outils.Utilitaires;
import com. farm.tinyfarm.model.TypeRole;
import com.farm.tinyfarm.model.TypeSexe;
import com.farm.tinyfarm.model.TypeStade;

public class AnimalService {

    private final AnimalRepository animalRepository;
    private final FermeService fermeService;

    private Utilitaires utilitaire; //Sert à appeler des fonctions de la classe Utilitaires

    public AnimalService(AnimalRepository animalRepository, FermeService fermeService){
        this.animalRepository = animalRepository;
        this.fermeService = fermeService;
    }

    //Crée un animal de base à la naissance
    public Animal createBaseAnimal(Animal animal){
        Utilitaires.validationNom(animal.getNom()); //Appel de fonction pour entrer un nom valide
        
        switch (animal.getTypeAnimal()){
            case POULE :
                animal.setPoids((float)0.5);
                animal.setStade(TypeStade.ENFANT);
                break;
            case LAPIN : //TODO
                break;
            case VACHE :
                animal.setPoids((float) 1.0);
                animal.setStade(TypeStade.ENFANT);
                break;
        }
        return animal;
    }

    public void updateChickenStatus(Animal animal){
        assert(animal.getTypeAnimal().equals(TypeAnimal.POULE));
        //Met a jour un poussin
        if (animal.getAge() >= 4 && animal.getStade().equals(TypeStade.ENFANT)){
            animal.setStade(TypeStade.ADULTE);
            animal.setSexe(utilitaire.generateRandomGender()); 
        }
        //Met a jour les poules adultes
        if (animal.getStade().equals(TypeStade.ADULTE)){

            if(animal.getAge() >= 5 && animal.getPoids() >= 2.5){

                if(animal.getSexe() == TypeSexe.MALE){
                    animal.setRole(TypeRole.REPRODUCTEUR);
                }
                else {
                    animal.setRole(TypeRole.PONDEUSE);
                }
            }
            else{
                animal.setRole(TypeRole.ELEVAGE);
            }
        }
    }
    
    //Méthode qui augmente l'age d'une poule
    public void updateChickenAge(Animal animal){
        assert(animal.getTypeAnimal().equals(TypeAnimal.POULE));
        animal.setAge(animal.getAge() + 1);
    }

    //Méthode d'augmentation du poids d'une poule
    public void updateChickenWeight(Animal animal, float addWeight){
        assert(animal.getTypeAnimal().equals(TypeAnimal.POULE));
        if (animal.getPoids() + addWeight > 3.5){
            animal.setPoids((float)3.5);
        }
        else {
            animal.setPoids(animal.getPoids() + addWeight);
        }
    }

    @Transactional
    //Méthode qui nourrit une poule
    public void nourrirPoule(Animal animal) {
        assert(animal.getTypeAnimal().equals(TypeAnimal.POULE));
        if (animal.getJaugeFaim() == 100){throw new IllegalCallerException ("ERREUR : La poule ne peut manger qu'une fois par jour");}
        
        animal.setJaugeFaim(100);
        if(animal.getJaugeHydratation() == 100) { //Cas ou la poule a bu mais sans avoir mangé au préalable
            animal.setPoids((float) (animal.getPoids() + 0.65));
        }
        else {
            animal.setPoids((float) (animal.getPoids() + 0.5));
        }
        //Retrait d'écus
        Integer id = animal.getFerme().getIdFerme();
        fermeService.retirerEcus(id, 3);

        updateChickenStatus(animal);
    }

    public void hydraterPoule(Animal animal){
        assert(animal.getTypeAnimal().equals(TypeAnimal.POULE));
        if (animal.getJaugeFaim() == 100){throw new IllegalCallerException ("ERREUR : La poule ne peut manger qu'une fois par jour");}
        
        animal.setJaugeHydratation(100); 

        if(animal.getJaugeFaim() == 100) { //Cas ou la poule a mangé
            animal.setPoids((float) (animal.getPoids() + 0.15));
            updateChickenStatus(animal);
        }

        //Retrait d'écus
        Integer id = animal.getFerme().getIdFerme();
        fermeService.retirerEcus(id, 1);
    }

    @Transactional
    public void soignerPoule(Animal animal){
        assert(animal.getTypeAnimal().equals(TypeAnimal.POULE));
        if (!animal.estMalade()) {
            throw new IllegalCallerException("ERREUR : la poule ne peut pas être soignée si elle n'est pas malade");
        }
        animal.setJaugeSante(100);
        // Retrait d'écus
        Integer id = animal.getFerme().getIdFerme();
        fermeService.retirerEcus(id, 6);
    }
    
}//Class

/*
switch (animal.get()){
    case POULE :
        break;
    case LAPIN :
        break;
    case VACHE :
        break;
}

*/
