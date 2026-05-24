package com.prateek.ai.aigithubissuesolver.util;

import com.prateek.ai.aigithubissuesolver.exception.AgentException;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class GitHubUrlParser {

    // matches: https://github.com/owner/repo/issues/123
    private static final Pattern GITHUB_ISSUE_PATTERN =
            Pattern.compile("https://github\\.com/([\\w.-]+)/([\\w.-]+)/issues/(\\d+)");

    public ParsedGitHubUrl parse(String url) {

        if (url == null || url.isBlank()) {
            throw new AgentException("GitHub URL cannot be null or blank");
        }

        Matcher matcher = GITHUB_ISSUE_PATTERN.matcher(url.trim());

        if (!matcher.matches()) {
            throw new AgentException(
                    "Invalid GitHub issue URL: " + url +
                            " | Expected format: https://github.com/owner/repo/issues/123"
            );
        }

        return ParsedGitHubUrl.builder()
                .owner(matcher.group(1))
                .repo(matcher.group(2))
                .issueNumber(Integer.parseInt(matcher.group(3)))
                .build();
    }

    // ── Inner record to hold parsed values ───────────
    @lombok.Builder
    @lombok.Data
    public static class ParsedGitHubUrl {
        private String owner;
        private String repo;
        private int issueNumber;
    }

}