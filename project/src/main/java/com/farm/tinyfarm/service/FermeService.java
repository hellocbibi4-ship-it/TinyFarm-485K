package com.farm.tinyfarm.service;

import com.farm.tinyfarm.model.Animal;
import com.farm.tinyfarm.model.Ferme;
import com.farm.tinyfarm.model.Remise;
import com.farm.tinyfarm.model.TypeAnimal;
import com.farm.tinyfarm.outils.Utilitaires;
import com.farm.tinyfarm.repository.AnimalRepository;
import com.farm.tinyfarm.repository.FermeRepository;
import com.farm.tinyfarm.repository.RemiseRepository;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public class FermeService {
    public static final int GAME_DAY_DURATION_SECONDS = 60;
    public static final int DAILY_COMMUNITY_PURCHASE_LIMIT = 12;

    private final FermeRepository fermeRepository;
    private final RemiseRepository remiseRepository;
    private final AnimalRepository animalRepository;

    public FermeService(
        FermeRepository fermeRepository,
        RemiseRepository remiseRepository,
        AnimalRepository animalRepository
    ) {
        this.fermeRepository = fermeRepository;
        this.remiseRepository = remiseRepository;
        this.animalRepository = animalRepository;
    }

    @Transactional
    public Ferme create(Ferme ferme) {
        Utilitaires.validationNom(ferme.getNom());
        ferme.setScore(0);
        ferme.setSoldeEcus(1500);
        ferme.setHibernation(false);
        ferme.setDateCreation(LocalDateTime.now());
        ferme.setDerniereCo(LocalDateTime.now());
        ferme.setJourActuel(1);
        ferme.setAchatsCollectiviteRestants(DAILY_COMMUNITY_PURCHASE_LIMIT);
        ferme.setNbVaches(1);
        ferme.setNbPoules(3);
        ferme.setNbLapins(2);
        ferme.setNbLapinsMalades(0);
        ferme.setNbVachesAffamees(0);
        ferme.setNbVachesAssoiffees(0);
        ferme.setNbPouleAffamees(0);
        ferme.setNbPouleAssoiffees(0);
        ferme.setNbLapinsAffames(0);
        ferme.setNbLapinsAssoiffes(0);

        Ferme savedFerme = fermeRepository.save(ferme);

        if (!remiseRepository.existsById(savedFerme.getIdFerme())) {
            Remise remise = new Remise();
            remise.setFerme(savedFerme);
            remiseRepository.save(remise);
        }

        synchroniserAnimaux(savedFerme);
        synchroniserCompteursDepuisAnimaux(savedFerme);
        return fermeRepository.save(savedFerme);
    }

    public void deleteById(Integer id) {
        fermeRepository.deleteById(id);
    }

    @Transactional
    public void ajouterEcus(Integer idFerme, int montant) {
        Ferme ferme = fermeRepository.findById(idFerme)
            .orElseThrow(() -> new RuntimeException("Impossible d'ajouter les ecus: la ferme n'existe pas"));

        if (montant < 0) {
            throw new IllegalArgumentException("Le montant d'ecus a ajouter doit etre positif");
        }

        ferme.setSoldeEcus(ferme.getSoldeEcus() + montant);
    }

    @Transactional
    public void retirerEcus(Integer idFerme, int montant) {
        Ferme ferme = fermeRepository.findById(idFerme)
            .orElseThrow(() -> new RuntimeException("Impossible d'ajouter les ecus: la ferme n'existe pas"));

        if (montant < 0) {
            throw new IllegalArgumentException("Le montant d'ecus a retirer doit etre positif");
        }

        ferme.setSoldeEcus(ferme.getSoldeEcus() - montant);
    }

    @Transactional
    public void ajouterScore(Integer idFerme, int montant) {
        Ferme ferme = fermeRepository.findById(idFerme)
            .orElseThrow(() -> new RuntimeException("Impossible d'ajouter le score: la ferme n'existe pas"));

        if (montant < 0) {
            throw new IllegalArgumentException("Le score a ajouter doit etre positif");
        }

        ferme.setScore(ferme.getScore() + montant);
    }

    @Transactional
    public Ferme acheterAnimal(Integer idFerme, String typeAnimal) {
        Ferme ferme = fermeRepository.findById(idFerme)
            .orElseThrow(() -> new RuntimeException("Impossible d'acheter un animal: la ferme n'existe pas"));
        normalizeDailyState(ferme);
        synchroniserAnimaux(ferme);

        if (typeAnimal == null || typeAnimal.isBlank()) {
            throw new IllegalArgumentException("Type d'animal invalide");
        }

        TypeAnimal animalType = switch (typeAnimal.trim().toLowerCase()) {
            case "vache" -> TypeAnimal.VACHE;
            case "poule" -> TypeAnimal.POULE;
            case "lapin" -> TypeAnimal.LAPIN;
            default -> throw new IllegalArgumentException("Type d'animal inconnu");
        };

        int prix = switch (animalType) {
            case VACHE -> 50;
            case POULE, LAPIN -> 10;
        };

        if (ferme.getSoldeEcus() < prix) {
            throw new IllegalArgumentException("Solde insuffisant");
        }

        consommerAchatCollectivite(ferme);
        ferme.setSoldeEcus(ferme.getSoldeEcus() - prix);
        animalRepository.save(creerAnimalPourFerme(ferme, animalType));
        synchroniserCompteursDepuisAnimaux(ferme);
        return fermeRepository.save(ferme);
    }

    @Transactional
    public void hibernation(Integer idFerme, boolean bool) {
        Ferme ferme = fermeRepository.findById(idFerme)
            .orElseThrow(() -> new RuntimeException("Impossible de modifier l'etat d'hibernation: la ferme n'existe pas"));

        ferme.setHibernation(bool);
    }

    @Transactional
    public void mettreAJourferme(Ferme ferme) {
        Integer fermeId = ferme.getIdFerme();
        long joursAbsence = ChronoUnit.DAYS.between(ferme.getDerniereCo(), LocalDateTime.now());

        if (joursAbsence <= 0) {
            return;
        }

        if (joursAbsence >= 3) {
            hibernation(fermeId, true);
        }

        if (joursAbsence >= 50) {
            deleteById(fermeId);
        }

        ferme.setDerniereCo(LocalDateTime.now());
        fermeRepository.save(ferme);
    }

    @Transactional
    public Ferme getById(Integer id) {
        Ferme ferme = fermeRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Ferme introuvable"));
        boolean changed = normalizeDailyState(ferme);
        changed = synchroniserAnimaux(ferme) || changed;
        changed = synchroniserCompteursDepuisAnimaux(ferme) || changed;
        return changed ? fermeRepository.save(ferme) : ferme;
    }

    @Transactional
    public Ferme passerJour(Integer idFerme) {
        Ferme ferme = fermeRepository.findById(idFerme)
            .orElseThrow(() -> new RuntimeException("Ferme introuvable"));
        normalizeDailyState(ferme);
        synchroniserAnimaux(ferme);

        Remise remise = remiseRepository.findById(idFerme).orElseGet(() -> {
            Remise nouvelleRemise = new Remise();
            nouvelleRemise.setFerme(ferme);
            return remiseRepository.save(nouvelleRemise);
        });

        List<Animal> animaux = animalRepository.findByFerme_IdFermeOrderByIdAnimalAsc(idFerme);
        int nbPoules = (int) animaux.stream().filter(animal -> animal.getTypeAnimal() == TypeAnimal.POULE).count();
        int nbVaches = (int) animaux.stream().filter(animal -> animal.getTypeAnimal() == TypeAnimal.VACHE).count();

        if (nbPoules > 0) {
            remise.setStockOeuf(remise.getStockOeuf() + nbPoules);
        }
        if (nbVaches > 0) {
            remise.setStockLait(remise.getStockLait() + nbVaches);
        }

        appliquerEtatsQuotidiensAnimaux(animaux);
        ferme.setJourActuel(ferme.getJourActuel() + 1);
        ferme.setAchatsCollectiviteRestants(DAILY_COMMUNITY_PURCHASE_LIMIT);

        animalRepository.saveAll(animaux);
        synchroniserCompteursDepuisAnimaux(ferme, animaux);
        remiseRepository.save(remise);
        return fermeRepository.save(ferme);
    }

    @Transactional
    public void consommerAchatCollectivite(Integer idFerme) {
        Ferme ferme = fermeRepository.findById(idFerme)
            .orElseThrow(() -> new RuntimeException("Ferme introuvable"));
        consommerAchatCollectivite(ferme);
        fermeRepository.save(ferme);
    }

    public List<Animal> getAnimaux(Integer idFerme) {
        Ferme ferme = getById(idFerme);
        synchroniserAnimaux(ferme);
        return animalRepository.findByFerme_IdFermeOrderByIdAnimalAsc(idFerme);
    }

    public Animal getAnimalDeFerme(Integer idFerme, Integer idAnimal) {
        Animal animal = animalRepository.findById(idAnimal)
            .orElseThrow(() -> new RuntimeException("Animal introuvable"));
        if (animal.getFerme() == null || !idFerme.equals(animal.getFerme().getIdFerme())) {
            throw new IllegalArgumentException("Cet animal n'appartient pas a cette ferme");
        }
        return animal;
    }

    @Transactional
    public Ferme sauvegarderApresActionsAnimaux(Integer idFerme, List<Animal> animaux) {
        Ferme ferme = fermeRepository.findById(idFerme)
            .orElseThrow(() -> new RuntimeException("Ferme introuvable"));
        animalRepository.saveAll(animaux);
        synchroniserCompteursDepuisAnimaux(ferme);
        return fermeRepository.save(ferme);
    }

    private void consommerAchatCollectivite(Ferme ferme) {
        normalizeDailyState(ferme);
        if (ferme.getAchatsCollectiviteRestants() <= 0) {
            throw new IllegalArgumentException("Quota d'achats de la collectivite atteint pour aujourd'hui");
        }
        ferme.setAchatsCollectiviteRestants(ferme.getAchatsCollectiviteRestants() - 1);
    }

    private boolean normalizeDailyState(Ferme ferme) {
        boolean changed = false;

        if (ferme.getJourActuel() == null || ferme.getJourActuel() < 1) {
            ferme.setJourActuel(1);
            changed = true;
        }

        Integer achatsRestants = ferme.getAchatsCollectiviteRestants();
        if (achatsRestants == null || achatsRestants < 0 || achatsRestants > DAILY_COMMUNITY_PURCHASE_LIMIT) {
            ferme.setAchatsCollectiviteRestants(DAILY_COMMUNITY_PURCHASE_LIMIT);
            changed = true;
        }

        return changed;
    }

    private boolean synchroniserAnimaux(Ferme ferme) {
        boolean changed = false;
        changed = synchroniserAnimauxType(ferme, TypeAnimal.VACHE, ferme.getNbVaches()) || changed;
        changed = synchroniserAnimauxType(ferme, TypeAnimal.POULE, ferme.getNbPoules()) || changed;
        changed = synchroniserAnimauxType(ferme, TypeAnimal.LAPIN, ferme.getNbLapins()) || changed;
        return changed;
    }

    private boolean synchroniserAnimauxType(Ferme ferme, TypeAnimal typeAnimal, Integer expectedCount) {
        List<Animal> animaux = animalRepository.findByFerme_IdFermeAndTypeAnimalOrderByIdAnimalAsc(
            ferme.getIdFerme(),
            typeAnimal
        );
        int cible = Math.max(0, expectedCount == null ? 0 : expectedCount);
        boolean changed = false;

        while (animaux.size() < cible) {
            Animal animal = creerAnimalPourFerme(ferme, typeAnimal);
            animalRepository.save(animal);
            animaux.add(animal);
            changed = true;
        }

        if (animaux.size() > cible) {
            animalRepository.deleteAll(animaux.subList(cible, animaux.size()));
            changed = true;
        }

        return changed;
    }

    private Animal creerAnimalPourFerme(Ferme ferme, TypeAnimal typeAnimal) {
        long existingCount = animalRepository.countByFerme_IdFermeAndTypeAnimal(ferme.getIdFerme(), typeAnimal);
        Animal animal = new Animal();
        animal.setFerme(ferme);
        animal.setTypeAnimal(typeAnimal);
        animal.setNom(buildAnimalName(typeAnimal, (int) existingCount + 1));
        animal.setPoids(defaultWeight(typeAnimal));
        animal.setJaugeSante(100);
        animal.setJaugeFaim(100);
        animal.setJaugeHydratation(100);
        animal.setJaugeProprete(100);
        animal.setEstMalade(false);
        animal.setAMange(false);
        animal.setAEteTraite(false);
        return animal;
    }

    private String buildAnimalName(TypeAnimal typeAnimal, int index) {
        return switch (typeAnimal) {
            case VACHE -> "Vache " + index;
            case POULE -> "Poule " + index;
            case LAPIN -> "Lapin " + index;
        };
    }

    private float defaultWeight(TypeAnimal typeAnimal) {
        return switch (typeAnimal) {
            case VACHE -> 500f;
            case POULE -> 2f;
            case LAPIN -> 2f;
        };
    }

    private void appliquerEtatsQuotidiensAnimaux(List<Animal> animaux) {
        for (Animal animal : animaux) {
            boolean etaitAffame = animal.getJaugeFaim() < 100;
            boolean etaitAssoiffe = animal.getJaugeHydratation() < 100;

            if (etaitAffame) {
                animal.setEstMalade(true);
                animal.setJaugeSante(0);
            }

            if (etaitAssoiffe) {
                animal.setJaugeProprete(0);
            }

            animal.setJaugeFaim(0);
            animal.setJaugeHydratation(0);
        }
    }

    private boolean synchroniserCompteursDepuisAnimaux(Ferme ferme) {
        return synchroniserCompteursDepuisAnimaux(
            ferme,
            animalRepository.findByFerme_IdFermeOrderByIdAnimalAsc(ferme.getIdFerme())
        );
    }

    private boolean synchroniserCompteursDepuisAnimaux(Ferme ferme, List<Animal> animaux) {
        int nbVaches = 0;
        int nbPoules = 0;
        int nbLapins = 0;
        int nbVachesAffamees = 0;
        int nbPouleAffamees = 0;
        int nbLapinsAffames = 0;
        int nbVachesAssoiffees = 0;
        int nbPouleAssoiffees = 0;
        int nbLapinsAssoiffes = 0;
        int nbLapinsMalades = 0;

        for (Animal animal : animaux) {
            switch (animal.getTypeAnimal()) {
                case VACHE -> {
                    nbVaches++;
                    if (animal.getJaugeFaim() < 100) {
                        nbVachesAffamees++;
                    }
                    if (animal.getJaugeHydratation() < 100) {
                        nbVachesAssoiffees++;
                    }
                }
                case POULE -> {
                    nbPoules++;
                    if (animal.getJaugeFaim() < 100) {
                        nbPouleAffamees++;
                    }
                    if (animal.getJaugeHydratation() < 100) {
                        nbPouleAssoiffees++;
                    }
                }
                case LAPIN -> {
                    nbLapins++;
                    if (animal.getJaugeFaim() < 100) {
                        nbLapinsAffames++;
                    }
                    if (animal.getJaugeHydratation() < 100) {
                        nbLapinsAssoiffes++;
                    }
                    if (animal.estMalade()) {
                        nbLapinsMalades++;
                    }
                }
            }
        }

        boolean changed = false;
        changed = setIfDifferent(ferme.getNbVaches(), nbVaches, ferme::setNbVaches) || changed;
        changed = setIfDifferent(ferme.getNbPoules(), nbPoules, ferme::setNbPoules) || changed;
        changed = setIfDifferent(ferme.getNbLapins(), nbLapins, ferme::setNbLapins) || changed;
        changed = setIfDifferent(ferme.getNbVachesAffamees(), nbVachesAffamees, ferme::setNbVachesAffamees) || changed;
        changed = setIfDifferent(ferme.getNbPouleAffamees(), nbPouleAffamees, ferme::setNbPouleAffamees) || changed;
        changed = setIfDifferent(ferme.getNbLapinsAffames(), nbLapinsAffames, ferme::setNbLapinsAffames) || changed;
        changed = setIfDifferent(ferme.getNbVachesAssoiffees(), nbVachesAssoiffees, ferme::setNbVachesAssoiffees) || changed;
        changed = setIfDifferent(ferme.getNbPouleAssoiffees(), nbPouleAssoiffees, ferme::setNbPouleAssoiffees) || changed;
        changed = setIfDifferent(ferme.getNbLapinsAssoiffes(), nbLapinsAssoiffes, ferme::setNbLapinsAssoiffes) || changed;
        changed = setIfDifferent(ferme.getNbLapinsMalades(), nbLapinsMalades, ferme::setNbLapinsMalades) || changed;
        return changed;
    }

    private boolean setIfDifferent(Integer currentValue, int expectedValue, java.util.function.IntConsumer setter) {
        if ((currentValue == null ? 0 : currentValue) != expectedValue) {
            setter.accept(expectedValue);
            return true;
        }
        return false;
    }

    public List<Map<String, Object>> getClassementData() {
        return fermeRepository.findAll().stream()
            .sorted(
                Comparator.comparing((Ferme ferme) -> ferme.getSoldeEcus() == null ? 0 : ferme.getSoldeEcus())
                    .reversed()
                    .thenComparing(ferme -> ferme.getNom() == null ? "" : ferme.getNom())
            )
            .map(ferme -> Map.<String, Object>of(
                "name", ferme.getUtilisateur() != null && ferme.getUtilisateur().getGithubUsername() != null
                    ? ferme.getUtilisateur().getGithubUsername()
                    : (ferme.getNom() == null ? "-" : ferme.getNom()),
                "money", ferme.getSoldeEcus() == null ? 0 : ferme.getSoldeEcus(),
                "poules", ferme.getNbPoules() == null ? 0 : ferme.getNbPoules(),
                "vaches", ferme.getNbVaches() == null ? 0 : ferme.getNbVaches(),
                "lapins", ferme.getNbLapins() == null ? 0 : ferme.getNbLapins()
            ))
            .toList();
    }
}
