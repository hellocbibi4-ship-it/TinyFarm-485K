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
import java.time.LocalDate;
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
    public static final int MAX_RABBIT_POPULATION = 50;
    private static final float COW_DEFAULT_WEIGHT = 1f;
    private static final float COW_DAILY_GRASS_WEIGHT_GAIN = 5f;
    private static final float COW_FEED_WEIGHT_GAIN = 3f;
    private static final float COW_WATER_WEIGHT_GAIN = 1f;
    private static final float CHICKEN_DEFAULT_WEIGHT = 2f;
    private static final float CHICKEN_MIN_ADULT_WEIGHT = 2.5f;
    private static final float CHICKEN_MAX_WEIGHT = 3.5f;
    private static final float CHICKEN_FEED_WEIGHT_GAIN = 0.5f;
    private static final float CHICKEN_WATER_WEIGHT_GAIN = 0.15f;
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
        ferme.setAchatsJour(0);
        ferme.setDateDernierAchat(LocalDate.now());
        ferme.setHibernation(false);
        ferme.setDateCreation(LocalDateTime.now());
        ferme.setDerniereCo(LocalDateTime.now());
        ferme.setJourActuel(1);
        ferme.setAchatsCollectiviteRestants(DAILY_COMMUNITY_PURCHASE_LIMIT);
        ferme.setNbVaches(1);
        ferme.setNbPoules(4);
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
        ferme.setNbPoules(4);
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

        // Les compteurs metier sur Ferme sont remis avant de recréer
        // exactement 4 poules, 1 vache et 8 lapins via synchronisation.
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

        if (animalType == TypeAnimal.LAPIN
            && animalRepository.countByFerme_IdFermeAndTypeAnimal(idFerme, TypeAnimal.LAPIN) >= MAX_RABBIT_POPULATION) {
            throw new IllegalArgumentException("Le clapier a atteint sa capacite maximale de 50 lapins");
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

    //Procédure de réinitialisation des achats journaliers
    @Transactional
    public void ajouterAchats(Integer idFerme, int quantite) {
        Ferme ferme = fermeRepository.findById(idFerme)
            .orElseThrow(() -> new RuntimeException("Ferme introuvable"));

        int achatsJour = ferme.getAchatsJour() == null ? 0 : ferme.getAchatsJour();
        LocalDate aujourdHui = LocalDate.now();

        if (ferme.getDateDernierAchat() == null || !ferme.getDateDernierAchat().equals(aujourdHui)) {
            achatsJour = 0;
        }

        ferme.setAchatsJour(achatsJour + quantite);
        ferme.setDateDernierAchat(aujourdHui);
    }

    @Transactional
    public int getAchatsJourActuels(Integer idFerme) {
        Ferme ferme = fermeRepository.findById(idFerme)
            .orElseThrow(() -> new RuntimeException("Ferme introuvable"));

        LocalDate aujourdHui = LocalDate.now();
        int achatsJour = ferme.getAchatsJour() == null ? 0 : ferme.getAchatsJour();

        if (ferme.getDateDernierAchat() == null || !ferme.getDateDernierAchat().equals(aujourdHui)) {
            ferme.setAchatsJour(0);
            ferme.setDateDernierAchat(aujourdHui);
            return 0;
        }

        return achatsJour;
    }

    public Ferme getById(Integer id) {
        Ferme ferme = fermeRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Ferme introuvable"));
        boolean changed = normalizeDailyState(ferme);
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
        // La production est calculee avant la degradation du nouveau jour :
        // un animal sale ou malade ne produit deja plus.
        int nbOeufsProduits = calculerProductionOeufs(animaux);
        int nbVaches = (int) animaux.stream()
            .filter(animal -> animal.getTypeAnimal() == TypeAnimal.VACHE)
            .filter(this::peutProduireLait)
            .count();

        if (nbOeufsProduits > 0) {
            remise.setStockOeuf(remise.getStockOeuf() + nbOeufsProduits);
        }
        if (nbVaches > 0) {
            remise.setStockLait(remise.getStockLait() + nbVaches);
        }

        // Regle simplifiee choisie ici : chaque nouveau jour, la vache
        // broute automatiquement et gagne 5 kg.
        animaux.stream()
            .filter(animal -> animal.getTypeAnimal() == TypeAnimal.VACHE)
            .forEach(animal -> animal.setPoids(Math.max(0f, animal.getPoids() + COW_DAILY_GRASS_WEIGHT_GAIN)));

        // Les naissances sont calculees avant la degradation quotidienne pour
        // que les conditions de reproduction (jaugeFaim, jaugeHydratation)
        // refletent l'etat reel des animaux avant la remise a zero des jauges.
        ajouterNaissancesLapins(ferme, animaux);
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
            case "OEUF", "OEUFS" -> {
                remise.setStockOeuf(retirerDepuisRemise(remise.getStockOeuf(), quantite, "oeufs"));
                ferme.setVOeuf((ferme.getVOeuf() != null ? ferme.getVOeuf() : 0) + quantite);
            }
            case "LAIT" -> {
                remise.setStockLait(retirerDepuisRemise(remise.getStockLait(), quantite, "lait"));
                ferme.setVLact((ferme.getVLact() != null ? ferme.getVLact() : 0) + quantite);
            }
            case "LAPIN", "LAPINS" -> {
                retirerLapinsVivants(idFerme, quantite);
                ferme.setVLapCop((ferme.getVLapCop() != null ? ferme.getVLapCop() : 0) + quantite);
            }
            default -> throw new IllegalArgumentException("Produit non pris en charge par la collectivite");
        }

        ferme.setSoldeEcus((ferme.getSoldeEcus() == null ? 0 : ferme.getSoldeEcus()) + (prixUnitaire * quantite));
        synchroniserCompteursDepuisAnimaux(ferme);
        remiseRepository.save(remise);
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

    private Animal creerAnimalPourFerme(Ferme ferme, TypeAnimal typeAnimal, int displayIndex) {
        // Tous les animaux naissent dans un etat neutre et complet :
        // pas malades, nourris, propres et hydratÃ©s.
        Animal animal = new Animal();
        animal.setFerme(ferme);
        animal.setTypeAnimal(typeAnimal);
        animal.setNom(buildAnimalName(typeAnimal, displayIndex));
        animal.setPoids(defaultWeight(typeAnimal));
        animal.setAge(1);
        animal.setStade(TypeStade.ENFANT);
        animal.setSexe(defaultSexe(typeAnimal));
        animal.setRole(defaultRole(typeAnimal));
        animal.setJaugeSante(100);
        animal.setJaugeFaim(100);
        animal.setJaugeHydratation(100);
        animal.setJaugeProprete(100);
        animal.setJoursMaladeConsecutifs(0);
        animal.setJoursJeuneConsecutifs(0);
        animal.setEstMalade(false);
        animal.setAMange(false);
        animal.setABu(false);
        animal.setAEteTraite(false);
        return animal;
    }

    private Animal creerAnimalPourFerme(Ferme ferme, TypeAnimal typeAnimal) {
        // Tous les animaux naissent dans un etat neutre et complet :
        // pas malades, nourris, propres et hydratés.
        long existingCount = animalRepository.countByFerme_IdFermeAndTypeAnimal(ferme.getIdFerme(), typeAnimal);
        return creerAnimalPourFerme(ferme, typeAnimal, (int) existingCount + 1);
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
            case VACHE -> COW_DEFAULT_WEIGHT;
            case POULE -> CHICKEN_DEFAULT_WEIGHT;
            case LAPIN -> 2f;
        };
    }

    private TypeSexe defaultSexe(TypeAnimal typeAnimal) {
        return switch (typeAnimal) {
            case VACHE -> TypeSexe.FEMELLE;
            case POULE, LAPIN -> TypeSexe.INCONNU;
        };
    }

    private TypeRole defaultRole(TypeAnimal typeAnimal) {
        return switch (typeAnimal) {
            case VACHE, LAPIN -> TypeRole.ELEVAGE;
            case POULE -> TypeRole.ELEVAGE;
        };
    }

    public void appliquerEffetsNourrirAnimal(Animal animal) {
        if (animal == null) {
            return;
        }

        if (animal.getTypeAnimal() == TypeAnimal.POULE) {
            // Regle du sujet pour les volailles : manger fait grossir
            // et casse la serie de jours de jeune.
            animal.setPoids(bornerPoidsPoule(animal.getPoids() + CHICKEN_FEED_WEIGHT_GAIN));
            animal.setJoursJeuneConsecutifs(0);
            mettreAJourSexeEtRolePoule(animal);
            return;
        }

        if (animal.getTypeAnimal() == TypeAnimal.VACHE) {
            // Nourrir la vache avec une botte de paille lui ajoute 3 kg.
            animal.setPoids(Math.max(0f, animal.getPoids() + COW_FEED_WEIGHT_GAIN));
            animal.setAMange(true);
        }
    }

    public void appliquerEffetsAbreuverAnimal(Animal animal) {
        if (animal == null) {
            return;
        }

        if (animal.getTypeAnimal() == TypeAnimal.POULE) {
            // L'eau fait aussi gagner un peu de poids a la poule.
            animal.setPoids(bornerPoidsPoule(animal.getPoids() + CHICKEN_WATER_WEIGHT_GAIN));
            mettreAJourSexeEtRolePoule(animal);
            return;
        }

        if (animal.getTypeAnimal() == TypeAnimal.VACHE) {
            animal.setPoids(Math.max(0f, animal.getPoids() + COW_WATER_WEIGHT_GAIN));
            animal.setABu(true);
        }
    }

    public void verifierActionQuotidienneVache(Animal animal, String action) {
        if (animal == null || animal.getTypeAnimal() != TypeAnimal.VACHE) {
            return;
        }

        String normalizedAction = action == null ? "" : action.trim().toLowerCase();
        switch (normalizedAction) {
            case "feed" -> {
                if (animal.isAMange()) {
                    throw new IllegalArgumentException("La vache ne peut manger qu'une seule fois par jour");
                }
            }
            case "water" -> {
                if (animal.isABu()) {
                    throw new IllegalArgumentException("La vache ne peut boire qu'une seule fois par jour");
                }
            }
            default -> {
                return;
            }
        }
    }

    private boolean peutPondre(Animal animal) {
        return animal.getTypeAnimal() == TypeAnimal.POULE
            && TypeStade.ADULTE.equals(animal.getStade())
            && TypeSexe.FEMELLE.equals(animal.getSexe())
            && TypeRole.PONDEUSE.equals(animal.getRole())
            && animal.getPoids() >= CHICKEN_MIN_ADULT_WEIGHT
            && animal.getPoids() <= CHICKEN_MAX_WEIGHT
            && !animal.estMalade()
            && animal.getJaugeProprete() >= 100;
    }

    private boolean peutProduireLait(Animal animal) {
        return animal.getTypeAnimal() == TypeAnimal.VACHE
            && TypeStade.ADULTE.equals(animal.getStade())
            && animal.getRole() != TypeRole.ELEVAGE
            && !animal.estMalade()
            && animal.getJaugeProprete() >= 100;
    }

    private List<Animal> appliquerEtatsQuotidiensAnimaux(List<Animal> animaux) {
        // Le clapier a une logique de mortalite de groupe distincte,
        // appliquee avant la degradation individuelle du nouveau jour.
        List<Animal> animauxMorts = gererMortaliteClapier(animaux);

        if (!animauxMorts.isEmpty()) {
            animaux.removeAll(animauxMorts);
        }

        for (Animal animal : animaux) {
            boolean etaitAffame = animal.getJaugeFaim() < 100;
            boolean etaitAssoiffe = animal.getJaugeHydratation() < 100;
            boolean etaitMalade = animal.estMalade();
            boolean etaitNourriEtAbreuve = !etaitAffame && !etaitAssoiffe;

            faireVieillirAnimal(animal, etaitNourriEtAbreuve);

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
                appliquerJeuneEtPertePoids(animal, animauxMorts);
            } else {
                animal.setJoursJeuneConsecutifs(0);
            }

            if (etaitAssoiffe) {
                // Regle choisie pour le projet : ne pas abreuver un animal
                // deja assoiffe le rend sale le jour suivant.
                animal.setJaugeProprete(0);
            }

            animal.setJaugeFaim(0);
            animal.setJaugeHydratation(0);
            animal.setAMange(false);
            animal.setABu(false);

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

    private void faireVieillirAnimal(Animal animal, boolean etaitNourriEtAbreuve) {
        animal.setAge(Math.max(1, animal.getAge() + 1));

        switch (animal.getTypeAnimal()) {
            case POULE -> {
                if (animal.getAge() >= 5) {
                    animal.setStade(TypeStade.ADULTE);
                }
                mettreAJourSexeEtRolePoule(animal);
            }
            case VACHE -> {
                if (animal.getAge() >= 10 && animal.getPoids() > 80f) {
                    animal.setStade(TypeStade.ADULTE);
                }
                mettreAJourRoleVache(animal);
            }
            case LAPIN -> {
                faireEvoluerLapin(animal, etaitNourriEtAbreuve);
                mettreAJourSexeLapin(animal);
                mettreAJourRoleLapin(animal);
            }
        }
    }

    private void mettreAJourSexeEtRolePoule(Animal animal) {
        if (!TypeStade.ADULTE.equals(animal.getStade())) {
            animal.setRole(TypeRole.ELEVAGE);
            return;
        }

        if (animal.getSexe() == null || animal.getSexe() == TypeSexe.INCONNU) {
            animal.setSexe(generateRandomSexe());
        }

        boolean poidsReproductif = animal.getPoids() >= CHICKEN_MIN_ADULT_WEIGHT && animal.getPoids() <= CHICKEN_MAX_WEIGHT;
        if (!poidsReproductif) {
            animal.setRole(TypeRole.ELEVAGE);
            return;
        }

        if (animal.getSexe() == TypeSexe.MALE) {
            animal.setRole(TypeRole.REPRODUCTEUR);
            return;
        }

        animal.setRole(TypeRole.PONDEUSE);
    }

    private void faireEvoluerLapin(Animal animal, boolean etaitNourriEtAbreuve) {
        if (!etaitNourriEtAbreuve) {
            return;
        }

        TypeStade stadeActuel = animal.getStade() == null ? TypeStade.ENFANT : animal.getStade();

        if (stadeActuel == TypeStade.ENFANT) {
            animal.setStade(TypeStade.GROS_LAPEREAU);
            return;
        }

        if (stadeActuel == TypeStade.GROS_LAPEREAU) {
            animal.setStade(TypeStade.ADULTE);
        }
    }

    private void mettreAJourSexeLapin(Animal animal) {
        if (animal.getTypeAnimal() != TypeAnimal.LAPIN || !TypeStade.ADULTE.equals(animal.getStade())) {
            return;
        }

        if (animal.getSexe() == null || animal.getSexe() == TypeSexe.INCONNU) {
            animal.setSexe(generateRandomSexe());
        }
    }

    private void mettreAJourRoleLapin(Animal animal) {
        if (animal.getTypeAnimal() != TypeAnimal.LAPIN) {
            return;
        }

        if (TypeStade.ADULTE.equals(animal.getStade())) {
            animal.setRole(null);
            return;
        }

        animal.setRole(TypeRole.ELEVAGE);
    }

    private void mettreAJourRoleVache(Animal animal) {
        if (animal.getTypeAnimal() != TypeAnimal.VACHE) {
            return;
        }

        if (TypeStade.ADULTE.equals(animal.getStade())) {
            animal.setRole(null);
            return;
        }

        animal.setRole(TypeRole.ELEVAGE);
    }

    private TypeSexe generateRandomSexe() {
        return ThreadLocalRandom.current().nextBoolean() ? TypeSexe.MALE : TypeSexe.FEMELLE;
    }

    private int calculerProductionOeufs(List<Animal> animaux) {
        List<Animal> coqsReproducteurs = animaux.stream()
            .filter(animal -> animal.getTypeAnimal() == TypeAnimal.POULE)
            .filter(animal -> TypeStade.ADULTE.equals(animal.getStade()))
            .filter(animal -> TypeSexe.MALE.equals(animal.getSexe()))
            .filter(animal -> TypeRole.REPRODUCTEUR.equals(animal.getRole()))
            .filter(animal -> animal.getPoids() >= CHICKEN_MIN_ADULT_WEIGHT && animal.getPoids() <= CHICKEN_MAX_WEIGHT)
            .toList();

        if (coqsReproducteurs.isEmpty()) {
            return 0;
        }

        int pontesAutorisees = coqsReproducteurs.size() * 5;

        List<Animal> poulesPondeuses = animaux.stream()
            .filter(this::peutPondre)
            .limit(pontesAutorisees)
            .toList();

        int totalOeufs = 0;
        for (Animal poule : poulesPondeuses) {
            totalOeufs += ThreadLocalRandom.current().nextInt(3);
        }
        return totalOeufs;
    }

    private void ajouterNaissancesLapins(Ferme ferme, List<Animal> animaux) {
        List<Animal> lapinsAdultes = animaux.stream()
            .filter(animal -> animal.getTypeAnimal() == TypeAnimal.LAPIN)
            .filter(animal -> TypeStade.ADULTE.equals(animal.getStade()))
            .toList();

        if (lapinsAdultes.isEmpty()) {
            return;
        }

        long males = lapinsAdultes.stream()
            .filter(this::peutSeReproduireLapin)
            .filter(animal -> TypeSexe.MALE.equals(animal.getSexe()))
            .count();
        long femelles = lapinsAdultes.stream()
            .filter(this::peutSeReproduireLapin)
            .filter(animal -> TypeSexe.FEMELLE.equals(animal.getSexe()))
            .count();

        int couples = (int) Math.min(males, femelles);
        if (couples <= 0) {
            return;
        }

        int populationActuelle = (int) animaux.stream()
            .filter(animal -> animal.getTypeAnimal() == TypeAnimal.LAPIN)
            .count();
        int placesDisponibles = MAX_RABBIT_POPULATION - populationActuelle;
        if (placesDisponibles <= 0) {
            return;
        }

        int totalNaissances = 0;
        for (int i = 0; i < couples && placesDisponibles > 0; i++) {
            int portee = ThreadLocalRandom.current().nextInt(1, 5);
            int naissancesEffectives = Math.min(portee, placesDisponibles);
            totalNaissances += naissancesEffectives;
            placesDisponibles -= naissancesEffectives;
        }

        for (int i = 0; i < totalNaissances; i++) {
            animaux.add(creerAnimalPourFerme(ferme, TypeAnimal.LAPIN, populationActuelle + i + 1));
        }
    }

    private boolean peutSeReproduireLapin(Animal animal) {
        return animal != null
            && animal.getTypeAnimal() == TypeAnimal.LAPIN
            && TypeStade.ADULTE.equals(animal.getStade())
            && !animal.estMalade()
            && animal.getJaugeProprete() >= 100
            && animal.getJaugeFaim() >= 100
            && animal.getJaugeHydratation() >= 100;
    }

    private void appliquerJeuneEtPertePoids(Animal animal, List<Animal> animauxMorts) {
        if (animal == null || animal.getTypeAnimal() == TypeAnimal.VACHE) {
            return;
        }

        // On memorise les jours de jeune consecutifs pour appliquer
        // les paliers de perte de poids demandes dans le sujet.
        int joursJeune = animal.getJoursJeuneConsecutifs() + 1;
        animal.setJoursJeuneConsecutifs(joursJeune);

        if (joursJeune >= 4) {
            animauxMorts.add(animal);
            return;
        }

        float pertePoids = getFastingWeightLoss(animal, joursJeune);

        float nouveauPoids = Math.max(0f, animal.getPoids() - pertePoids);
        animal.setPoids(nouveauPoids);

        if (nouveauPoids <= 0f) {
            animauxMorts.add(animal);
        }
    }

    private float getFastingWeightLoss(Animal animal, int joursJeune) {
        if (animal == null || joursJeune <= 0) {
            return 0f;
        }

        return switch (animal.getTypeAnimal()) {
            case POULE, LAPIN -> switch (joursJeune) {
                case 1 -> 0.2f;
                case 2 -> 0.5f;
                case 3 -> 1f;
                default -> 0f;
            };
            case VACHE -> 0f;
        };
    }

    private float bornerPoidsPoule(float poids) {
        return Math.max(0f, Math.min(CHICKEN_MAX_WEIGHT, poids));
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

    public void retirerLapinsVivants(Integer idFerme, int quantite) {
        List<Animal> lapinsVendables = animalRepository.findByFerme_IdFermeAndTypeAnimalOrderByIdAnimalAsc(idFerme, TypeAnimal.LAPIN)
            .stream()
            .filter(this::estLapinVendable)
            .toList();
        if (lapinsVendables.size() < quantite) {
            throw new IllegalArgumentException("Stock insuffisant pour lapins adultes");
        }

        animalRepository.deleteAll(lapinsVendables.subList(0, quantite));
    }

    public int countLapinsVendables(Integer idFerme) {
        return (int) animalRepository.findByFerme_IdFermeAndTypeAnimalOrderByIdAnimalAsc(idFerme, TypeAnimal.LAPIN)
            .stream()
            .filter(this::estLapinVendable)
            .count();
    }

    private boolean estLapinVendable(Animal animal) {
        return animal != null
            && animal.getTypeAnimal() == TypeAnimal.LAPIN
            && TypeStade.ADULTE.equals(animal.getStade())
            && animal.getRole() != TypeRole.ELEVAGE;
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
                "score",ferme.getScore() == null ? 0 : ferme.getScore(),
                "ventes", (ferme.getVOeuf() != null ? ferme.getVOeuf() : 0) +
                          (ferme.getVLact() != null ? ferme.getVLact() : 0) +
                          (ferme.getVLaine() != null ? ferme.getVLaine() : 0),
                "achats", (ferme.getBBlack() != null ? ferme.getBBlack() : 0) +
                          (ferme.getVBLack() != null ? ferme.getVBLack() : 0)
            ))
            .toList();
    }
     public List<Map<String, Object>> updateClassementData() {
        Map<String, Object> coeffs = Map.ofEntries(
            Map.entry("money", 30.0),
            Map.entry("Oeuf", 15.0),
            Map.entry("VLapCop", 30.0),
            Map.entry("VPouleCop", 25.0),
            Map.entry("VLact", 20.0),
            Map.entry("VLaine", 20.0),
            Map.entry("PChamps", 15.0),
            Map.entry("PPot", 15.0),
            Map.entry("PFood", 5.0),
            Map.entry("BElev", 10.0),
            Map.entry("VElev", 15.0),
            Map.entry("BBlack", -5.0),
            Map.entry("VBLack", -8.0)
        );
        List<Ferme> fermes = fermeRepository.findAll().stream()
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
            .toList();

        int totalPlayers = fermes.size();

        // Calculate totals
        Map<String, Double> totals = Map.of(
            "money", fermes.stream().mapToDouble(f -> f.getSoldeEcus() != null ? f.getSoldeEcus() : 0).sum(),
            "Oeuf", fermes.stream().mapToDouble(f -> f.getVOeuf() != null ? f.getVOeuf() : 0).sum(),
            "VLapCop", fermes.stream().mapToDouble(f -> f.getVLapCop() != null ? f.getVLapCop() : 0).sum(),
            "VPouleCop", fermes.stream().mapToDouble(f -> f.getVPouleCop() != null ? f.getVPouleCop() : 0).sum(),
            "VLact", fermes.stream().mapToDouble(f -> f.getVLact() != null ? f.getVLact() : 0).sum(),
            "PChamps", fermes.stream().mapToDouble(f -> f.getPChamps() != null ? f.getPChamps() : 0).sum(),
            "PPot", fermes.stream().mapToDouble(f -> f.getPPot() != null ? f.getPPot() : 0).sum(),
            "PFood", fermes.stream().mapToDouble(f -> f.getPFood() != null ? f.getPFood() : 0).sum(),
            "BBlack", fermes.stream().mapToDouble(f -> f.getBBlack() != null ? f.getBBlack() : 0).sum(),
            "VBLack", fermes.stream().mapToDouble(f -> f.getVBLack() != null ? f.getVBLack() : 0).sum()
        );

        fermes.forEach(ferme -> {
            double newScore = 0.0;
            // For excluded fields: value * coef
            newScore += (ferme.getSoldeEcus() != null ? ferme.getSoldeEcus() : 0) * ((Double) coeffs.getOrDefault("money", 1.0));
            newScore += (ferme.getVLaine() != null ? ferme.getVLaine() : 0) * ((Double) coeffs.getOrDefault("VLaine", 1.0));
            newScore += (ferme.getBElev() != null ? ferme.getBElev() : 0) * ((Double) coeffs.getOrDefault("BElev", 1.0));
            newScore += (ferme.getVElev() != null ? ferme.getVElev() : 0) * ((Double) coeffs.getOrDefault("VElev", 1.0));

            // For other fields: coef * totalPlayers * (value / total) if total != 0
            String[] includedFields = {"Oeuf", "VLapCop", "VPouleCop", "VLact", "PChamps", "PPot", "PFood", "BBlack", "VBLack"};
            for (String field : includedFields) {
                double value = 0.0;
                switch (field) {
                    case "Oeuf": value = ferme.getVOeuf() != null ? ferme.getVOeuf() : 0; break;
                    case "VLapCop": value = ferme.getVLapCop() != null ? ferme.getVLapCop() : 0; break;
                    case "VPouleCop": value = ferme.getVPouleCop() != null ? ferme.getVPouleCop() : 0; break;
                    case "VLact": value = ferme.getVLact() != null ? ferme.getVLact() : 0; break;
                    case "PChamps": value = ferme.getPChamps() != null ? ferme.getPChamps() : 0; break;
                    case "PPot": value = ferme.getPPot() != null ? ferme.getPPot() : 0; break;
                    case "PFood": value = ferme.getPFood() != null ? ferme.getPFood() : 0; break;
                    case "BBlack": value = ferme.getBBlack() != null ? ferme.getBBlack() : 0; break;
                    case "VBLack": value = ferme.getVBLack() != null ? ferme.getVBLack() : 0; break;
                }
                double total = totals.get(field);
                double coef = (Double) coeffs.getOrDefault(field, 1.0);
                if (total != 0) {
                    newScore += coef * totalPlayers * (value / total);
                }
            }
            ferme.setScore((int) Math.round(newScore));
        });
        fermeRepository.saveAll(fermes);

       return getClassementData();

    }

}
