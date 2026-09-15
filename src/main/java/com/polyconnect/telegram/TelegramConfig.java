package com.polyconnect.telegram;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class TelegramConfig {

    @Value("${polyconnect.telegram.api-url}")
    private String apiUrl;

    @Bean
    public RestClient telegramRestClient() {
        return RestClient.builder()
                .baseUrl(apiUrl)
                .build();
    }
}