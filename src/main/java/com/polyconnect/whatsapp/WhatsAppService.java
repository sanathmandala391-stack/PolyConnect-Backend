package com.polyconnect.whatsapp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.List;
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
     * Send an approved WhatsApp template.
     *
     * Currently using:
     * hello_world
     */
    public JsonNode sendHelloWorldTemplate(String phoneNumber) {

        String normalizedPhone = normalizePhoneNumber(phoneNumber);

        Map<String, Object> template = new HashMap<>();

        template.put("name", "hello_world");

        template.put(
                "language",
                Map.of("code", "en_US")
        );

        Map<String, Object> body = new HashMap<>();

        body.put("messaging_product", "whatsapp");
        body.put("to", normalizedPhone);
        body.put("type", "template");
        body.put("template", template);

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

    /**
     * Convert common Indian phone number formats
     * into WhatsApp Cloud API format.
     *
     * Example:
     * 9876543210      -> 919876543210
     * +919876543210   -> 919876543210
     * 919876543210    -> 919876543210
     */
    private String normalizePhoneNumber(String phoneNumber) {

        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new IllegalArgumentException(
                    "Phone number is empty"
            );
        }

        String digits = phoneNumber.replaceAll("\\D", "");

        if (digits.length() == 10) {
            return "91" + digits;
        }

        if (digits.length() == 12 && digits.startsWith("91")) {
            return digits;
        }

        if (digits.startsWith("00")) {
            return digits.substring(2);
        }

        throw new IllegalArgumentException(
                "Invalid phone number format: " + phoneNumber
        );
    }
}