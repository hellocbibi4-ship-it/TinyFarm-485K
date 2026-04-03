package com.farm.tinyfarm.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.farm.tinyfarm.model.Ferme;
import com.farm.tinyfarm.model.Utilisateur;
import com.farm.tinyfarm.repository.UtilisateurRepository;
import com.farm.tinyfarm.service.FermeService;
import com.farm.tinyfarm.service.RemiseService;

@Configuration
public class DevDataInitializer {

    @Bean
    CommandLineRunner seedLocalUsers(
        UtilisateurRepository utilisateurRepository,
        FermeService fermeService,
        RemiseService remiseService
    ) {
        return args -> {
            ensureLocalUser(utilisateurRepository, fermeService, remiseService, "a", "a1", "f1");
            ensureLocalUser(utilisateurRepository, fermeService, remiseService, "b", "b2", "f2");
        };
    }

    private void ensureLocalUser(
        UtilisateurRepository utilisateurRepository,
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

        utilisateurRepository.save(utilisateur);
    }
}
