package com.prateek.ai.aigithubissuesolver.agent.planner;

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
public class PlannerAgent implements BaseAgent {

    private final GeminiClient geminiClient;
    private final PromptBuilder promptBuilder;

    @Override
    public void run(AgentState state) {
        log.info("[{}] Starting...", getAgentName());

        // Step 1 — build planner prompt using issue + codeContext
        String prompt = promptBuilder.buildPlannerPrompt(state);

        // Step 2 — call Gemini
        String response = geminiClient.sendPrompt(prompt);

        // Step 3 — save plan to state
        state.setPlan(response);

        log.info("[{}] Completed. Fix plan saved to state.", getAgentName());
    }

    @Override
    public String getAgentName() {
        return "PlannerAgent";
    }

}
