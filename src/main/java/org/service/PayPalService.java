package org.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Base64;

@ApplicationScoped
public class PayPalService {

    @ConfigProperty(name = "paypal.client.id")
    String clientId;

    @ConfigProperty(name = "paypal.client.secret")
    String clientSecret;

    @ConfigProperty(name = "paypal.api.base.url")
    String paypalBaseUrl;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String getAccessToken() {
        Client client = ClientBuilder.newClient();

        String auth = clientId + ":" + clientSecret;
        String authHeader = "Basic " + Base64.getEncoder().encodeToString(auth.getBytes());


        Response response = client.target(paypalBaseUrl + "/v1/oauth2/token")
                .request(MediaType.APPLICATION_JSON)  // Set Accept header to application/json
                .header(HttpHeaders.AUTHORIZATION, authHeader)
                .post(Entity.form(
                        new jakarta.ws.rs.core.Form()
                                .param("grant_type", "client_credentials")));

        if (response.getStatus() != 200) {
            throw new RuntimeException("Failed to get PayPal access token. Status: " + response.getStatus());
        }

        String responseBody = response.readEntity(String.class);
        try {
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            return jsonNode.get("access_token").asText();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse PayPal token response", e);
        }
    }

    public String createOrder(String episodeId, Integer episodeNumber, String price) {
        Client client = ClientBuilder.newClient();
        String accessToken = getAccessToken();

        try {
            ObjectNode orderRequest = objectMapper.createObjectNode();
            orderRequest.put("intent", "CAPTURE");

            ArrayNode purchaseUnits = objectMapper.createArrayNode();
            ObjectNode purchaseUnit = objectMapper.createObjectNode();

            purchaseUnit.put("reference_id", "episode-" + episodeId);
            purchaseUnit.put("description", "Monogatari Episode " + episodeNumber);
            purchaseUnit.put("custom_id", "episode-" + episodeId);

            ObjectNode amount = objectMapper.createObjectNode();
            amount.put("currency_code", "USD");
            amount.put("value", price);

            purchaseUnit.set("amount", amount);
            purchaseUnits.add(purchaseUnit);

            orderRequest.set("purchase_units", purchaseUnits);

            ObjectNode applicationContext = objectMapper.createObjectNode();
            applicationContext.put("brand_name", "Monogatari");
            applicationContext.put("landing_page", "NO_PREFERENCE");
            applicationContext.put("user_action", "PAY_NOW");
            applicationContext.put("return_url", "https://tu-sitio.com/success");
            applicationContext.put("cancel_url", "https://tu-sitio.com/cancel");

            orderRequest.set("application_context", applicationContext);

            Response response = client.target(paypalBaseUrl + "/v2/checkout/orders")
                    .request(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .post(Entity.json(orderRequest.toString()));

            if (response.getStatus() != 201) {
                throw new RuntimeException("Failed to create PayPal order. Status: " + response.getStatus());
            }

            String responseBody = response.readEntity(String.class);
            JsonNode jsonNode = objectMapper.readTree(responseBody);

            return jsonNode.get("id").asText();

        } catch (Exception e) {
            throw new RuntimeException("Error creating PayPal order", e);
        }
    }

    public JsonNode captureOrder(String orderId) {
        Client client = ClientBuilder.newClient();
        String accessToken = getAccessToken();

        try {
            Response response = client.target(paypalBaseUrl + "/v2/checkout/orders/" + orderId + "/capture")
                    .request(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .post(Entity.json("{}"));

            if (response.getStatus() != 201) {
                throw new RuntimeException("Failed to capture PayPal order. Status: " + response.getStatus());
            }

            String responseBody = response.readEntity(String.class);
            return objectMapper.readTree(responseBody);

        } catch (Exception e) {
            throw new RuntimeException("Error capturing PayPal order", e);
        }
    }
}