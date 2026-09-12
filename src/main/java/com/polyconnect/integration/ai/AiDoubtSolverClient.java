package com.polyconnect.integration.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class AiDoubtSolverClient {

    private final RestClient restClient;
    private final String apiKey;
    private final String modelName;
    private final ObjectMapper objectMapper;

    public AiDoubtSolverClient(
            @Value("${polyconnect.gemini.api-key:}") String apiKey,
            @Value("${polyconnect.gemini.model:gemini-3.6-flash}") String modelName
    ) {
        String key = (apiKey != null && !apiKey.isBlank()) ? apiKey : System.getenv("GEMINI_API_KEY");
        this.apiKey = key != null ? key.trim() : "";
        this.modelName = (modelName != null && !modelName.isBlank()) ? modelName : "gemini-3.6-flash";
        this.restClient = RestClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com/v1beta")
                .defaultHeader("User-Agent", "PolyConnect-SBTET-AI")
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public String solveDoubt(String subjectCode, String subjectName, String topic, String questionText, String base64Image) {
        String keyToUse = this.apiKey;
        if (keyToUse == null || keyToUse.isBlank()) {
            keyToUse = System.getenv("GEMINI_API_KEY");
        }
        if (keyToUse == null || keyToUse.isBlank()) {
            return generateFallbackResponse(questionText);
        }

        try {
            String systemPrompt = """
                You are the official State Board of Technical Education and Training (SBTET) Telangana AI Academic Mentor on PolyConnect.
                Provide a clear, accurate, step-by-step solution for diploma/polytechnic engineering students.
                Include principles, formulas, step-by-step working out with units, code snippets if applicable, and SBTET board exam tips in clean Markdown.
                """;

            String promptText = String.format("""
                %s

                Subject: %s (%s)
                Topic: %s

                Student Question:
                %s
                """,
                systemPrompt,
                subjectName != null ? subjectName : "Diploma Subject",
                subjectCode != null ? subjectCode : "DIPLOMA",
                topic != null ? topic : "Academic Doubt",
                questionText
            );

            List<Map<String, Object>> parts = new ArrayList<>();

            // Attach image/PDF if provided
            if (base64Image != null && !base64Image.isBlank() && base64Image.startsWith("data:")) {
                try {
                    String cleanBase64 = base64Image.contains(",") ? base64Image.split(",")[1] : base64Image;
                    String mimeType = base64Image.startsWith("data:application/pdf") ? "application/pdf" : "image/jpeg";

                    Map<String, Object> inlineData = new HashMap<>();
                    inlineData.put("mimeType", mimeType);
                    inlineData.put("data", cleanBase64);

                    Map<String, Object> imagePart = new HashMap<>();
                    imagePart.put("inlineData", inlineData);
                    parts.add(imagePart);
                } catch (Exception ignore) {}
            }

            Map<String, Object> textPart = new HashMap<>();
            textPart.put("text", promptText);
            parts.add(textPart);

            Map<String, Object> contents = new HashMap<>();
            contents.put("parts", parts);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("contents", List.of(contents));

            String[] candidateModels = new String[]{modelName, "gemini-3.6-flash", "gemini-flash-latest", "gemini-3.5-flash-lite"};

            for (String model : candidateModels) {
                try {
                    String responseBody = restClient.post()
                            .uri(uriBuilder -> uriBuilder
                                    .path("/models/" + model + ":generateContent")
                                    .queryParam("key", keyToUse)
                                    .build())
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(requestBody)
                            .retrieve()
                            .body(String.class);

                    if (responseBody != null) {
                        JsonNode root = objectMapper.readTree(responseBody);
                        JsonNode candidate = root.path("candidates").get(0);
                        String generatedText = candidate.path("content").path("parts").get(0).path("text").asText();
                        if (generatedText != null && !generatedText.isBlank()) {
                            return generatedText;
                        }
                    }
                } catch (Exception ex) {
                    System.err.println("Model " + model + " failed, trying next: " + ex.getMessage());
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return generateFallbackResponse(questionText);
    }

    private String generateFallbackResponse(String question) {
        return "### SBTET AI Academic Mentor\n\n" +
               "Here is guidance for your academic query: **" + question + "**\n\n" +
               "1. **Core Concept:** Please verify the formula and step-by-step principles for this diploma topic.\n" +
               "2. **Board Exam Tip:** Ensure you draw neat diagrams and write all unit conversions clearly in your SBTET examination papers.";
    }
}
