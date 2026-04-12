package com.farm.tinyfarm.controller;

import com.farm.tinyfarm.model.Utilisateur;
import com.farm.tinyfarm.repository.UtilisateurRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class UserController {

    private final UtilisateurRepository utilisateurRepository;

    public UserController(UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }

    /**
     * Appelé par le JS au chargement de la page.
     * - Si l'utilisateur est connecté  → 200 + ses infos
     * - Si l'utilisateur n'est pas connecté → 401
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal OAuth2User principal) {

        // principal == null  ⟹  pas de session active
        if (principal == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }

        String login = principal.getAttribute("login");

        // On récupère la ferme depuis la BDD pour renvoyer le solde, etc.
        Optional<Utilisateur> userOpt = utilisateurRepository.findByGithubUsername(login);

        Map<String, Object> response = new HashMap<>();
        response.put("login", login);
        response.put("name",       principal.getAttribute("name"));
        response.put("avatar_url", principal.getAttribute("avatar_url"));

        userOpt.ifPresent(u -> {
            response.put("role", u.getRole());
            response.put("hasFarm", u.getFerme() != null);
            if (u.getFerme() != null) {
                response.put("farmId",   u.getFerme().getIdFerme());
                response.put("farmName", u.getFerme().getNom());
                response.put("solde",    u.getFerme().getSoldeEcus());
                response.put("score",    u.getFerme().getScore());
            }
        });

        return ResponseEntity.ok(response);
    }

    /**
     * Déconnexion propre (optionnel mais pratique).
     * Appel depuis le JS : fetch('/api/logout', { method: 'POST' })
     */
    @GetMapping("/logout-url")
    public ResponseEntity<?> logoutUrl() {
        return ResponseEntity.ok(Map.of("url", "/logout"));
    }
}
