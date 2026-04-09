package com.farm.tinyfarm.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
    @ConditionalOnProperty(name = "tinyfarm.dev.seed-local-users", havingValue = "true", matchIfMissing = true)
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
            utilisateur.setFerme(savedFerme);
        } else {
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

        if ("a".equals(username) || "b".equals(username)) {
            ferme.setJourActuel(1);
            ferme.setAchatsCollectiviteRestants(12);
            ferme.setNbVaches(1);
            ferme.setNbPoules(4);
            ferme.setNbLapins(8);
            ferme.setNbLapinsMalades(0);
            ferme.setNbVachesAffamees(0);
            ferme.setNbVachesAssoiffees(0);
            ferme.setNbPouleAffamees(0);
            ferme.setNbPouleAssoiffees(0);
            ferme.setNbLapinsAffames(0);
            ferme.setNbLapinsAssoiffes(0);
            fermeRepository.save(ferme);

            setExactStock(remiseService, ferme.getIdFerme(), TypeStock.OEUF, 0);
            setExactStock(remiseService, ferme.getIdFerme(), TypeStock.LAIT, 0);
            setExactStock(remiseService, ferme.getIdFerme(), TypeStock.LAPIN, 0);
            setExactStock(remiseService, ferme.getIdFerme(), TypeStock.NOURRITURE, 0);
            setExactStock(remiseService, ferme.getIdFerme(), TypeStock.EAU, 0);
            setExactStock(remiseService, ferme.getIdFerme(), TypeStock.PAILLE, 0);
            setExactStock(remiseService, ferme.getIdFerme(), TypeStock.SAVON, 0);
            setExactStock(remiseService, ferme.getIdFerme(), TypeStock.SERINGUE, 0);
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
