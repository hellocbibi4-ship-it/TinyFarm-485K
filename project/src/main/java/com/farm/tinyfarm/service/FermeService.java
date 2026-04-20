package com.farm.tinyfarm.service;

import com.farm.tinyfarm.model.Animal;
import com.farm.tinyfarm.model.Ferme;
import com.farm.tinyfarm.model.Remise;
import com.farm.tinyfarm.model.TypeAnimal;
import com.farm.tinyfarm.model.TypeRole;
import com.farm.tinyfarm.model.TypeSexe;
import com.farm.tinyfarm.model.TypeStade;
import com.farm.tinyfarm.outils.Utilitaires;
import com.farm.tinyfarm.repository.AnimalRepository;
import com.farm.tinyfarm.repository.FermeRepository;
import com.farm.tinyfarm.repository.MarcheRepository;
import com.farm.tinyfarm.repository.RemiseRepository;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Service;

@Service
public class FermeService {
    public static final int GAME_DAY_DURATION_SECONDS = 60;
    public static final int DAILY_COMMUNITY_PURCHASE_LIMIT = 12;
    public static final int COMMUNITY_BUYBACK_EGG_PRICE = 8;
    public static final int COMMUNITY_BUYBACK_MILK_PRICE = 2;
    public static final int COMMUNITY_BUYBACK_RABBIT_PRICE = 25;
    public static final int INITIAL_CHICKEN_COUNT = 4;
    public static final int MAX_HENS_PER_ROOSTER_PER_DAY = 5;
    public static final int ROOSTER_MIN_REPRODUCTIVE_AGE_DAYS = 5;
    public static final float ROOSTER_MIN_WEIGHT_KG = 2.5f;
    public static final float ROOSTER_MAX_WEIGHT_KG = 3.5f;
    public static final int CHICK_TO_ADULT_DAYS = 4;
    public static final float CHICK_DEFAULT_WEIGHT_KG = 0.5f;
    public static final float CHICKEN_FEED_WEIGHT_GAIN_KG = 0.5f;
    public static final float CHICKEN_MAX_WEIGHT_KG = 3.5f;
    // Petites listes de prenoms pour eviter les animaux tous nommes
    // "Poule 1" ou "Vache 1" dans l'interface.
    private static final List<String> COW_NAMES = List.of(
        "Marguerite", "Rosalie", "Belle", "Capucine", "Noisette",
        "Luna", "Biscotte", "Cannelle", "Bijou", "Praline"
    );
    private static final List<String> CHICKEN_NAMES = List.of(
        "Cocotte", "Plume", "Pistache", "Nugget", "Pompon",
        "Biscuit", "Pepette", "Mimosa", "Choupette", "Perline"
    );

    private final FermeRepository fermeRepository;
    private final RemiseRepository remiseRepository;
    private final AnimalRepository animalRepository;
    private final MarcheRepository marcheRepository;

    public FermeService(
        FermeRepository fermeRepository,
        RemiseRepository remiseRepository,
        AnimalRepository animalRepository,
        MarcheRepository marcheRepository
    ) {
        this.fermeRepository = fermeRepository;
        this.remiseRepository = remiseRepository;
        this.animalRepository = animalRepository;
        this.marcheRepository = marcheRepository;
    }

