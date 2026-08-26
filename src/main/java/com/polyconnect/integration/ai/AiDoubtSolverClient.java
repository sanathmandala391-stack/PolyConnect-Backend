//package com.polyconnect.integration.ai;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.MediaType;
//import org.springframework.stereotype.Component;
//import org.springframework.web.client.RestClient;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//@Component
//public class AiDoubtSolverClient {
//
//    private final RestClient restClient;
//    private final String apiKey;
//    private final String modelName;
//    private final ObjectMapper objectMapper;
//
//    public AiDoubtSolverClient(
//        @Value("${polyconnect.gemini.api-key:}") String apiKey,
//        @Value("${polyconnect.gemini.model:gemini-3.7-flash}") String modelName
//    ) {
//        this.apiKey = apiKey;
//        this.modelName = modelName;
//        this.restClient = RestClient.builder()
//            .baseUrl("https://generativelanguage.googleapis.com/v1beta")
//            .defaultHeader("User-Agent", "aistudio-build")
//            .build();
//        this.objectMapper = new ObjectMapper();
//    }
//
//    public String solveDoubt(String subjectCode, String subjectName, String topic, String questionText, String base64Image) {
//        if (apiKey == null || apiKey.isBlank()) {
//            return generateFallbackDiplomaExplanation(subjectCode, subjectName, topic, questionText);
//        }
//
//        try {
//            String prompt = String.format("""
//                You are a senior SBTET Telangana Polytechnic Subject Expert and Professor for Diploma Engineering.
//                Subject Code: %s
//                Subject Name: %s
//                Topic: %s
//
//                Student Question:
//                %s
//
//                Provide a structured, syllabus-oriented solution following this exact layout:
//                1. 🎯 Direct Concept & Summary
//                2. 📝 Step-by-Step Derivation / Explanation / Code / Calculation
//                3. 🔑 Key Formulas, Truth Tables, or Pin Diagrams (if applicable)
//                4. 💡 Exam Tip & Common Pitfalls to Avoid in SBTET Board Exams
//                """, subjectCode, subjectName, topic, questionText);
//
//            Map<String, Object> contentPart = new HashMap<>();
//            contentPart.put("text", prompt);
//
//            Map<String, Object> contents = new HashMap<>();
//            contents.put("parts", List.of(contentPart));
//
//            Map<String, Object> requestBody = new HashMap<>();
//            requestBody.put("contents", List.of(contents));
//
//            String responseBody = restClient.post()
//                .uri(uriBuilder -> uriBuilder
//                    .path("/models/" + modelName + ":generateContent")
//                    .queryParam("key", apiKey)
//                    .build())
//                .contentType(MediaType.APPLICATION_JSON)
//                .body(requestBody)
//                .retrieve()
//                .body(String.class);
//
//            JsonNode root = objectMapper.readTree(responseBody);
//            JsonNode candidate = root.path("candidates").get(0);
//            return candidate.path("content").path("parts").get(0).path("text").asText();
//        } catch (Exception ex) {
//            return generateFallbackDiplomaExplanation(subjectCode, subjectName, topic, questionText);
//        }
//    }
//
//    private String generateFallbackDiplomaExplanation(String subjectCode, String subjectName, String topic, String question) {
//        return "### 🎯 Concept Summary for " + subjectName + " (" + subjectCode + ")\n" +
//               "**Topic:** " + topic + "\n\n" +
//               "### 📝 Step-by-Step Explanation\n" +
//               "1. **Fundamental Principle:** The query addresses " + topic + " as prescribed in the SBTET Diploma curriculum.\n" +
//               "2. **Detailed Breakdown:**\n" +
//               "   - Given question: *" + question + "*\n" +
//               "   - Core concept analysis confirms standard textbook procedure and circuit/algorithm logic.\n" +
//               "3. **Key Formula / Rule:**\n" +
//               "   - Ensure proper units and step-wise notations for board exam credit.\n\n" +
//               "### 💡 SBTET Board Exam Tips\n" +
//               "- Draw neat, labeled diagrams with pencils where applicable.\n" +
//               "- State all assumptions clearly in Part-B long-answer questions (worth 8/10 marks).";
//    }
//}

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

//    public AiDoubtSolverClient(
//            @Value("${polyconnect.gemini.api-key:}") String apiKey,
//            @Value("${polyconnect.gemini.model:gemini-2.5-flash}") String modelName
//    ) {
//        this.apiKey = apiKey;
//        this.modelName = modelName;
//        this.restClient = RestClient.builder()
//                .baseUrl("https://generativelanguage.googleapis.com/v1beta")
//                .defaultHeader("User-Agent", "aistudio-build")
//                .build();
//        this.objectMapper = new ObjectMapper();
//    }
public AiDoubtSolverClient(
        @Value("${polyconnect.gemini.api-key:}") String apiKey,
        @Value("${polyconnect.gemini.model:gemini-3.6-flash}") String modelName
) {
    this.apiKey = apiKey;
    this.modelName = modelName;
    this.restClient = RestClient.builder()
            .baseUrl("https://generativelanguage.googleapis.com/v1beta")
            .defaultHeader("User-Agent", "aistudio-build")
            .build();
    this.objectMapper = new ObjectMapper();
}

    public String solveDoubt(String subjectCode, String subjectName, String topic, String questionText, String base64Image) {
        if (apiKey == null || apiKey.isBlank()) {
            return generateFallbackResponse(questionText);
        }

        try {
            // Conversational prompt matching ChatGPT-style responses
            String promptText = String.format("""
                You are a helpful AI tutor for Polytechnic and Diploma Engineering students (%s - %s).
                Answer the student's question directly, clearly, and concisely in standard markdown format (like ChatGPT).
                Do NOT format your response into rigid exam templates unless the user explicitly asks for an exam study layout.

                Question: %s
                """,
                    subjectCode != null ? subjectCode : "General",
                    subjectName != null ? subjectName : "Subject",
                    questionText
            );

            List<Map<String, Object>> parts = new ArrayList<>();

            // Add image / file data if present
            if (base64Image != null && !base64Image.isBlank()) {
                String cleanBase64 = base64Image.contains(",") ? base64Image.split(",")[1] : base64Image;
                String mimeType = base64Image.startsWith("data:application/pdf") ? "application/pdf" : "image/jpeg";

                Map<String, Object> inlineData = new HashMap<>();
                inlineData.put("mime_type", mimeType);
                inlineData.put("data", cleanBase64);

                Map<String, Object> imagePart = new HashMap<>();
                imagePart.put("inline_data", inlineData);
                parts.add(imagePart);
            }

            Map<String, Object> textPart = new HashMap<>();
            textPart.put("text", promptText);
            parts.add(textPart);

            Map<String, Object> contents = new HashMap<>();
            contents.put("parts", parts);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("contents", List.of(contents));

            String responseBody = restClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/models/" + modelName + ":generateContent")
                            .queryParam("key", apiKey)
                            .build())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode candidate = root.path("candidates").get(0);
            return candidate.path("content").path("parts").get(0).path("text").asText();
        } catch (Exception ex) {
            ex.printStackTrace();
            return generateFallbackResponse(questionText);
        }
    }

    private String generateFallbackResponse(String question) {
        return "I'm having trouble connecting to the AI service right now. Please try again in a moment.\n\n" +
                "**Your Query:** " + question;
    }
}
