package com.github.loefberg.gitlogn.jira;

import java.util.Objects;

public class JiraIssueRef {
    public final String issueKey;

    public JiraIssueRef(String issueKey) {
        this.issueKey = issueKey;
    }

    public String getIssueKey() { return issueKey; }
    @Override
    public String toString() {
        return "JiraIssueRef{" +
                "issueKey='" + issueKey + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JiraIssueRef that = (JiraIssueRef) o;
        return Objects.equals(issueKey, that.issueKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(issueKey);
    }
}
