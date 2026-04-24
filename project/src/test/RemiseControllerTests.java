package com.farm.tinyfarm.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:tinyfarm-remise-tests;DB_CLOSE_DELAY=-1;MODE=LEGACY",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.sql.init.mode=never",
    "tinyfarm.dev.seed-local-users=false"
})
class TestRemiseController {

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void getRemiseByFermeIdRetourne200() throws Exception {
        int fermeId = createFarm("remise");
        mockMvc.perform(get("/api/remise/{id}", fermeId))
            .andExpect(status().isOk());
    }

    @Test
    void ajouterStockAugmenteLeStockCorrespondant() throws Exception {
        int fermeId = createFarm("add");
        mockMvc.perform(patch("/api/remise/{id}/ajouter-stock", fermeId)
                .param("montant", "5")
                .param("stock", "OEUF"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.stockOeuf").value(5));
    }

    @Test
    void retirerStockDiminueLeStockCorrespondant() throws Exception {
        int fermeId = createFarm("rem");
        mockMvc.perform(patch("/api/remise/{id}/ajouter-stock", fermeId)
                .param("montant", "10")
                .param("stock", "LAIT"))
            .andExpect(status().isOk());

        mockMvc.perform(patch("/api/remise/{id}/retirer-stock", fermeId)
                .param("montant", "3")
                .param("stock", "LAIT"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.stockLait").value(7));
    }

    @Test
    void retirerStockInsuffisantRemonteLExceptionEnErreurServeur() throws Exception {
        int fermeId = createFarm("fail");
        // il n'y a pas de handler dans RemiseController donc on attend une 500
        mockMvc.perform(patch("/api/remise/{id}/retirer-stock", fermeId)
                .param("montant", "999")
                .param("stock", "OEUF"))
            .andExpect(status().is5xxServerError());
    }

    private int createFarm(String prefix) throws Exception {
        String uniqueName = prefix + "-" + UUID.randomUUID().toString().substring(0, 4);
        MvcResult result = mockMvc.perform(post("/api/fermes")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nom\":\"%s\"}".formatted(uniqueName)))
            .andExpect(status().isCreated())
            .andReturn();
        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        return body.get("idFerme").asInt();
    }
}