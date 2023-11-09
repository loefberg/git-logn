package com.github.loefberg.gitlogn.jira;

import io.swagger.client.model.IssueBean;

public class JiraIssue {
    public final String title;
    public final String url;

    public JiraIssue(IssueBean issue, String basePath) {
        this.title = issue.getKey() + " " + (String)issue.getFields().get("summary");
        this.url = basePath + "/browse/" + issue.getKey();
    }
}
