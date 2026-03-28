package com.farm.tinyfarm.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.Customizer;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;

    public SecurityConfig(CustomOAuth2UserService customOAuth2UserService) {
        this.customOAuth2UserService = customOAuth2UserService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

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
}