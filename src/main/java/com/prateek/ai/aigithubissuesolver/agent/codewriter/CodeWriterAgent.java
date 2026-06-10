package com.prateek.ai.aigithubissuesolver.agent.codewriter;

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
public class CodeWriterAgent implements BaseAgent {

    private final GeminiClient geminiClient;
    private final PromptBuilder promptBuilder;

    @Override
    public void run(AgentState state) {
        log.info("[{}] Starting... Attempt #{}", getAgentName(), state.getRetryCount() + 1);

        // Step 1 — if retry, save previous patch to history
        if (state.getPatch() != null) {
            state.savePreviousPatch(state.getPatch());
        }

        // Step 2 — reset review state before new attempt
        state.resetReviewState();

        // Step 3 — build prompt
        // on retry — prompt automatically includes previous feedback
        String prompt = promptBuilder.buildCodeWriterPrompt(state);

        // Step 4 — call Gemini
        String response = geminiClient.sendPrompt(prompt);

        // Step 5 — save new patch to state
        state.setPatch(response);

        log.info("[{}] Completed. Code patch saved to state.", getAgentName());
    }

    @Override
    public String getAgentName() {
        return "CodeWriterAgent";
    }

}