package com.farm.tinyfarm.service;

import com.farm.tinyfarm.repository.AnimalRepository;

import ch.qos.logback.classic.pattern.Util;

import com.farm.tinyfarm.model.Animal;
import com.farm.tinyfarm.outils.Utilitaires;
public class AnimalService {

    private final AnimalRepository animalRepository;
    private Utilitaires utilitaire; //Sert à appeler des fonctions de la classe Utilitaires

    public AnimalService(AnimalRepository animalRepository){
        this.animalRepository = animalRepository;
    }
    
    public Animal create(Animal animal){
        utilitaire.validationNom(animal.getNom());
        return animal;
    }
}
