package com.prateek.ai.aigithubissuesolver.agent.propener;

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
public class PrOpenerAgent implements BaseAgent {

    private final GeminiClient geminiClient;
    private final PromptBuilder promptBuilder;

    @Override
    public void run(AgentState state) {
        log.info("[{}] Starting...", getAgentName());

        // Step 1 — build PR opener prompt using full state
        String prompt = promptBuilder.buildPrOpenerPrompt(state);

        // Step 2 — call Gemini
        String response = geminiClient.sendPrompt(prompt);

        // Step 3 — save PR description to state
        state.setPrDescription(response);

        log.info("[{}] Completed. PR description saved to state.", getAgentName());
    }

    @Override
    public String getAgentName() {
        return "PrOpenerAgent";
    }

}