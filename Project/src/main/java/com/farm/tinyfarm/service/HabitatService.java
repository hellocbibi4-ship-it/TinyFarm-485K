package com.farm.tinyfarm.service;

import org.springframework.stereotype.Service;

import com.farm.tinyfarm.repository.HabitatRepository;

@Service
public class HabitatService {

    private final HabitatRepository habitatRepository;

    public HabitatService(HabitatRepository habitatRepository){
        this.habitatRepository = habitatRepository;
    }

    //Méthodes

    //TODO
    //Méthode qui ajoute un animal dans son habitat
    //PRE : taille de listeAnimaux < capaMax
    public void ajouterAnimal() {}

    //TODO
    //Méthode qui supprime un animal dans son habitat
    public void supprimerAnimal() {}

    //TODO
    //Méthode qui lave un clapier
    //PRE : l'habitat est un clapier
    public void nettoyerClapier() {}

    //TODO
    //Méthode qui soigne un clapier
    //PRE : l'habitat est un clapier
    public void soignerClapier() {}

    //TODO
    //Méthode qui nourrit un clapier
    //PRE : l'habitat est un clapier
    public void nourrirClapier() {}

    //TODO
    //Méthode qui abreuve un clapier
    //PRE : l'habitat est un clapier
    public void abreuverClapier() {}

    //TODO
    //Méthode qui tue une partie des lapins si le clapier n'est pas nettoyé le jour même
    //PRE : le clapier est sale et date > date courante
    public void actionClapierSale() {}
    
    //TODO
    //Méthode qui tue une partie des lapins si le clapier n'est pas soigné le jour même
    //PRE : le clapier est malade et date > date courante
    public void actionClapierMalade() {}
}
