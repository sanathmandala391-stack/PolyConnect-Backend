//package com.polyconnect.whatsapp;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.MediaType;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestClient;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//@Service
//public class WhatsAppService {
//
//    private final RestClient restClient;
//    private final ObjectMapper objectMapper;
//
//    @Value("${polyconnect.whatsapp.access-token}")
//    private String accessToken;
//
//    @Value("${polyconnect.whatsapp.phone-number-id}")
//    private String phoneNumberId;
//
//    public WhatsAppService(
//            RestClient whatsappRestClient,
//            ObjectMapper objectMapper
//    ) {
//        this.restClient = whatsappRestClient;
//        this.objectMapper = objectMapper;
//    }
//
//    /**
//     * Send the Meta hello_world test template.
//     *
//     * NOTE:
//     * hello_world works only with Meta's public test numbers.
//     * It is NOT used by the production daily job.
//     */
//    public JsonNode sendHelloWorldTemplate(String phoneNumber) {
//
//        String normalizedPhone = normalizePhoneNumber(phoneNumber);
//
//        Map<String, Object> template = new HashMap<>();
//
//        template.put("name", "hello_world");
//
//        template.put(
//                "language",
//                Map.of("code", "en_US")
//        );
//
//        Map<String, Object> body = new HashMap<>();
//
//        body.put("messaging_product", "whatsapp");
//        body.put("to", normalizedPhone);
//        body.put("type", "template");
//        body.put("template", template);
//
//        String response = restClient.post()
//                .uri("/{phoneNumberId}/messages", phoneNumberId)
//                .header(
//                        "Authorization",
//                        "Bearer " + accessToken
//                )
//                .contentType(MediaType.APPLICATION_JSON)
//                .body(body)
//                .retrieve()
//                .body(String.class);
//
//        return parseResponse(response);
//    }
//
//    /**
//     * Send a normal WhatsApp text message.
//     *
//     * Kept because WhatsAppController uses this method.
//     */
//    public JsonNode sendTextMessage(
//            String phoneNumber,
//            String message
//    ) {
//
//        String normalizedPhone = normalizePhoneNumber(phoneNumber);
//
//        Map<String, Object> body = new HashMap<>();
//
//        body.put("messaging_product", "whatsapp");
//        body.put("recipient_type", "individual");
//        body.put("to", normalizedPhone);
//        body.put("type", "text");
//
//        Map<String, Object> text = new HashMap<>();
//
//        text.put("preview_url", false);
//        text.put("body", message);
//
//        body.put("text", text);
//
//        String response = restClient.post()
//                .uri("/{phoneNumberId}/messages", phoneNumberId)
//                .header(
//                        "Authorization",
//                        "Bearer " + accessToken
//                )
//                .contentType(MediaType.APPLICATION_JSON)
//                .body(body)
//                .retrieve()
//                .body(String.class);
//
//        return parseResponse(response);
//    }
//
//    /**
//     * Send POLYCONNECT daily attendance template.
//     *
//     * Template name:
//     * polyconnect_daily_attendance
//     *
//     * Template language:
//     * English (US) -> en_US
//     *
//     * Variables:
//     *
//     * {{1}} = Student name
//     * {{2}} = Current attendance
//     * {{3}} = Change since last check
//     * {{4}} = Attendance status message
//     * {{5}} = Classes needed
//     */
//    public JsonNode sendDailyAttendanceTemplate(
//            String phoneNumber,
//            String studentName,
//            String currentAttendance,
//            String changeSinceLastCheck,
//            String statusMessage,
//            String classesNeeded
//    ) {
//
//        String normalizedPhone =
//                normalizePhoneNumber(phoneNumber);
//
//        /*
//         * Create template parameters.
//         */
//        List<Map<String, Object>> parameters = List.of(
//
//                Map.of(
//                        "type", "text",
//                        "text", studentName
//                ),
//
//                Map.of(
//                        "type", "text",
//                        "text", currentAttendance
//                ),
//
//                Map.of(
//                        "type", "text",
//                        "text", changeSinceLastCheck
//                ),
//
//                Map.of(
//                        "type", "text",
//                        "text", statusMessage
//                ),
//
//                Map.of(
//                        "type", "text",
//                        "text", classesNeeded
//                )
//        );
//
//        /*
//         * Create BODY component.
//         */
//        Map<String, Object> bodyComponent =
//                new HashMap<>();
//
//        bodyComponent.put(
//                "type",
//                "body"
//        );
//
//        bodyComponent.put(
//                "parameters",
//                parameters
//        );
//
//        /*
//         * Create template.
//         */
//        Map<String, Object> template =
//                new HashMap<>();
//
//        template.put(
//                "name",
//                "polyconnect_daily_attendance"
//        );
//
//        /*
//         * IMPORTANT:
//         * Your Meta template is English (US).
//         */
//        template.put(
//                "language",
//                Map.of("code", "en_US")
//        );
//
//        template.put(
//                "components",
//                List.of(bodyComponent)
//        );
//
//        /*
//         * Create WhatsApp request body.
//         */
//        Map<String, Object> requestBody =
//                new HashMap<>();
//
//        requestBody.put(
//                "messaging_product",
//                "whatsapp"
//        );
//
//        requestBody.put(
//                "to",
//                normalizedPhone
//        );
//
//        requestBody.put(
//                "type",
//                "template"
//        );
//
//        requestBody.put(
//                "template",
//                template
//        );
//
//        /*
//         * Send request to Meta Graph API.
//         */
//        String response = restClient.post()
//                .uri(
//                        "/{phoneNumberId}/messages",
//                        phoneNumberId
//                )
//                .header(
//                        "Authorization",
//                        "Bearer " + accessToken
//                )
//                .contentType(
//                        MediaType.APPLICATION_JSON
//                )
//                .body(requestBody)
//                .retrieve()
//                .body(String.class);
//
//        return parseResponse(response);
//    }
//
//    /**
//     * Parse Meta API response.
//     */
//    private JsonNode parseResponse(String response) {
//
//        try {
//
//            return objectMapper.readTree(response);
//
//        } catch (Exception e) {
//
//            throw new RuntimeException(
//                    "Failed to parse WhatsApp API response",
//                    e
//            );
//        }
//    }
//
//    /**
//     * Normalize phone number for WhatsApp Cloud API.
//     */
//    private String normalizePhoneNumber(
//            String phoneNumber
//    ) {
//
//        if (
//                phoneNumber == null
//                        || phoneNumber.isBlank()
//        ) {
//
//            throw new IllegalArgumentException(
//                    "Phone number is empty"
//            );
//        }
//
//        String digits =
//                phoneNumber.replaceAll("\\D", "");
//
//        /*
//         * Indian 10-digit number.
//         * Example:
//         * 9876543210
//         *
//         * becomes:
//         * 919876543210
//         */
//        if (digits.length() == 10) {
//
//            return "91" + digits;
//        }
//
//        /*
//         * Indian number already containing
//         * country code.
//         */
//        if (
//                digits.length() == 12
//                        && digits.startsWith("91")
//        ) {
//
//            return digits;
//        }
//
//        /*
//         * International number beginning with 00.
//         */
//        if (digits.startsWith("00")) {
//
//            return digits.substring(2);
//        }
//
//        throw new IllegalArgumentException(
//                "Invalid phone number format: "
//                        + phoneNumber
//        );
//    }
//}



























































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
     * Send Meta's hello_world test template.
     *
     * NOTE:
     * This template is normally associated with Meta's test setup
     * and uses the en_US language code.
     */
    public JsonNode sendHelloWorldTemplate(String phoneNumber) {

        String normalizedPhone = normalizePhoneNumber(phoneNumber);

        Map<String, Object> template = new HashMap<>();

        template.put(
                "name",
                "hello_world"
        );

        template.put(
                "language",
                Map.of("code", "en_US")
        );

        Map<String, Object> requestBody = new HashMap<>();

        requestBody.put(
                "messaging_product",
                "whatsapp"
        );

        requestBody.put(
                "to",
                normalizedPhone
        );

        requestBody.put(
                "type",
                "template"
        );

        requestBody.put(
                "template",
                template
        );

        String response = restClient.post()
                .uri(
                        "/{phoneNumberId}/messages",
                        phoneNumberId
                )
                .header(
                        "Authorization",
                        "Bearer " + accessToken
                )
                .contentType(
                        MediaType.APPLICATION_JSON
                )
                .body(requestBody)
                .retrieve()
                .body(String.class);

        return parseResponse(response);
    }

    /**
     * Send a normal WhatsApp text message.
     *
     * IMPORTANT:
     * Business-initiated messages outside the 24-hour customer
     * service window generally need an approved template.
     */
    public JsonNode sendTextMessage(
            String phoneNumber,
            String message
    ) {

        String normalizedPhone =
                normalizePhoneNumber(phoneNumber);

        Map<String, Object> requestBody =
                new HashMap<>();

        requestBody.put(
                "messaging_product",
                "whatsapp"
        );

        requestBody.put(
                "recipient_type",
                "individual"
        );

        requestBody.put(
                "to",
                normalizedPhone
        );

        requestBody.put(
                "type",
                "text"
        );

        Map<String, Object> text =
                new HashMap<>();

        text.put(
                "preview_url",
                false
        );

        text.put(
                "body",
                message
        );

        requestBody.put(
                "text",
                text
        );

        String response = restClient.post()
                .uri(
                        "/{phoneNumberId}/messages",
                        phoneNumberId
                )
                .header(
                        "Authorization",
                        "Bearer " + accessToken
                )
                .contentType(
                        MediaType.APPLICATION_JSON
                )
                .body(requestBody)
                .retrieve()
                .body(String.class);

        return parseResponse(response);
    }

    /**
     * Send POLYCONNECT daily attendance template.
     *
     * Template name:
     * polyconnect_daily_attendance
     *
     * Template language in Meta:
     * English
     *
     * Language code:
     * en
     *
     * Variables:
     *
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

        String normalizedPhone =
                normalizePhoneNumber(phoneNumber);

        /*
         * Create template parameters.
         */
        List<Map<String, Object>> parameters = List.of(

                Map.of(
                        "type",
                        "text",
                        "text",
                        studentName
                ),

                Map.of(
                        "type",
                        "text",
                        "text",
                        currentAttendance
                ),

                Map.of(
                        "type",
                        "text",
                        "text",
                        changeSinceLastCheck
                ),

                Map.of(
                        "type",
                        "text",
                        "text",
                        statusMessage
                ),

                Map.of(
                        "type",
                        "text",
                        "text",
                        classesNeeded
                )
        );

        /*
         * Create BODY component.
         */
        Map<String, Object> bodyComponent =
                new HashMap<>();

        bodyComponent.put(
                "type",
                "body"
        );

        bodyComponent.put(
                "parameters",
                parameters
        );

        /*
         * Create template object.
         *
         * IMPORTANT:
         * Meta shows your template language as "English",
         * not "English (US)".
         *
         * Therefore:
         * en
         *
         * instead of:
         * en_US
         */
        Map<String, Object> template =
                new HashMap<>();

        template.put(
                "name",
                "polyconnect_daily_attendance"
        );

        template.put(
                "language",
                Map.of("code", "en")
        );

        template.put(
                "components",
                List.of(bodyComponent)
        );

        /*
         * Create WhatsApp request body.
         */
        Map<String, Object> requestBody =
                new HashMap<>();

        requestBody.put(
                "messaging_product",
                "whatsapp"
        );

        requestBody.put(
                "to",
                normalizedPhone
        );

        requestBody.put(
                "type",
                "template"
        );

        requestBody.put(
                "template",
                template
        );

        /*
         * Send request to Meta Graph API.
         */
        String response = restClient.post()
                .uri(
                        "/{phoneNumberId}/messages",
                        phoneNumberId
                )
                .header(
                        "Authorization",
                        "Bearer " + accessToken
                )
                .contentType(
                        MediaType.APPLICATION_JSON
                )
                .body(requestBody)
                .retrieve()
                .body(String.class);

        return parseResponse(response);
    }

    /**
     * Parse Meta API response.
     */
    private JsonNode parseResponse(String response) {

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
     *
     * Examples:
     *
     * 9876543210
     *      -> 919876543210
     *
     * +919876543210
     *      -> 919876543210
     *
     * 919876543210
     *      -> 919876543210
     *
     * 00919876543210
     *      -> 919876543210
     */
    private String normalizePhoneNumber(
            String phoneNumber
    ) {

        if (
                phoneNumber == null
                        || phoneNumber.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "Phone number is empty"
            );
        }

        String digits =
                phoneNumber.replaceAll("\\D", "");

        /*
         * Indian 10-digit number.
         */
        if (digits.length() == 10) {

            return "91" + digits;
        }

        /*
         * Indian number already containing
         * country code.
         */
        if (
                digits.length() == 12
                        && digits.startsWith("91")
        ) {

            return digits;
        }

        /*
         * International number beginning with 00.
         */
        if (digits.startsWith("00")) {

            String internationalNumber =
                    digits.substring(2);

            if (!internationalNumber.isBlank()) {
                return internationalNumber;
            }
        }

        throw new IllegalArgumentException(
                "Invalid phone number format: "
                        + phoneNumber
        );
    }
}