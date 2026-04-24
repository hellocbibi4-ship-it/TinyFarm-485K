package com.farm.tinyfarm.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
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
import com.farm.tinyfarm.model.TypeStock;
import com.farm.tinyfarm.repository.FermeRepository;
import com.farm.tinyfarm.repository.MarcheRepository;
import com.farm.tinyfarm.repository.RemiseRepository;
import com.farm.tinyfarm.service.FermeService;
import com.farm.tinyfarm.service.MarcheService;
import com.farm.tinyfarm.service.RemiseService;

@ExtendWith(MockitoExtension.class)
class MarcheServiceTests {

    @Mock
    private FermeRepository fermeRepository;

    @Mock
    private MarcheRepository marcheRepository;

    @Mock
    private RemiseRepository remiseRepository;

    @Mock
    private RemiseService remiseService;

    @Mock
    private FermeService fermeService;

    @InjectMocks
    private MarcheService marcheService;

    private Ferme ferme;

    @BeforeEach
    void setUp() {
        ferme = new Ferme();
        ferme.setIdFerme(1);
        ferme.setSoldeEcus(1000);
        ferme.setNom("fermetest");
        ferme.setVElev(0.0);
        ferme.setBElev(0.0);

        lenient().when(fermeRepository.findById(1)).thenReturn(Optional.of(ferme));
        
        // Configuration des comportements par défaut pour éviter les NullPointer lors des tests create
        lenient().when(remiseService.fromProduitMarche("OEUF")).thenReturn(TypeStock.OEUF);
        lenient().when(remiseService.fromProduitMarche("oeuf")).thenReturn(TypeStock.OEUF);
        lenient().when(remiseService.fromProduitMarche("LAIT")).thenReturn(TypeStock.LAIT);
        lenient().when(remiseService.fromProduitMarche("lait")).thenReturn(TypeStock.LAIT);
        lenient().when(remiseService.fromProduitMarche("LAPIN")).thenReturn(TypeStock.LAPIN);
        lenient().when(remiseService.fromProduitMarche("lapin")).thenReturn(TypeStock.LAPIN);
    }

    // ---------- create ----------

    @Test
    void createOffreOeufRetireLeStockEtCreeOffre() {
        when(marcheRepository.findByFerme_IdFermeAndProduitAndPrixUnitaire(eq(1), anyString(), eq(5)))
            .thenReturn(Optional.empty());
        when(marcheRepository.save(any(Marche.class))).thenAnswer(inv -> inv.getArgument(0));

        Marche res = marcheService.create(1, "OEUF", 3, 5);
        assertEquals(3, res.getQuantite());
        assertEquals(5, res.getPrix());
        assertEquals("oeuf", res.getProduit());
        verify(remiseService).retirerStock(1, TypeStock.OEUF, 3);
    }

    @Test
    void createOffreFusionneSiMemePrixEtMemeProduit() {
        Marche existant = new Marche();
        existant.setFerme(ferme);
        existant.setProduit("oeuf");
        existant.setPrix(5);
        existant.setQuantite(2);

        when(marcheRepository.findByFerme_IdFermeAndProduitAndPrixUnitaire(eq(1), eq("oeuf"), eq(5)))
            .thenReturn(Optional.of(existant));
        when(marcheRepository.save(any(Marche.class))).thenAnswer(inv -> inv.getArgument(0));

        Marche res = marcheService.create(1, "OEUF", 3, 5);
        assertEquals(5, res.getQuantite());
    }

    @Test
    void createOffreQuantiteNulleLeveException() {
        assertThrows(IllegalArgumentException.class, () -> marcheService.create(1, "OEUF", 0, 5));
    }

    @Test
    void createOffreQuantiteNegativeLeveException() {
        assertThrows(IllegalArgumentException.class, () -> marcheService.create(1, "OEUF", -1, 5));
    }

    @Test
    void createOffrePrixNegatifLeveException() {
        assertThrows(IllegalArgumentException.class, () -> marcheService.create(1, "OEUF", 1, -1));
    }

    @Test
    void createOffrePrixNulLeveException() {
        assertThrows(IllegalArgumentException.class, () -> marcheService.create(1, "OEUF", 1, 0));
    }

    @Test
    void createOffreFermeInconnueLeveException() {
        when(fermeRepository.findById(99)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> marcheService.create(99, "OEUF", 1, 2));
    }

    @Test
    void createOffreLapinStockInsuffisantLeveException() {
        when(fermeService.countLapinsVendables(1)).thenReturn(2);
        assertThrows(IllegalArgumentException.class, () -> marcheService.create(1, "LAPIN", 5, 20));
    }

