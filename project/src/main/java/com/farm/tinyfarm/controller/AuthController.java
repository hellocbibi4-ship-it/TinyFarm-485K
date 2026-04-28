/*
 * Contrôleur REST gérant les opérations authentification de TinyFarm et exposant les points d'API correspondants.
 */



package com.farm.tinyfarm.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.farm.tinyfarm.model.Ferme;
import com.farm.tinyfarm.model.Utilisateur;
import com.farm.tinyfarm.repository.UtilisateurRepository;
import com.farm.tinyfarm.service.FermeService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UtilisateurRepository utilisateurRepository;
    private final FermeService fermeService;
    private final String githubClientId;
    private final String githubClientSecret;

    public AuthController(
        UtilisateurRepository utilisateurRepository,
        FermeService fermeService,
        @Value("${spring.security.oauth2.client.registration.github.client-id:dummy-client-id}") String githubClientId,
        @Value("${spring.security.oauth2.client.registration.github.client-secret:dummy-client-secret}") String githubClientSecret
    ) {
        this.utilisateurRepository = utilisateurRepository;
        this.fermeService = fermeService;
        this.githubClientId = githubClientId;
        this.githubClientSecret = githubClientSecret;
    }

    @GetMapping("/oauth-status")
    public ResponseEntity<?> oauthStatus() {
        // Le front s'appuie sur cet endpoint pour savoir s'il doit lancer
        // le flux OAuth GitHub ou afficher un message de configuration.
        boolean configured = isGithubOAuthConfigured();
        return ResponseEntity.ok(Map.of(
            "githubConfigured", configured,
            "authorizationUrl", "/oauth2/authorization/github"
        ));
    }

    @PostMapping("/github/farm/use")
    public ResponseEntity<?> useGithubFarm(@AuthenticationPrincipal OAuth2User principal) {
        // Connexion standard : si le compte GitHub a deja une ferme,
        // on renvoie simplement son identifiant au front.
        Utilisateur utilisateur = getAuthenticatedGithubUser(principal);

        if (utilisateur == null) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Utilisateur GitHub non authentifie"));
        }

        if (utilisateur.getFerme() == null) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Aucune ferme n'est associee a ce compte GitHub"));
        }

        return ResponseEntity.ok(Map.of(
            "farmId", utilisateur.getFerme().getIdFerme(),
            "created", false,
            "replaced", false
        ));
    }

    @PostMapping("/github/farm/new")
    public ResponseEntity<?> createGithubFarm(@AuthenticationPrincipal OAuth2User principal) {
        // Creation initiale : si une ferme existe deja, elle est remplacee.
        // Ce endpoint est surtout utile au premier login d'un compte GitHub.
        Utilisateur utilisateur = getAuthenticatedGithubUser(principal);

        if (utilisateur == null) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Utilisateur GitHub non authentifie"));
        }

        boolean replaced = utilisateur.getFerme() != null;
        Integer previousFarmId = replaced ? utilisateur.getFerme().getIdFerme() : null;

        if (replaced) {
            utilisateur.setFerme(null);
            utilisateurRepository.save(utilisateur);
            fermeService.deleteFarmWithDependencies(previousFarmId);
        }

        Ferme nouvelleFerme = new Ferme();
        nouvelleFerme.setNom(buildFarmName(utilisateur.getGithubUsername()));
        nouvelleFerme.setUtilisateur(utilisateur);

        Ferme fermeCreee = fermeService.create(nouvelleFerme);
        utilisateur.setFerme(fermeCreee);
        utilisateurRepository.save(utilisateur);

        return ResponseEntity.ok(Map.of(
            "farmId", fermeCreee.getIdFerme(),
            "created", true,
            "replaced", replaced
        ));
    }

    @PostMapping("/github/farm/reset")
    public ResponseEntity<?> resetGithubFarm(@AuthenticationPrincipal OAuth2User principal) {
        // Reset explicite depuis l'interface : on garde le meme compte GitHub
        // mais on remet sa ferme a l'etat par defaut attendu par le projet.
        Utilisateur utilisateur = getAuthenticatedGithubUser(principal);

        if (utilisateur == null) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Utilisateur GitHub non authentifie"));
        }

        Ferme fermeCreee;

        if (utilisateur.getFerme() == null) {
            Ferme nouvelleFerme = new Ferme();
            nouvelleFerme.setNom(buildFarmName(utilisateur.getGithubUsername()));
            nouvelleFerme.setUtilisateur(utilisateur);
            fermeCreee = fermeService.create(nouvelleFerme);
            utilisateur.setFerme(fermeCreee);
            utilisateurRepository.save(utilisateur);
        } else {
            fermeCreee = fermeService.resetToDefaults(utilisateur.getFerme().getIdFerme());
        }

        return ResponseEntity.ok(Map.of(
            "farmId", fermeCreee.getIdFerme(),
            "reset", true
        ));
    }

    private String buildFarmName(String username) {
        String normalized = username.replaceAll("[^A-Za-z0-9_-]", "");
        if (normalized.isBlank()) {
            normalized = "fermier";
        }

        return normalized.length() > 16 ? normalized.substring(0, 16) : normalized;
    }

    private boolean isGithubOAuthConfigured() {
        return githubClientId != null
            && githubClientSecret != null
            && !githubClientId.isBlank()
            && !githubClientSecret.isBlank()
            && !"dummy-client-id".equals(githubClientId)
            && !"dummy-client-secret".equals(githubClientSecret);
    }

    private Utilisateur getAuthenticatedGithubUser(OAuth2User principal) {
        if (principal == null) {
            return null;
        }

        String login = principal.getAttribute("login");
        if (login == null || login.isBlank()) {
            return null;
        }

        return utilisateurRepository.findByGithubUsername(login).orElse(null);
    }
}
