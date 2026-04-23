package com.farm.tinyfarm.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.farm.tinyfarm.model.Animal;
import com.farm.tinyfarm.model.Ferme;
import com.farm.tinyfarm.model.Habitat;
import com.farm.tinyfarm.model.TypeHabitat;
import com.farm.tinyfarm.repository.HabitatRepository;

@ExtendWith(MockitoExtension.class)
class HabitatServiceTests {

    @Mock
    private HabitatRepository habitatRepository;

    @Mock
    private FermeService fermeService;

    @InjectMocks
    private HabitatService habitatService;

    private Habitat clapier;

    @BeforeEach
    void setUp() {
        Ferme ferme = new Ferme();
        ferme.setIdFerme(1);

        clapier = new Habitat();
        clapier.setFerme(ferme);
        clapier.setTypeHabitat(TypeHabitat.CLAPIER);
        clapier.setCapaMax(2);
        clapier.setListeAnimaux(new ArrayList<>());
    }

    @Test
    void ajouterAnimal_habitat_plein_leve_une_exception() {
        clapier.getListeAnimaux().add(new Animal());
        clapier.getListeAnimaux().add(new Animal());

        assertThrows(IllegalStateException.class, () -> habitatService.ajouterAnimal(clapier, new Animal()));
    }

    @Test
    void nettoyerClapier_fonctionne_et_retire_des_ecus() {
        clapier.setEstSale(true);

        habitatService.nettoyerClapier(clapier);

        assertFalse(clapier.estSale());
        assertNull(clapier.getDateEstSale());
        verify(fermeService).retirerEcus(1, 3);
        verify(habitatRepository).save(clapier);
    }

    @Test
    void nourrirClapier_type_invalide_leve_une_exception() {
        Habitat pre = new Habitat();
        pre.setTypeHabitat(TypeHabitat.PRE);
        pre.setListeAnimaux(new ArrayList<>());

        assertThrows(IllegalArgumentException.class, () -> habitatService.nourrirClapier(pre));
    }
}
