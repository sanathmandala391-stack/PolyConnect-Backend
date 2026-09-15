package com.polyconnect.telegram;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.Map;

@Service
public class TelegramService {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Value("${polyconnect.telegram.bot-token}")
    private String botToken;

    public TelegramService(
            RestClient telegramRestClient,
            ObjectMapper objectMapper
    ) {
        this.restClient = telegramRestClient;
        this.objectMapper = objectMapper;
    }

    public JsonNode sendMessage(
            String chatId,
            String message
    ) {

        if (chatId == null || chatId.isBlank()) {
            throw new IllegalArgumentException(
                    "Telegram chat ID is empty"
            );
        }

        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException(
                    "Telegram message is empty"
            );
        }

        Map<String, Object> body = new HashMap<>();

        body.put("chat_id", chatId);
        body.put("text", message);

        String response = restClient.post()
                .uri("/bot" + botToken + "/sendMessage")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(String.class);

        try {
            return objectMapper.readTree(response);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to parse Telegram API response",
                    e
            );
        }
    }
}