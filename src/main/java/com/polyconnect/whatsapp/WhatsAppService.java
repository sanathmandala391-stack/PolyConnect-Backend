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
     * Existing test method.
     *
     * This is kept because WhatsAppController.java
     * already uses it.
     */
    public JsonNode sendTextMessage(
            String phoneNumber,
            String message
    ) {

        String normalizedPhone = normalizePhoneNumber(phoneNumber);

        Map<String, Object> body = new HashMap<>();

        body.put("messaging_product", "whatsapp");
        body.put("recipient_type", "individual");
        body.put("to", normalizedPhone);
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

    /**
     * Normalize phone number for WhatsApp Cloud API.
     */
    private String normalizePhoneNumber(String phoneNumber) {

        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new IllegalArgumentException(
                    "Phone number is empty"
            );
        }

        String digits = phoneNumber.replaceAll("\\D", "");

        // Indian 10-digit number
        if (digits.length() == 10) {
            return "91" + digits;
        }

        // Indian number already containing country code
        if (digits.length() == 12 && digits.startsWith("91")) {
            return digits;
        }

        // International format beginning with 00
        if (digits.startsWith("00")) {
            return digits.substring(2);
        }

        throw new IllegalArgumentException(
                "Invalid phone number format: " + phoneNumber
        );
    }


    /**
     * Send the POLYCONNECT daily attendance template.
     *
     * Template:
     * polyconnect_daily_attendance
     *
     * Variables:
     * {{1}} = Student name
     * {{2}} = Current attendance
     * {{3}} = Change since last check
     * {{4}} = Attendance status message
     * {{5}} = Classes needed
     */
    public JsonNode sendDailyAttendanceTemplate(
            String phoneNumber,
            String studentName,
            String currentAttendance,
            String changeSinceLastCheck,
            String statusMessage,
            String classesNeeded
    ) {

        String normalizedPhone = normalizePhoneNumber(phoneNumber);

        Map<String, Object> template = new HashMap<>();

        template.put(
                "name",
                "polyconnect_daily_attendance"
        );

        template.put(
                "language",
                Map.of("code", "en")
        );

        Map<String, Object> component = new HashMap<>();

        component.put("type", "body");

        component.put(
                "parameters",
                java.util.List.of(
                        Map.of(
                                "type", "text",
                                "text", studentName
                        ),
                        Map.of(
                                "type", "text",
                                "text", currentAttendance
                        ),
                        Map.of(
                                "type", "text",
                                "text", changeSinceLastCheck
                        ),
                        Map.of(
                                "type", "text",
                                "text", statusMessage
                        ),
                        Map.of(
                                "type", "text",
                                "text", classesNeeded
                        )
                )
        );

        template.put(
                "components",
                java.util.List.of(component)
        );

        Map<String, Object> body = new HashMap<>();

        body.put("messaging_product", "whatsapp");
        body.put("to", normalizedPhone);
        body.put("type", "template");
        body.put("template", template);

        String response = restClient.post()
                .uri("/{phoneNumberId}/messages", phoneNumberId)
                .header(
                        "Authorization",
                        "Bearer " + accessToken
                )
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