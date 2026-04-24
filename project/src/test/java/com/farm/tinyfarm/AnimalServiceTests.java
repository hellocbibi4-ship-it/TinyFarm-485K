package com.farm.tinyfarm.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.farm.tinyfarm.model.Animal;
import com.farm.tinyfarm.model.Ferme;
import com.farm.tinyfarm.model.TypeAnimal;
import com.farm.tinyfarm.model.TypeStade;
import com.farm.tinyfarm.model.TypeRole;
import com.farm.tinyfarm.model.TypeSexe;
import com.farm.tinyfarm.repository.AnimalRepository;
import com.farm.tinyfarm.service.AnimalService;
import com.farm.tinyfarm.service.FermeService;

class AnimalServiceTests {

    private AnimalRepository animalRepository;
    private FermeService fermeService;
    private AnimalService animalService;
    private Ferme ferme;

    @BeforeEach
    void setUp() {
        animalRepository = mock(AnimalRepository.class);
        fermeService = mock(FermeService.class);
        animalService = new AnimalService(animalRepository, fermeService);
        ferme = new Ferme();
        ferme.setIdFerme(1);
        ferme.setNom("TestFerme");
        when(animalRepository.save(any(Animal.class))).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(fermeService).retirerEcus(anyInt(), anyInt());
    }

    private Animal newPoule(int faim, int hydratation) {
        Animal a = new Animal();
        a.setTypeAnimal(TypeAnimal.POULE);
        a.setFerme(ferme);
        a.setJaugeFaim(faim);
        a.setJaugeHydratation(hydratation);
        a.setPoids(2f);
        a.setStade(TypeStade.ADULTE);
        return a;
    }

    private Animal newVache() {
        Animal a = new Animal();
        a.setTypeAnimal(TypeAnimal.VACHE);
        a.setFerme(ferme);
        a.setPoids(100f);
        a.setStade(TypeStade.ADULTE);
        a.setJaugeFaim(50);
        a.setJaugeHydratation(50);
        a.setJaugeProprete(100);
        a.setJaugeSante(100);
        a.setEstMalade(false);
        a.setAMange(false);
        return a;
    }

    // ---------- createBaseAnimal ----------

    @Test
    void createBaseAnimalPouleInitialisePoidsEtStade() {
        Animal a = new Animal();
        a.setTypeAnimal(TypeAnimal.POULE);
        a.setNom("poule1");
        Animal res = animalService.createBaseAnimal(a);
        assertEquals(0.05f, res.getPoids());
        assertEquals(TypeStade.ENFANT, res.getStade());
    }

    @Test
    void testCreateBaseAnimalPoule() {
        Animal animal = new Animal();
        animal.setNom("PouleTest");
        animal.setTypeAnimal(TypeAnimal.POULE);

        animalService.createBaseAnimal(animal);

        assertEquals(0, animal.getAge());
        assertEquals(0.05f, animal.getPoids());
        assertEquals(TypeStade.ENFANT, animal.getStade());
        assertEquals(TypeRole.ELEVAGE, animal.getRole());
        assertEquals(TypeSexe.INCONNU, animal.getSexe());
        assertTrue(animal.isVivant());
    }

    @Test
    void testUpdateChickenStatusRevealsRoleAtAgeFive() {
        Animal animal = new Animal();
        animal.setNom("PouleAdulte");
        animal.setTypeAnimal(TypeAnimal.POULE);
        animal.setAge(5);
        animal.setPoids(2.5f);
        animal.setStade(TypeStade.ADULTE);
        animal.setSexe(TypeSexe.MALE);

        animalService.updateChickenStatus(animal);

        assertEquals(TypeRole.REPRODUCTEUR, animal.getRole());
        assertNotNull(animal.getSexe());
    }

