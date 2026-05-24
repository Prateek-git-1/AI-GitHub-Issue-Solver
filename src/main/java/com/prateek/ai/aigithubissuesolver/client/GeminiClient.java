package com.prateek.ai.aigithubissuesolver.client;

import com.prateek.ai.aigithubissuesolver.exception.AgentException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class GeminiClient {

    private final WebClient geminiWebClient;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${gemini.api.model}")
    private String geminiModel;

    public GeminiClient(
            @Qualifier("geminiWebClient")
            WebClient geminiWebClient
    ) {
        this.geminiWebClient = geminiWebClient;
    }

    // ─────────────────────────────────────────────
    // Send Prompt to Gemini
    // ─────────────────────────────────────────────
    public String sendPrompt(String prompt) {

        log.info("[GeminiClient] Sending prompt to Gemini model: {}", geminiModel);

        log.debug("[GeminiClient] Prompt preview: {}",
                prompt.length() > 200
                        ? prompt.substring(0, 200) + "..."
                        : prompt
        );

        // Gemini request body
        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of(
                                "parts", List.of(
                                        Map.of("text", prompt)
                                )
                        )
                ),
                "generationConfig", Map.of(
                        "temperature", 0.7,
                        "maxOutputTokens", 8192
                )
        );

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = geminiWebClient
                    .post()
                    .uri(
                            "/v1beta/models/{model}:generateContent?key={key}",
                            geminiModel,
                            geminiApiKey
                    )
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            log.debug("[GeminiClient] Raw Gemini response: {}", response);

            return extractTextFromResponse(response);

        } catch (WebClientResponseException e) {

            log.error(
                    "[GeminiClient] Gemini API error: {} {}",
                    e.getStatusCode(),
                    e.getResponseBodyAsString(),
                    e
            );

            throw new AgentException(
                    "Gemini API call failed. Status: "
                            + e.getStatusCode()
                            + " | Response: "
                            + e.getResponseBodyAsString(),
                    e
            );

        } catch (Exception e) {

            log.error("[GeminiClient] Unexpected error while calling Gemini", e);

            throw new AgentException(
                    "Unexpected error while calling Gemini API",
                    e
            );
        }
    }

    // ─────────────────────────────────────────────
    // Extract Gemini Response Text
    // ─────────────────────────────────────────────
    @SuppressWarnings("unchecked")
    private String extractTextFromResponse(
            Map<String, Object> response
    ) {

        if (response == null) {
            throw new AgentException("Gemini returned null response");
        }

        try {

            // Check if Gemini blocked the response
            if (response.containsKey("promptFeedback")) {

                Map<String, Object> feedback =
                        (Map<String, Object>) response.get("promptFeedback");

                log.warn("[GeminiClient] Prompt feedback received: {}", feedback);

                Object blockReason = feedback.get("blockReason");

                if (blockReason != null) {
                    throw new AgentException(
                            "Gemini blocked response. Reason: " + blockReason
                    );
                }
            }

            // Extract candidates
            List<Map<String, Object>> candidates =
                    (List<Map<String, Object>>) response.get("candidates");

            if (candidates == null || candidates.isEmpty()) {
                throw new AgentException(
                        "Gemini response has no candidates"
                );
            }

            // Extract content
            Map<String, Object> candidate = candidates.getFirst();

            Map<String, Object> content =
                    (Map<String, Object>) candidate.get("content");

            if (content == null) {
                throw new AgentException(
                        "Gemini response content is null"
                );
            }

            // Extract parts
            List<Map<String, Object>> parts =
                    (List<Map<String, Object>>) content.get("parts");

            if (parts == null || parts.isEmpty()) {
                throw new AgentException(
                        "Gemini response parts are empty"
                );
            }

            // Extract text
            String text = (String) parts.getFirst().get("text");

            if (text == null || text.isBlank()) {
                throw new AgentException(
                        "Gemini returned empty text"
                );
            }

            log.info("[GeminiClient] Successfully received response from Gemini");

            log.debug("[GeminiClient] Response preview: {}",
                    text.length() > 200
                            ? text.substring(0, 200) + "..."
                            : text
            );

            return text;

        } catch (ClassCastException e) {

            log.error(
                    "[GeminiClient] Failed to parse Gemini response structure",
                    e
            );

            throw new AgentException(
                    "Failed to parse Gemini response structure",
                    e
            );

        } catch (NullPointerException e) {

            log.error(
                    "[GeminiClient] Null value found while parsing Gemini response",
                    e
            );

            throw new AgentException(
                    "Invalid Gemini response structure",
                    e
            );
        }
    }
}