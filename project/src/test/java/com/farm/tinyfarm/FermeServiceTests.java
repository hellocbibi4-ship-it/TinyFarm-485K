package com.farm.tinyfarm.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.farm.tinyfarm.model.Animal;
import com.farm.tinyfarm.model.Ferme;
import com.farm.tinyfarm.model.TypeAnimal;
import com.farm.tinyfarm.model.TypeRole;
import com.farm.tinyfarm.model.TypeStade;
import com.farm.tinyfarm.repository.AnimalRepository;
import com.farm.tinyfarm.repository.FermeRepository;
import com.farm.tinyfarm.repository.MarcheRepository;
import com.farm.tinyfarm.repository.RemiseRepository;
import com.farm.tinyfarm.service.FermeService;

class TestFermeServiceUnit {

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
}