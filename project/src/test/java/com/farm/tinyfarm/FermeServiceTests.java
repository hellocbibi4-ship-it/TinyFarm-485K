package com.farm.tinyfarm.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.farm.tinyfarm.model.Ferme;
import com.farm.tinyfarm.model.Remise;
import com.farm.tinyfarm.repository.FermeRepository;
import com.farm.tinyfarm.repository.RemiseRepository;

@ExtendWith(MockitoExtension.class)
class FermeServiceTests {

    @Mock
    private FermeRepository fermeRepository;

    @Mock
    private RemiseRepository remiseRepository;

    @InjectMocks
    private FermeService fermeService;

    private Ferme ferme;

    @BeforeEach
    void setUp() {
        ferme = new Ferme();
        ferme.setIdFerme(1);
        ferme.setNom("FermeTest");
        ferme.setSoldeEcus(1500);
        ferme.setScore(0);
    }

    @Test
    void create_initialise_les_valeurs_par_defaut() {
        when(fermeRepository.save(ferme)).thenReturn(ferme);

        Ferme result = fermeService.create(ferme);

        assertEquals(1500, result.getSoldeEcus());
        assertEquals(0, result.getScore());
        assertFalse(result.getHibernation());
        assertEquals(0, result.getAchatsJour());
        verify(remiseRepository).save(org.mockito.ArgumentMatchers.any(Remise.class));
        verify(fermeRepository).save(ferme);
    }

    @Test
    void ajouterEcus_montant_negatif_leve_une_exception() {
        when(fermeRepository.findById(1)).thenReturn(Optional.of(ferme));

        assertThrows(IllegalArgumentException.class, () -> fermeService.ajouterEcus(1, -5));
    }

    @Test
    void getAchatsJourActuels_reset_si_jour_différent() {
        ferme.setAchatsJour(9);
        ferme.setDateDernierAchat(LocalDate.now().minusDays(1));
        when(fermeRepository.findById(1)).thenReturn(Optional.of(ferme));

        int achats = fermeService.getAchatsJourActuels(1);

        assertEquals(0, achats);
        assertEquals(0, ferme.getAchatsJour());
        assertEquals(LocalDate.now(), ferme.getDateDernierAchat());
    }
}
