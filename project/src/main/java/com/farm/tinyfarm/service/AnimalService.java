package com.farm.tinyfarm.service;

import com.farm.tinyfarm.repository.AnimalRepository;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

import com.farm.tinyfarm.model.Animal;
import com.farm.tinyfarm.model.TypeAnimal;
import com.farm.tinyfarm.outils.Utilitaires;
import com.farm.tinyfarm.model.TypeRole;
import com.farm.tinyfarm.model.TypeSexe;
import com.farm.tinyfarm.model.TypeStade;

@Service
public class AnimalService {

    // Couts operations vache
    private static final int COUT_NOURRIR = 5;
    private static final int COUT_ABREUVER = 2;
    private static final int COUT_NETTOYER = 3;
    private static final int COUT_SOIGNER = 6;

    // Gains de poids vache
    private static final float POIDS_HERBE = 5f;
    private static final float POIDS_PAILLE = 3f;
    private static final float POIDS_EAU = 1f; // uniquement si combinée à de la nourriture
    private static final float POIDS_MAX = 750f;

    // Production de lait
    private static final int LITRES_PREMIERE_TRAITE = 4;
    private static final int LITRES_DEUXIEME_TRAITE = 8;
    private static final int MAX_PONDEUSES_PAR_COQ = 5;

    // Seuils adulte vache
    private static final int AGE_ADULTE = 10;// jours
    private static final float POIDS_ADULTE = 80f;// kg

    // Seuils malades et jeunes
    private static final int MAX_JOURS_JEUNE = 4;
    private static final int MAX_JOURS_MALADE = 4;

    private final AnimalRepository animalRepository;
    private final FermeService fermeService;

    public AnimalService(AnimalRepository animalRepository, FermeService fermeService) {
        this.animalRepository = animalRepository;
        this.fermeService = fermeService;
    }

    private void assertVache(Animal animal) {
        if (!TypeAnimal.VACHE.equals(animal.getTypeAnimal())) {
            throw new IllegalArgumentException("ERREUR : cet animal n'est pas une vache.");
        }
    }

    private void assertPoule(Animal animal) {
        if (!TypeAnimal.POULE.equals(animal.getTypeAnimal())) {
            throw new IllegalArgumentException("ERREUR : cet animal n'est pas une poule.");
        }
    }

