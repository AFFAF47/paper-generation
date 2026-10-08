package com.example.exam.papergenerator.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.bedrock.titan.BedrockTitanEmbeddingModel;
import org.springframework.ai.bedrock.titan.api.TitanEmbeddingBedrockApi;
import org.springframework.ai.bedrock.titan.api.TitanEmbeddingBedrockApi.TitanEmbeddingModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;

import java.time.Duration;

@Configuration
public class BedrockEmbeddingConfig {

    @Value("${spring.ai.bedrock.aws.region:ap-south-1}")
    private String region;

    @Bean
    public EmbeddingModel embeddingModel() {
        TitanEmbeddingBedrockApi titanEmbeddingApi = new TitanEmbeddingBedrockApi(
                TitanEmbeddingModel.TITAN_EMBED_TEXT_V2.id(),
                DefaultCredentialsProvider.create(),
                region,
                new ObjectMapper(),
                Duration.ofMinutes(2)
        );

        return new BedrockTitanEmbeddingModel(titanEmbeddingApi);
    }
}