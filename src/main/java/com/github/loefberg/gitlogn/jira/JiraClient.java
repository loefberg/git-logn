package com.github.loefberg.gitlogn.jira;

import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.JSON;
import io.swagger.client.api.IssueSearchApi;
import io.swagger.client.api.ProjectsApi;
import io.swagger.client.model.IssueBean;
import io.swagger.client.model.Project;
import io.swagger.client.model.SearchResults;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;


public class JiraClient {
    private final ApiClient client;
    private final Set<String> allProjectKeys = new HashSet<>();
    private final JiraIssueKeyParser keyParser;

    public JiraClient(String basePath, String username, String password) {
        this.client = new ApiClient();
        client.setBasePath(basePath);
        client.setUsername(username);
        client.setPassword(password);
        JSON json = new JSON();
        json.setOffsetDateTimeFormat(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'.000'Z"));
        client.setJSON(json);

        keyParser = new JiraIssueKeyParser(getAllProjectKeys());
    }

    public List<JiraIssue> findIssueInShortCommitMessage(String shortMessage) {
        if(shortMessage.startsWith("Revert")) {
            return Collections.emptyList();
        }

        return loadIssues(keyParser.findIssueInShortCommitMessage(shortMessage)).stream().map(bean -> new JiraIssue(bean, client.getBasePath())).collect(toList());
    }

    private Set<String> getAllProjectKeys() {
        try {
            ProjectsApi projectsApi = new ProjectsApi(client);
            if (allProjectKeys.isEmpty()) {
                projectsApi.getAllProjects(null, null, null).stream().map(Project::getKey).forEach(allProjectKeys::add);

            }
            return allProjectKeys;
        } catch(ApiException ex) {
            throw new RuntimeException("Failed to get project keys from jira", ex);
        }
    }

    private List<IssueBean> loadIssues(Collection<JiraIssueRef> issueRefs) {
        try {
            List<IssueBean> results = new ArrayList<>();
            IssueSearchApi searchApi = new IssueSearchApi(client);
            Set<String> keysLeftToLoad = issueRefs.stream().map(JiraIssueRef::getIssueKey).collect(toCollection(HashSet::new));
            Set<String> foundKeys = new HashSet<>();

            while (!keysLeftToLoad.isEmpty()) {
                Set<String> chunkOfKeys = new HashSet<>();
                Iterator<String> it = keysLeftToLoad.iterator();
                for (int i = 0; i < 10 && it.hasNext(); i++) {
                    chunkOfKeys.add(it.next());
                    it.remove();
                }

                // Example format for jql:
                //  project = SP and key in ("SP-406","SP-395","SP-474","SP-473","SP-472","SP-455","SP-458") ORDER BY key ASC
                String jql = "key in (" + chunkOfKeys.stream().map(key -> "\"" + key + "\"").collect(joining(", ")) + ")";
                SearchResults searchResults = searchApi.searchForIssuesUsingJql(jql, null, null, null, null, null, null, null);
                for (IssueBean issue : searchResults.getIssues()) {
                    results.add(issue);
                    foundKeys.add(issue.getKey());
                }
            }

            // assert that all of them were loaded
            Set<String> expected = issueRefs.stream().map(JiraIssueRef::getIssueKey).collect(toCollection(HashSet::new));
            expected.removeAll(foundKeys);
            if (!expected.isEmpty()) {
                // nop
            }

            return results;
        } catch(ApiException ex) {
            throw new RuntimeException("Failed to load issues", ex);
        }
    }

}