    @Transactional
    public Ferme create(Ferme ferme) {
        // Une nouvelle ferme repart avec un etat jouable immediat :
        // animaux de base, solde initial et quota journalier plein.
        Utilitaires.validationNom(ferme.getNom());
        ferme.setScore(0);
        ferme.setSoldeEcus(1500);
        ferme.setHibernation(false);
        ferme.setDateCreation(LocalDateTime.now());
        ferme.setDerniereCo(LocalDateTime.now());
        ferme.setJourActuel(1);
        ferme.setAchatsCollectiviteRestants(DAILY_COMMUNITY_PURCHASE_LIMIT);
        ferme.setNbVaches(1);
        ferme.setNbPoules(INITIAL_CHICKEN_COUNT);
        ferme.setNbLapins(8);
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

    @Transactional
    public void deleteById(Integer id) {
        deleteFarmWithDependencies(id);
    }

    @Transactional
    public Ferme resetToDefaults(Integer idFerme) {
        // Contrairement a une suppression/recreation complete, ce reset agit
        // "en place" sur la ferme courante pour rester fiable cote front.
        Ferme ferme = fermeRepository.findById(idFerme)
            .orElseThrow(() -> new RuntimeException("Ferme introuvable"));

        marcheRepository.deleteByFerme_IdFerme(idFerme);
        animalRepository.deleteByFerme_IdFerme(idFerme);

        Remise remise = remiseRepository.findById(idFerme).orElseGet(() -> {
            Remise nouvelleRemise = new Remise();
            nouvelleRemise.setFerme(ferme);
            return remiseRepository.save(nouvelleRemise);
        });

        ferme.setScore(0);
        ferme.setSoldeEcus(1500);
        ferme.setHibernation(false);
        ferme.setDateCreation(LocalDateTime.now());
        ferme.setDerniereCo(LocalDateTime.now());
        ferme.setJourActuel(1);
        ferme.setAchatsCollectiviteRestants(DAILY_COMMUNITY_PURCHASE_LIMIT);
        ferme.setNbVaches(1);
        ferme.setNbPoules(INITIAL_CHICKEN_COUNT);
        ferme.setNbLapins(8);
        ferme.setNbLapinsMalades(0);
        ferme.setNbVachesAffamees(0);
        ferme.setNbVachesAssoiffees(0);
        ferme.setNbPouleAffamees(0);
        ferme.setNbPouleAssoiffees(0);
        ferme.setNbLapinsAffames(0);
        ferme.setNbLapinsAssoiffes(0);

        remise.setStockOeuf(0);
        remise.setStockLait(0);
        remise.setStockLapin(0);
        remise.setStockNourriture(0);
        remise.setStockEau(0);
        remise.setStockSavon(0);
        remise.setStockSeringue(0);
        remise.setStockPaille(0);
        remiseRepository.save(remise);

        // Les compteurs metier sur Ferme sont remis avant de recreer
        // exactement 3 poules + 1 coq, 1 vache et 8 lapins via synchronisation.
        synchroniserAnimaux(ferme);
        synchroniserCompteursDepuisAnimaux(ferme);
        return fermeRepository.save(ferme);
    }

    @Transactional
    public void deleteFarmWithDependencies(Integer idFerme) {
        if (idFerme == null || !fermeRepository.existsById(idFerme)) {
            return;
        }

        // Une recreation de ferme doit vraiment repartir de zero,
        // y compris pour le marche et les dependances directes.
        marcheRepository.deleteByFerme_IdFerme(idFerme);
        animalRepository.deleteByFerme_IdFerme(idFerme);

        if (remiseRepository.existsById(idFerme)) {
            remiseRepository.deleteById(idFerme);
        }

        fermeRepository.deleteById(idFerme);
        fermeRepository.flush();
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

        // La vache est volontairement unique dans une ferme.
        if (animalType == TypeAnimal.VACHE
            && animalRepository.countByFerme_IdFermeAndTypeAnimal(idFerme, TypeAnimal.VACHE) > 0) {
            throw new IllegalArgumentException("Une ferme ne peut posseder qu'une seule vache");
        }

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
        changed = garantirPresenceCoqReproducteur(ferme) || changed;
        changed = normaliserVolaillesAdultesLegacy(ferme) || changed;
        changed = synchroniserCompteursDepuisAnimaux(ferme) || changed;
        return changed ? fermeRepository.save(ferme) : ferme;
    }

    @Transactional
    public Ferme passerJour(Integer idFerme) {
        Ferme ferme = fermeRepository.findById(idFerme)
            .orElseThrow(() -> new RuntimeException("Ferme introuvable"));
        normalizeDailyState(ferme);

        Remise remise = remiseRepository.findById(idFerme).orElseGet(() -> {
            Remise nouvelleRemise = new Remise();
            nouvelleRemise.setFerme(ferme);
            return remiseRepository.save(nouvelleRemise);
        });

        List<Animal> animaux = animalRepository.findByFerme_IdFermeOrderByIdAnimalAsc(idFerme);

        int oeufsAIncuber = Math.max(0, remise.getStockOeuf());
        if (oeufsAIncuber > 0) {
            List<Animal> nouveauxPoussins = creerPoussinsDepuisOeufs(ferme, oeufsAIncuber);
            if (!nouveauxPoussins.isEmpty()) {
                animalRepository.saveAll(nouveauxPoussins);
                animaux.addAll(nouveauxPoussins);
            }
            remise.setStockOeuf(0);
        }

        // La production est calculee avant la degradation du nouveau jour :
        // un animal sale ou malade ne produit deja plus.
        int nbOeufsPondus = calculerNombreOeufsDuJour(animaux);
        int nbVaches = (int) animaux.stream()
            .filter(animal -> animal.getTypeAnimal() == TypeAnimal.VACHE)
            .filter(this::peutProduireLait)
            .count();

        if (nbOeufsPondus > 0) {
            remise.setStockOeuf(remise.getStockOeuf() + nbOeufsPondus);
        }
        if (nbVaches > 0) {
            remise.setStockLait(remise.getStockLait() + nbVaches);
        }

        List<Animal> animauxMorts = appliquerEtatsQuotidiensAnimaux(animaux);
        ferme.setJourActuel(ferme.getJourActuel() + 1);
        ferme.setAchatsCollectiviteRestants(DAILY_COMMUNITY_PURCHASE_LIMIT);

        if (!animauxMorts.isEmpty()) {
            animalRepository.deleteAll(animauxMorts);
        }
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
        return animalRepository.findByFerme_IdFermeOrderByIdAnimalAsc(idFerme);
    }

    @Transactional
    public Ferme vendreStockACollectivite(Integer idFerme, String produit, Integer quantite) {
        Ferme ferme = fermeRepository.findById(idFerme)
            .orElseThrow(() -> new RuntimeException("Ferme introuvable"));

        if (quantite == null || quantite <= 0) {
            throw new IllegalArgumentException("La quantite doit etre superieure a 0");
        }

        String produitNormalise = produit == null ? "" : produit.trim().toUpperCase();
        int prixUnitaire = getCommunityBuybackPrice(produitNormalise);
        Remise remise = remiseRepository.findById(idFerme).orElseGet(() -> {
            Remise nouvelleRemise = new Remise();
            nouvelleRemise.setFerme(ferme);
            return remiseRepository.save(nouvelleRemise);
        });

        // La collectivite rachete a prix fixe et sans creer d'offre persistante.
        switch (produitNormalise) {
            case "OEUF", "OEUFS" -> remise.setStockOeuf(retirerDepuisRemise(remise.getStockOeuf(), quantite, "oeufs"));
            case "LAIT" -> remise.setStockLait(retirerDepuisRemise(remise.getStockLait(), quantite, "lait"));
            case "LAPIN", "LAPINS" -> retirerLapinsVivants(idFerme, quantite);
            default -> throw new IllegalArgumentException("Produit non pris en charge par la collectivite");
        }

        ferme.setSoldeEcus((ferme.getSoldeEcus() == null ? 0 : ferme.getSoldeEcus()) + (prixUnitaire * quantite));
        synchroniserCompteursDepuisAnimaux(ferme);
        return fermeRepository.save(ferme);
    }

    @Transactional
    public void payerActionAnimale(Integer idFerme, String typeAnimal, String action) {
        Ferme ferme = fermeRepository.findById(idFerme)
            .orElseThrow(() -> new RuntimeException("Ferme introuvable"));

        // Les soins coutent des ecus en plus de l'objet consomme.
        int cout = getAnimalActionCost(typeAnimal, action);
        int solde = ferme.getSoldeEcus() == null ? 0 : ferme.getSoldeEcus();
        if (solde < cout) {
            throw new IllegalArgumentException("Solde insuffisant pour cette action");
        }

        ferme.setSoldeEcus(solde - cout);
        fermeRepository.save(ferme);
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

    public void nourrirVolaille(Animal animal) {
        if (animal == null || animal.getTypeAnimal() != TypeAnimal.POULE) {
            return;
        }

        animal.setJaugeFaim(100);
        float nouveauPoids = Math.min(CHICKEN_MAX_WEIGHT_KG, animal.getPoids() + CHICKEN_FEED_WEIGHT_GAIN_KG);
        animal.setPoids(nouveauPoids);
    }

    private void consommerAchatCollectivite(Ferme ferme) {
        normalizeDailyState(ferme);
        if (ferme.getAchatsCollectiviteRestants() <= 0) {
            throw new IllegalArgumentException("Quota d'achats de la collectivite atteint pour aujourd'hui");
        }
        ferme.setAchatsCollectiviteRestants(ferme.getAchatsCollectiviteRestants() - 1);
    }

    private boolean normalizeDailyState(Ferme ferme) {
        // Ce garde-fou corrige les anciennes fermes ou les bases locales
        // qui n'ont pas encore les champs journaliers propres.
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
        // Tous les animaux naissent dans un etat neutre et complet :
        // pas malades, nourris, propres et hydratés.
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
        animal.setJoursMaladeConsecutifs(0);
        animal.setEstMalade(false);
        animal.setAMange(false);
        animal.setAEteTraite(false);

        if (typeAnimal == TypeAnimal.POULE) {
            initialiserProfilVolaille(ferme, animal);
        } else {
            animal.setSexe(TypeSexe.INCONNU);
            animal.setRole(TypeRole.ELEVAGE);
        }

        return animal;
    }

    private String buildAnimalName(TypeAnimal typeAnimal, int index) {
        return switch (typeAnimal) {
            case VACHE -> buildRandomAnimalName(COW_NAMES, "Vache", index);
            case POULE -> buildRandomAnimalName(CHICKEN_NAMES, "Poule", index);
            case LAPIN -> "Lapin " + index;
        };
    }

    private String buildRandomAnimalName(List<String> candidates, String fallbackLabel, int index) {
        if (candidates.isEmpty()) {
            return fallbackLabel + " " + index;
        }

        // On garde l'index en suffixe pour rester lisible meme si deux
        // animaux tirent le meme prenom aleatoire.
        String baseName = candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
        return baseName + " " + index;
    }

    private float defaultWeight(TypeAnimal typeAnimal) {
        return switch (typeAnimal) {
            case VACHE -> 500f;
            case POULE -> 2f;
            case LAPIN -> 2f;
        };
    }

    private boolean peutPondre(Animal animal) {
        return !animal.estMalade() && animal.getJaugeProprete() >= 100;
    }

    private int calculerNombreOeufsDuJour(List<Animal> animaux) {
        int nbPondeusesEligibles = (int) animaux.stream()
            .filter(this::estPoulePondeuseEligible)
            .count();

        if (nbPondeusesEligibles <= 0) {
            return 0;
        }

        int nbCoqsReproducteurs = (int) animaux.stream()
            .filter(this::estCoqReproducteur)
            .count();

        if (nbCoqsReproducteurs <= 0) {
            return 0;
        }

        int capaciteMaxPonte = nbCoqsReproducteurs * MAX_HENS_PER_ROOSTER_PER_DAY;
        int nbPondeusesActives = Math.min(nbPondeusesEligibles, capaciteMaxPonte);

        int totalOeufs = 0;
        for (int index = 0; index < nbPondeusesActives; index++) {
            // Une poule peut pondre 0, 1 ou 2 oeufs sur une journee.
            totalOeufs += ThreadLocalRandom.current().nextInt(0, 3);
        }
        return totalOeufs;
    }

    private boolean estPoulePondeuseEligible(Animal animal) {
        return animal.getTypeAnimal() == TypeAnimal.POULE
            && animal.getSexe() == TypeSexe.FEMELLE
            && animal.getAge() >= ROOSTER_MIN_REPRODUCTIVE_AGE_DAYS
            && animal.getPoids() >= ROOSTER_MIN_WEIGHT_KG
            && animal.getPoids() <= CHICKEN_MAX_WEIGHT_KG
            && animal.getJaugeFaim() >= 100
            && peutPondre(animal);
    }

    private boolean estCoqReproducteur(Animal animal) {
        if (animal.getTypeAnimal() != TypeAnimal.POULE) {
            return false;
        }

        if (animal.getSexe() != TypeSexe.MALE) {
            return false;
        }

        float poids = animal.getPoids();
        return animal.getAge() >= ROOSTER_MIN_REPRODUCTIVE_AGE_DAYS
            && poids >= ROOSTER_MIN_WEIGHT_KG
            && poids <= ROOSTER_MAX_WEIGHT_KG
            && !animal.estMalade()
            && animal.getJaugeProprete() >= 100;
    }

    private void initialiserProfilVolaille(Ferme ferme, Animal animal) {
        boolean hasRooster = animalRepository
            .findByFerme_IdFermeAndTypeAnimalOrderByIdAnimalAsc(ferme.getIdFerme(), TypeAnimal.POULE)
            .stream()
            .anyMatch(existingChicken -> existingChicken.getSexe() == TypeSexe.MALE);

        if (!hasRooster) {
            animal.setSexe(TypeSexe.MALE);
            animal.setRole(TypeRole.REPRODUCTEUR);
            animal.setAge(ROOSTER_MIN_REPRODUCTIVE_AGE_DAYS);
            animal.setPoids(Math.min(ROOSTER_MIN_WEIGHT_KG, ROOSTER_MAX_WEIGHT_KG));
            animal.setStade(TypeStade.ADULTE);
            return;
        }

        animal.setSexe(TypeSexe.FEMELLE);
        animal.setRole(TypeRole.PONDEUSE);
        animal.setAge(ROOSTER_MIN_REPRODUCTIVE_AGE_DAYS);
        animal.setPoids(Math.max(ROOSTER_MIN_WEIGHT_KG, Math.min(animal.getPoids(), CHICKEN_MAX_WEIGHT_KG)));
        animal.setStade(TypeStade.ADULTE);
    }

    private List<Animal> creerPoussinsDepuisOeufs(Ferme ferme, int quantiteOeufs) {
        List<Animal> poussins = new ArrayList<>();
        if (quantiteOeufs <= 0) {
            return poussins;
        }

        long existingCount = animalRepository.countByFerme_IdFermeAndTypeAnimal(ferme.getIdFerme(), TypeAnimal.POULE);
        for (int index = 0; index < quantiteOeufs; index++) {
            Animal poussin = new Animal();
            poussin.setFerme(ferme);
            poussin.setTypeAnimal(TypeAnimal.POULE);
            poussin.setNom("Poussin " + (existingCount + index + 1));
            poussin.setPoids(CHICK_DEFAULT_WEIGHT_KG);
            poussin.setAge(0);
            poussin.setStade(TypeStade.ENFANT);
            poussin.setSexe(TypeSexe.INCONNU);
            poussin.setRole(TypeRole.ELEVAGE);
            poussin.setJaugeSante(100);
            poussin.setJaugeFaim(100);
            poussin.setJaugeHydratation(100);
            poussin.setJaugeProprete(100);
            poussin.setJoursMaladeConsecutifs(0);
            poussin.setEstMalade(false);
            poussin.setAMange(false);
            poussin.setAEteTraite(false);
            poussins.add(poussin);
        }

        return poussins;
    }

    private void actualiserCycleDeVieVolaille(Animal animal) {
        if (animal.getTypeAnimal() != TypeAnimal.POULE) {
            return;
        }

        animal.setAge(animal.getAge() + 1);

        if (animal.getStade() == TypeStade.ENFANT) {
            if (animal.getAge() >= CHICK_TO_ADULT_DAYS) {
                animal.setStade(TypeStade.ADULTE);
                TypeSexe sexeAdulte = ThreadLocalRandom.current().nextBoolean() ? TypeSexe.MALE : TypeSexe.FEMELLE;
                animal.setSexe(sexeAdulte);
            }
        }

        if (animal.getAge() < ROOSTER_MIN_REPRODUCTIVE_AGE_DAYS || animal.getPoids() < ROOSTER_MIN_WEIGHT_KG) {
            animal.setRole(TypeRole.ELEVAGE);
            return;
        }

        if (animal.getSexe() == TypeSexe.FEMELLE) {
            boolean peutPondreAujourdhui = !animal.estMalade()
                && animal.getJaugeProprete() >= 100
                && animal.getJaugeFaim() >= 100
                && animal.getPoids() <= CHICKEN_MAX_WEIGHT_KG;
            animal.setRole(peutPondreAujourdhui ? TypeRole.PONDEUSE : TypeRole.ELEVAGE);
            return;
        }

        if (animal.getSexe() == TypeSexe.MALE) {
            animal.setRole(estCoqReproducteur(animal) ? TypeRole.REPRODUCTEUR : TypeRole.ELEVAGE);
            return;
        }

        animal.setRole(TypeRole.ELEVAGE);
    }

    private boolean garantirPresenceCoqReproducteur(Ferme ferme) {
        List<Animal> volailles = animalRepository.findByFerme_IdFermeAndTypeAnimalOrderByIdAnimalAsc(
            ferme.getIdFerme(),
            TypeAnimal.POULE
        );

        if (volailles.isEmpty()) {
            return false;
        }

        boolean hasRooster = volailles.stream().anyMatch(chicken -> chicken.getSexe() == TypeSexe.MALE);
        if (hasRooster) {
            return false;
        }

        // Migration douce des anciennes fermes: on convertit la premiere volaille en coq.
        Animal rooster = volailles.get(0);
        rooster.setSexe(TypeSexe.MALE);
        rooster.setRole(TypeRole.REPRODUCTEUR);
        rooster.setAge(Math.max(rooster.getAge(), ROOSTER_MIN_REPRODUCTIVE_AGE_DAYS));
        rooster.setPoids(Math.max(ROOSTER_MIN_WEIGHT_KG, Math.min(rooster.getPoids(), ROOSTER_MAX_WEIGHT_KG)));

        for (int index = 1; index < volailles.size(); index++) {
            Animal hen = volailles.get(index);
            if (hen.getSexe() != TypeSexe.MALE) {
                hen.setSexe(TypeSexe.FEMELLE);
                hen.setRole(TypeRole.PONDEUSE);
            }
        }

        animalRepository.saveAll(volailles);
        return true;
    }

    private boolean normaliserVolaillesAdultesLegacy(Ferme ferme) {
        List<Animal> volailles = animalRepository.findByFerme_IdFermeAndTypeAnimalOrderByIdAnimalAsc(
            ferme.getIdFerme(),
            TypeAnimal.POULE
        );

        if (volailles.isEmpty()) {
            return false;
        }

        boolean changed = false;
        for (Animal volaille : volailles) {
            if (volaille.getStade() == TypeStade.ENFANT) {
                continue;
            }

            if (volaille.getAge() < ROOSTER_MIN_REPRODUCTIVE_AGE_DAYS) {
                volaille.setAge(ROOSTER_MIN_REPRODUCTIVE_AGE_DAYS);
                changed = true;
            }

            float poidsCible = Math.max(ROOSTER_MIN_WEIGHT_KG, Math.min(volaille.getPoids(), CHICKEN_MAX_WEIGHT_KG));
            if (Float.compare(volaille.getPoids(), poidsCible) != 0) {
                volaille.setPoids(poidsCible);
                changed = true;
            }
        }

        if (!changed) {
            return false;
        }

        animalRepository.saveAll(volailles);
        return true;
    }

    private boolean peutProduireLait(Animal animal) {
        return !animal.estMalade() && animal.getJaugeProprete() >= 100;
    }

    private List<Animal> appliquerEtatsQuotidiensAnimaux(List<Animal> animaux) {
        // Le clapier a une logique de mortalite de groupe distincte,
        // appliquee avant la degradation individuelle du nouveau jour.
        List<Animal> animauxMorts = gererMortaliteClapier(animaux);

        if (!animauxMorts.isEmpty()) {
            animaux.removeAll(animauxMorts);
        }

        for (Animal animal : animaux) {
            actualiserCycleDeVieVolaille(animal);

            boolean etaitAffame = animal.getJaugeFaim() < 100;
            boolean etaitAssoiffe = animal.getJaugeHydratation() < 100;
            boolean etaitMalade = animal.estMalade();

            if (etaitMalade) {
                animal.setJoursMaladeConsecutifs(animal.getJoursMaladeConsecutifs() + 1);
            } else {
                animal.setJoursMaladeConsecutifs(0);
            }

            if (etaitAffame) {
                // Regle choisie pour le projet : ne pas nourrir un animal
                // deja affame le rend malade le jour suivant.
                animal.setEstMalade(true);
                animal.setJaugeSante(0);
                if (!etaitMalade) {
                    animal.setJoursMaladeConsecutifs(1);
                }
            }

            if (etaitAssoiffe) {
                // Regle choisie pour le projet : ne pas abreuver un animal
                // deja assoiffe le rend sale le jour suivant.
                animal.setJaugeProprete(0);
            }

            animal.setJaugeFaim(0);
            animal.setJaugeHydratation(0);

            if ((animal.getTypeAnimal() == TypeAnimal.POULE || animal.getTypeAnimal() == TypeAnimal.VACHE)
                && animal.getJoursMaladeConsecutifs() >= 4) {
                animauxMorts.add(animal);
            }
        }

        animaux.removeAll(animauxMorts);
        return animauxMorts;
    }

    private List<Animal> gererMortaliteClapier(List<Animal> animaux) {
        // Le clapier est traite comme un ensemble : on retire une partie
        // des lapins si l'etat sale et/ou malade a ete laisse trainer.
        List<Animal> animauxMorts = new ArrayList<>();
        List<Animal> lapinsVivants = animaux.stream()
            .filter(animal -> animal.getTypeAnimal() == TypeAnimal.LAPIN)
            .toList();

        if (lapinsVivants.isEmpty()) {
            return animauxMorts;
        }

        List<Animal> lapinsSales = lapinsVivants.stream()
            .filter(animal -> animal.getJaugeProprete() < 100)
            .limit(calculerPertesClapier(lapinsVivants.size()))
            .toList();
        animauxMorts.addAll(lapinsSales);

        List<Animal> lapinsRestants = lapinsVivants.stream()
            .filter(animal -> !animauxMorts.contains(animal))
            .toList();

        List<Animal> lapinsMalades = lapinsRestants.stream()
            .filter(Animal::estMalade)
            .limit(calculerPertesClapier(lapinsRestants.size()))
            .toList();
        animauxMorts.addAll(lapinsMalades);

        return animauxMorts;
    }

    private int calculerPertesClapier(int population) {
        if (population <= 0) {
            return 0;
        }
        return (int) Math.ceil(population * 0.25d);
    }

    private boolean synchroniserCompteursDepuisAnimaux(Ferme ferme) {
        return synchroniserCompteursDepuisAnimaux(
            ferme,
            animalRepository.findByFerme_IdFermeOrderByIdAnimalAsc(ferme.getIdFerme())
        );
    }

    private boolean synchroniserCompteursDepuisAnimaux(Ferme ferme, List<Animal> animaux) {
        // Les compteurs historiques sur Ferme servent encore au front ;
        // on les reconstruit donc depuis la liste d'animaux canonique.
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

    private int retirerDepuisRemise(Integer stockActuel, int quantite, String libelle) {
        int stock = stockActuel == null ? 0 : stockActuel;
        if (stock < quantite) {
            throw new IllegalArgumentException("Stock insuffisant pour " + libelle);
        }
        return stock - quantite;
    }

    private void retirerLapinsVivants(Integer idFerme, int quantite) {
        List<Animal> lapins = animalRepository.findByFerme_IdFermeAndTypeAnimalOrderByIdAnimalAsc(idFerme, TypeAnimal.LAPIN);
        if (lapins.size() < quantite) {
            throw new IllegalArgumentException("Stock insuffisant pour lapins");
        }

        animalRepository.deleteAll(lapins.subList(0, quantite));
    }

    private int getCommunityBuybackPrice(String produitNormalise) {
        return switch (produitNormalise) {
            case "OEUF", "OEUFS" -> COMMUNITY_BUYBACK_EGG_PRICE;
            case "LAIT" -> COMMUNITY_BUYBACK_MILK_PRICE;
            case "LAPIN", "LAPINS" -> COMMUNITY_BUYBACK_RABBIT_PRICE;
            default -> throw new IllegalArgumentException("Produit non pris en charge par la collectivite");
        };
    }

    private int getAnimalActionCost(String typeAnimal, String action) {
        String normalizedType = typeAnimal == null ? "" : typeAnimal.trim().toLowerCase();
        String normalizedAction = action == null ? "" : action.trim().toLowerCase();

        return switch (normalizedType) {
            case "poule", "poules" -> switch (normalizedAction) {
                case "feed" -> 3;
                case "water" -> 1;
                case "clean" -> 3;
                case "heal" -> 6;
                default -> throw new IllegalArgumentException("Action animale inconnue");
            };
            case "lapin", "lapins" -> switch (normalizedAction) {
                case "feed" -> 5;
                case "water" -> 2;
                case "clean" -> 3;
                case "heal" -> 6;
                default -> throw new IllegalArgumentException("Action animale inconnue");
            };
            case "vache", "vaches" -> switch (normalizedAction) {
                case "feed" -> 5;
                case "water" -> 2;
                case "clean" -> 3;
                case "heal" -> 6;
                default -> throw new IllegalArgumentException("Action animale inconnue");
            };
            default -> throw new IllegalArgumentException("Type d'animal inconnu");
        };
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
            .filter(ferme -> {
                if (ferme.getUtilisateur() == null || ferme.getUtilisateur().getGithubUsername() == null) {
                    return false;
                }

                String farmName = ferme.getNom() == null ? "" : ferme.getNom().toLowerCase();
                String username = ferme.getUtilisateur().getGithubUsername().toLowerCase().trim();

                if (username.isBlank()) {
                    return false;
                }

                // Garde-fou front : on masque les anciennes donnees de test
                // du marche qui ont pu creer des fermes temporaires seller-*.
                return !farmName.startsWith("seller-") && !username.startsWith("seller-");
            })
            .sorted(
                Comparator.comparing((Ferme ferme) -> ferme.getSoldeEcus() == null ? 0 : ferme.getSoldeEcus())
                    .reversed()
                    .thenComparing(ferme -> ferme.getNom() == null ? "" : ferme.getNom())
            )
            .map(ferme -> Map.<String, Object>of(
                "name", ferme.getUtilisateur().getGithubUsername(),
                "money", ferme.getSoldeEcus() == null ? 0 : ferme.getSoldeEcus(),
                "poules", ferme.getNbPoules() == null ? 0 : ferme.getNbPoules(),
                "vaches", ferme.getNbVaches() == null ? 0 : ferme.getNbVaches(),
                "lapins", ferme.getNbLapins() == null ? 0 : ferme.getNbLapins()
            ))
            .toList();
    }
}
