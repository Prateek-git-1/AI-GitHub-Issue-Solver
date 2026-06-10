package com.prateek.ai.aigithubissuesolver.agent.codereader;

import com.prateek.ai.aigithubissuesolver.agent.BaseAgent;
import com.prateek.ai.aigithubissuesolver.client.GeminiClient;
import com.prateek.ai.aigithubissuesolver.client.GitHubClient;
import com.prateek.ai.aigithubissuesolver.prompt.PromptBuilder;
import com.prateek.ai.aigithubissuesolver.state.AgentState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class CodeReaderAgent implements BaseAgent {

    private final GeminiClient geminiClient;
    private final GitHubClient gitHubClient;
    private final PromptBuilder promptBuilder;

    @Override
    public void run(AgentState state) {
        log.info("[{}] Starting...", getAgentName());

        // Step 1 — fetch issue from GitHub
        Map<String, Object> issue = gitHubClient.fetchIssue(
                state.getRepoOwner(),
                state.getRepoName(),
                state.getIssueNumber()
        );

        // Step 2 — extract title and body from response
        String title = (String) issue.getOrDefault("title", "No title");
        String body  = (String) issue.getOrDefault("body", "No description");

        state.setIssueTitle(title);
        state.setIssueBody(body);

        // Step 3 — fetch comments for extra context
        List<Map<String, Object>> comments = gitHubClient.fetchIssueComments(
                state.getRepoOwner(),
                state.getRepoName(),
                state.getIssueNumber()
        );

        // Step 4 — append comments to issue body
        if (!comments.isEmpty()) {
            StringBuilder commentsText = new StringBuilder("\n\nCOMMENTS:\n");
            for (Map<String, Object> comment : comments) {
                String commentBody = (String) comment.getOrDefault("body", "");
                if (!commentBody.isBlank()) {
                    commentsText.append("- ").append(commentBody).append("\n");
                }
            }
            state.setIssueBody(body + commentsText);
        }

        // Step 5 — build prompt and call Gemini
        String prompt   = promptBuilder.buildCodeReaderPrompt(state);
        String response = geminiClient.sendPrompt(prompt);

        // Step 6 — save result to state
        state.setCodeContext(response);

        log.info("[{}] Completed. Code context saved to state.", getAgentName());
    }

    @Override
    public String getAgentName() {
        return "CodeReaderAgent";
    }

}
