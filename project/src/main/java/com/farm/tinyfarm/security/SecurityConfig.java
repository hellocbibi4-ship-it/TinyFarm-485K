package com.farm.tinyfarm.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.PkceParameterNames;
import org.springframework.security.web.SecurityFilterChain;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequest;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;

    public SecurityConfig(CustomOAuth2UserService customOAuth2UserService) {
        this.customOAuth2UserService = customOAuth2UserService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
            ClientRegistrationRepository clientRegistrationRepository) throws Exception {

        http
                //.cors(Customizer.withDefaults()) // Active CORS 
                /*.authorizeHttpRequests(authorize -> authorize
                        // 🔹 Ce qui est public (accueil, assets statiques, console de BDD)
                        .requestMatchers("/", "/index.html", "/static/**").permitAll()
                        .requestMatchers("/h2-console/**").permitAll() 
                        // 🔹 Ce qui nécessite le rôle ADMIN
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        // 🔹 Tout le reste nécessite d'être connecté (joueur normal)
                        .anyRequest().authenticated()
                )*/
               .authorizeHttpRequests(authorize -> authorize
                        //ajouté "/assets/**", "/css/**", "/js/**", "/data/**" pour laisser passer le design et les scripts
                        .requestMatchers("/", "/index.html", "/static/**", "/assets/**", "/css/**", "/js/**", "/data/**").permitAll()
                        .requestMatchers("/h2-console/**").permitAll() 
                        .requestMatchers("/api/**").permitAll() 
                        // 🔹 Tout le reste nécessite d'être connecté
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(authorization -> authorization
                                .authorizationRequestResolver(
                                        githubAuthorizationRequestResolver(clientRegistrationRepository)))
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                        .defaultSuccessUrl("/", true) 
                )
                .exceptionHandling(exceptions -> exceptions
                    .authenticationEntryPoint((request, response, authException) -> {
                        if (request.getRequestURI().startsWith("/api/")) {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"error\":\"Not authenticated\"}");
                        } else {
                            response.sendRedirect("/oauth2/authorization/github");
                        }
                    })
                    .accessDeniedHandler((request, response, accessDeniedException) -> {
                        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
                    })
                )
                .csrf(AbstractHttpConfigurer::disable)
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable));

        return http.build();
    }

    private OAuth2AuthorizationRequestResolver githubAuthorizationRequestResolver(
            ClientRegistrationRepository clientRegistrationRepository) {
        DefaultOAuth2AuthorizationRequestResolver delegate =
                new DefaultOAuth2AuthorizationRequestResolver(clientRegistrationRepository);

        return new OAuth2AuthorizationRequestResolver() {
            @Override
            public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
                return stripPkceForGitHub(request, delegate.resolve(request));
            }

            @Override
            public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
                OAuth2AuthorizationRequest authorizationRequest = delegate.resolve(request, clientRegistrationId);
                if (!"github".equals(clientRegistrationId)) {
                    return authorizationRequest;
                }
                return stripPkce(authorizationRequest);
            }
        };
    }

    private OAuth2AuthorizationRequest stripPkceForGitHub(HttpServletRequest request,
            OAuth2AuthorizationRequest authorizationRequest) {
        if (authorizationRequest == null || !request.getRequestURI().endsWith("/github")) {
            return authorizationRequest;
        }
        return stripPkce(authorizationRequest);
    }

    private OAuth2AuthorizationRequest stripPkce(OAuth2AuthorizationRequest authorizationRequest) {
        return OAuth2AuthorizationRequest.from(authorizationRequest)
                .additionalParameters(parameters -> {
                    parameters.remove(PkceParameterNames.CODE_CHALLENGE);
                    parameters.remove(PkceParameterNames.CODE_CHALLENGE_METHOD);
                })
                .attributes(attributes -> attributes.remove(PkceParameterNames.CODE_VERIFIER))
                .build();
    }
}
