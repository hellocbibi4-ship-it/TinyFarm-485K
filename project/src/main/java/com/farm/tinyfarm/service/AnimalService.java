package com.farm.tinyfarm.service;

import com.farm.tinyfarm.repository.AnimalRepository;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;


import com.farm.tinyfarm.model.Animal;
import com.farm.tinyfarm.model.TypeAnimal;
import com.farm.tinyfarm.outils.Utilitaires;
import com.farm.tinyfarm.model.TypeRole;
import com.farm.tinyfarm.model.TypeSexe;
import com.farm.tinyfarm.model.TypeStade;

@Service
public class AnimalService {

    // Couts operations vache
    private static final int COUT_NOURRIR  = 5;
    private static final int COUT_ABREUVER = 2;
    private static final int COUT_NETTOYER = 3;
    private static final int COUT_SOIGNER  = 6;


    // Gains de poids vache
    private static final float POIDS_HERBE  = 5f;
    private static final float POIDS_PAILLE = 3f;
    private static final float POIDS_EAU    = 1f;  // uniquement si combinée à de la nourriture
    private static final float POIDS_MAX    = 750f;

    // Production de lait
    private static final int LITRES_PREMIERE_TRAITE  = 4;
    private static final int LITRES_DEUXIEME_TRAITE  = 8;

    // Seuils adulte vache
    private static final int AGE_ADULTE   = 10;//jours
    private static final float POIDS_ADULTE = 80f;//kg
    
    // Seuils malades et jeunes
    private static final int MAX_JOURS_JEUNE = 4;
    private static final int MAX_JOURS_MALADE = 4;

    private final AnimalRepository animalRepository;
    private final FermeService fermeService;

    private Utilitaires utilitaire; //Sert à appeler des fonctions de la classe Utilitaires

    public AnimalService(AnimalRepository animalRepository, FermeService fermeService){
        this.animalRepository = animalRepository;
        this.fermeService = fermeService;
    }

    private void assertVache(Animal animal) {
        if (!TypeAnimal.VACHE.equals(animal.getTypeAnimal())) {
            throw new IllegalArgumentException("ERREUR : cet animal n'est pas une vache.");
        }
    }   

    private void markAsDead(Animal animal) {
        animal.setVivant(false);
        animal.setJaugeSante(0);
        animal.setEstMalade(false);
    }

    private void verifyMortality(Animal animal) {
        if (animal.getPoids() <= 0
                || animal.getNbJoursSansNourriture() >= MAX_JOURS_JEUNE
                || animal.getNbJoursMalade() >= MAX_JOURS_MALADE) {
            markAsDead(animal);
        }
    }

