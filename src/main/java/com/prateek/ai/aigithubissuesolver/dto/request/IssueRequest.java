package com.prateek.ai.aigithubissuesolver.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class IssueRequest {

    @NotBlank(message = "GitHub issue URL cannot be blank")
    @Pattern(
            regexp = "https://github\\.com/[\\w.-]+/[\\w.-]+/issues/\\d+",
            message = "Invalid GitHub issue URL format. Expected: https://github.com/owner/repo/issues/123"
    )
    private String githubIssueUrl;

}