    @Test
    void createBaseAnimalLapinInitialisePoidsEtStade() {
        Animal a = new Animal();
        a.setTypeAnimal(TypeAnimal.LAPIN);
        a.setNom("lap1");
        Animal res = animalService.createBaseAnimal(a);
        assertEquals(0.0f, res.getPoids());
        assertEquals(TypeStade.ENFANT, res.getStade());
    }

    @Test
    void createBaseAnimalVacheInitialisePoidsEtStade() {
        Animal a = new Animal();
        a.setTypeAnimal(TypeAnimal.VACHE);
        a.setNom("vache1");
        Animal res = animalService.createBaseAnimal(a);
        assertEquals(1.0f, res.getPoids());
        assertEquals(TypeStade.ENFANT, res.getStade());
    }

    @Test
    void createBaseAnimalNomInvalideLeveException() {
        Animal a = new Animal();
        a.setTypeAnimal(TypeAnimal.POULE);
        a.setNom("x");
        assertThrows(IllegalArgumentException.class, () -> animalService.createBaseAnimal(a));
    }

    // ---------- updateChickenAge / updateChickenWeight ----------

    @Test
    void updateChickenAgeIncremente() {
        Animal a = newPoule(100, 100);
        a.setAge(3);
        animalService.updateChickenAge(a);
        assertEquals(4, a.getAge());
    }

    @Test
    void updateChickenWeightPlafonneA35() {
        Animal a = newPoule(100, 100);
        a.setPoids(3.4f);
        animalService.updateChickenWeight(a, 1f);
        assertEquals(3.5f, a.getPoids());
    }

    @Test
    void updateChickenWeightAjouteNormalement() {
        Animal a = newPoule(100, 100);
        a.setPoids(1f);
        animalService.updateChickenWeight(a, 0.5f);
        assertEquals(1.5f, a.getPoids());
    }

    // ---------- nourrirPoule ----------

    @Test
    void nourrirPouleMetFaimA100() {
        Animal a = newPoule(50, 50);
        animalService.nourrirPoule(a);
        assertEquals(100, a.getJaugeFaim());
    }

    @Test
    void nourrirPouleAjoutePoidsSiDejaHydrate() {
        Animal a = newPoule(50, 100);
        a.setPoids(1f);
        animalService.nourrirPoule(a);
        assertEquals(1.65f, a.getPoids(), 0.0001);
    }

    @Test
    void nourrirPouleAjoutePoidsStandardSiPasHydrate() {
        Animal a = newPoule(50, 50);
        a.setPoids(1f);
        animalService.nourrirPoule(a);
        assertEquals(1.5f, a.getPoids(), 0.0001);
    }

    @Test
    void nourrirPouleRetire3EcusAFerme() {
        Animal a = newPoule(50, 50);
        animalService.nourrirPoule(a);
        verify(fermeService).retirerEcus(1, 3);
    }

    @Test
    void nourrirPouleDejaNourrieLeveException() {
        Animal a = newPoule(100, 50);
        assertThrows(IllegalCallerException.class, () -> animalService.nourrirPoule(a));
    }

    // ---------- hydraterPoule ----------

    @Test
    void hydraterPouleMetHydratationA100() {
        Animal a = newPoule(50, 50);
        animalService.hydraterPoule(a);
        assertEquals(100, a.getJaugeHydratation());
    }

    @Test
    void hydraterPouleRetire1Ecu() {
        Animal a = newPoule(50, 50);
        animalService.hydraterPoule(a);
        verify(fermeService).retirerEcus(1, 1);
    }

    @Test
    void hydraterPouleDejaNourrieLeveException() {
        Animal a = newPoule(100, 50);
        assertThrows(IllegalCallerException.class, () -> animalService.hydraterPoule(a));
    }

    // ---------- soignerPoule ----------

    @Test
    void soignerPouleMaladeRemetSanteA100() {
        Animal a = newPoule(50, 50);
        a.setEstMalade(true);
        a.setJaugeSante(0);
        animalService.soignerPoule(a);
        assertEquals(100, a.getJaugeSante());
        verify(fermeService).retirerEcus(1, 6);
    }

