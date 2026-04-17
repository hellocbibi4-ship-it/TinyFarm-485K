package com.farm.tinyfarm.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.farm.tinyfarm.model.Ferme;
import com.farm.tinyfarm.model.Remise;
import com.farm.tinyfarm.model.TypeStock;
import com.farm.tinyfarm.repository.FermeRepository;
import com.farm.tinyfarm.repository.RemiseRepository;
import com.farm.tinyfarm.service.FermeService;
import com.farm.tinyfarm.service.RemiseService;

class TestRemiseService {

    private RemiseRepository remiseRepository;
    private FermeRepository fermeRepository;
    private FermeService fermeService;
    private RemiseService remiseService;
    private Ferme ferme;
    private Remise remise;

    @BeforeEach
    void setUp() {
        remiseRepository = mock(RemiseRepository.class);
        fermeRepository = mock(FermeRepository.class);
        fermeService = mock(FermeService.class);
        remiseService = new RemiseService(remiseRepository, fermeRepository, fermeService);

        ferme = new Ferme();
        ferme.setIdFerme(1);
        ferme.setSoldeEcus(100);

        remise = new Remise();
        remise.setFerme(ferme);

        when(remiseRepository.save(any(Remise.class))).thenAnswer(inv -> inv.getArgument(0));
        when(fermeRepository.save(any(Ferme.class))).thenAnswer(inv -> inv.getArgument(0));
        when(fermeRepository.findById(1)).thenReturn(Optional.of(ferme));
        when(remiseRepository.findById(1)).thenReturn(Optional.of(remise));
        doNothing().when(fermeService).consommerAchatCollectivite(anyInt());
    }

    // ---------- ajouterStock ----------

    @Test
    void ajouterStockOeufIncremente() {
        remiseService.ajouterStock(1, TypeStock.OEUF, 5);
        assertEquals(5, remise.getStockOeuf());
    }

    @Test
    void ajouterStockLaitIncremente() {
        remiseService.ajouterStock(1, TypeStock.LAIT, 3);
        assertEquals(3, remise.getStockLait());
    }

    @Test
    void ajouterStockNourritureIncremente() {
        remiseService.ajouterStock(1, TypeStock.NOURRITURE, 2);
        assertEquals(2, remise.getStockNourriture());
    }

    @Test
    void ajouterStockEauIncremente() {
        remiseService.ajouterStock(1, TypeStock.EAU, 4);
        assertEquals(4, remise.getStockEau());
    }

    @Test
    void ajouterStockPailleIncremente() {
        remiseService.ajouterStock(1, TypeStock.PAILLE, 2);
        assertEquals(2, remise.getStockPaille());
    }

    @Test
    void ajouterStockSavonIncremente() {
        remiseService.ajouterStock(1, TypeStock.SAVON, 1);
        assertEquals(1, remise.getStockSavon());
    }

    @Test
    void ajouterStockSeringueIncremente() {
        remiseService.ajouterStock(1, TypeStock.SERINGUE, 1);
        assertEquals(1, remise.getStockSeringue());
    }

    @Test
    void ajouterStockLapinIncremente() {
        remiseService.ajouterStock(1, TypeStock.LAPIN, 3);
        assertEquals(3, remise.getStockLapin());
    }

    @Test
    void ajouterStockMontantNulLeveException() {
        assertThrows(IllegalArgumentException.class,
            () -> remiseService.ajouterStock(1, TypeStock.OEUF, 0));
    }

    @Test
    void ajouterStockMontantNegatifLeveException() {
        assertThrows(IllegalArgumentException.class,
            () -> remiseService.ajouterStock(1, TypeStock.OEUF, -2));
    }

    // ---------- retirerStock ----------

    @Test
    void retirerStockOeufDecremente() {
        remise.setStockOeuf(10);
        remiseService.retirerStock(1, TypeStock.OEUF, 3);
        assertEquals(7, remise.getStockOeuf());
    }

    @Test
    void retirerStockInsuffisantLeveException() {
        remise.setStockOeuf(1);
        assertThrows(IllegalArgumentException.class,
            () -> remiseService.retirerStock(1, TypeStock.OEUF, 5));
    }

    @Test
    void retirerStockMontantNulLeveException() {
        assertThrows(IllegalArgumentException.class,
            () -> remiseService.retirerStock(1, TypeStock.OEUF, 0));
    }

    @Test
    void retirerStockLaitDecremente() {
        remise.setStockLait(5);
        remiseService.retirerStock(1, TypeStock.LAIT, 2);
        assertEquals(3, remise.getStockLait());
    }

