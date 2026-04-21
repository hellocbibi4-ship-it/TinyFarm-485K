package com.farm.tinyfarm.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.farm.tinyfarm.model.Animal;
import com.farm.tinyfarm.model.Ferme;
import com.farm.tinyfarm.model.Remise;
import com.farm.tinyfarm.model.TypeAnimal;
import com.farm.tinyfarm.model.TypeRole;
import com.farm.tinyfarm.model.TypeStade;
import com.farm.tinyfarm.model.TypeSexe;
import com.farm.tinyfarm.repository.AnimalRepository;
import com.farm.tinyfarm.repository.FermeRepository;
import com.farm.tinyfarm.repository.MarcheRepository;
import com.farm.tinyfarm.repository.RemiseRepository;
import com.farm.tinyfarm.service.FermeService;

class TestFermeService {

    private FermeRepository fermeRepository;
    private RemiseRepository remiseRepository;
    private AnimalRepository animalRepository;
    private MarcheRepository marcheRepository;
    private FermeService fermeService;
    private Ferme ferme;

    @BeforeEach
    void setUp() {
        fermeRepository = mock(FermeRepository.class);
        remiseRepository = mock(RemiseRepository.class);
        animalRepository = mock(AnimalRepository.class);
        marcheRepository = mock(MarcheRepository.class);
        fermeService = new FermeService(fermeRepository, remiseRepository, animalRepository, marcheRepository);

        ferme = new Ferme();
        ferme.setIdFerme(1);
        ferme.setSoldeEcus(1000);
        ferme.setScore(10);
        ferme.setHibernation(false);

        when(fermeRepository.findById(1)).thenReturn(Optional.of(ferme));
        when(fermeRepository.save(any(Ferme.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    // ---------- ajouterEcus ----------

    @Test
    void ajouterEcusMetAJourLeSolde() {
        fermeService.ajouterEcus(1, 250);
        assertEquals(1250, ferme.getSoldeEcus());
    }

    @Test
    void ajouterEcusMontantNegatifLeveException() {
        assertThrows(IllegalArgumentException.class, () -> fermeService.ajouterEcus(1, -10));
    }

    @Test
    void ajouterEcusFermeInexistanteLeveException() {
        when(fermeRepository.findById(99)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> fermeService.ajouterEcus(99, 10));
    }

    // ---------- retirerEcus ----------

    @Test
    void retirerEcusMetAJourLeSolde() {
        fermeService.retirerEcus(1, 300);
        assertEquals(700, ferme.getSoldeEcus());
    }

    @Test
    void retirerEcusMontantNegatifLeveException() {
        assertThrows(IllegalArgumentException.class, () -> fermeService.retirerEcus(1, -5));
    }

    @Test
    void retirerEcusFermeInexistanteLeveException() {
        when(fermeRepository.findById(99)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> fermeService.retirerEcus(99, 10));
    }

    // ---------- ajouterScore ----------

    @Test
    void ajouterScoreIncrementeLeScore() {
        fermeService.ajouterScore(1, 5);
        assertEquals(15, ferme.getScore());
    }

    @Test
    void ajouterScoreMontantNegatifLeveException() {
        assertThrows(IllegalArgumentException.class, () -> fermeService.ajouterScore(1, -1));
    }

    @Test
    void ajouterScoreFermeInexistanteLeveException() {
        when(fermeRepository.findById(99)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> fermeService.ajouterScore(99, 10));
    }

    // ---------- hibernation ----------

    @Test
    void hibernationMetLEtatVrai() {
        fermeService.hibernation(1, true);
        assertTrue(ferme.getHibernation());
    }

    @Test
    void hibernationMetLEtatFaux() {
        ferme.setHibernation(true);
        fermeService.hibernation(1, false);
        assertFalse(ferme.getHibernation());
    }

    @Test
    void hibernationFermeInexistanteLeveException() {
        when(fermeRepository.findById(99)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> fermeService.hibernation(99, true));
    }

    // ---------- consommerAchatCollectivite ----------

    @Test
    void consommerAchatCollectiviteDecrementeLeQuota() {
        ferme.setAchatsCollectiviteRestants(5);
        ferme.setJourActuel(1);
        fermeService.consommerAchatCollectivite(1);
        assertEquals(4, ferme.getAchatsCollectiviteRestants());
    }

    @Test
    void consommerAchatCollectiviteQuotaEpuiseLeveException() {
        ferme.setAchatsCollectiviteRestants(0);
        ferme.setJourActuel(1);
        assertThrows(IllegalArgumentException.class, () -> fermeService.consommerAchatCollectivite(1));
    }

    // ---------- payerActionAnimale ----------

    @Test
    void payerActionAnimaleRetireLeCout() {
        fermeService.payerActionAnimale(1, "poule", "feed");
        assertEquals(997, ferme.getSoldeEcus());
    }

    @Test
    void payerActionAnimaleSoldeInsuffisantLeveException() {
        ferme.setSoldeEcus(1);
        assertThrows(IllegalArgumentException.class,
            () -> fermeService.payerActionAnimale(1, "vache", "heal"));
    }

    @Test
    void payerActionAnimaleTypeInconnuLeveException() {
        assertThrows(IllegalArgumentException.class,
            () -> fermeService.payerActionAnimale(1, "dragon", "feed"));
    }

    @Test
    void payerActionAnimaleActionInconnueLeveException() {
        assertThrows(IllegalArgumentException.class,
            () -> fermeService.payerActionAnimale(1, "poule", "dance"));
    }

    // ---------- countLapinsVendables / retirerLapinsVivants ----------

    @Test
    void countLapinsVendablesCompteSeulementAdultesNonElevage() {
        Animal adulteVendable = newLapin(TypeStade.ADULTE, TypeRole.PONDEUSE);
        Animal adulteElevage = newLapin(TypeStade.ADULTE, TypeRole.ELEVAGE);
        Animal jeune = newLapin(TypeStade.ENFANT, TypeRole.ELEVAGE);
        when(animalRepository.findByFerme_IdFermeAndTypeAnimalOrderByIdAnimalAsc(1, TypeAnimal.LAPIN))
            .thenReturn(List.of(adulteVendable, adulteElevage, jeune));
        assertEquals(1, fermeService.countLapinsVendables(1));
    }

    @Test
    void retirerLapinsVivantsStockInsuffisantLeveException() {
        when(animalRepository.findByFerme_IdFermeAndTypeAnimalOrderByIdAnimalAsc(1, TypeAnimal.LAPIN))
            .thenReturn(List.of());
        assertThrows(IllegalArgumentException.class, () -> fermeService.retirerLapinsVivants(1, 2));
    }

    // ---------- getAnimalDeFerme ----------

    @Test
    void getAnimalDeFermeOkRetourneAnimal() {
        Animal a = new Animal();
        a.setIdAnimal(5);
        a.setFerme(ferme);
        when(animalRepository.findById(5)).thenReturn(Optional.of(a));
        Animal res = fermeService.getAnimalDeFerme(1, 5);
        assertEquals(5, res.getIdAnimal());
    }

    @Test
    void getAnimalDeFermeInexistantLeveException() {
        when(animalRepository.findById(99)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> fermeService.getAnimalDeFerme(1, 99));
    }

    @Test
    void getAnimalDeFermeMauvaiseFermeLeveException() {
        Ferme autre = new Ferme();
        autre.setIdFerme(2);
        Animal a = new Animal();
        a.setIdAnimal(5);
        a.setFerme(autre);
        when(animalRepository.findById(5)).thenReturn(Optional.of(a));
        assertThrows(IllegalArgumentException.class, () -> fermeService.getAnimalDeFerme(1, 5));
    }

    // ---------- deleteFarmWithDependencies (ne rien faire si id inconnu) ----------

    @Test
    void deleteFarmWithDependenciesIdNullNeFaitRien() {
        fermeService.deleteFarmWithDependencies(null);
        // rien ne doit lever
    }

    @Test
    void deleteFarmWithDependenciesFermeInexistanteNeFaitRien() {
        when(fermeRepository.existsById(99)).thenReturn(false);
        fermeService.deleteFarmWithDependencies(99);
    }

    // ---------- utilitaires ----------

    private Animal newLapin(TypeStade stade, TypeRole role) {
        Animal a = new Animal();
        a.setTypeAnimal(TypeAnimal.LAPIN);
        a.setStade(stade);
        a.setRole(role);
        a.setFerme(ferme);
        return a;
    }
    // Crée un lapin adulte dans un état optimal (sain, propre, nourri, abreuvé).
    private Animal newLapinAdulteOptimal(TypeSexe sexe) {
        Animal a = new Animal();
        a.setTypeAnimal(TypeAnimal.LAPIN);
        a.setStade(TypeStade.ADULTE);
        a.setSexe(sexe);
        a.setRole(null);
        a.setFerme(ferme);
        a.setJaugeFaim(100);
        a.setJaugeHydratation(100);
        a.setJaugeProprete(100);
        a.setJaugeSante(100);
        a.setEstMalade(false);
        a.setJoursMaladeConsecutifs(0);
        a.setJoursJeuneConsecutifs(0);
        return a;
    }

    /** Crée une poule dans l'état indiqué. */
    private Animal newPoule(TypeStade stade, TypeSexe sexe, float poids) {
        Animal a = new Animal();
        a.setTypeAnimal(TypeAnimal.POULE);
        a.setStade(stade);
        a.setSexe(sexe);
        a.setPoids(poids);
        a.setFerme(ferme);
        a.setJaugeFaim(0);
        a.setJaugeHydratation(0);
        a.setJaugeProprete(100);
        a.setJaugeSante(100);
        a.setEstMalade(false);
        a.setJoursMaladeConsecutifs(0);
        a.setJoursJeuneConsecutifs(0);
        return a;
    }

    /** Configure les mocks nécessaires à passerJour et retourne la remise. */
    private Remise setUpPasserJour(List<Animal> animaux) {
        Remise remise = new Remise();
        remise.setFerme(ferme);
        ferme.setJourActuel(1);
        ferme.setAchatsCollectiviteRestants(12);
        when(remiseRepository.findById(1)).thenReturn(Optional.of(remise));
        when(animalRepository.findByFerme_IdFermeOrderByIdAnimalAsc(1))
            .thenReturn(new ArrayList<>(animaux));
        when(animalRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));
        when(remiseRepository.save(any(Remise.class))).thenAnswer(inv -> inv.getArgument(0));
        return remise;
    }

    // Sexe et rôle de la poule (mettreAJourSexeEtRolePoule)
    // via appliquerEffetsNourrirAnimal / appliquerEffetsAbreuverAnimal
    @Test
    void sexeRolePouleEnfantGardeRoleElevage() {
        Animal poule = newPoule(TypeStade.ENFANT, TypeSexe.INCONNU, 1.0f);
        fermeService.appliquerEffetsNourrirAnimal(poule);
        assertEquals(TypeRole.ELEVAGE, poule.getRole());
    }

    @Test
    void sexeRolePouleAdultePoidsInsuffisantGardeRoleElevage() {
        // poids 1.0 + 0.5 = 1.5 < 2.5 => hors plage reproductive
        Animal poule = newPoule(TypeStade.ADULTE, TypeSexe.MALE, 1.0f);
        fermeService.appliquerEffetsNourrirAnimal(poule);
        assertEquals(TypeRole.ELEVAGE, poule.getRole());
    }

    @Test
    void sexeRolePouleAdulteMalePoidsOkDevientReproducteur() {
        // poids 2.5 + 0.5 = 3.0, en plage [2.5, 3.5]
        Animal poule = newPoule(TypeStade.ADULTE, TypeSexe.MALE, 2.5f);
        fermeService.appliquerEffetsNourrirAnimal(poule);
        assertEquals(TypeRole.REPRODUCTEUR, poule.getRole());
    }

    @Test
    void sexeRolePouleAdulteFemellePoidOkDevientPondeuse() {
        Animal poule = newPoule(TypeStade.ADULTE, TypeSexe.FEMELLE, 2.5f);
        fermeService.appliquerEffetsNourrirAnimal(poule);
        assertEquals(TypeRole.PONDEUSE, poule.getRole());
    }

    @Test
    void sexeRolePouleAdulteInconnuAssigneSexeApresNourrir() {
        Animal poule = newPoule(TypeStade.ADULTE, TypeSexe.INCONNU, 2.5f);
        fermeService.appliquerEffetsNourrirAnimal(poule);
        assertNotNull(poule.getSexe());
        assertNotEquals(TypeSexe.INCONNU, poule.getSexe());
    }

    @Test
    void sexeRolePouleAdulteSexeNullAssigneSexeApresNourrir() {
        Animal poule = newPoule(TypeStade.ADULTE, null, 2.5f);
        fermeService.appliquerEffetsNourrirAnimal(poule);
        assertNotNull(poule.getSexe());
        assertNotEquals(TypeSexe.INCONNU, poule.getSexe());
    }

    @Test
    void sexeRolePouleAdulteMalePoidsOkViaAbreuver() {
        Animal poule = newPoule(TypeStade.ADULTE, TypeSexe.MALE, 2.5f);
        fermeService.appliquerEffetsAbreuverAnimal(poule);
        assertEquals(TypeRole.REPRODUCTEUR, poule.getRole());
    }

    @Test
    void sexeRolePouleAdulteInconnuAssigneSexeApresAbreuver() {
        Animal poule = newPoule(TypeStade.ADULTE, TypeSexe.INCONNU, 2.5f);
        fermeService.appliquerEffetsAbreuverAnimal(poule);
        assertNotNull(poule.getSexe());
        assertNotEquals(TypeSexe.INCONNU, poule.getSexe());
    }

    // Évolution de stade du lapin (faireEvoluerLapin) via passerJour
    @Test
    void lapinEnfantNourriEtAbreuvePasseGrosLapereau() {
        Animal lapin = newLapin(TypeStade.ENFANT, TypeRole.ELEVAGE);
        lapin.setJaugeFaim(100);
        lapin.setJaugeHydratation(100);
        lapin.setJaugeProprete(100);
        lapin.setSexe(TypeSexe.INCONNU);
        setUpPasserJour(List.of(lapin));

        fermeService.passerJour(1);

        assertEquals(TypeStade.GROS_LAPEREAU, lapin.getStade());
    }

    @Test
    void lapinGrosLapereauNourriEtAbreuvePasseAdulte() {
        Animal lapin = newLapin(TypeStade.GROS_LAPEREAU, TypeRole.ELEVAGE);
        lapin.setJaugeFaim(100);
        lapin.setJaugeHydratation(100);
        lapin.setJaugeProprete(100);
        lapin.setSexe(TypeSexe.INCONNU);
        setUpPasserJour(List.of(lapin));

        fermeService.passerJour(1);

        assertEquals(TypeStade.ADULTE, lapin.getStade());
    }

    @Test
    void lapinEnfantNonNourriResteEnfant() {
        Animal lapin = newLapin(TypeStade.ENFANT, TypeRole.ELEVAGE);
        lapin.setJaugeFaim(50);  // affamé => etaitNourriEtAbreuve = false
        lapin.setJaugeHydratation(100);
        lapin.setJaugeProprete(100);
        lapin.setSexe(TypeSexe.INCONNU);
        setUpPasserJour(List.of(lapin));

        fermeService.passerJour(1);

        assertEquals(TypeStade.ENFANT, lapin.getStade());
    }

    // Sexe du lapin (mettreAJourSexeLapin) via passerJour

    @Test
    void lapinAdulteInconnuAssigneSexeApresPasser() {
        Animal lapin = newLapinAdulteOptimal(TypeSexe.INCONNU);
        setUpPasserJour(List.of(lapin));

        fermeService.passerJour(1);

        assertNotNull(lapin.getSexe());
        assertNotEquals(TypeSexe.INCONNU, lapin.getSexe());
    }

    @Test
    void lapinEnfantInconnuGardeInconnuApresPasser() {
        Animal lapin = newLapin(TypeStade.ENFANT, TypeRole.ELEVAGE);
        lapin.setJaugeFaim(100);
        lapin.setJaugeHydratation(100);
        lapin.setJaugeProprete(100);
        lapin.setSexe(TypeSexe.INCONNU);
        setUpPasserJour(List.of(lapin));

        fermeService.passerJour(1);

        // Le lapin passe à GROS_LAPEREAU, pas encore ADULTE => sexe non assigné
        assertEquals(TypeSexe.INCONNU, lapin.getSexe());
    }

    // Rôle du lapin (mettreAJourRoleLapin) via passerJour

    @Test
    void lapinAdulteARoleNullApresPasser() {
        Animal lapin = newLapinAdulteOptimal(TypeSexe.MALE);
        lapin.setRole(TypeRole.ELEVAGE);
        setUpPasserJour(List.of(lapin));

        fermeService.passerJour(1);

        // Les lapins adultes n'ont pas de rôle prédéfini (null)
        assertEquals(null, lapin.getRole());
    }

    @Test
    void lapinEnfantARoleElevageApresPasser() {
        Animal lapin = newLapin(TypeStade.ENFANT, null);
        lapin.setJaugeFaim(100);
        lapin.setJaugeHydratation(100);
        lapin.setJaugeProprete(100);
        lapin.setSexe(TypeSexe.INCONNU);
        setUpPasserJour(List.of(lapin));

        fermeService.passerJour(1);

        // Après passage à GROS_LAPEREAU (toujours non adulte) => ELEVAGE
        assertEquals(TypeRole.ELEVAGE, lapin.getRole());
    }

    // Reproduction des lapins (peutSeReproduireLapin + ajouterNnaissancesLapins)
    // via passerJour
    @Test
    void reproductionLapinsCoupleParfaitProduisDesNaissances() {
        Animal male   = newLapinAdulteOptimal(TypeSexe.MALE);
        Animal femelle = newLapinAdulteOptimal(TypeSexe.FEMELLE);
        setUpPasserJour(List.of(male, femelle));

        Ferme result = fermeService.passerJour(1);

        // Au moins 1 naissance (portée de 1 à 4)
        assertTrue(result.getNbLapins() > 2,
            "Un couple reproducteur doit produire des naissances");
    }

    @Test
    void reproductionLapinsSansMaleAucuneNaissance() {
        Animal f1 = newLapinAdulteOptimal(TypeSexe.FEMELLE);
        Animal f2 = newLapinAdulteOptimal(TypeSexe.FEMELLE);
        setUpPasserJour(List.of(f1, f2));

        Ferme result = fermeService.passerJour(1);

        assertEquals(2, result.getNbLapins(), "Sans mâle il ne doit pas y avoir de naissances");
    }

    @Test
    void reproductionLapinsSansFemellAucuneNaissance() {
        Animal m1 = newLapinAdulteOptimal(TypeSexe.MALE);
        Animal m2 = newLapinAdulteOptimal(TypeSexe.MALE);
        setUpPasserJour(List.of(m1, m2));

        Ferme result = fermeService.passerJour(1);

        assertEquals(2, result.getNbLapins(), "Sans femelle il ne doit pas y avoir de naissances");
    }

    @Test
    void reproductionLapinsMaladePasDeNaissance() {
        Animal male   = newLapinAdulteOptimal(TypeSexe.MALE);
        Animal femelle = newLapinAdulteOptimal(TypeSexe.FEMELLE);
        male.setEstMalade(true);
        femelle.setEstMalade(true);
        setUpPasserJour(List.of(male, femelle));

        Ferme result = fermeService.passerJour(1);

        // Des lapins malades ne peuvent pas se reproduire (la mortalité peut aussi réduire le total)
        assertFalse(result.getNbLapins() > 2,
            "Des lapins malades ne doivent pas produire de naissances");
    }

    @Test
    void reproductionLapinsSaleAucuneNaissance() {
        Animal male   = newLapinAdulteOptimal(TypeSexe.MALE);
        Animal femelle = newLapinAdulteOptimal(TypeSexe.FEMELLE);
        male.setJaugeProprete(50);
        femelle.setJaugeProprete(50);
        setUpPasserJour(List.of(male,femelle));

        Ferme result = fermeService.passerJour(1);

        // Sales => aucune naissance possible (jaugeProprete < 100).
        // gererMortaliteClapier tue ceil(2 * 0.25) = 1 lapin sale, il en reste 1.
        assertEquals(1, result.getNbLapins(),
            "Des lapins sales ne peuvent pas se reproduire");
    }

    @Test
    void reproductionLapinsAffamesAucuneNaissance() {
        Animal male   = newLapinAdulteOptimal(TypeSexe.MALE);
        Animal femelle = newLapinAdulteOptimal(TypeSexe.FEMELLE);
        male.setJaugeFaim(50);
        femelle.setJaugeFaim(50);
        setUpPasserJour(List.of(male, femelle));

        Ferme result = fermeService.passerJour(1);

        assertEquals(2, result.getNbLapins(), "Des lapins affamés ne peuvent pas se reproduire");
    }

    @Test
    void reproductionLapinsAssoiffesAucuneNaissance() {
        Animal male   = newLapinAdulteOptimal(TypeSexe.MALE);
        Animal femelle = newLapinAdulteOptimal(TypeSexe.FEMELLE);
        male.setJaugeHydratation(50);
        femelle.setJaugeHydratation(50);
        setUpPasserJour(List.of(male, femelle));

        Ferme result = fermeService.passerJour(1);

        assertEquals(2, result.getNbLapins(), "Des lapins assoiffés ne peuvent pas se reproduire");
    }

    @Test
    void reproductionLapinsPopulationMaxPasDeNaissances() {
        List<Animal> animaux = new ArrayList<>();
        for (int i = 0; i < FermeService.MAX_RABBIT_POPULATION; i++) {
            animaux.add(newLapinAdulteOptimal(i % 2 == 0 ? TypeSexe.MALE : TypeSexe.FEMELLE));
        }
        setUpPasserJour(animaux);

        Ferme result = fermeService.passerJour(1);

        assertEquals(FermeService.MAX_RABBIT_POPULATION, result.getNbLapins(),
            "La population maximale ne doit pas être dépassée");
    }

    @Test
    void reproductionLapinsPlacesLimiteesNaissancesBorneesAMax() {
        // 49 lapins : 1 place libre implique au maximum 1 naissance possible
        List<Animal> animaux = new ArrayList<>();
        for (int i = 0; i < FermeService.MAX_RABBIT_POPULATION - 1; i++) {
            animaux.add(newLapinAdulteOptimal(i % 2 == 0 ? TypeSexe.MALE : TypeSexe.FEMELLE));
        }
        setUpPasserJour(animaux);

        Ferme result = fermeService.passerJour(1);

        assertTrue(result.getNbLapins() <= FermeService.MAX_RABBIT_POPULATION,
            "Le nombre de lapins ne doit pas dépasser le maximum autorisé");
    }
}