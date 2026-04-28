package com.farm.tinyfarm.security;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.farm.tinyfarm.model.Utilisateur;
import com.farm.tinyfarm.repository.UtilisateurRepository;

@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UtilisateurRepository utilisateurRepository;

    public CustomOAuth2UserService(UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
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
            utilisateurRepository.save(nouvelUtilisateur);
        } else {
            Utilisateur userExistant = userOpt.get();
            userExistant.setRole(role);
            utilisateurRepository.save(userExistant);
        }

        return new CustomOAuth2User(oAuth2User, authorities);
    }
}
