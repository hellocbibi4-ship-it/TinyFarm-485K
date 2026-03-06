package com.example.authgithub.service;


import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.stereotype.Service;

import com.example.authgithub.CustomOAuth2User;

import java.util.*;

@Service // ✅ Indique que c'est un service Spring
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private static final Set<String> ADMIN_USERS = Set.of("momo54", "skaf54"); // ✅ Liste des admins autorisés

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        // ✅ Utilise le service par défaut de Spring pour charger l'utilisateur
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        // 🔹 Récupère le login de l'utilisateur (GitHub : "login", Google : "email")
        String userLogin = oAuth2User.getAttribute("login");  // Pour GitHub

        // 🔥 Ajoute ROLE_ADMIN si l'utilisateur est dans la liste des admins
        List<GrantedAuthority> authorities = new ArrayList<>(oAuth2User.getAuthorities());
        if (ADMIN_USERS.contains(userLogin)) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }

        return new CustomOAuth2User(oAuth2User, authorities);
    }
}
