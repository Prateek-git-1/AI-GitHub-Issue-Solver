package com.prateek.ai.aigithubissuesolver.agent.testwriter;

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
public class TestWriterAgent implements BaseAgent {

    private final GeminiClient geminiClient;
    private final PromptBuilder promptBuilder;

    @Override
    public void run(AgentState state) {
        log.info("[{}] Starting...", getAgentName());

        // Step 1 — build test writer prompt using patch
        String prompt = promptBuilder.buildTestWriterPrompt(state);

        // Step 2 — call Gemini
        String response = geminiClient.sendPrompt(prompt);

        // Step 3 — save tests to state
        state.setTests(response);

        log.info("[{}] Completed. Tests saved to state.", getAgentName());
    }

    @Override
    public String getAgentName() {
        return "TestWriterAgent";
    }

}