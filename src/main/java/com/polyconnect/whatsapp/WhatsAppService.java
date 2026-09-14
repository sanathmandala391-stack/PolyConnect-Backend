package com.polyconnect.whatsapp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.Map;

@Service
public class WhatsAppService {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Value("${polyconnect.whatsapp.access-token}")
    private String accessToken;

    @Value("${polyconnect.whatsapp.phone-number-id}")
    private String phoneNumberId;

    public WhatsAppService(
            RestClient whatsappRestClient,
            ObjectMapper objectMapper
    ) {
        this.restClient = whatsappRestClient;
        this.objectMapper = objectMapper;
    }

    /**
     * Send a normal WhatsApp text message.
     *
     * Example:
     * sendTextMessage("919876543210", "Hello from POLYCONNECT!");
     */
    public JsonNode sendTextMessage(String phoneNumber, String message) {

        Map<String, Object> body = new HashMap<>();

        body.put("messaging_product", "whatsapp");
        body.put("recipient_type", "individual");
        body.put("to", phoneNumber);
        body.put("type", "text");

        Map<String, Object> text = new HashMap<>();
        text.put("preview_url", false);
        text.put("body", message);

        body.put("text", text);

        String response = restClient.post()
                .uri("/{phoneNumberId}/messages", phoneNumberId)
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(String.class);

        try {
            return objectMapper.readTree(response);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to parse WhatsApp API response",
                    e
            );
        }
    }
}
