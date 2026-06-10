package com.prateek.ai.aigithubissuesolver.agent.review;

import com.prateek.ai.aigithubissuesolver.agent.BaseAgent;
import com.prateek.ai.aigithubissuesolver.client.GeminiClient;
import com.prateek.ai.aigithubissuesolver.prompt.PromptBuilder;
import com.prateek.ai.aigithubissuesolver.state.AgentState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewAgent implements BaseAgent {

    private final GeminiClient geminiClient;
    private final PromptBuilder promptBuilder;

    @Override
    public void run(AgentState state) {
        log.info("[{}] Starting review of patch...", getAgentName());

        // Step 1 — build review prompt
        String prompt = promptBuilder.buildReviewPrompt(state);

        // Step 2 — call Gemini
        String response = geminiClient.sendPrompt(prompt);

        // Step 3 — save full review feedback to state
        state.setReviewFeedback(response);

        // Step 4 — parse Gemini response to check approval
        // Gemini responds with "APPROVED: YES" or "APPROVED: NO"
        boolean approved = parseApproval(response);
        state.setFixApproved(approved);

        if (approved) {
            log.info("[{}] ✅ Fix APPROVED!", getAgentName());
        } else {
            log.warn("[{}] ❌ Fix REJECTED. Feedback: {}", getAgentName(),
                    response.length() > 200 ? response.substring(0, 200) + "..." : response);
        }
    }

    // ── Parse "APPROVED: YES/NO" from Gemini response ──
    private boolean parseApproval(String response) {
        if (response == null || response.isBlank()) {
            return false;
        }
        // look for "APPROVED: YES" anywhere in response
        return response.toUpperCase().contains("APPROVED: YES");
    }

    @Override
    public String getAgentName() {
        return "ReviewAgent";
    }

}