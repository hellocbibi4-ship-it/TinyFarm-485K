package com.farm.tinyfarm;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;

import com.farm.tinyfarm.model.TypeStock;
import com.farm.tinyfarm.service.RemiseService;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
class MarcheIntegrationTests {

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RemiseService remiseService;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void createOfferViaFarmEndpointReturnsUpdatedFrontData() throws Exception {
        int sellerFarmId = createFarm("seller");
        remiseService.ajouterStock(sellerFarmId, TypeStock.OEUF, 4);

        MvcResult result = mockMvc.perform(post("/api/fermes/{id}/marche/offres", sellerFarmId)
                .param("produit", "OEUF")
                .param("quantite", "3")
                .param("prix", "7"))
            .andExpect(status().isOk())
            .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        assertOffer(body, sellerFarmId, "oeuf", 3, 7);
        assertStockQuantity(body, "Oeufs", 1);
    }

    @Test
    void buyOfferViaFarmEndpointUpdatesBuyerFrontData() throws Exception {
        int sellerFarmId = createFarm("seller");
        int buyerFarmId = createFarm("buyer");
        remiseService.ajouterStock(sellerFarmId, TypeStock.OEUF, 3);

        mockMvc.perform(post("/api/fermes/{id}/marche/offres", sellerFarmId)
                .param("produit", "OEUF")
                .param("quantite", "3")
                .param("prix", "4"))
            .andExpect(status().isOk());

        int offerId = findOfferIdForSeller(sellerFarmId);

        MvcResult buyerResult = mockMvc.perform(post("/api/fermes/{id}/marche/achat", buyerFarmId)
                .param("idOffre", String.valueOf(offerId))
                .param("quantite", "2"))
            .andExpect(status().isOk())
            .andReturn();

        JsonNode buyerBody = objectMapper.readTree(buyerResult.getResponse().getContentAsString());
        assertIntegerField(buyerBody, "cash", 1492);
        assertStockQuantity(buyerBody, "Oeufs", 2);
        assertOfferQuantity(buyerBody, offerId, 1);

        MvcResult sellerResult = mockMvc.perform(get("/api/fermes/{id}/front-data", sellerFarmId))
            .andExpect(status().isOk())
            .andReturn();

        JsonNode sellerBody = objectMapper.readTree(sellerResult.getResponse().getContentAsString());
        assertIntegerField(sellerBody, "cash", 1508);
    }

    @Test
    void secondSaleOfSameProductAtSamePriceMergesIntoExistingOffer() throws Exception {
        int sellerFarmId = createFarm("seller");
        remiseService.ajouterStock(sellerFarmId, TypeStock.OEUF, 5);

        mockMvc.perform(post("/api/fermes/{id}/marche/offres", sellerFarmId)
                .param("produit", "OEUF")
                .param("quantite", "2")
                .param("prix", "6"))
            .andExpect(status().isOk());

        MvcResult secondSaleResult = mockMvc.perform(post("/api/fermes/{id}/marche/offres", sellerFarmId)
                .param("produit", "OEUF")
                .param("quantite", "3")
                .param("prix", "6"))
            .andExpect(status().isOk())
            .andReturn();

        JsonNode body = objectMapper.readTree(secondSaleResult.getResponse().getContentAsString());
        assertOffer(body, sellerFarmId, "oeuf", 5, 6);
        assertSingleOfferCount(body, sellerFarmId, "oeuf", 6);
        assertStockQuantity(body, "Oeufs", 0);
    }

    @Test
    void passDayProducesEggsAndMilkAndResetsCommunityQuotaOnlyForCurrentFarm() throws Exception {
        int activeFarmId = createFarm("active");
        int otherFarmId = createFarm("other");

        mockMvc.perform(post("/api/fermes/{id}/acheter-objet-entretien", activeFarmId)
                .param("type", "EAU"))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/fermes/{id}/acheter-animal", activeFarmId)
                .param("type", "poule"))
            .andExpect(status().isOk());

        MvcResult activeDayResult = mockMvc.perform(post("/api/fermes/{id}/passer-jour", activeFarmId))
            .andExpect(status().isOk())
            .andReturn();

