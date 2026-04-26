package com.idb.directchannels.bankAgentDemo.configuration;

import com.google.genai.Client;
import com.google.genai.types.HttpOptions;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class BankAgentDemoConfig {

    @Bean
    public ChatModel bankAgentChatModel(
            @Value("${spring.ai.google.genai.api-key}") String apiKey,
            @Value("${spring.ai.google.genai.base-url}") String baseUrl,
            @Value("${spring.ai.google.genai.chat.options.model}") String model) {
        Client genAiClient = Client.builder()
                .apiKey(apiKey)
                .httpOptions(HttpOptions.builder()
                        .baseUrl(baseUrl)
                        .apiVersion("")
                        .build())
                .build();

        return GoogleGenAiChatModel.builder()
                .genAiClient(genAiClient)
                .defaultOptions(GoogleGenAiChatOptions.builder()
                        .model(model)
                        .build())
                .build();
    }

    @Bean
    public ChatClient bankAgentChatClient(ChatModel bankAgentChatModel) {
        return ChatClient.builder(bankAgentChatModel).build();
    }

    @Bean
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }
}
