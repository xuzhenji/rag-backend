package com.rag.ragbackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class ChromaConfig {

    @Bean
    public WebClient chromaClient() {
        return WebClient.builder()
                .baseUrl("http://localhost:8000/api/v2")
                .build();
    }
}
