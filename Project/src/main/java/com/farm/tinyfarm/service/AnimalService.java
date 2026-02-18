package com.farm.tinyfarm.service;

import com.farm.tinyfarm.repository.AnimalRepository;

import com.farm.tinyfarm.model.Animal;
import com.farm.tinyfarm.model.TypeAnimal;
import com.farm.tinyfarm.outils.Utilitaires;
import com. farm.tinyfarm.model.TypeRole;
import com.farm.tinyfarm.model.TypeSexe;
import com.farm.tinyfarm.model.TypeStade;

public class AnimalService {

    private final AnimalRepository animalRepository;
    private Utilitaires utilitaire; //Sert à appeler des fonctions de la classe Utilitaires

    public AnimalService(AnimalRepository animalRepository){
        this.animalRepository = animalRepository;
    }

    public Animal create(Animal animal){
        utilitaire.validationNom(animal.getNom()); //Appel de fonction pour entrer un nom valide

    }

    public void updateChickenStatus(Animal animal){
        //Met a jour un poussin
        if (animal.getNbJours() >= 4 && animal.getStade().equals(TypeStade.ENFANT)){
            animal.setStade(TypeStade.ADULTE);
            animal.setSexe(utilitaire.generateRandomGender()); 
        }
        //Met a jour une poule adulte en Reproducteur
        if (animal.getSexe() == TypeSexe.MALE && animal.getStade().equals(TypeStade.ADULTE)){
            animal.setRole(TypeRole.REPRODUCTEUR);
        }
        if(animal.getSexe() == TypeSexe.FEMELLE && animal.getStade() == TypeStade.ADULTE 
                                && animal.getNbJours() >= 5 && animal.getPoids() >= 2.5){
            animal.setRole(TypeRole.PONDEUSE);
        }
    }
    

    public void nourrirAnimal() {
    }

}//Class