    @Test
    void createOffreLapinOkRetireLesLapins() {
        when(fermeService.countLapinsVendables(1)).thenReturn(5);
        when(marcheRepository.findByFerme_IdFermeAndProduitAndPrixUnitaire(eq(1), eq("lapin"), eq(20)))
            .thenReturn(Optional.empty());
        when(marcheRepository.save(any(Marche.class))).thenAnswer(inv -> inv.getArgument(0));

        Marche res = marcheService.create(1, "LAPIN", 3, 20);
        assertEquals(3, res.getQuantite());
        verify(fermeService).retirerLapinsVivants(1, 3);
    }

    @Test
    void create_sauvegarde_l_offre_valide() {
        when(marcheRepository.findByFerme_IdFermeAndProduitAndPrixUnitaire(eq(1), anyString(), eq(12)))
            .thenReturn(Optional.empty());
        when(marcheRepository.save(any(Marche.class))).thenAnswer(inv -> inv.getArgument(0));

        Marche offre = marcheService.create(1, "OEUF", 4, 12);

        assertEquals("oeuf", offre.getProduit());
        assertEquals(4, offre.getQuantite());
        assertEquals(12, offre.getPrix());
        assertEquals(ferme, offre.getFerme());
    }

    // ---------- transaction ----------

    @Test
    void transactionOffreInconnueLeveException() {
        when(marcheRepository.findById(99)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> marcheService.transaction(1, 99, 1));
    }

    @Test
    void transactionQuantiteNulleLeveException() {
        Marche offre = newOffre(5, 3, "oeuf");
        when(marcheRepository.findById(10)).thenReturn(Optional.of(offre));
        assertThrows(IllegalArgumentException.class, () -> marcheService.transaction(1, 10, 0));
    }

    @Test
    void transactionQuantiteTropGrandeLeveException() {
        Marche offre = newOffre(2, 3, "oeuf");
        when(marcheRepository.findById(10)).thenReturn(Optional.of(offre));

        assertThrows(IllegalArgumentException.class, () -> marcheService.transaction(2, 10, 5));
    }

    @Test
    void transactionAchatDeSaPropreOffreLeveException() {
        Marche offre = newOffre(5, 3, "oeuf");
        when(marcheRepository.findById(10)).thenReturn(Optional.of(offre));
        assertThrows(IllegalArgumentException.class, () -> marcheService.transaction(1, 10, 1));
    }

    @Test
    void transactionSoldeInsuffisantLeveException() {
        Marche offre = newOffre(5, 100, "oeuf");
        when(marcheRepository.findById(10)).thenReturn(Optional.of(offre));
        
        Ferme acheteur = new Ferme();
        acheteur.setIdFerme(2);
        acheteur.setSoldeEcus(10);
        when(fermeRepository.findById(2)).thenReturn(Optional.of(acheteur));
        
        assertThrows(IllegalArgumentException.class, () -> marcheService.transaction(2, 10, 1));
    }

    @Test
    void transactionReussitMetAJourSoldesEtStock() {
        Marche offre = newOffre(5, 10, "oeuf");
        when(marcheRepository.findById(10)).thenReturn(Optional.of(offre));
        
        Ferme acheteur = new Ferme();
        acheteur.setIdFerme(2);
        acheteur.setSoldeEcus(1000);
        acheteur.setBElev(0.0);
        when(fermeRepository.findById(2)).thenReturn(Optional.of(acheteur));
        when(fermeRepository.save(any(Ferme.class))).thenAnswer(inv -> inv.getArgument(0));

        marcheService.transaction(2, 10, 3);

        assertEquals(970, acheteur.getSoldeEcus());
        assertEquals(1030, ferme.getSoldeEcus());
        verify(remiseService).ajouterStock(2, TypeStock.OEUF, 3);
    }

    @Test
    void transactionTotaleSupprimeLOffre() {
        Marche offre = newOffre(3, 10, "oeuf");
        when(marcheRepository.findById(10)).thenReturn(Optional.of(offre));
        
        Ferme acheteur = new Ferme();
        acheteur.setIdFerme(2);
        acheteur.setSoldeEcus(1000);
        acheteur.setBElev(0.0);
        when(fermeRepository.findById(2)).thenReturn(Optional.of(acheteur));

        marcheService.transaction(2, 10, 3);
        verify(marcheRepository).delete(offre);
    }

    // ---------- getById / reset ----------

    @Test
    void getByIdInconnuLeveException() {
        when(marcheRepository.findById(99)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> marcheService.getById(99));
    }

    @Test
    void resetAppelleDeleteAll() {
        marcheService.reset();
        verify(marcheRepository).deleteAll();
    }

    // ---------- utilitaires ----------

    private Marche newOffre(int quantite, int prix, String produit) {
        Marche m = new Marche();
        m.setFerme(ferme);
        m.setQuantite(quantite);
        m.setPrix(prix);
        m.setProduit(produit);
        m.setIdOffre(10);
        return m;
    }
}