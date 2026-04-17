package com.farm.tinyfarm.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:tinyfarm-auth-tests;DB_CLOSE_DELAY=-1;MODE=LEGACY",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.sql.init.mode=never",
    "tinyfarm.dev.seed-local-users=false"
})
class TestAuthAndUserController {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    // ---------- UserController ----------

    @Test
    void getMeSansAuthentificationRetourne401() throws Exception {
        mockMvc.perform(get("/api/me"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error").value("Not authenticated"));
    }

    @Test
    void logoutUrlRetourneUrl() throws Exception {
        mockMvc.perform(get("/api/logout-url"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.url").value("/logout"));
    }

    // ---------- AuthController ----------

    @Test
    void useGithubFarmSansAuthentificationRetourne401() throws Exception {
        mockMvc.perform(post("/api/auth/github/farm/use"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void createGithubFarmSansAuthentificationRetourne401() throws Exception {
        mockMvc.perform(post("/api/auth/github/farm/new"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void resetGithubFarmSansAuthentificationRetourne401() throws Exception {
        mockMvc.perform(post("/api/auth/github/farm/reset"))
            .andExpect(status().isUnauthorized());
    }
}