    @Test
    void retirerStockPailleDecremente() {
        remise.setStockPaille(4);
        remiseService.retirerStock(1, TypeStock.PAILLE, 1);
        assertEquals(3, remise.getStockPaille());
    }

    // ---------- getCout ----------

    @Test
    void getCoutNourritureEst5() {
        assertEquals(5, remiseService.getCout(TypeStock.NOURRITURE));
    }

    @Test
    void getCoutPailleEst5() {
        assertEquals(5, remiseService.getCout(TypeStock.PAILLE));
    }

    @Test
    void getCoutEauEst2() {
        assertEquals(2, remiseService.getCout(TypeStock.EAU));
    }

    @Test
    void getCoutSavonEst3() {
        assertEquals(3, remiseService.getCout(TypeStock.SAVON));
    }

    @Test
    void getCoutSeringueEst6() {
        assertEquals(6, remiseService.getCout(TypeStock.SERINGUE));
    }

    @Test
    void getCoutOeufLeveException() {
        assertThrows(IllegalArgumentException.class, () -> remiseService.getCout(TypeStock.OEUF));
    }

    // ---------- fromProduitMarche ----------

    @Test
    void fromProduitMarcheOeufsRetourneOeuf() {
        assertEquals(TypeStock.OEUF, remiseService.fromProduitMarche("OEUF"));
        assertEquals(TypeStock.OEUF, remiseService.fromProduitMarche("oeufs"));
    }

    @Test
    void fromProduitMarcheLapinsRetourneLapin() {
        assertEquals(TypeStock.LAPIN, remiseService.fromProduitMarche("lapin"));
        assertEquals(TypeStock.LAPIN, remiseService.fromProduitMarche("LAPINS"));
    }

    @Test
    void fromProduitMarcheLait() {
        assertEquals(TypeStock.LAIT, remiseService.fromProduitMarche("lait"));
    }

    @Test
    void fromProduitMarcheGrainRetourneNourriture() {
        assertEquals(TypeStock.NOURRITURE, remiseService.fromProduitMarche("grain"));
        assertEquals(TypeStock.NOURRITURE, remiseService.fromProduitMarche("NOURRITURE"));
    }

    @Test
    void fromProduitMarcheAutresProduits() {
        assertEquals(TypeStock.EAU, remiseService.fromProduitMarche("eau"));
        assertEquals(TypeStock.SAVON, remiseService.fromProduitMarche("savon"));
        assertEquals(TypeStock.SERINGUE, remiseService.fromProduitMarche("seringue"));
        assertEquals(TypeStock.PAILLE, remiseService.fromProduitMarche("paille"));
    }

    @Test
    void fromProduitMarcheNullLeveException() {
        assertThrows(IllegalArgumentException.class, () -> remiseService.fromProduitMarche(null));
    }

    @Test
    void fromProduitMarcheVideLeveException() {
        assertThrows(IllegalArgumentException.class, () -> remiseService.fromProduitMarche("   "));
    }

    @Test
    void fromProduitMarcheInconnuLeveException() {
        assertThrows(IllegalArgumentException.class, () -> remiseService.fromProduitMarche("banane"));
    }

    // ---------- acheterObjetEntretien ----------

    @Test
    void acheterObjetEntretienRetireLeSoldeEtAjouteAuStock() {
        remiseService.acheterObjetEntretien(1, TypeStock.EAU);
        assertEquals(98, ferme.getSoldeEcus());
        assertEquals(1, remise.getStockEau());
        verify(fermeService).consommerAchatCollectivite(1);
    }

    @Test
    void acheterObjetEntretienSoldeInsuffisantLeveException() {
        ferme.setSoldeEcus(1);
        assertThrows(IllegalArgumentException.class,
            () -> remiseService.acheterObjetEntretien(1, TypeStock.SERINGUE));
    }

    // ---------- createRemise / getOrCreate ----------

    @Test
    void createRemiseInitialiseTousLesStocksAZero() {
        when(remiseRepository.findById(2)).thenReturn(Optional.empty());
        when(fermeRepository.findById(2)).thenReturn(Optional.of(ferme));
        Remise nouvelle = remiseService.createRemise(2);
        assertEquals(0, nouvelle.getStockOeuf());
        assertEquals(0, nouvelle.getStockLait());
        assertEquals(0, nouvelle.getStockNourriture());
    }

    @Test
    void createRemiseFermeIntrouvableLeveException() {
        when(fermeRepository.findById(99)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> remiseService.createRemise(99));
    }
}