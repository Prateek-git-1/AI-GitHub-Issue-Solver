package com.prateek.ai.aigithubissuesolver.prompt;

import com.prateek.ai.aigithubissuesolver.state.AgentState;
import org.springframework.stereotype.Component;

@Component
public class PromptBuilder {

    // ── Agent 01: Code Reader Prompt ─────────────────
    public String buildCodeReaderPrompt(AgentState state) {
        return """
                You are an expert software engineer analyzing a GitHub issue.

                GITHUB ISSUE TITLE:
                %s

                GITHUB ISSUE DESCRIPTION:
                %s

                REPOSITORY: %s/%s

                YOUR TASK:
                1. Understand the issue deeply.
                2. Identify which parts of the codebase are likely affected.
                3. Identify the root cause of the issue.
                4. List affected files, classes, methods if mentioned or inferable.
                5. Summarize the technical context clearly.

                Respond in this format:
                - ROOT CAUSE: ...
                - AFFECTED AREAS: ...
                - TECHNICAL CONTEXT: ...
                - KEY OBSERVATIONS: ...
                """.formatted(
                state.getIssueTitle(),
                state.getIssueBody(),
                state.getRepoOwner(),
                state.getRepoName()
        );
    }

    // ── Agent 02: Planner Prompt ─────────────────────
    public String buildPlannerPrompt(AgentState state) {
        return """
                You are a senior software architect creating a fix plan for a GitHub issue.

                GITHUB ISSUE:
                Title: %s
                Description: %s

                CODE CONTEXT (from analysis):
                %s

                YOUR TASK:
                Create a clear, step-by-step plan to fix this issue.
                Be specific about what needs to change and why.

                Respond in this format:
                - APPROACH: (overall strategy)
                - STEP 1: ...
                - STEP 2: ...
                - STEP 3: ...
                - FILES TO MODIFY: ...
                - RISKS: ...
                """.formatted(
                state.getIssueTitle(),
                state.getIssueBody(),
                state.getCodeContext()
        );
    }

    // ── Agent 03: Code Writer Prompt ─────────────────
    public String buildCodeWriterPrompt(AgentState state) {

        // on retry — include previous feedback
        String retryContext = "";
        if (state.getRetryCount() > 0) {
            retryContext = """

                    ⚠️ PREVIOUS ATTEMPT FAILED — RETRY #%d
                    REVIEW FEEDBACK:
                    %s

                    PREVIOUS FAILED PATCH:
                    %s

                    Fix the issues mentioned in the feedback above.
                    """.formatted(
                    state.getRetryCount(),
                    state.getReviewFeedback(),
                    state.getPreviousPatches().isEmpty() ? "N/A" :
                            state.getPreviousPatches().get(state.getPreviousPatches().size() - 1)
            );
        }

        return """
                You are an expert software engineer writing a code fix.

                GITHUB ISSUE:
                Title: %s
                Description: %s

                FIX PLAN:
                %s

                %s

                YOUR TASK:
                Write the complete, production-ready code fix.
                Include:
                - Actual working code
                - Proper error handling
                - Comments where needed
                - Follow existing code style

                Respond with:
                - FILE: (filename)
                - CHANGE TYPE: (new file / modify / delete)
                - CODE: 
                (complete code here)
                - EXPLANATION: (what you changed and why)
                                """.formatted(
                                        state.getIssueTitle(),
                                        state.getIssueBody(),
                                        state.getPlan(),
                                        retryContext
                                );
    }

    // ── Agent 04: Test Writer Prompt ─────────────────
    public String buildTestWriterPrompt(AgentState state) {
        return """
                You are a senior QA engineer writing tests for a code fix.
   \s
                GITHUB ISSUE:
                %s
   \s
                CODE FIX:
                %s
   \s
                YOUR TASK:
                Write comprehensive tests for this fix including:
                1. Unit tests
                2. Integration tests
                3. Edge cases
                4. Negative test cases
   \s
                Use JUnit 5 + Mockito for Java code.
                Use proper test naming conventions.
   \s
                Respond with:
                - TEST FILE NAME: ...
                - UNIT TESTS:
    ```java
                (tests here)
    ```
                - EDGE CASES COVERED: ...
               \s""".formatted(
                        state.getIssueTitle(),
                        state.getPatch()
                );
}

    // ── Agent 05: Review Prompt ──────────────────────
    public String buildReviewPrompt(AgentState state) {
        return """
                You are a strict senior code reviewer.

                GITHUB ISSUE:
                %s

                FIX PLAN:
                %s

                CODE FIX SUBMITTED:
                %s

                TESTS WRITTEN:
                %s

                YOUR TASK:
                Review the code fix critically.
                Check for:
                1. Does the fix actually solve the issue?
                2. Are there bugs or edge cases missed?
                3. Is the code clean and production-ready?
                4. Do the tests cover the fix properly?
                5. Any security or performance concerns?

                Respond EXACTLY in this format:
                APPROVED: YES or NO
                ISSUES FOUND:
                - ...
                - ...
                SUGGESTIONS:
                - ...
                """.formatted(
                        state.getIssueTitle(),
                        state.getPlan(),
                        state.getPatch(),
                        state.getTests()
                );
    }

    // ── Agent 06: PR Opener Prompt ───────────────────
    public String buildPrOpenerPrompt(AgentState state) {
        return """
                You are a professional software engineer writing a pull request.

                GITHUB ISSUE:
                Title: %s
                URL: %s

                FIX PLAN:
                %s

                CODE FIX:
                %s

                TESTS WRITTEN:
                %s

                YOUR TASK:
                Generate professional GitHub pull request content.

                Respond in this format:

                PR TITLE:
                (concise, descriptive title)

                COMMIT MESSAGE:
                (conventional commit format: fix/feat/chore: description)

                PR DESCRIPTION:
                ## Summary
                (what this PR does)

                ## Root Cause
                (why the issue happened)

                ## Changes Made
                - ...
                - ...

                ## Testing
                (how it was tested)

                ## Related Issue
                Closes #%d
                """.formatted(
                        state.getIssueTitle(),
                        state.getIssueUrl(),
                        state.getPlan(),
                        state.getPatch(),
                        state.getTests(),
                        state.getIssueNumber()
                );
    }

}
