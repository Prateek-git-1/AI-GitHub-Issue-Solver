package com.prateek.ai.aigithubissuesolver.dto.reponse;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class IssueResponse {

    // issue meta
    private String issueTitle;
    private String issueUrl;

    // agent outputs
    private String codeContext;        // from CodeReaderAgent
    private String plan;               // from PlannerAgent
    private String codeFix;            // from CodeWriterAgent
    private String tests;              // from TestWriterAgent
    private String prDescription;      // from PrOpenerAgent

    // pipeline meta
    private int totalRetries;          // how many retries it took
    private boolean fixApproved;       // was fix approved by ReviewAgent
    private String status;             // "SUCCESS" or "FAILED_AFTER_MAX_RETRIES"


}