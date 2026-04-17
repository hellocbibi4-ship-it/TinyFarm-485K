package com.farm.tinyfarm.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.farm.tinyfarm.model.Animal;
import com.farm.tinyfarm.model.Ferme;
import com.farm.tinyfarm.model.Habitat;
import com.farm.tinyfarm.model.TypeAnimal;
import com.farm.tinyfarm.model.TypeHabitat;
import com.farm.tinyfarm.repository.HabitatRepository;
import com.farm.tinyfarm.service.FermeService;
import com.farm.tinyfarm.service.HabitatService;

import jakarta.persistence.EntityNotFoundException;

class TestHabitatService {

    private HabitatRepository habitatRepository;
    private FermeService fermeService;
    private HabitatService habitatService;
    private Ferme ferme;

    @BeforeEach
    void setUp() {
        habitatRepository = mock(HabitatRepository.class);
        fermeService = mock(FermeService.class);
        habitatService = new HabitatService(habitatRepository, fermeService);
        ferme = new Ferme();
        ferme.setIdFerme(1);
        when(habitatRepository.save(any(Habitat.class))).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(fermeService).retirerEcus(anyInt(), anyInt());
    }

    private Habitat newClapier(int capacite) {
        Habitat h = new Habitat(capacite, TypeHabitat.CLAPIER);
        h.setFerme(ferme);
        return h;
    }

    private Animal newLapin() {
        Animal a = new Animal();
        a.setTypeAnimal(TypeAnimal.LAPIN);
        a.setFerme(ferme);
        a.setJaugeSante(100);
        a.setJaugeFaim(100);
        a.setJaugeHydratation(100);
        return a;
    }

    // ---------- ajouter / supprimer ----------

    @Test
    void ajouterAnimalAjouteDansLaListe() {
        Habitat h = newClapier(5);
        Animal a = newLapin();
        habitatService.ajouterAnimal(h, a);
        assertEquals(1, h.getListeAnimaux().size());
        verify(habitatRepository).save(h);
    }

    @Test
    void ajouterAnimalClapierPleinLeveException() {
        Habitat h = newClapier(1);
        h.getListeAnimaux().add(newLapin());
        assertThrows(IllegalStateException.class, () -> habitatService.ajouterAnimal(h, newLapin()));
    }

    @Test
    void ajouterAnimalAvecIdHabitatUtiliseLeRepository() {
        Habitat h = newClapier(5);
        when(habitatRepository.findById(10)).thenReturn(Optional.of(h));
        habitatService.ajouterAnimal(10, newLapin());
        assertEquals(1, h.getListeAnimaux().size());
    }

    @Test
    void ajouterAnimalHabitatInexistantLeveException() {
        when(habitatRepository.findById(999)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> habitatService.ajouterAnimal(999, newLapin()));
    }

    @Test
    void supprimerAnimalRetireDeLaListe() {
        Habitat h = newClapier(5);
        Animal a = newLapin();
        h.getListeAnimaux().add(a);
        habitatService.supprimerAnimal(h, a);
        assertEquals(0, h.getListeAnimaux().size());
    }

    @Test
    void supprimerAnimalAbsentLeveException() {
        Habitat h = newClapier(5);
        assertThrows(IllegalArgumentException.class,
            () -> habitatService.supprimerAnimal(h, newLapin()));
    }

    // ---------- nettoyerClapier ----------

    @Test
    void nettoyerClapierSaleRemetPropre() {
        Habitat h = newClapier(5);
        h.setEstSale(true);
        h.setDateEstSale(Date.valueOf(LocalDate.now()));
        habitatService.nettoyerClapier(h);
        assertFalse(h.estSale());
        verify(fermeService).retirerEcus(1, 3);
    }

    @Test
    void nettoyerClapierDejaPropreLeveException() {
        Habitat h = newClapier(5);
        h.setEstSale(false);
        assertThrows(IllegalStateException.class, () -> habitatService.nettoyerClapier(h));
    }

