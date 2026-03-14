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
            case LAPIN : 
                animal.setPoids((float)0.0);
                animal.setStade(TypeStade.ENFANT);
                break;
            case VACHE :
                animal.setPoids((float) 1.0);
                animal.setStade(TypeStade.ENFANT);
                break;
        }
        return animal;
    }

    //TODO Ajouter dans la fonction les jours de jeune, et mort de l'animal
    //(1 jour : -2KG, 2 jours : - 0.5KG, 3 jours : 1KG, 4 jours ; meurt)
    //Si poids = 0 alors la poule meurt
    //Si malade 4 jours de suite, meurt (Ajouter attribut nombre jours malade)
    //Vérifie une poule et la fait grandir si nécessaire
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

    //TODO
    //Méthode qui actualise le statut d'un lapin en fonction de son age
    //Doit aussi choisir un sexe une fois l'age adulte atteint (voir méthode pour la poule)
    //PRE : l'animal doit être un lapin
    public void updateRabbitStatus() {}
    
    //TODO
    //Méthode qui augmente l'age d'un lapin
    //Si un lapin n'est pas nourri, il ne change pas d'age
    //PRE : l'animal doit être un lapin
    public void updateRabbitAge() {}

    //TODO
    //méthode qui actualise le statut d'une vache en fonction de son age et de son poids
    //(adulte quand 80 kg et 10 jours)
    //Meurt quand malade 4 jours de suite
    //PRE : l'animal doit être une vache
    public void updateCowStatus() {}

    //TODO
    //Méthode qui ajoute du poids a une vache (poids max 750 kg)
    public void updateCowWeight() {}

    //TODO
    //Méthode qui nourrit une vache (+5kg si herbe, +3kg si paille)
    //(L'eau fait prendre 1 kg mais seulement si la vache a déja mangé)
    //PRE : La vache ne doit pas avoir déja mangé dans la journée
    public void nourrirPoule() {}

    //TODO
    //Méthode qui abreuve une vache
    public void abreuverVache() {}

    //TODO
    //Méthode qui fait produire du lait à une vache et l'ajoute dans la remise
    //Adulte : 8L à 6H et 18H si la vache n'est pas traite, 4L sinon (Possibilité d'ajouter attributs estTraite)
    //PRE l'animal doit être une vache non sale et nourrie
    public void produireLait() {}

}//Class

