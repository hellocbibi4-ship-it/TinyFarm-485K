package com.farm.tinyfarm.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import com.farm.tinyfarm.model.Ferme;
import com.farm.tinyfarm.model.TypeStock;
import com.farm.tinyfarm.model.Utilisateur;
import com.farm.tinyfarm.repository.FermeRepository;
import com.farm.tinyfarm.repository.UtilisateurRepository;
import com.farm.tinyfarm.service.FermeService;
import com.farm.tinyfarm.service.RemiseService;

@Configuration
public class DevDataInitializer {

    @Bean
    @Order(1)
    CommandLineRunner seedLocalUsers(
        UtilisateurRepository utilisateurRepository,
        FermeRepository fermeRepository,
        FermeService fermeService,
        RemiseService remiseService
    ) {
        return args -> {
            ensureLocalUser(utilisateurRepository, fermeRepository, fermeService, remiseService, "a", "a1", "f1");
            ensureLocalUser(utilisateurRepository, fermeRepository, fermeService, remiseService, "b", "b2", "f2");
        };
    }

    private void ensureLocalUser(
        UtilisateurRepository utilisateurRepository,
        FermeRepository fermeRepository,
        FermeService fermeService,
        RemiseService remiseService,
        String username,
        String password,
        String farmName
    ) {
        Utilisateur utilisateur = utilisateurRepository.findByGithubUsername(username)
            .orElseGet(() -> {
                Utilisateur createdUser = new Utilisateur();
                createdUser.setGithubUsername(username);
                createdUser.setRole("ROLE_USER");
                return utilisateurRepository.save(createdUser);
            });

        utilisateur.setPassword(password);
        if (utilisateur.getRole() == null || utilisateur.getRole().isBlank()) {
            utilisateur.setRole("ROLE_USER");
        }

        if (utilisateur.getFerme() == null) {
            Ferme ferme = new Ferme();
            ferme.setUtilisateur(utilisateur);
            ferme.setNom(farmName);

            Ferme savedFerme = fermeService.create(ferme);
            if ("a".equals(username)) {
                savedFerme.setNbPoules(3);
                savedFerme.setNbLapins(3);
                fermeRepository.save(savedFerme);
            }
            utilisateur.setFerme(savedFerme);
        } else {
            if ("a".equals(username)) {
                Ferme ferme = utilisateur.getFerme();
                ferme.setNbPoules(3);
                ferme.setNbLapins(3);
                fermeRepository.save(ferme);
            }
            remiseService.getOrCreateByFermeId(utilisateur.getFerme().getIdFerme());
        }

        seedDemoFarmState(username, utilisateur.getFerme(), fermeRepository, remiseService);
        utilisateurRepository.save(utilisateur);
    }

    private void seedDemoFarmState(
        String username,
        Ferme ferme,
        FermeRepository fermeRepository,
        RemiseService remiseService
    ) {
        if (ferme == null) {
            return;
        }

        if ("a".equals(username)) {
            ferme.setJourActuel(ferme.getJourActuel() == null ? 1 : Math.max(1, ferme.getJourActuel()));
            ferme.setAchatsCollectiviteRestants(
                ferme.getAchatsCollectiviteRestants() == null ? 12 : Math.max(0, Math.min(12, ferme.getAchatsCollectiviteRestants()))
            );
            ferme.setNbPoules(Math.max(ferme.getNbPoules() == null ? 0 : ferme.getNbPoules(), 3));
            ferme.setNbLapins(Math.max(ferme.getNbLapins() == null ? 0 : ferme.getNbLapins(), 3));
            fermeRepository.save(ferme);
            setExactStock(remiseService, ferme.getIdFerme(), TypeStock.OEUF, 0);
            ensureMinimumStock(remiseService, ferme.getIdFerme(), TypeStock.NOURRITURE, 2);
            return;
        }

        if ("b".equals(username)) {
            ferme.setJourActuel(ferme.getJourActuel() == null ? 1 : Math.max(1, ferme.getJourActuel()));
            ferme.setAchatsCollectiviteRestants(
                ferme.getAchatsCollectiviteRestants() == null ? 12 : Math.max(0, Math.min(12, ferme.getAchatsCollectiviteRestants()))
            );
            ferme.setNbPoules(Math.max(ferme.getNbPoules() == null ? 0 : ferme.getNbPoules(), 3));
            ferme.setNbLapins(Math.max(ferme.getNbLapins() == null ? 0 : ferme.getNbLapins(), 2));
            fermeRepository.save(ferme);
            setExactStock(remiseService, ferme.getIdFerme(), TypeStock.OEUF, 0);
            ensureMinimumStock(remiseService, ferme.getIdFerme(), TypeStock.SAVON, 1);
        }
    }

    private void ensureMinimumStock(RemiseService remiseService, Integer fermeId, TypeStock typeStock, int minimum) {
        int stockActuel = switch (typeStock) {
            case OEUF -> remiseService.getById(fermeId).getStockOeuf();
            case LAIT -> remiseService.getById(fermeId).getStockLait();
            case LAPIN -> remiseService.getById(fermeId).getStockLapin();
            case NOURRITURE -> remiseService.getById(fermeId).getStockNourriture();
            case EAU -> remiseService.getById(fermeId).getStockEau();
            case PAILLE -> remiseService.getById(fermeId).getStockPaille();
            case SAVON -> remiseService.getById(fermeId).getStockSavon();
            case SERINGUE -> remiseService.getById(fermeId).getStockSeringue();
        };

        if (stockActuel < minimum) {
            remiseService.ajouterStock(fermeId, typeStock, minimum - stockActuel);
        }
    }

    private void setExactStock(RemiseService remiseService, Integer fermeId, TypeStock typeStock, int expectedValue) {
        int stockActuel = switch (typeStock) {
            case OEUF -> remiseService.getById(fermeId).getStockOeuf();
            case LAIT -> remiseService.getById(fermeId).getStockLait();
            case LAPIN -> remiseService.getById(fermeId).getStockLapin();
            case NOURRITURE -> remiseService.getById(fermeId).getStockNourriture();
            case EAU -> remiseService.getById(fermeId).getStockEau();
            case PAILLE -> remiseService.getById(fermeId).getStockPaille();
            case SAVON -> remiseService.getById(fermeId).getStockSavon();
            case SERINGUE -> remiseService.getById(fermeId).getStockSeringue();
        };

        if (stockActuel > expectedValue) {
            remiseService.retirerStock(fermeId, typeStock, stockActuel - expectedValue);
        } else if (stockActuel < expectedValue) {
            remiseService.ajouterStock(fermeId, typeStock, expectedValue - stockActuel);
        }
    }
}
