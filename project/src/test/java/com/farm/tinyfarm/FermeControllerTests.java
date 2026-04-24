package com.farm.tinyfarm.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
    "spring.datasource.url=jdbc:h2:mem:tinyfarm-fermectrl-tests;DB_CLOSE_DELAY=-1;MODE=LEGACY",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.sql.init.mode=never",
    "tinyfarm.dev.seed-local-users=false"
})
class TestFermeController {

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    // ---------- création / suppression ----------

    @Test
    void creerFermeAvecNomValideRetourne201() throws Exception {
        String nom = "abc-" + UUID.randomUUID().toString().substring(0, 4);
        mockMvc.perform(post("/api/fermes")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nom\":\"%s\"}".formatted(nom)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.idFerme").exists())
            .andExpect(jsonPath("$.soldeEcus").value(1500))
            .andExpect(jsonPath("$.jourActuel").value(1));
    }

    @Test
    void creerFermeAvecNomInvalideRetourne400() throws Exception {
        mockMvc.perform(post("/api/fermes")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nom\":\"x\"}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void supprimerFermeRetourne204() throws Exception {
        int id = createFarm("del");
        mockMvc.perform(delete("/api/fermes/{id}", id))
            .andExpect(status().isNoContent());
    }

    // ---------- front-data ----------

    @Test
    void getFrontDataContientTousLesChampsAttendus() throws Exception {
        int id = createFarm("front");
        mockMvc.perform(get("/api/fermes/{id}/front-data", id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.farmId").value(id))
            .andExpect(jsonPath("$.cash").value(1500))
            .andExpect(jsonPath("$.animals").isArray())
            .andExpect(jsonPath("$.stockInventory").isArray())
            .andExpect(jsonPath("$.careInventory.feed-bag").value(0))
            .andExpect(jsonPath("$.gameTime.day").value(1))
            .andExpect(jsonPath("$.communityPurchases.remaining").value(12))
            .andExpect(jsonPath("$.communityPurchases.maxPerDay").value(12));
    }

    // ---------- score / écus ----------

    @Test
    void augmenterScoreMetAJourLeScore() throws Exception {
        int id = createFarm("sc");
        mockMvc.perform(patch("/api/fermes/{id}/score", id).param("montant", "50"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.score").value(50));
    }

    @Test
    void ajouterEcusMetAJourLeSolde() throws Exception {
        int id = createFarm("ec");
        mockMvc.perform(patch("/api/fermes/{id}/ajout-ecus", id).param("montant", "100"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.soldeEcus").value(1600));
    }

    @Test
    void retirerEcusMetAJourLeSolde() throws Exception {
        int id = createFarm("rt");
        mockMvc.perform(patch("/api/fermes/{id}/retirer-ecus", id).param("montant", "200"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.soldeEcus").value(1300));
    }

    // ---------- hibernation ----------

    @Test
    void hibernationRetourneLEtat() throws Exception {
        int id = createFarm("hib");
        mockMvc.perform(patch("/api/fermes/{id}/hibernation", id).param("etat", "true"))
            .andExpect(status().isOk());
    }

    // ---------- classement ----------

    @Test
    void getClassementRetourneListe() throws Exception {
        mockMvc.perform(get("/api/fermes/classement"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.ranking").isArray());
    }

    @Test
    void updateClassementRetourneListe() throws Exception {
        mockMvc.perform(get("/api/fermes/classement/update"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.ranking").isArray());
    }

    // ---------- statuts des animaux ----------

    @Test
    void statusPoulesParDefautRetourne4() throws Exception {
        int id = createFarm("sp");
        mockMvc.perform(get("/api/fermes/{id}/animaux/poules/status", id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.total").value(4));
    }

    @Test
    void statusVachesParDefautRetourne1() throws Exception {
        int id = createFarm("sv");
        mockMvc.perform(get("/api/fermes/{id}/animaux/vaches/status", id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.total").value(1));
    }

    @Test
    void statusLapinsParDefautRetourne8() throws Exception {
        int id = createFarm("sl");
        mockMvc.perform(get("/api/fermes/{id}/animaux/lapins/status", id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.total").value(8));
    }

    @Test
    void clapierStatusRetourneLesCompteurs() throws Exception {
        int id = createFarm("cl");
        mockMvc.perform(get("/api/fermes/{id}/animaux/clapier", id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalLapins").value(8))
            .andExpect(jsonPath("$.sickLapins").value(0));
    }

    // ---------- acheter animal ----------

    @Test
    void acheterPouleIncremente() throws Exception {
        int id = createFarm("apo");
        mockMvc.perform(post("/api/fermes/{id}/acheter-animal", id).param("type", "poule"))
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/fermes/{id}/animaux/poules/status", id))
            .andExpect(jsonPath("$.total").value(5));
    }

    @Test
    void acheterAnimalTypeInconnuRetourne400() throws Exception {
        int id = createFarm("ai");
        mockMvc.perform(post("/api/fermes/{id}/acheter-animal", id).param("type", "dragon"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void nourrirVacheDeuxFoisDansLaMemeJourneeRetourne400() throws Exception {
        int id = createFarm("vf");
        int cowId = getSingleAnimalId(id, "vache");

        mockMvc.perform(post("/api/fermes/{id}/acheter-objet-entretien", id).param("type", "PAILLE"))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/fermes/{id}/animaux/vache/feed", id).param("animalId", String.valueOf(cowId)))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/fermes/{id}/acheter-objet-entretien", id).param("type", "PAILLE"))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/fermes/{id}/animaux/vache/feed", id).param("animalId", String.valueOf(cowId)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void abreuverVacheDeuxFoisDansLaMemeJourneeRetourne400() throws Exception {
        int id = createFarm("vw");
        int cowId = getSingleAnimalId(id, "vache");

        mockMvc.perform(post("/api/fermes/{id}/acheter-objet-entretien", id).param("type", "EAU"))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/fermes/{id}/animaux/vache/water", id).param("animalId", String.valueOf(cowId)))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/fermes/{id}/acheter-objet-entretien", id).param("type", "EAU"))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/fermes/{id}/animaux/vache/water", id).param("animalId", String.valueOf(cowId)))
            .andExpect(status().isBadRequest());
    }

    // ---------- utilitaires ----------

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

    private int getSingleAnimalId(int farmId, String type) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/fermes/{id}/front-data", farmId))
            .andExpect(status().isOk())
            .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        for (JsonNode animal : body.get("animals")) {
            if (type.equals(animal.get("type").asText())) {
                return animal.get("idAnimal").asInt();
            }
        }

        throw new IllegalStateException("Animal introuvable pour le type " + type);
    }
}
