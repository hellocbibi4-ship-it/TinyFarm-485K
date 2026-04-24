package com.farm.tinyfarm.security;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.farm.tinyfarm.model.Ferme;
import com.farm.tinyfarm.model.Remise;
import com.farm.tinyfarm.model.Utilisateur;
import com.farm.tinyfarm.repository.RemiseRepository;
import com.farm.tinyfarm.model.Utilisateur;
import com.farm.tinyfarm.repository.UtilisateurRepository;

@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UtilisateurRepository utilisateurRepository;
    private final RemiseRepository remiseRepository;

    public CustomOAuth2UserService(UtilisateurRepository utilisateurRepository, RemiseRepository remiseRepository) {
        this.utilisateurRepository = utilisateurRepository;
        this.remiseRepository = remiseRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        String userLogin = oAuth2User.getAttribute("login");
        List<GrantedAuthority> authorities = new ArrayList<>(oAuth2User.getAuthorities());
        String role = "ROLE_USER";

        Optional<Utilisateur> userOpt = utilisateurRepository.findByGithubUsername(userLogin);

        if (userOpt.isEmpty()) {
            Utilisateur nouvelUtilisateur = new Utilisateur();
            nouvelUtilisateur.setGithubUsername(userLogin);
            nouvelUtilisateur.setRole(role);

            Ferme nouvelleFerme = new Ferme();
            nouvelleFerme.setNom("La ferme de " + userLogin);
            nouvelleFerme.setSoldeEcus(1500);
            nouvelleFerme.setDateCreation(LocalDateTime.now());
            nouvelleFerme.setDerniereCo(LocalDateTime.now());
            nouvelleFerme.setHibernation(false);
            nouvelleFerme.setScore(0);
            nouvelleFerme.setAchatsJour(0);

            // On fait le lien dans les deux sens pour que JPA s'y retrouve
            nouvelleFerme.setUtilisateur(nouvelUtilisateur);
            nouvelUtilisateur.setFerme(nouvelleFerme);

            // On sauvegarde. Grâce au CascadeType.ALL dans Utilisateur, la ferme sera sauvegardée en même temps
            utilisateurRepository.save(nouvelUtilisateur);

            // Création de la remise (stockage) liée à la ferme
            Ferme savedFerme = nouvelUtilisateur.getFerme();
            Remise remise = new Remise();
            remise.setFerme(savedFerme);
            remiseRepository.save(remise);
        } else {
            Utilisateur userExistant = userOpt.get();
            userExistant.setRole(role);
            utilisateurRepository.save(userExistant);
        }

        return new CustomOAuth2User(oAuth2User, authorities);
    }
}
