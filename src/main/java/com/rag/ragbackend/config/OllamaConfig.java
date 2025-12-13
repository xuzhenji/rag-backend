package com.rag.ragbackend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "ollama")
public class OllamaConfig {

    /**
     * Ollama 服务地址，例如：
     * http://localhost:11434
     */
    private String baseUrl = "http://localhost:11434";

    /**
     * 默认模型，需与你本地 ollama pull 的模型一致
     * 例如：
     * deepseek-r1:1.5b
     */
    private String model = "deepseek-r1:1.5b";

    @Bean
    public OllamaConfig ollamaProperties() {
        return this;
    }
}
