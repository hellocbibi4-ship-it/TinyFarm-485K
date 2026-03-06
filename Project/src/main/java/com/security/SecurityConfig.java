package com.example.authgithub;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import com.example.authgithub.controller.TestAuthController;
import com.example.authgithub.service.CustomOAuth2UserService;
import org.springframework.security.config.Customizer;

import java.util.Optional;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    CustomOAuth2UserService customOAuth2UserService;
    @Autowired
    private ClientRegistrationRepository clientRegistrationRepository;

    public SecurityConfig(CustomOAuth2UserService customOAuth2UserService) {
        this.customOAuth2UserService = customOAuth2UserService;
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withSecretKey(TestAuthController.SECRET_KEY).build(); // ✅ Utilise la clé directement
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthoritiesClaimName("authorities"); // 🔥 Lire les rôles depuis "authorities"
        grantedAuthoritiesConverter.setAuthorityPrefix(""); // 🔥 Supprime le préfixe "ROLE_" si déjà présent

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        String baseUrl = Optional.ofNullable(System.getenv("BASE_URL"))
                .orElse("http://localhost:8080");

        System.out.println("🔹 BASE_URL utilisé : " + baseUrl);

        http
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/", "/counter/**", "/index.html", "/index-github.html", "/auth/test-token", "/static/**").permitAll()
 //                       .requestMatchers("/", "/counter/**", "/index.html", "/index-github.html", "/static/**").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                        .defaultSuccessUrl("/index.html", true))
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(
                        jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                                    "Unauthorized: Please authenticate");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                                    "Forbidden: You do not have access to this resource");
                        }))
                .csrf(AbstractHttpConfigurer::disable) // Désactive CSRF pour éviter les erreurs
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable) 
                //.csrf(csrf -> csrf
                //.ignoringRequestMatchers("/logout", "/counter/increment") // CSRF ne bloque
                // pas POST /logout
                );
        return http.build();
    }

}
