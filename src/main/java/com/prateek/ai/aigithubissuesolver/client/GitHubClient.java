package com.prateek.ai.aigithubissuesolver.client;

import com.prateek.ai.aigithubissuesolver.exception.AgentException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class GitHubClient {

    private final WebClient githubWebClient;

    public GitHubClient(
            @Qualifier("githubWebClient")
            WebClient githubWebClient
    ) {
        this.githubWebClient = githubWebClient;
    }

    // ─────────────────────────────────────────────
    // Fetch GitHub Issue
    // ─────────────────────────────────────────────
    @SuppressWarnings("unchecked")
    public Map<String, Object> fetchIssue(
            String owner,
            String repo,
            int issueNumber
    ) {

        log.info(
                "[GitHubClient] Fetching issue #{} from {}/{}",
                issueNumber,
                owner,
                repo
        );

        try {

            Map<String, Object> response =
                    (Map<String, Object>) githubWebClient
                            .get()
                            .uri(
                                    "/repos/{owner}/{repo}/issues/{issueNumber}",
                                    owner,
                                    repo,
                                    issueNumber
                            )
                            .retrieve()
                            .bodyToMono(Map.class)
                            .block();

            if (response == null) {
                throw new AgentException(
                        "GitHub API returned null for issue #" + issueNumber
                );
            }

            log.info(
                    "[GitHubClient] Successfully fetched issue: {}",
                    response.get("title")
            );

            return response;

        } catch (WebClientResponseException e) {

            log.error(
                    "[GitHubClient] GitHub API error: {} {}",
                    e.getStatusCode(),
                    e.getResponseBodyAsString(),
                    e
            );

            throw new AgentException(
                    "Failed to fetch GitHub issue. Status: "
                            + e.getStatusCode()
                            + " | Response: "
                            + e.getResponseBodyAsString(),
                    e
            );

        } catch (Exception e) {

            log.error(
                    "[GitHubClient] Unexpected error while fetching issue",
                    e
            );

            throw new AgentException(
                    "Unexpected error while fetching GitHub issue",
                    e
            );
        }
    }

    // ─────────────────────────────────────────────
    // Fetch GitHub Issue Comments
    // ─────────────────────────────────────────────
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> fetchIssueComments(
            String owner,
            String repo,
            int issueNumber
    ) {

        log.info(
                "[GitHubClient] Fetching comments for issue #{}",
                issueNumber
        );

        try {

            List<Map<String, Object>> comments =
                    (List<Map<String, Object>>) (List<?>) githubWebClient
                            .get()
                            .uri(
                                    "/repos/{owner}/{repo}/issues/{issueNumber}/comments",
                                    owner,
                                    repo,
                                    issueNumber
                            )
                            .retrieve()
                            .bodyToFlux(Map.class)
                            .collectList()
                            .block();

            if (comments == null) {
                return List.of();
            }

            log.info(
                    "[GitHubClient] Successfully fetched {} comments",
                    comments.size()
            );

            return comments;

        } catch (WebClientResponseException e) {

            log.error(
                    "[GitHubClient] GitHub comments API error: {} {}",
                    e.getStatusCode(),
                    e.getResponseBodyAsString(),
                    e
            );

            return List.of();

        } catch (Exception e) {

            log.error(
                    "[GitHubClient] Unexpected error while fetching comments",
                    e
            );

            return List.of();
        }
    }
}