        JsonNode activeBody = objectMapper.readTree(activeDayResult.getResponse().getContentAsString());
        assertIntegerField(activeBody.path("gameTime"), "day", 2);
        assertIntegerField(activeBody.path("communityPurchases"), "remaining", 12);
        assertStockQuantity(activeBody, "Oeufs", 4);
        assertStockQuantity(activeBody, "Lait", 1);

        MvcResult otherFarmResult = mockMvc.perform(get("/api/fermes/{id}/front-data", otherFarmId))
            .andExpect(status().isOk())
            .andReturn();

        JsonNode otherBody = objectMapper.readTree(otherFarmResult.getResponse().getContentAsString());
        assertIntegerField(otherBody.path("gameTime"), "day", 1);
        assertIntegerField(otherBody.path("communityPurchases"), "remaining", 12);
        assertStockQuantity(otherBody, "Oeufs", 0);
        assertStockQuantity(otherBody, "Lait", 0);
    }

    @Test
    void communityPurchasesAreBlockedAfterTwelveBuysUntilNextDay() throws Exception {
        int farmId = createFarm("quota");

        for (int i = 0; i < 12; i++) {
            mockMvc.perform(post("/api/fermes/{id}/acheter-objet-entretien", farmId)
                    .param("type", "EAU"))
                .andExpect(status().isOk());
        }

        mockMvc.perform(post("/api/fermes/{id}/acheter-objet-entretien", farmId)
                .param("type", "EAU"))
            .andExpect(status().isBadRequest());

        MvcResult nextDayResult = mockMvc.perform(post("/api/fermes/{id}/passer-jour", farmId))
            .andExpect(status().isOk())
            .andReturn();

        JsonNode nextDayBody = objectMapper.readTree(nextDayResult.getResponse().getContentAsString());
        assertIntegerField(nextDayBody.path("communityPurchases"), "remaining", 12);
    }

    @Test
    void passDayMakesAllAnimalsHungryAndThirsty() throws Exception {
        int farmId = createFarm("states");

        mockMvc.perform(post("/api/fermes/{id}/acheter-animal", farmId)
                .param("type", "poule"))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/fermes/{id}/acheter-animal", farmId)
                .param("type", "lapin"))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/fermes/{id}/passer-jour", farmId))
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/fermes/{id}/animaux/poules/status", farmId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.total").value(4))
            .andExpect(jsonPath("$.hungry").value(4))
            .andExpect(jsonPath("$.thirsty").value(4));

        mockMvc.perform(get("/api/fermes/{id}/animaux/vaches/status", farmId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.total").value(1))
            .andExpect(jsonPath("$.hungry").value(1))
            .andExpect(jsonPath("$.thirsty").value(1));

        mockMvc.perform(get("/api/fermes/{id}/animaux/lapins/status", farmId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.total").value(3))
            .andExpect(jsonPath("$.hungry").value(3))
            .andExpect(jsonPath("$.thirsty").value(3));
    }

    @Test
    void secondUncaredDayMakesAnimalsDirtyAndSick() throws Exception {
        int farmId = createFarm("decay");

        mockMvc.perform(post("/api/fermes/{id}/passer-jour", farmId))
            .andExpect(status().isOk());

        MvcResult secondDayResult = mockMvc.perform(post("/api/fermes/{id}/passer-jour", farmId))
            .andExpect(status().isOk())
            .andReturn();

        JsonNode body = objectMapper.readTree(secondDayResult.getResponse().getContentAsString());
        assertAnimalState(body, "vache", true, 0, 0, 0);
        assertAnimalState(body, "poule", true, 0, 0, 0);
        assertAnimalState(body, "lapin", true, 0, 0, 0);
    }

    @Test
    void caringActionsTreatChickensIndividuallyAndRabbitHutchAsGroup() throws Exception {
        int farmId = createFarm("care");

        mockMvc.perform(post("/api/fermes/{id}/acheter-animal", farmId)
                .param("type", "poule"))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/fermes/{id}/acheter-animal", farmId)
                .param("type", "lapin"))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/fermes/{id}/acheter-objet-entretien", farmId)
                .param("type", "NOURRITURE"))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/fermes/{id}/acheter-objet-entretien", farmId)
                .param("type", "NOURRITURE"))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/fermes/{id}/acheter-objet-entretien", farmId)
                .param("type", "EAU"))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/fermes/{id}/passer-jour", farmId))
            .andExpect(status().isOk());

        int chickenId = findAnimalIdByType(farmId, "poule");

        mockMvc.perform(post("/api/fermes/{id}/animaux/poules/feed", farmId)
                .param("animalId", String.valueOf(chickenId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.rabbitHealth.totalLapins").value(3))
            .andExpect(jsonPath("$.careInventory.feed-bag").value(1));

        mockMvc.perform(get("/api/fermes/{id}/animaux/poules/status", farmId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.hungry").value(3))
            .andExpect(jsonPath("$.thirsty").value(4));

        mockMvc.perform(post("/api/fermes/{id}/animaux/lapins/feed", farmId))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/fermes/{id}/animaux/lapins/water", farmId))
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/fermes/{id}/animaux/lapins/status", farmId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.hungry").value(0))
            .andExpect(jsonPath("$.thirsty").value(0));

        MvcResult frontDataResult = mockMvc.perform(get("/api/fermes/{id}/front-data", farmId))
            .andExpect(status().isOk())
            .andReturn();

        JsonNode frontData = objectMapper.readTree(frontDataResult.getResponse().getContentAsString());
        assertIntegerField(frontData.path("careInventory"), "feed-bag", 0);
        assertIntegerField(frontData.path("careInventory"), "water-bucket", 0);
    }

    @Test
    void careActionsConsumeTheExpectedItemsForEachAnimalType() throws Exception {
        int farmId = createFarm("stockcare");

        mockMvc.perform(post("/api/fermes/{id}/acheter-objet-entretien", farmId)
                .param("type", "NOURRITURE"))
            .andExpect(status().isOk());
        mockMvc.perform(post("/api/fermes/{id}/acheter-objet-entretien", farmId)
                .param("type", "PAILLE"))
            .andExpect(status().isOk());
        mockMvc.perform(post("/api/fermes/{id}/acheter-objet-entretien", farmId)
                .param("type", "EAU"))
            .andExpect(status().isOk());
        mockMvc.perform(post("/api/fermes/{id}/acheter-objet-entretien", farmId)
                .param("type", "SERINGUE"))
            .andExpect(status().isOk());
        mockMvc.perform(post("/api/fermes/{id}/acheter-objet-entretien", farmId)
                .param("type", "SAVON"))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/fermes/{id}/passer-jour", farmId))
            .andExpect(status().isOk());

        int chickenId = findAnimalIdByType(farmId, "poule");
        int cowId = findAnimalIdByType(farmId, "vache");

        mockMvc.perform(post("/api/fermes/{id}/animaux/poules/feed", farmId)
                .param("animalId", String.valueOf(chickenId)))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/fermes/{id}/animaux/vaches/feed", farmId)
                .param("animalId", String.valueOf(cowId)))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/fermes/{id}/animaux/vaches/water", farmId)
                .param("animalId", String.valueOf(cowId)))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/fermes/{id}/animaux/vaches/heal", farmId)
                .param("animalId", String.valueOf(cowId)))
            .andExpect(status().isOk());

        MvcResult cleanResult = mockMvc.perform(post("/api/fermes/{id}/animaux/vaches/clean", farmId)
                .param("animalId", String.valueOf(cowId)))
            .andExpect(status().isOk())
            .andReturn();

        JsonNode body = objectMapper.readTree(cleanResult.getResponse().getContentAsString());
        assertIntegerField(body.path("careInventory"), "feed-bag", 0);
        assertIntegerField(body.path("careInventory"), "straw-bales", 0);
        assertIntegerField(body.path("careInventory"), "water-bucket", 0);
        assertIntegerField(body.path("careInventory"), "syringe", 0);
        assertIntegerField(body.path("careInventory"), "soap", 0);
    }

    private int createFarm(String prefix) throws Exception {
        String normalizedPrefix = prefix.length() > 10 ? prefix.substring(0, 10) : prefix;
        String uniqueName = normalizedPrefix + "-" + UUID.randomUUID().toString().substring(0, 4);
        MvcResult result = mockMvc.perform(post("/api/fermes")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nom\":\"%s\"}".formatted(uniqueName)))
            .andExpect(status().isCreated())
            .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        return body.get("idFerme").asInt();
    }

    private int findOfferIdForSeller(int sellerFarmId) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/marche"))
            .andExpect(status().isOk())
            .andReturn();

        JsonNode offers = objectMapper.readTree(result.getResponse().getContentAsString());
        for (JsonNode offer : offers) {
            if (offer.path("sellerFarmId").asInt() == sellerFarmId) {
                return offer.path("id").asInt();
            }
        }

        throw new IllegalStateException("Offre de test introuvable");
    }

    private int findAnimalIdByType(int farmId, String type) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/fermes/{id}/front-data", farmId))
            .andExpect(status().isOk())
            .andReturn();

        JsonNode animals = objectMapper.readTree(result.getResponse().getContentAsString()).path("animals");
        for (JsonNode animal : animals) {
            if (type.equals(animal.path("type").asText())) {
                return animal.path("idAnimal").asInt();
            }
        }

        throw new IllegalStateException("Animal de type " + type + " introuvable");
    }

    private void assertOffer(JsonNode body, int sellerFarmId, String product, int quantity, int unitPrice) {
        for (JsonNode offer : body.path("marketOffers")) {
            if (offer.path("sellerFarmId").asInt() == sellerFarmId
                && product.equals(offer.path("product").asText())
                && offer.path("quantity").asInt() == quantity
                && offer.path("unitPrice").asInt() == unitPrice) {
                return;
            }
        }

        throw new AssertionError("Offre attendue introuvable dans la reponse");
    }

    private void assertOfferQuantity(JsonNode body, int offerId, int expectedQuantity) {
        for (JsonNode offer : body.path("marketOffers")) {
            if (offer.path("id").asInt() == offerId) {
                if (offer.path("quantity").asInt() != expectedQuantity) {
                    throw new AssertionError("Quantite restante inattendue pour l'offre " + offerId);
                }
                return;
            }
        }

        throw new AssertionError("Offre " + offerId + " introuvable dans la reponse");
    }

    private void assertStockQuantity(JsonNode body, String label, int expectedQuantity) {
        for (JsonNode stockRow : body.path("stockInventory")) {
            if (label.equals(stockRow.path("label").asText())) {
                if (stockRow.path("quantity").asInt() != expectedQuantity) {
                    throw new AssertionError("Quantite inattendue pour le stock " + label);
                }
                return;
            }
        }

        throw new AssertionError("Ligne de stock introuvable pour " + label);
    }

    private void assertIntegerField(JsonNode body, String fieldName, int expectedValue) {
        int actualValue = body.path(fieldName).asInt();
        if (actualValue != expectedValue) {
            throw new AssertionError("Champ " + fieldName + " attendu a " + expectedValue + " mais vaut " + actualValue);
        }
    }

    private void assertSingleOfferCount(JsonNode body, int sellerFarmId, String product, int unitPrice) {
        int count = 0;
        for (JsonNode offer : body.path("marketOffers")) {
            if (offer.path("sellerFarmId").asInt() == sellerFarmId
                && product.equals(offer.path("product").asText())
                && offer.path("unitPrice").asInt() == unitPrice) {
                count++;
            }
        }

        if (count != 1) {
            throw new AssertionError("Le nombre d'offres fusionnees attendues est 1 mais vaut " + count);
        }
    }

    private void assertAnimalState(
        JsonNode body,
        String type,
        boolean expectedSick,
        int expectedCleanliness,
        int expectedHunger,
        int expectedHydration
    ) {
        for (JsonNode animal : body.path("animals")) {
            if (type.equals(animal.path("type").asText())) {
                if (animal.path("isSick").asBoolean() != expectedSick) {
                    throw new AssertionError("Etat de sante inattendu pour " + type);
                }
                if (animal.path("cleanliness").asInt() != expectedCleanliness) {
                    throw new AssertionError("Proprete inattendue pour " + type);
                }
                if (animal.path("hunger").asInt() != expectedHunger) {
                    throw new AssertionError("Faim inattendue pour " + type);
                }
                if (animal.path("hydration").asInt() != expectedHydration) {
                    throw new AssertionError("Hydratation inattendue pour " + type);
                }
                return;
            }
        }

        throw new AssertionError("Animal de type " + type + " introuvable");
    }
}
