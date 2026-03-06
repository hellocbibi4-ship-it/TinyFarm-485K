package com.example.authgithub;


import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;

// Pourquoi cette classe est utile ?
// ✅ Stocke ROLE_ADMIN dans l'utilisateur OAuth2.
// ✅ Permet de vérifier les rôles avec @PreAuthorize("hasRole('ADMIN')").


public class CustomOAuth2User extends DefaultOAuth2User {

    public CustomOAuth2User(OAuth2User delegate, Collection<? extends GrantedAuthority> authorities) {
        super(authorities, delegate.getAttributes(), "login"); // GitHub utilise "login" comme identifiant
    }
}