    @Test
    void nettoyerNonClapierLeveException() {
        Habitat h = new Habitat(5, TypeHabitat.POULLAILLER);
        h.setFerme(ferme);
        h.setEstSale(true);
        assertThrows(IllegalArgumentException.class, () -> habitatService.nettoyerClapier(h));
    }

    // ---------- soignerClapier ----------

    @Test
    void soignerClapierGuritTousLesAnimaux() {
        Habitat h = newClapier(5);
        h.setEstMalade(true);
        Animal a1 = newLapin();
        a1.setEstMalade(true);
        a1.setJaugeSante(0);
        Animal a2 = newLapin();
        a2.setEstMalade(true);
        a2.setJaugeSante(10);
        h.getListeAnimaux().add(a1);
        h.getListeAnimaux().add(a2);
        habitatService.soignerClapier(h);
        assertFalse(h.estMalade());
        assertFalse(a1.estMalade());
        assertFalse(a2.estMalade());
        assertEquals(100, a1.getJaugeSante());
        verify(fermeService).retirerEcus(1, 6);
    }

    @Test
    void soignerClapierNonMaladeLeveException() {
        Habitat h = newClapier(5);
        h.setEstMalade(false);
        assertThrows(IllegalStateException.class, () -> habitatService.soignerClapier(h));
    }

    // ---------- nourrirClapier ----------

    @Test
    void nourrirClapierMetFaim100ATousLesLapins() {
        Habitat h = newClapier(5);
        Animal a1 = newLapin();
        a1.setJaugeFaim(0);
        Animal a2 = newLapin();
        a2.setJaugeFaim(50);
        h.getListeAnimaux().add(a1);
        h.getListeAnimaux().add(a2);
        habitatService.nourrirClapier(h);
        assertEquals(100, a1.getJaugeFaim());
        assertEquals(100, a2.getJaugeFaim());
        assertTrue(a1.isAMange());
        verify(fermeService).retirerEcus(1, 5);
    }

    @Test
    void nourrirClapierVideLeveException() {
        Habitat h = newClapier(5);
        assertThrows(IllegalStateException.class, () -> habitatService.nourrirClapier(h));
    }

    // ---------- abreuverClapier ----------

    @Test
    void abreuverClapierMetHydratation100ATousLesLapins() {
        Habitat h = newClapier(5);
        Animal a1 = newLapin();
        a1.setJaugeHydratation(0);
        h.getListeAnimaux().add(a1);
        habitatService.abreuverClapier(h);
        assertEquals(100, a1.getJaugeHydratation());
        verify(fermeService).retirerEcus(1, 2);
    }

    @Test
    void abreuverClapierVideLeveException() {
        Habitat h = newClapier(5);
        assertThrows(IllegalStateException.class, () -> habitatService.abreuverClapier(h));
    }

    // ---------- actionClapierSale / Malade ----------

    @Test
    void actionClapierSaleNonSaleLeveException() {
        Habitat h = newClapier(5);
        h.setEstSale(false);
        assertThrows(IllegalStateException.class, () -> habitatService.actionClapierSale(h));
    }

    @Test
    void actionClapierSaleTueUnQuartSiDatePassee() {
        Habitat h = newClapier(10);
        h.setEstSale(true);
        h.setDateEstSale(Date.valueOf(LocalDate.now().minusDays(1)));
        for (int i = 0; i < 4; i++) {
            h.getListeAnimaux().add(newLapin());
        }
        habitatService.actionClapierSale(h);
        // ceil(4 * 0.25) = 1
        assertEquals(3, h.getListeAnimaux().size());
    }

    @Test
    void actionClapierMaladeNonMaladeLeveException() {
        Habitat h = newClapier(5);
        h.setEstMalade(false);
        assertThrows(IllegalStateException.class, () -> habitatService.actionClapierMalade(h));
    }
}