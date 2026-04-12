package com.farm.tinyfarm.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.transaction.support.TransactionTemplate;

import com.farm.tinyfarm.model.Ferme;
import com.farm.tinyfarm.model.Utilisateur;
import com.farm.tinyfarm.repository.FermeRepository;
import com.farm.tinyfarm.repository.UtilisateurRepository;
import com.farm.tinyfarm.service.FermeService;

@Configuration
public class LegacyUserCleanupConfig {

    @Bean
    @Order(1)
    @ConditionalOnProperty(name = "tinyfarm.dev.cleanup-legacy-local-users", havingValue = "true", matchIfMissing = true)
    CommandLineRunner cleanupLegacyUsers(
        UtilisateurRepository utilisateurRepository,
        FermeRepository fermeRepository,
        FermeService fermeService,
        TransactionTemplate transactionTemplate
    ) {
        return args -> {
            transactionTemplate.executeWithoutResult(status -> {
                cleanupLegacyUser(utilisateurRepository, fermeService, "a");
                cleanupLegacyUser(utilisateurRepository, fermeService, "b");
                cleanupSellerUsers(utilisateurRepository, fermeService);
                cleanupSellerFarms(fermeRepository, fermeService);
                cleanupOrphanFarms(fermeRepository, fermeService);
            });
        };
    }

    private void cleanupLegacyUser(
        UtilisateurRepository utilisateurRepository,
        FermeService fermeService,
        String username
    ) {
        Utilisateur legacyUser = utilisateurRepository.findByGithubUsername(username).orElse(null);
        if (legacyUser == null) {
            return;
        }

        if (legacyUser.getFerme() != null) {
            fermeService.deleteFarmWithDependencies(legacyUser.getFerme().getIdFerme());
        }

        utilisateurRepository.deleteByGithubUsername(username);
    }

    private void cleanupSellerUsers(
        UtilisateurRepository utilisateurRepository,
        FermeService fermeService
    ) {
        for (Utilisateur utilisateur : utilisateurRepository.findAllByGithubUsernameStartingWith("seller-")) {
            if (utilisateur.getFerme() != null) {
                fermeService.deleteFarmWithDependencies(utilisateur.getFerme().getIdFerme());
            }
            utilisateurRepository.deleteByGithubUsername(utilisateur.getGithubUsername());
        }
    }

    private void cleanupSellerFarms(
        FermeRepository fermeRepository,
        FermeService fermeService
    ) {
        for (Ferme ferme : fermeRepository.findAllByNomStartingWith("seller-")) {
            fermeService.deleteFarmWithDependencies(ferme.getIdFerme());
        }
    }

    private void cleanupOrphanFarms(
        FermeRepository fermeRepository,
        FermeService fermeService
    ) {
        for (Ferme ferme : fermeRepository.findAllByUtilisateurIsNull()) {
            fermeService.deleteFarmWithDependencies(ferme.getIdFerme());
        }
    }
}
