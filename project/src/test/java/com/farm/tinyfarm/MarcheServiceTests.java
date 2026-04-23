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
import com.farm.tinyfarm.model.Marche;
import com.farm.tinyfarm.model.Remise;
import com.farm.tinyfarm.repository.FermeRepository;
import com.farm.tinyfarm.repository.MarcheRepository;
import com.farm.tinyfarm.repository.RemiseRepository;

@ExtendWith(MockitoExtension.class)
class MarcheServiceTests {

    @Mock
    private FermeRepository fermeRepository;

    @Mock
    private RemiseRepository remiseRepository;

    @Mock
    private MarcheRepository marcheRepository;

    @InjectMocks
    private MarcheService marcheService;

    private Ferme vendeur;

    @BeforeEach
    void setUp() {
        vendeur = new Ferme();
        vendeur.setIdFerme(1);
        vendeur.setSoldeEcus(500);
    }

    @Test
    void create_quantite_invalide_leve_une_exception() {
        when(fermeRepository.findById(1)).thenReturn(Optional.of(vendeur));

        assertThrows(IllegalArgumentException.class, () -> marcheService.create(1, "OEUF", 0, 5));
    }

    @Test
    void create_sauvegarde_l_offre_valide() {
        when(fermeRepository.findById(1)).thenReturn(Optional.of(vendeur));
        when(marcheRepository.save(org.mockito.ArgumentMatchers.any(Marche.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        Marche offre = marcheService.create(1, "OEUF", 4, 12);

        assertEquals("OEUF", offre.getProduit());
        assertEquals(4, offre.getQuantite());
        assertEquals(12, offre.getPrix());
        assertEquals(vendeur, offre.getFerme());
    }

    @Test
    void transaction_refuse_si_solde_acheteur_insuffisant() {
        Ferme acheteur = new Ferme();
        acheteur.setIdFerme(2);
        acheteur.setSoldeEcus(10);

        Remise remise = new Remise();
        remise.setRemiseId(2);

        Marche offre = new Marche();
        offre.setFerme(vendeur);
        offre.setProduit("OEUF");
        offre.setQuantite(2);
        offre.setPrix(20);

        when(fermeRepository.findById(2)).thenReturn(Optional.of(acheteur));
        when(remiseRepository.findById(2)).thenReturn(Optional.of(remise));

        assertThrows(IllegalArgumentException.class, () -> marcheService.transaction(2, offre, 1));
    }
}
