package com.farm.tinyfarm.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.farm.tinyfarm.model.Animal;
import com.farm.tinyfarm.model.Habitat;
import com.farm.tinyfarm.model.Marche;
import com.farm.tinyfarm.model.TypeAnimal;
import com.farm.tinyfarm.model.TypeHabitat;

class TestModels {

    // ---------- Habitat ----------

    @Test
    void habitatConstructeurParDefautInitialiseListeVide() {
        Habitat h = new Habitat();
        assertNotNull(h.getListeAnimaux());
        assertEquals(0, h.getListeAnimaux().size());
    }

    @Test
    void habitatConstructeurCompletInitialiseLesChamps() {
        Habitat h = new Habitat(10, TypeHabitat.CLAPIER);
        assertEquals(10, h.getCapaMax());
        assertEquals(TypeHabitat.CLAPIER, h.getTypeHabitat());
        assertFalse(h.estSale());
        assertFalse(h.estMalade());
        assertNull(h.getDateEstSale());
    }

    @Test
    void habitatEstSaleReflechitLeSetter() {
        Habitat h = new Habitat(5, TypeHabitat.CLAPIER);
        h.setEstSale(true);
        assertTrue(h.estSale());
        h.setEstSale(false);
        assertFalse(h.estSale());
    }

    @Test
    void habitatEstMaladeReflechitLeSetter() {
        Habitat h = new Habitat(5, TypeHabitat.CLAPIER);
        h.setEstMalade(true);
        assertTrue(h.estMalade());
    }

    // ---------- Marche ----------

    @Test
    void marchePrixSetterGetterCoherents() {
        Marche m = new Marche();
        m.setPrix(42);
        assertEquals(42, m.getPrix());
        assertEquals(42, m.getPrixUnitaire());
    }

    @Test
    void marcheProduitEtQuantite() {
        Marche m = new Marche();
        m.setProduit("oeuf");
        m.setQuantite(5);
        assertEquals("oeuf", m.getProduit());
        assertEquals(5, m.getQuantite());
    }

    // ---------- Animal ----------

    @Test
    void animalEstMaladeReflechitLeSetter() {
        Animal a = new Animal();
        a.setEstMalade(true);
        assertTrue(a.estMalade());
        a.setEstMalade(false);
        assertFalse(a.estMalade());
    }

    @Test
    void animalValeursParDefaut() {
        Animal a = new Animal();
        assertEquals(100, a.getJaugeSante());
        assertEquals(100, a.getJaugeFaim());
        assertEquals(100, a.getJaugeProprete());
        assertEquals(100, a.getJaugeHydratation());
        assertEquals(0, a.getAge());
        assertFalse(a.estMalade());
        assertFalse(a.isAMange());
        assertFalse(a.isAEteTraite());
    }

    @Test
    void animalSettersTypeEtNom() {
        Animal a = new Animal();
        a.setTypeAnimal(TypeAnimal.LAPIN);
        a.setNom("Pompon");
        assertEquals(TypeAnimal.LAPIN, a.getTypeAnimal());
        assertEquals("Pompon", a.getNom());
    }
}