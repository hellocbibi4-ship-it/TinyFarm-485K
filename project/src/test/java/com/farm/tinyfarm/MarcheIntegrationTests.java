package com.farm.tinyfarm;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

    private int createFarm(String prefix) throws Exception {
        String uniqueName = prefix + "-" + UUID.randomUUID().toString().substring(0, 8);
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
}