    @Test
    void soignerPouleNonMaladeLeveException() {
        Animal a = newPoule(50, 50);
        a.setEstMalade(false);
        assertThrows(IllegalCallerException.class, () -> animalService.soignerPoule(a));
    }

    // ---------- vache ----------

    @Test
    void nourrirHerbeAjoute5Kg() {
        Animal v = newVache();
        v.setPoids(100f);
        animalService.nourrirHerbe(v);
        assertEquals(105f, v.getPoids(), 0.0001);
        assertTrue(v.isAMange());
        assertEquals(100, v.getJaugeFaim());
        verify(fermeService).retirerEcus(1, 5);
    }

    @Test
    void nourrirPailleAjoute3Kg() {
        Animal v = newVache();
        v.setPoids(100f);
        animalService.nourrirPaille(v);
        assertEquals(103f, v.getPoids(), 0.0001);
        assertTrue(v.isAMange());
    }

    @Test
    void nourrirDeuxFoisLeveException() {
        Animal v = newVache();
        animalService.nourrirHerbe(v);
        assertThrows(IllegalStateException.class, () -> animalService.nourrirHerbe(v));
        assertThrows(IllegalStateException.class, () -> animalService.nourrirPaille(v));
    }

    @Test
    void nourrirNonVacheLeveException() {
        Animal a = newPoule(50, 50);
        assertThrows(IllegalArgumentException.class, () -> animalService.nourrirHerbe(a));
        assertThrows(IllegalArgumentException.class, () -> animalService.nourrirPaille(a));
    }

    @Test
    void abreuverVacheMetHydratationA100() {
        Animal v = newVache();
        animalService.abreuverVache(v);
        assertEquals(100, v.getJaugeHydratation());
        verify(fermeService).retirerEcus(1, 2);
    }

    @Test
    void abreuverVacheApresManger1kgDePlus() {
        Animal v = newVache();
        animalService.nourrirHerbe(v); 
        animalService.abreuverVache(v);
        assertEquals(106f, v.getPoids(), 0.0001);
    }

    @Test
    void abreuverVacheDejaPleineLeveException() {
        Animal v = newVache();
        v.setJaugeHydratation(100);
        assertThrows(IllegalStateException.class, () -> animalService.abreuverVache(v));
    }

    @Test
    void nettoyerVacheSaleMetProprete100() {
        Animal v = newVache();
        v.setJaugeProprete(30);
        animalService.nettoyer(v);
        assertEquals(100, v.getJaugeProprete());
        verify(fermeService).retirerEcus(1, 3);
    }

    @Test
    void nettoyerVacheDejaPropreLeveException() {
        Animal v = newVache();
        v.setJaugeProprete(100);
        assertThrows(IllegalStateException.class, () -> animalService.nettoyer(v));
    }

    @Test
    void soignerVacheMaladeLaGuerit() {
        Animal v = newVache();
        v.setEstMalade(true);
        v.setJaugeSante(0);
        animalService.soignerVache(v);
        assertEquals(false, v.estMalade());
        assertEquals(100, v.getJaugeSante());
        verify(fermeService).retirerEcus(1, 6);
    }

    @Test
    void soignerVacheNonMaladeLeveException() {
        Animal v = newVache();
        v.setEstMalade(false);
        assertThrows(IllegalStateException.class, () -> animalService.soignerVache(v));
    }

    @Test
    void updateCowStatusEnfantAdulteSiAgeEtPoidsOk() {
        Animal v = newVache();
        v.setStade(TypeStade.ENFANT);
        v.setAge(10);
        v.setPoids(80f);
        animalService.updateCowStatus(v);
        assertEquals(TypeStade.ADULTE, v.getStade());
    }

