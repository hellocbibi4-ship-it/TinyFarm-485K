package com.farm.tinyfarm.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.farm.tinyfarm.model.Ferme;
import com.farm.tinyfarm.model.Remise;
import com.farm.tinyfarm.model.TypeStock;
import com.farm.tinyfarm.repository.FermeRepository;
import com.farm.tinyfarm.repository.RemiseRepository;

@ExtendWith(MockitoExtension.class)
class RemiseServiceTests {

    @Mock
    private RemiseRepository remiseRepository;

    @Mock
    private FermeRepository fermeRepository;

    @InjectMocks
    private RemiseService remiseService;

    private Ferme ferme;
    private Remise remise;

    @BeforeEach
    void setUp() {
        ferme = new Ferme();
        ferme.setIdFerme(1);

        remise = new Remise();
        remise.setRemiseId(1);
        remise.setFerme(ferme);
    }

    @Test
    void createRemise_initialise_les_stocks_a_zero() {
        when(fermeRepository.findById(1)).thenReturn(Optional.of(ferme));
        when(remiseRepository.save(org.mockito.ArgumentMatchers.any(Remise.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        Remise created = remiseService.createRemise(1);

        assertEquals(0, created.getStockSavon());
        assertEquals(0, created.getStockPaille());
        assertEquals(0, created.getStockNourriture());
    }

    @Test
    void ajouterStock_augmente_le_stock_cible() {
        when(remiseRepository.findById(1)).thenReturn(Optional.of(remise));

        remiseService.ajouterStock(1, TypeStock.PAILLE, 3);

        assertEquals(3, remise.getStockPaille());
    }

    @Test
    void retirerStock_montant_invalide_leve_une_exception() {
        when(remiseRepository.findById(1)).thenReturn(Optional.of(remise));

        assertThrows(IllegalArgumentException.class, () -> remiseService.retirerStock(1, TypeStock.EAU, 0));
    }
}
