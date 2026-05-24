package com.prateek.ai.aigithubissuesolver.state;


import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentState {

    // ── Phase 1: Input ──────────────────────────────
    private String issueUrl;           // original GitHub URL
    private String issueTitle;         // fetched from GitHub
    private String issueBody;          // fetched from GitHub
    private String repoOwner;          // parsed from URL
    private String repoName;           // parsed from URL
    private int issueNumber;           // parsed from URL

    // ── Phase 2: Agent Outputs ───────────────────────
    private String codeContext;        // CodeReaderAgent  → what is affected
    private String plan;               // PlannerAgent     → how to fix
    private String patch;              // CodeWriterAgent  → actual code fix
    private String tests;              // TestWriterAgent  → unit/integration tests
    private String prDescription;      // PrOpenerAgent    → PR desc + commit msg

    // ── Phase 3: Review & Retry ──────────────────────
    private String reviewFeedback;     // ReviewAgent      → what is wrong
    private boolean fixApproved;       // true = go to PR, false = retry
    private int retryCount;            // current retry attempt

    @Builder.Default
    private List<String> previousPatches = new ArrayList<>();  // history of failed fixes

    // ── Helper Methods ───────────────────────────────

    // called by orchestrator on every retry
    public void incrementRetry() {
        this.retryCount++;
    }

    // save failed patch before retry
    public void savePreviousPatch(String failedPatch) {
        if (failedPatch != null) {
            this.previousPatches.add(failedPatch);
        }
    }

    // reset review fields before each retry
    public void resetReviewState() {
        this.reviewFeedback = null;
        this.fixApproved = false;
    }

}