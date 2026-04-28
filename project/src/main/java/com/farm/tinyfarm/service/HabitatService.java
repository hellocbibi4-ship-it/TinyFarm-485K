/*
 * Couche métier gérant la logique de habitat pour TinyFarm.
 */



package com.farm.tinyfarm.service;

import java.sql.Date;
import java.time.LocalDate;

import org.springframework.stereotype.Service;

import com.farm.tinyfarm.model.Animal;
import com.farm.tinyfarm.model.Habitat;
import com.farm.tinyfarm.model.TypeHabitat;
import com.farm.tinyfarm.repository.HabitatRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class HabitatService {

    private static final int COUT_NETTOYAGE   = 3;
    private static final int COUT_SOIN        = 6;
    private static final int COUT_NOURRITURE  = 5;
    private static final int COUT_ABREUVEMENT = 2;

    private final HabitatRepository habitatRepository;
    private final FermeService fermeService;

    public HabitatService(HabitatRepository habitatRepository,
                          FermeService fermeService) {
        this.habitatRepository = habitatRepository;
        this.fermeService = fermeService;
    }

    // -------------------------------------------------------------------------
    // Helpers privés
    // -------------------------------------------------------------------------

    private Habitat findHabitat(Integer habitatId) {
        return habitatRepository.findById(habitatId)
            .orElseThrow(() -> new EntityNotFoundException("Habitat introuvable"));
    }

    private void assertClapier(Habitat habitat) {
        if (!TypeHabitat.CLAPIER.equals(habitat.getTypeHabitat())) {
            throw new IllegalArgumentException("ERREUR : cet habitat n'est pas un clapier.");
        }
    }

    private Integer getIdFerme(Habitat habitat) {
        return habitat.getFerme().getIdFerme();
    }

    // -------------------------------------------------------------------------
    // Méthodes génériques
    // -------------------------------------------------------------------------

    // Méthode qui ajoute un animal dans son habitat
    // PRE : taille de listeAnimaux < capaMax
    @Transactional
    public void ajouterAnimal(Habitat habitat, Animal animal) {
        if (habitat.getListeAnimaux().size() >= habitat.getCapaMax()) {
            throw new IllegalStateException(
                "ERREUR : l'habitat est plein (capacité max atteinte).");
        }
        habitat.getListeAnimaux().add(animal);
        habitatRepository.save(habitat);
    }
    // Version avec idHabitat
    @Transactional
    public void ajouterAnimal(Integer habitatId, Animal animal) {
        ajouterAnimal(findHabitat(habitatId), animal);
    }

    // Méthode qui supprime un animal dans son habitat
    @Transactional
    public void supprimerAnimal(Habitat habitat, Animal animal) {
        boolean removed = habitat.getListeAnimaux().remove(animal);
        if (!removed) {
            throw new IllegalArgumentException(
                "ERREUR : cet animal n'appartient pas à cet habitat.");
        }
        habitatRepository.save(habitat);
    }
    // Version avec idHabitat
    @Transactional
    public void supprimerAnimal(Integer habitatId, Animal animal) {
        supprimerAnimal(findHabitat(habitatId), animal);
    }

    // -------------------------------------------------------------------------
    // Méthodes spécifiques au clapier
    // -------------------------------------------------------------------------

    // Méthode qui lave un clapier
    // PRE : l'habitat est un clapier
    @Transactional
    public void nettoyerClapier(Habitat habitat) {
        assertClapier(habitat);
        if (!habitat.estSale()) {
            throw new IllegalStateException(
                "ERREUR : le clapier est déjà propre.");
        }
        habitat.setEstSale(false);
        habitat.setDateEstSale(null);
        fermeService.retirerEcus(getIdFerme(habitat), COUT_NETTOYAGE);
        habitatRepository.save(habitat);
    }
    // Version avec idHabitat
    @Transactional
    public void nettoyerClapier(Integer habitatId) {
        nettoyerClapier(findHabitat(habitatId));
    }

    // Méthode qui soigne un clapier
    // PRE : l'habitat est un clapier
    @Transactional
    public void soignerClapier(Habitat habitat) {
        assertClapier(habitat);
        if (!habitat.estMalade()) {
            throw new IllegalStateException(
                "ERREUR : le clapier est déjà en bonne santé.");
        }
        habitat.setEstMalade(false);
        for (Animal animal : habitat.getListeAnimaux()) {
            animal.setEstMalade(false);
            animal.setJaugeSante(100);
        }
        fermeService.retirerEcus(getIdFerme(habitat), COUT_SOIN);
        habitatRepository.save(habitat);
    }
    // Version avec idHabitat
    @Transactional
    public void soignerClapier(Integer habitatId) {
        soignerClapier(findHabitat(habitatId));
    }

    // Méthode qui nourrit un clapier
    // PRE : l'habitat est un clapier
    @Transactional
    public void nourrirClapier(Habitat habitat) {
        assertClapier(habitat);
        if (habitat.getListeAnimaux().isEmpty()) {
            throw new IllegalStateException(
                "ERREUR : le clapier est vide, il n'y a pas d'animaux à nourrir.");
        }
        for (Animal animal : habitat.getListeAnimaux()) {
            animal.setJaugeFaim(100);
            animal.setAMange(true);
        }
        fermeService.retirerEcus(getIdFerme(habitat), COUT_NOURRITURE);
        habitatRepository.save(habitat);
    }
    // Version avec idHabitat
    @Transactional
    public void nourrirClapier(Integer habitatId) {
        nourrirClapier(findHabitat(habitatId));
    }

    // Méthode qui abreuve un clapier
    // PRE : l'habitat est un clapier
    @Transactional
    public void abreuverClapier(Habitat habitat) {
        assertClapier(habitat);
        if (habitat.getListeAnimaux().isEmpty()) {
            throw new IllegalStateException(
                "ERREUR : le clapier est vide, il n'y a pas d'animaux à abreuver.");
        }
        for (Animal animal : habitat.getListeAnimaux()) {
            animal.setJaugeHydratation(100);
        }
        fermeService.retirerEcus(getIdFerme(habitat), COUT_ABREUVEMENT);
        habitatRepository.save(habitat);
    }
    // Version avec idHabitat
    @Transactional
    public void abreuverClapier(Integer habitatId) {
        abreuverClapier(findHabitat(habitatId));
    }

    // Méthode qui tue une partie des lapins si le clapier n'est pas nettoyé le jour même
    // PRE : le clapier est sale et date > date courante
    @Transactional
    public void actionClapierSale(Habitat habitat) {
        assertClapier(habitat);
        if (!habitat.estSale()) {
            throw new IllegalStateException("ERREUR : le clapier n'est pas sale.");
        }
        Date dateCourante = Date.valueOf(LocalDate.now());
        if (habitat.getDateEstSale() != null && habitat.getDateEstSale().before(dateCourante)) {
            int nbMorts = (int) Math.ceil(habitat.getListeAnimaux().size() * 0.25);
            for (int i = 0; i < nbMorts && !habitat.getListeAnimaux().isEmpty(); i++) {
                habitat.getListeAnimaux().remove(0);
            }
            habitatRepository.save(habitat);
        }
    }
    // Version avec idHabitat
    @Transactional
    public void actionClapierSale(Integer habitatId) {
        actionClapierSale(findHabitat(habitatId));
    }

    // Méthode qui tue une partie des lapins si le clapier n'est pas soigné le jour même
    // PRE : le clapier est malade et date > date courante
    @Transactional
    public void actionClapierMalade(Habitat habitat) {
        assertClapier(habitat);
        if (!habitat.estMalade()) {
            throw new IllegalStateException("ERREUR : le clapier n'est pas malade.");
        }
        Date dateCourante = Date.valueOf(LocalDate.now());
        if (habitat.getDateEstSale() != null && habitat.getDateEstSale().before(dateCourante)) {
            int nbMorts = (int) Math.ceil(habitat.getListeAnimaux().size() * 0.25);
            for (int i = 0; i < nbMorts && !habitat.getListeAnimaux().isEmpty(); i++) {
                habitat.getListeAnimaux().remove(0);
            }
            habitatRepository.save(habitat);
        }
    }
    // Version avec idHabitat
    @Transactional
    public void actionClapierMalade(Integer habitatId) {
        actionClapierMalade(findHabitat(habitatId));
    }
}