    @Test
    void updateCowStatusResteEnfantSiPoidsInsuffisant() {
        Animal v = newVache();
        v.setStade(TypeStade.ENFANT);
        v.setAge(10);
        v.setPoids(50f);
        animalService.updateCowStatus(v);
        assertEquals(TypeStade.ENFANT, v.getStade());
    }

    @Test
    void updateCowWeightPlafonneA750() {
        Animal v = newVache();
        v.setPoids(740f);
        animalService.updateCowWeight(v, 50f);
        assertEquals(750f, v.getPoids(), 0.0001);
    }

    @Test
    void updateCowWeightAjouteNormalement() {
        Animal v = newVache();
        v.setPoids(100f);
        animalService.updateCowWeight(v, 25f);
        assertEquals(125f, v.getPoids(), 0.0001);
    }

    // ---------- produireLait ----------

    @Test
    void produireLaitAdulteNourrieProprePremiereTraite4L() {
        Animal v = newVache();
        v.setAMange(true);
        int litres = animalService.produireLait(v);
        assertEquals(4, litres);
        assertTrue(v.isAEteTraite());
    }

    @Test
    void produireLaitDeuxiemeTraite8L() {
        Animal v = newVache();
        v.setAMange(true);
        v.setAEteTraite(true);
        int litres = animalService.produireLait(v);
        assertEquals(8, litres);
    }

    @Test
    void produireLaitRenvoie0SiEnfant() {
        Animal v = newVache();
        v.setStade(TypeStade.ENFANT);
        v.setAMange(true);
        assertEquals(0, animalService.produireLait(v));
    }

    @Test
    void produireLaitRenvoie0SiPasMange() {
        Animal v = newVache();
        v.setAMange(false);
        assertEquals(0, animalService.produireLait(v));
    }

    @Test
    void produireLaitRenvoie0SiSale() {
        Animal v = newVache();
        v.setAMange(true);
        v.setJaugeProprete(50);
        assertEquals(0, animalService.produireLait(v));
    }

    @Test
    void produireLaitRenvoie0SiMalade() {
        Animal v = newVache();
        v.setAMange(true);
        v.setEstMalade(true);
        assertEquals(0, animalService.produireLait(v));
    }

    // ---------- updateChickenStatus(adulte)----------

    @Test
    void updateChickenStatusAdulteMalePoidsOkDevientReproducteur() {
        Animal a = newPoule(100, 100);
        a.setStade(TypeStade.ADULTE);
        a.setSexe(TypeSexe.MALE);
        a.setAge(5);
        a.setPoids(2.5f);
        animalService.updateChickenStatus(a);
        assertEquals(TypeRole.REPRODUCTEUR, a.getRole());
    }

    @Test
    void updateChickenStatusAdulteFemellePoidOkDevientPondeuse() {
        Animal a = newPoule(100, 100);
        a.setStade(TypeStade.ADULTE);
        a.setSexe(TypeSexe.FEMELLE);
        a.setAge(5);
        a.setPoids(2.5f);
        animalService.updateChickenStatus(a);
        assertEquals(TypeRole.PONDEUSE, a.getRole());
    }

    @Test
    void updateChickenStatusAdultePoidsInsuffisantGardeRoleElevage() {
        Animal a = newPoule(100, 100);
        a.setStade(TypeStade.ADULTE);
        a.setSexe(TypeSexe.MALE);
        a.setAge(5);
        a.setPoids(1.0f);
        animalService.updateChickenStatus(a);
        assertEquals(TypeRole.ELEVAGE, a.getRole());
    }

    @Test
    void updateChickenStatusAdulteAgeInsuffisantGardeRoleElevage() {
        Animal a = newPoule(100, 100);
        a.setStade(TypeStade.ADULTE);
        a.setSexe(TypeSexe.MALE);
        a.setAge(3);
        a.setPoids(2.5f);
        animalService.updateChickenStatus(a);
        assertEquals(TypeRole.ELEVAGE, a.getRole());
    }
}