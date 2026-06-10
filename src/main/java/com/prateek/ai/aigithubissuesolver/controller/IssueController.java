package com.prateek.ai.aigithubissuesolver.controller;

import com.prateek.ai.aigithubissuesolver.dto.request.IssueRequest;
import com.prateek.ai.aigithubissuesolver.dto.response.IssueResponse;
import com.prateek.ai.aigithubissuesolver.orchestrator.AgentOrchestrator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Issue Solver", description = "Multi-Agent AI GitHub Issue Resolution")
public class IssueController {

    private final AgentOrchestrator agentOrchestrator;

    @PostMapping("/solve")
    @Operation(
            summary     = "Solve a GitHub Issue",
            description = "Pass a GitHub issue URL and get AI generated fix, tests and PR description"
    )
    public ResponseEntity<IssueResponse> solveIssue(
            @Valid @RequestBody IssueRequest request) {

        log.info("[IssueController] Received request for: {}", request.getGithubIssueUrl());

        IssueResponse response = agentOrchestrator.solve(request.getGithubIssueUrl());

        log.info("[IssueController] Response status: {}", response.getStatus());

        return ResponseEntity.ok(response);
    }

    // ── Health check ─────────────────────────────────
    @GetMapping("/health")
    @Operation(summary = "Health Check")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("AI GitHub Issue Solver is running ✅");
    }

}