    //Crée un animal de base à la naissance
    public Animal createBaseAnimal(Animal animal){
        Utilitaires.validationNom(animal.getNom()); //Appel de fonction pour entrer un nom valide
        
        animal.setAge(0);
        animal.setVivant(true);
        animal.setNbJoursSansNourriture(0);
        animal.setNbJoursSansHydratation(0);
        animal.setNbJoursMalade(0);
        animal.setEstMalade(false);
        animal.setAMange(false);
        animal.setAEteTraite(false);
        animal.setSexe(TypeSexe.INCONNU);
        
        switch (animal.getTypeAnimal()){
            case POULE :
                animal.setPoids(0.05f);
                animal.setStade(TypeStade.ENFANT);
                animal.setRole(TypeRole.ELEVAGE);
                break;
            case LAPIN : 
                animal.setPoids(0.0f);
                animal.setStade(TypeStade.BEBE);
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
   // Vérifie une poule et la fait grandir ou rétrograder si nécessaire
    public void updateChickenStatus(Animal animal){
        assert(animal.getTypeAnimal().equals(TypeAnimal.POULE));
        verifyMortality(animal);

        if (!animal.isVivant()) {
            return;
        }

        // 1. Passage à l'âge adulte à partir de 4 jours 
        if (animal.getAge() >= 4 && TypeStade.ENFANT.equals(animal.getStade())){
            animal.setStade(TypeStade.ADULTE);
        }

        // 2. Révélation du sexe dès l'âge adulte (4 jours) 
        if (animal.getAge() >= 4 && (animal.getSexe() == null || animal.getSexe().equals(TypeSexe.INCONNU))) {
            animal.setSexe(Utilitaires.generateRandomGender());
        }

        // 3. Détermination du rôle (Reproducteur/Pondeuse)
        // Doit avoir 5 jours ET peser au moins 2,5 kg 
        if (animal.getAge() >= 5 && animal.getPoids() >= 2.5f) {
            if (TypeSexe.MALE.equals(animal.getSexe())) {
                animal.setRole(TypeRole.REPRODUCTEUR);
            } else if (TypeSexe.FEMELLE.equals(animal.getSexe())) {
                animal.setRole(TypeRole.PONDEUSE);
            }
        } else {
            // Rétrogradation automatique si poids < 2,5 kg ou âge < 5 jours 
            animal.setRole(TypeRole.ELEVAGE);
        }
    }
    
    //Méthode qui augmente l'age d'une poule
   public void updateChickenAge(Animal animal){
        assert(animal.getTypeAnimal().equals(TypeAnimal.POULE));
        if (!animal.isVivant()) {
            return;
        }

        // 1. Augmenter l'âge quotidiennement 
        animal.setAge(animal.getAge() + 1);

        // 2. Gestion du jeûne et perte de poids cumulative 
        if (!animal.isAMange()) {
            animal.setNbJoursSansNourriture(animal.getNbJoursSansNourriture() + 1);
            
            int joursJeune = animal.getNbJoursSansNourriture();
            if (joursJeune == 1) {
                animal.setPoids(animal.getPoids() - 0.2f); // -0,2 kg 
            } else if (joursJeune == 2) {
                animal.setPoids(animal.getPoids() - 0.3f); // Total -0,5 kg 
            } else if (joursJeune == 3) {
                animal.setPoids(animal.getPoids() - 0.5f); // Total -1 kg 
            }
        } else {
            animal.setNbJoursSansNourriture(0); // Réinitialisation si nourri 
        }

        // 3. Gestion de la maladie (mort après 4 jours) 
        if (animal.estMalade()) {
            animal.setNbJoursMalade(animal.getNbJoursMalade() + 1);
        }

        // 4. Réinitialisation des indicateurs quotidiens
        animal.setAMange(false);
        animal.setJaugeFaim(0); 
        animal.setJaugeHydratation(0);
        
        // 5. Mise à jour du statut et vérification de la mort 
        updateChickenStatus(animal);
        verifyMortality(animal);

        animalRepository.save(animal);
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
        
        if (!animal.isVivant()) {
            throw new IllegalStateException("ERREUR : la poule est morte.");
        }

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
        animalRepository.save(animal);
    }

    public void hydraterPoule(Animal animal){
        assert(animal.getTypeAnimal().equals(TypeAnimal.POULE));
        
        if (!animal.isVivant()) {
            throw new IllegalStateException("ERREUR : la poule est morte.");
        }

        if (animal.getJaugeFaim() == 100){throw new IllegalCallerException ("ERREUR : La poule ne peut manger qu'une fois par jour");}
        
        animal.setJaugeHydratation(100); 

        if(animal.getJaugeFaim() == 100) { //Cas ou la poule a mangé
            animal.setPoids((float) (animal.getPoids() + 0.15));
            updateChickenStatus(animal);
        }

        //Retrait d'écus
        Integer id = animal.getFerme().getIdFerme();
        fermeService.retirerEcus(id, 1);
        animalRepository.save(animal);
    }

    @Transactional
    public void soignerPoule(Animal animal){
        assert(animal.getTypeAnimal().equals(TypeAnimal.POULE));
        
        if (!animal.isVivant()) {
            throw new IllegalStateException("ERREUR : la poule est morte.");
        }
        
        if (!animal.estMalade()) {
            throw new IllegalCallerException("ERREUR : la poule ne peut pas être soignée si elle n'est pas malade");
        }
        animal.setJaugeSante(100);
        // Retrait d'écus
        Integer id = animal.getFerme().getIdFerme();
        fermeService.retirerEcus(id, 6);
        animalRepository.save(animal);
    }

    //TODO
    //Méthode qui actualise le statut d'un lapin en fonction de son age
    //Doit aussi choisir un sexe une fois l'age adulte atteint (voir méthode pour la poule)
    //PRE : l'animal doit être un lapin
   public void updateRabbitStatus(Animal animal) {
        if (!TypeAnimal.LAPIN.equals(animal.getTypeAnimal())) {
            throw new IllegalArgumentException("ERREUR : cet animal n'est pas un lapin.");
        }
        
        if (!animal.isVivant()) {
            return;
        }

        int age = animal.getAge();

        // Les gros lapereaux deviennent des adultes à l'âge 3
        if (age >= 3) {
            animal.setStade(TypeStade.ADULTE); 
            
            // Le sexe est révélé uniquement à l'âge adulte 
            if (animal.getSexe() == null || TypeSexe.INCONNU.equals(animal.getSexe())) {
                animal.setSexe(Utilitaires.generateRandomGender());
            }
        } else {
            // Sexe caché pour tous les lapereaux 
            animal.setSexe(TypeSexe.INCONNU); 
            
            // Évolution dans la série des lapereaux 
            if (age == 0) {
                animal.setStade(TypeStade.BEBE);
            } else if (age == 1) {
                animal.setStade(TypeStade.PETIT);
            } else if (age == 2) {
                animal.setStade(TypeStade.GROS);
            }
        }
    }
    
    //TODO
    //Méthode qui augmente l'age d'un lapin
    //Si un lapin n'est pas nourri, il ne change pas d'age
    //PRE : l'animal doit être un lapin
    public void updateRabbitAge(Animal animal) {
        if (!TypeAnimal.LAPIN.equals(animal.getTypeAnimal())) {
            throw new IllegalArgumentException("ERREUR : cet animal n'est pas un lapin.");
        }
        
        if (!animal.isVivant()) {
            return;
        }

        // 1. Condition stricte : ils ne grandissent QUE s'ils sont nourris ET abreuvés
        if (animal.isAMange() && animal.getJaugeHydratation() == 100) {
            animal.setAge(animal.getAge() + 1);
        }

        // 2. Suivi des jours sans nourriture pour la mortalité
        if (!animal.isAMange()) {
            animal.setNbJoursSansNourriture(animal.getNbJoursSansNourriture() + 1);
        } else {
            animal.setNbJoursSansNourriture(0);
        }

        // 3. Suivi de la maladie
        if (animal.estMalade()) {
            animal.setNbJoursMalade(animal.getNbJoursMalade() + 1);
        }

        // 4. Réinitialisation des actions journalières
        animal.setAMange(false);
        animal.setJaugeFaim(0);
        animal.setJaugeHydratation(0);

        // 5. Mise à jour du statut (Bébé -> Petit -> Gros -> Adulte)
        updateRabbitStatus(animal);

        // 6. Sauvegarde
        animalRepository.save(animal);
    }

    //TODO
    //méthode qui actualise le statut d'une vache en fonction de son age et de son poids
    //(adulte quand 80 kg et 10 jours)
    //Meurt quand malade 4 jours de suite
    //PRE : l'animal doit être une vache
    public void updateCowStatus(Animal animal) {
        assertVache(animal);

        if (TypeStade.ENFANT.equals(animal.getStade())
                && animal.getAge() >= AGE_ADULTE
                && animal.getPoids() >= POIDS_ADULTE) {
            animal.setStade(TypeStade.ADULTE);
        }
    }

    @Transactional
    public void nettoyer(Animal animal) {
    assertVache(animal);

    if (animal.getJaugeProprete() == 100) {
        throw new IllegalStateException(
            "ERREUR : La vache est déjà propre.");
    }

    animal.setJaugeProprete(100);

    fermeService.retirerEcus(animal.getFerme().getIdFerme(), COUT_NETTOYER);
    animalRepository.save(animal);
}

    //TODO
    //Méthode qui ajoute du poids a une vache (poids max 750 kg)
    public void updateCowWeight(Animal animal, float addWeight) {
        assertVache(animal);
        if (animal.getPoids() + addWeight < POIDS_MAX) {
            animal.setPoids(animal.getPoids() + addWeight);
        } else {
            animal.setPoids(POIDS_MAX);
        }
    }

    //Méthodes qui nourrissent une vache (+5kg si herbe, +3kg si paille)
    //(L'eau fait prendre 1 kg mais seulement si la vache a déja mangé)
    //PRE : La vache ne doit pas avoir déja mangé dans la journée
    @Transactional
    public void nourrirHerbe(Animal animal) {
        assertVache(animal);

        if (animal.isAMange()) {
            throw new IllegalStateException(
                "ERREUR : La vache ne peut manger qu'une fois par jour.");
        }
        
        updateCowWeight(animal, POIDS_HERBE);
        animal.setJaugeFaim(100);
        animal.setAMange(true);

        fermeService.retirerEcus(animal.getFerme().getIdFerme(), COUT_NOURRIR);
        animalRepository.save(animal);
    }

    @Transactional
    public void nourrirPaille(Animal animal) {
        assertVache(animal);

        if (animal.isAMange()) {
            throw new IllegalStateException(
                "ERREUR : La vache ne peut manger qu'une fois par jour.");
        }

        updateCowWeight(animal, POIDS_PAILLE);
        animal.setJaugeFaim(100);
        animal.setAMange(true);

        fermeService.retirerEcus(animal.getFerme().getIdFerme(), COUT_NOURRIR);
        animalRepository.save(animal);
    }

    //TODO
    //Méthode qui abreuve une vache
    @Transactional
    public void abreuverVache(Animal animal) {
        assertVache(animal);

        if (animal.getJaugeHydratation() == 100) {
            throw new IllegalStateException(
                "ERREUR : La vache est déjà pleinement hydratée.");
        }

        animal.setJaugeHydratation(100);

        // L'eau seule ne fait pas grossir, seulement combinée à de la nourriture
        if (animal.isAMange()) {
            updateCowWeight(animal, POIDS_EAU);
        }

        fermeService.retirerEcus(animal.getFerme().getIdFerme(), COUT_ABREUVER);
        animalRepository.save(animal);
    }

    //TODO
    //Méthode qui fait produire du lait à une vache et l'ajoute dans la remise
    //Adulte : 8L à 6H et 18H si la vache n'est pas traite, 4L sinon (Possibilité d'ajouter attributs estTraite)
    //PRE l'animal doit être une vache non sale et nourrie
    @Transactional
    public int produireLait(Animal animal) {
        assertVache(animal);

        if (!TypeStade.ADULTE.equals(animal.getStade())) {
            return 0;
        }
        if (!animal.isAMange()) {
            return 0;
        }
        if (animal.getJaugeProprete() < 100 || animal.estMalade()) {
            return 0;
        }

        int litres;
        if (!animal.isAEteTraite()) {
            litres = LITRES_PREMIERE_TRAITE;
            animal.setAEteTraite(true);
        } else {
            litres = LITRES_DEUXIEME_TRAITE;
        }

        animalRepository.save(animal);
        return litres;
    }

    //Méthode pour soigner une vache 
    //PRE : La vache doit être malade et ne pas avoir été soignée dans la journée
    @Transactional
    public void soigner(Animal animal) {
    assertVache(animal);

    if (!animal.estMalade()) {
        throw new IllegalStateException(
            "ERREUR : La vache ne peut pas être soignée si elle n'est pas malade.");
    }

    animal.setEstMalade(false);
    animal.setJaugeSante(100);

    fermeService.retirerEcus(animal.getFerme().getIdFerme(), COUT_SOIGNER);
    animalRepository.save(animal);
}


}//Class

