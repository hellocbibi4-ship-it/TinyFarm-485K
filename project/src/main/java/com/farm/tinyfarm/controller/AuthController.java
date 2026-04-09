package com.farm.tinyfarm.controller;

import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.farm.tinyfarm.model.Utilisateur;
import com.farm.tinyfarm.repository.UtilisateurRepository;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UtilisateurRepository utilisateurRepository;

    public AuthController(UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }

    @PostMapping("/login-local")
    public ResponseEntity<?> loginLocal(@RequestBody Map<String, String> payload) {
        String username = Optional.ofNullable(payload.get("username")).orElse("").trim();
        String password = Optional.ofNullable(payload.get("password")).orElse("").trim();

        if (username.isBlank() || password.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "erreur de username ou de password"));
        }

        Optional<Utilisateur> utilisateur = utilisateurRepository.findByGithubUsernameAndPassword(username, password);

        if (utilisateur.isEmpty() || utilisateur.get().getFerme() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "erreur de username ou de password"));
        }

        return ResponseEntity.ok(Map.of(
            "farmId", utilisateur.get().getFerme().getIdFerme()
        ));
    }

    @PostMapping("/login-git")
    public ResponseEntity<?> loginGit(@RequestBody Map<String, String> payload) {
        String username = Optional.ofNullable(payload.get("login")).orElse("").trim();
       

        if (username.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "erreur de username "));
        }

        Optional<Utilisateur> utilisateur = utilisateurRepository.findByGithubUsername(username);

        if (utilisateur.isEmpty() || utilisateur.get().getFerme() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "erreur de username "));
        }

        return ResponseEntity.ok(Map.of(
            "farmId", utilisateur.get().getFerme().getIdFerme()
        ));
    }
}