    private void assertLapin(Animal animal) {
        if (!TypeAnimal.LAPIN.equals(animal.getTypeAnimal())) {
            throw new IllegalArgumentException("ERREUR : cet animal n'est pas un lapin.");
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

    // Crée un animal de base à la naissance
    public Animal createBaseAnimal(Animal animal) {
        Utilitaires.validationNom(animal.getNom()); // Appel de fonction pour entrer un nom valide

        animal.setAge(0);
        animal.setVivant(true);
        animal.setNbJoursSansNourriture(0);
        animal.setNbJoursSansHydratation(0);
        animal.setNbJoursMalade(0);
        animal.setEstMalade(false);
        animal.setAMange(false);
        animal.setAEteTraite(false);
        animal.setSexe(TypeSexe.INCONNU);

        switch (animal.getTypeAnimal()) {
            case POULE:
                animal.setPoids(0.05f);
                animal.setStade(TypeStade.ENFANT);
                animal.setRole(TypeRole.ELEVAGE);
                break;
            case LAPIN:
                animal.setPoids(0.0f);
                animal.setStade(TypeStade.ENFANT);
                animal.setRole(TypeRole.ELEVAGE);
                break;
            case VACHE:
                animal.setPoids(1.0f);
                animal.setStade(TypeStade.ENFANT);
                animal.setRole(TypeRole.ELEVAGE);
                break;
        }
        return animal;
    }

    // (1 jour : -2KG, 2 jours : - 0.5KG, 3 jours : 1KG, 4 jours ; meurt)
    // Si poids = 0 alors la poule meurt
    // Si malade 4 jours de suite, meurt (Ajouter attribut nombre jours malade)
    // Vérifie une poule et la fait grandir si nécessaire
    // Vérifie une poule et la fait grandir ou rétrograder si nécessaire
    public void updateChickenStatus(Animal animal) {
        assertPoule(animal);
        verifyMortality(animal);

        if (!animal.isVivant()) {
            return;
        }

        // Passage à l'âge adulte à partir de 4 jours
        if (animal.getAge() >= 4 && TypeStade.ENFANT.equals(animal.getStade())) {
            animal.setStade(TypeStade.ADULTE);
        }

        // Révélation du sexe dès l'âge adulte (4 jours)
        if (animal.getAge() >= 4 && (animal.getSexe() == null || animal.getSexe().equals(TypeSexe.INCONNU))) {
            animal.setSexe(Utilitaires.generateRandomGender());
        }

        // Détermination du rôle (Reproducteur/Pondeuse)
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

    // Méthode qui vérifie si une poule est un coq reproducteur en bonne santé
    private boolean coqReproducteur(Animal animal) {
        return TypeAnimal.POULE.equals(animal.getTypeAnimal())
                && animal.isVivant()
                && !animal.estMalade()
                && TypeStade.ADULTE.equals(animal.getStade())
                && TypeRole.REPRODUCTEUR.equals(animal.getRole())
                && TypeSexe.MALE.equals(animal.getSexe());
    }

    // Méthode qui vérifie si une poule est éligible à pondre un œuf aujourd'hui
    private boolean eligibilitePondeuse(Animal animal) {
        return TypeAnimal.POULE.equals(animal.getTypeAnimal())
                && animal.isVivant()
                && !animal.estMalade()
                && TypeStade.ADULTE.equals(animal.getStade())
                && TypeRole.PONDEUSE.equals(animal.getRole())
                && TypeSexe.FEMELLE.equals(animal.getSexe())
                && animal.isAMange()
                && animal.getJaugeProprete() == 100;
    }

    // Méthode qui vérifie si une poule peut pondre un œuf aujourd'hui en fonction
    // de la capacité de reproduction de la ferme
    private boolean peutPondreOeuf(Animal hen) {
        Integer idFerme = hen.getFerme().getIdFerme();
        List<Animal> animaux = animalRepository.findByFerme_IdFerme(idFerme);

        long nbCoqs = animaux.stream()
                .filter(this::coqReproducteur)
                .count();

        long capacite = nbCoqs * MAX_PONDEUSES_PAR_COQ;
        if (capacite <= 0) {
            return false;
        }

        List<Animal> pondeusesEligibles = animaux.stream()
                .filter(this::eligibilitePondeuse)
                .sorted(Comparator.comparing(a -> a.getIdAnimal() == null ? Integer.MAX_VALUE : a.getIdAnimal()))
                .toList();

        int position = -1;
        for (int i = 0; i < pondeusesEligibles.size(); i++) {
            Animal candidate = pondeusesEligibles.get(i);
            if (hen.getIdAnimal() != null && hen.getIdAnimal().equals(candidate.getIdAnimal())) {
                position = i;
                break;
            }
        }

        // Si la poule n'est pas encore persistée, on applique uniquement la capacité
        // globale.
        if (position < 0) {
            return capacite > 0;
        }

        return position < capacite;
    }

    // Méthode qui augmente l'age d'une poule
    public void updateChickenAge(Animal animal) {
        assertPoule(animal);
        if (!animal.isVivant()) {
            return;
        }

        // Vieillissement et gestion faim/poids existante
        animal.setAge(animal.getAge() + 1);

        if (!animal.isAMange()) {
            animal.setNbJoursSansNourriture(animal.getNbJoursSansNourriture() + 1);
        } else {
            animal.setNbJoursSansNourriture(0);
        }

        if (animal.estMalade()) {
            animal.setNbJoursMalade(animal.getNbJoursMalade() + 1);
        }
        // La poule doit être pondeuse, nourrie, propre et non malade
        if (TypeRole.PONDEUSE.equals(animal.getRole()) && animal.isAMange()
                && animal.getJaugeProprete() == 100 && !animal.estMalade()
                && peutPondreOeuf(animal)) {

            // Entre 0 et 2 oeufs
            int nbOeufs = new java.util.Random().nextInt(3);

            if (nbOeufs > 0) {
                int gain = nbOeufs * 8; // 8 ecus par oeuf
                fermeService.ajouterEcus(animal.getFerme().getIdFerme(), gain);
            }
        }

        // Réinitialisation pour le lendemain
        animal.setAMange(false);
        animal.setJaugeFaim(0);
        animal.setJaugeHydratation(0);

        updateChickenStatus(animal);
        verifyMortality(animal);
        animalRepository.save(animal);
    }

    // Méthode d'augmentation du poids d'une poule
    public void updateChickenWeight(Animal animal, float addWeight) {
        assertPoule(animal);
        if (animal.getPoids() + addWeight > 3.5) {
            animal.setPoids((float) 3.5);
        } else {
            animal.setPoids(animal.getPoids() + addWeight);
        }
    }

    // Méthode qui nourrit une poule
    @Transactional
    public void nourrirPoule(Animal animal) {
        assertPoule(animal);

        if (!animal.isVivant())
            throw new IllegalStateException("ERREUR : la poule est morte.");
        if (animal.getJaugeFaim() == 100)
            throw new IllegalCallerException("Déjà nourrie aujourd'hui.");

        animal.setJaugeFaim(100);
        animal.setAMange(true);

        // Calcul du poids selon l'eau
        float gainPoids = (animal.getJaugeHydratation() == 100) ? 0.65f : 0.5f;
        animal.setPoids(animal.getPoids() + gainPoids);

        fermeService.retirerEcus(animal.getFerme().getIdFerme(), 3); // Coût : 3 écus
        updateChickenStatus(animal);
        animalRepository.save(animal);
    }

    public void hydraterPoule(Animal animal) {
        assertPoule(animal);

        if (!animal.isVivant())
            throw new IllegalStateException("La poule est morte.");
        if (animal.getJaugeFaim() == 100)
            throw new IllegalCallerException("Déjà nourrie aujourd'hui.");
        if (animal.getJaugeHydratation() == 100)
            throw new IllegalCallerException("Déjà hydratée aujourd'hui.");

        animal.setJaugeHydratation(100);

        if (animal.getJaugeFaim() == 100) {
            animal.setPoids(animal.getPoids() + 0.15f); // L'eau ne fait grossir que si combinée au grain
            updateChickenStatus(animal);
        }

        fermeService.retirerEcus(animal.getFerme().getIdFerme(), 1); // Coût : 1 écu
        animalRepository.save(animal);
    }

    @Transactional
    public void soignerPoule(Animal animal) {
        assertPoule(animal);

        if (!animal.isVivant()) {
            throw new IllegalStateException("ERREUR : la poule est morte.");
        }

        if (!animal.estMalade()) {
            throw new IllegalCallerException("ERREUR : la poule ne peut pas être soignée si elle n'est pas malade");
        }

        animal.setEstMalade(false);
        animal.setNbJoursMalade(0);
        animal.setJaugeSante(100);

        // Retrait d'écus
        Integer id = animal.getFerme().getIdFerme();
        fermeService.retirerEcus(id, 6);
        animalRepository.save(animal);
    }

    // Méthode qui actualise le statut d'un lapin en fonction de son age
    // Doit aussi choisir un sexe une fois l'age adulte atteint (voir méthode pour
    // la poule)
    // PRE : l'animal doit être un lapin
    public void updateRabbitStatus(Animal animal) {
        assertLapin(animal);

        verifyMortality(animal);

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
                animal.setStade(TypeStade.GROS_LAPEREAU);
            }
        }
    }

    // Méthode qui augmente l'age d'un lapin
    // Si un lapin n'est pas nourri, il ne change pas d'age
    // PRE : l'animal doit être un lapin
    public void updateRabbitAge(Animal animal) {
        assertLapin(animal);

        verifyMortality(animal);

        if (!animal.isVivant()) {
            animalRepository.save(animal);
            return;
        }

        // Condition stricte : ils ne grandissent QUE s'ils sont nourris ET abreuvés
        if (animal.isAMange() && animal.getJaugeHydratation() == 100) {
            animal.setAge(animal.getAge() + 1);
        }

        // Suivi des jours sans nourriture pour la mortalité
        if (!animal.isAMange()) {
            animal.setNbJoursSansNourriture(animal.getNbJoursSansNourriture() + 1);
        } else {
            animal.setNbJoursSansNourriture(0);
        }

        // Suivi de la maladie
        if (animal.estMalade()) {
            animal.setNbJoursMalade(animal.getNbJoursMalade() + 1);
        } else {
            animal.setNbJoursMalade(0);
        }

        // Réinitialisation des actions journalières
        animal.setAMange(false);
        animal.setJaugeFaim(0);
        animal.setJaugeHydratation(0);

        // Mise à jour du statut (Bébé -> Petit -> Gros -> Adulte)
        updateRabbitStatus(animal);
        verifyMortality(animal);

        // Sauvegarde
        animalRepository.save(animal);
    }

    // méthode qui actualise le statut d'une vache en fonction de son age et de son
    // poids
    // (adulte quand 80 kg et 10 jours)
    // Meurt quand malade 4 jours de suite
    // PRE : l'animal doit être une vache
    public void updateCowStatus(Animal animal) {
        assertVache(animal);

        if (!animal.isVivant()) {
            return;
        }

        // La vache devient adulte uniquement quand elle a 10 jours ET pèse 80 kg
        if (TypeStade.ENFANT.equals(animal.getStade())) {
            if (animal.getAge() >= AGE_ADULTE && animal.getPoids() >= POIDS_ADULTE) {
                animal.setStade(TypeStade.ADULTE);

                // On peut imaginer que le sexe est aussi révélé ou fixé ici si nécessaire
                if (animal.getSexe() == null || TypeSexe.INCONNU.equals(animal.getSexe())) {
                    animal.setSexe(Utilitaires.generateRandomGender());
                }
            }
        }
    }

    @Transactional
    // Méthode qui augmente l'âge d'une vache
    public void updateCowAge(Animal animal) {
        assertVache(animal);

        verifyMortality(animal);

        if (!animal.isVivant()) {
            animalRepository.save(animal);
            return;
        }

        // Augmenter l'âge de 1 jour
        animal.setAge(animal.getAge() + 1);

        // Gestion de la maladie (Meurt si malade 4 jours de suite)
        if (animal.estMalade()) {
            animal.setNbJoursMalade(animal.getNbJoursMalade() + 1);
        } else {
            animal.setNbJoursMalade(0);
        }

        if (!animal.isAMange()) {
            animal.setNbJoursSansNourriture(animal.getNbJoursSansNourriture() + 1);
        } else {
            animal.setNbJoursSansNourriture(0);
        }

        // Réinitialisation des indicateurs quotidiens (Nourriture, Traite, etc.)
        animal.setAMange(false);
        animal.setAEteTraite(false);
        animal.setJaugeFaim(0);
        animal.setJaugeHydratation(0);

        // Mise à jour du stade et vérification de la mort
        updateCowStatus(animal);
        verifyMortality(animal);

        animalRepository.save(animal);
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

    // Méthode qui ajoute du poids a une vache (poids max 750 kg)
    public void updateCowWeight(Animal animal, float addWeight) {
        assertVache(animal);
        if (animal.getPoids() + addWeight < POIDS_MAX) {
            animal.setPoids(animal.getPoids() + addWeight);
        } else {
            animal.setPoids(POIDS_MAX);
        }
    }

    // Méthodes qui nourrissent une vache (+5kg si herbe, +3kg si paille)
    // (L'eau fait prendre 1 kg mais seulement si la vache a déja mangé)
    // PRE : La vache ne doit pas avoir déja mangé dans la journée
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

    // Méthode qui nourrit une vache avec de la paille
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

    // Méthode qui abreuve une vache
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

    // Méthode qui fait produire du lait à une vache et l'ajoute dans la remise
    // Adulte : 8L à 6H et 18H si la vache n'est pas traite, 4L sinon (Possibilité
    // d'ajouter attributs estTraite)
    // PRE l'animal doit être une vache non sale et nourrie
    @Transactional
    public int produireLait(Animal animal) {
        assertVache(animal);

        // Une vache morte, enfant, affamée ou malade ne produit rien
        if (!animal.isVivant() || !TypeStade.ADULTE.equals(animal.getStade())
                || !animal.isAMange() || animal.estMalade() || animal.getJaugeProprete() < 100) {
            return 0;
        }

        // Production basée sur l'historique de traite (intervalle précédent).
        int litresProduits = animal.isAEteTraite() ? LITRES_DEUXIEME_TRAITE : LITRES_PREMIERE_TRAITE;

        int stockActuel = animal.getStockLaitPis();
        int capaciteRestante = Math.max(0, animal.getStockLaitPisMax() - stockActuel);
        int litresStockes = Math.min(litresProduits, capaciteRestante);

        if (litresStockes <= 0) {
            return 0;
        }

        animal.setStockLaitPis(stockActuel + litresStockes);
        animal.setAEteTraite(true);
        animalRepository.save(animal);

        return litresStockes;
    }

    @Transactional
    // Methode qui traite une vache (et aussi de gagner de l'argent)
    public int traireVache(Animal animal) {
        assertVache(animal);

        if (!animal.isVivant()) {
            throw new IllegalStateException("ERREUR : La vache est morte.");
        }

        int litresDisponibles = animal.getStockLaitPis();

        if (litresDisponibles <= 0) {
            throw new IllegalStateException("ERREUR : Le pis de la vache est vide, il n'y a pas de lait à traire.");
        }

        // Calcul du gain (2 écus par litre)
        int gain = litresDisponibles * 2;

        // Ajouter les écus à la ferme
        fermeService.ajouterEcus(animal.getFerme().getIdFerme(), gain);

        // Réinitialiser le stock de lait du pis après la traite
        animal.setStockLaitPis(0);
        animalRepository.save(animal);

        // Retourner le nombre de litres traités pour information
        return litresDisponibles;
    }

    // Méthode pour soigner une vache
    // PRE : La vache doit être malade et ne pas avoir été soignée dans la journée
    @Transactional
    public void soignerVache(Animal animal) {
        assertVache(animal);

        if (!animal.estMalade()) {
            throw new IllegalStateException(
                    "ERREUR : La vache ne peut pas être soignée si elle n'est pas malade.");
        }

        animal.setEstMalade(false);
        animal.setNbJoursMalade(0);
        animal.setJaugeSante(100);

        fermeService.retirerEcus(animal.getFerme().getIdFerme(), COUT_SOIGNER);
        animalRepository.save(animal);
    }

}// Class
