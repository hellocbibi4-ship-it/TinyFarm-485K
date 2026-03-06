package com.farm.tinyfarm.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.farm.tinyfarm.model.Utilisateur;
import com.farm.tinyfarm.model.Ferme;
import com.farm.tinyfarm.repository.UtilisateurRepository;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private static final Set<String> ADMIN_USERS = Set.of("momo54", "skaf54");
    private final UtilisateurRepository utilisateurRepository;

    public CustomOAuth2UserService(UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        // Récupère le login GitHub
        String userLogin = oAuth2User.getAttribute("login");

        // Gestion des rôles
        List<GrantedAuthority> authorities = new ArrayList<>(oAuth2User.getAuthorities());
        String role = "ROLE_USER"; // Rôle par défaut
        if (ADMIN_USERS.contains(userLogin)) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            role = "ROLE_ADMIN";
        }

        // On cherche le joueur dans la base
        Optional<Utilisateur> userOpt = utilisateurRepository.findByGithubUsername(userLogin);

        if (userOpt.isEmpty()) {
            // Si c'est un nouveau Joueur
            System.out.println("Nouveau fermier détecté : " + userLogin + ". Création de sa ferme !");

            Utilisateur nouvelUtilisateur = new Utilisateur();
            nouvelUtilisateur.setGithubUsername(userLogin);
            nouvelUtilisateur.setRole(role);

            Ferme nouvelleFerme = new Ferme();
            nouvelleFerme.setNom("La ferme de " + userLogin);
            nouvelleFerme.setSoldeEcus(100); // Le pécule de départ (à toi de choisir le montant !)
            nouvelleFerme.setDateCreation(LocalDateTime.now());
            nouvelleFerme.setDerniereCo(LocalDateTime.now());
            nouvelleFerme.setHibernation(false);
            nouvelleFerme.setScore(0);

            // On fait le lien dans les deux sens pour que JPA s'y retrouve
            nouvelleFerme.setUtilisateur(nouvelUtilisateur);
            nouvelUtilisateur.setFerme(nouvelleFerme);

            // On sauvegarde. Grâce au CascadeType.ALL dans Utilisateur, la Ferme sera sauvegardée en même temps !
            utilisateurRepository.save(nouvelUtilisateur);
        } else {
            // 🧑‍🌾 Le joueur existe déjà, on met juste à jour sa date de dernière connexion
            System.out.println("Bon retour à la ferme, " + userLogin + " !");
            Utilisateur userExistant = userOpt.get();
            if (userExistant.getFerme() != null) {
                userExistant.getFerme().setDerniereCo(LocalDateTime.now());
                utilisateurRepository.save(userExistant);
            }
        }

        return new CustomOAuth2User(oAuth2User, authorities);
    }
}