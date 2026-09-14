package com.polyconnect.whatsapp;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class WhatsAppConfig {

    @Value("${polyconnect.whatsapp.graph-api-url}")
    private String graphApiUrl;

    @Bean
    public RestClient whatsappRestClient() {
        return RestClient.builder()
                .baseUrl(graphApiUrl)
                .build();
    }
}