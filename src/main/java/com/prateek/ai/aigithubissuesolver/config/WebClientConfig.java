package com.prateek.ai.aigithubissuesolver.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {


    @Value("${github.api.base-url}")
    private String githubBaseUrl;

    @Value("${github.api.token}")
    private String githubToken;

    @Value("${gemini.api.base-url}")
    private String geminiBaseUrl;

    // ── GitHub WebClient ─────────────────────────────
    @Bean(name = "githubWebClient")
    public WebClient githubWebClient() {
        return WebClient.builder()
                .baseUrl(githubBaseUrl)
                .defaultHeader("Authorization", "Bearer " + githubToken)
                .defaultHeader("Accept", "application/vnd.github+json")
                .defaultHeader("X-GitHub-Api-Version", "2022-11-28")
                .build();
    }

    // ── Gemini WebClient ─────────────────────────────
    @Bean(name = "geminiWebClient")
    public WebClient geminiWebClient() {
        return WebClient.builder()
                .baseUrl(geminiBaseUrl)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

}
