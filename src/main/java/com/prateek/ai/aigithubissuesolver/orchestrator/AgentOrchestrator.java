package com.prateek.ai.aigithubissuesolver.orchestrator;

import com.prateek.ai.aigithubissuesolver.agent.codereader.CodeReaderAgent;
import com.prateek.ai.aigithubissuesolver.agent.codewriter.CodeWriterAgent;
import com.prateek.ai.aigithubissuesolver.agent.planner.PlannerAgent;
import com.prateek.ai.aigithubissuesolver.agent.propener.PrOpenerAgent;
import com.prateek.ai.aigithubissuesolver.agent.review.ReviewAgent;
import com.prateek.ai.aigithubissuesolver.agent.testwriter.TestWriterAgent;
import com.prateek.ai.aigithubissuesolver.dto.response.IssueResponse;
import com.prateek.ai.aigithubissuesolver.exception.AgentException;
import com.prateek.ai.aigithubissuesolver.state.AgentState;
import com.prateek.ai.aigithubissuesolver.util.GitHubUrlParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AgentOrchestrator {

    private final CodeReaderAgent codeReaderAgent;
    private final PlannerAgent plannerAgent;
    private final CodeWriterAgent codeWriterAgent;
    private final TestWriterAgent testWriterAgent;
    private final ReviewAgent reviewAgent;
    private final PrOpenerAgent prOpenerAgent;
    private final GitHubUrlParser gitHubUrlParser;

    @Value("${agent.max-retries}")
    private int maxRetries;

    // ── Main Pipeline ────────────────────────────────
    public IssueResponse solve(String issueUrl) {

        log.info("[Orchestrator] 🚀 Starting pipeline for: {}", issueUrl);

        // Step 1 — parse URL
        GitHubUrlParser.ParsedGitHubUrl parsed = gitHubUrlParser.parse(issueUrl);

        // Step 2 — initialize shared state
        AgentState state = AgentState.builder()
                .issueUrl(issueUrl)
                .repoOwner(parsed.getOwner())
                .repoName(parsed.getRepo())
                .issueNumber(parsed.getIssueNumber())
                .retryCount(0)
                .fixApproved(false)
                .build();

        // ── Phase 1: Run once agents ─────────────────

        // Agent 01 — Code Reader
        log.info("[Orchestrator] Running Agent 01 — CodeReaderAgent");
        codeReaderAgent.run(state);

        // Agent 02 — Planner
        log.info("[Orchestrator] Running Agent 02 — PlannerAgent");
        plannerAgent.run(state);

        // ── Phase 2: Retry Loop ──────────────────────
        // CodeWriter → TestWriter → ReviewAgent
        // loops max 3 times until fix is approved

        log.info("[Orchestrator] Starting retry loop. Max retries: {}", maxRetries);

        while (state.getRetryCount() < maxRetries) {

            log.info("[Orchestrator] 🔄 Attempt #{}", state.getRetryCount() + 1);

            // Agent 03 — Code Writer
            log.info("[Orchestrator] Running Agent 03 — CodeWriterAgent");
            codeWriterAgent.run(state);

            // Agent 04 — Test Writer
            log.info("[Orchestrator] Running Agent 04 — TestWriterAgent");
            testWriterAgent.run(state);

            // Agent 05 — Review Agent
            log.info("[Orchestrator] Running Agent 05 — ReviewAgent");
            reviewAgent.run(state);

            if (state.isFixApproved()) {
                log.info("[Orchestrator] ✅ Fix approved on attempt #{}", state.getRetryCount() + 1);
                break;
            }

            // fix not approved — increment retry
            log.warn("[Orchestrator] ❌ Fix rejected. Retrying... ({}/{})",
                    state.getRetryCount() + 1, maxRetries);
            state.incrementRetry();
        }

        // ── Phase 3: Check final result ──────────────
        if (!state.isFixApproved()) {
            log.error("[Orchestrator] ❌ Fix not approved after {} attempts.", maxRetries);
            return buildFailedResponse(state);
        }

        // ── Phase 4: PR Opener ───────────────────────
        log.info("[Orchestrator] Running Agent 06 — PrOpenerAgent");
        prOpenerAgent.run(state);

        log.info("[Orchestrator] 🎉 Pipeline completed successfully!");
        return buildSuccessResponse(state);
    }

    // ── Build Success Response ───────────────────────
    private IssueResponse buildSuccessResponse(AgentState state) {
        return IssueResponse.builder()
                .issueTitle(state.getIssueTitle())
                .issueUrl(state.getIssueUrl())
                .codeContext(state.getCodeContext())
                .plan(state.getPlan())
                .codeFix(state.getPatch())
                .tests(state.getTests())
                .prDescription(state.getPrDescription())
                .totalRetries(state.getRetryCount())
                .fixApproved(true)
                .status("SUCCESS")
                .build();
    }

    // ── Build Failed Response ────────────────────────
    private IssueResponse buildFailedResponse(AgentState state) {
        return IssueResponse.builder()
                .issueTitle(state.getIssueTitle())
                .issueUrl(state.getIssueUrl())
                .codeContext(state.getCodeContext())
                .plan(state.getPlan())
                .codeFix(state.getPatch())
                .tests(state.getTests())
                .prDescription(null)
                .totalRetries(state.getRetryCount())
                .fixApproved(false)
                .status("FAILED_AFTER_MAX_RETRIES")
                .build();
    }

}