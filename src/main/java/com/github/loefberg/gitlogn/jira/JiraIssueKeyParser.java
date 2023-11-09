package com.github.loefberg.gitlogn.jira;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JiraIssueKeyParser {
    private static final Pattern SMART_COMMIT_ISSUE_KEY = Pattern.compile("([A-Z]{2,10})(-|_)(\\d{4,5})([^\\.]|$)");
    private final Set<String> existingProjectKeys;

    public JiraIssueKeyParser(Set<String> existingProjectKeys) {
        this.existingProjectKeys = existingProjectKeys;
    }

    public boolean isValidProjectKey(String projectKey) {
        return existingProjectKeys.contains(projectKey);
    }
    public Set<JiraIssueRef> findIssueInShortCommitMessage(String shortCommitMessage) {
        Matcher matcher = SMART_COMMIT_ISSUE_KEY.matcher(shortCommitMessage);
        Set<JiraIssueRef> issues = new HashSet<>();
        while(matcher.find()) {
            String issueKey = matcher.group(1) + "-" + matcher.group(3);
            String projectKey = matcher.group(1).toUpperCase();
            if(isValidProjectKey(projectKey)) {
                issues.add(new JiraIssueRef(issueKey));
            }
        }
        return issues;
    }
}
