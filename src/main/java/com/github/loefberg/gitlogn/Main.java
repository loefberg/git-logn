package com.github.loefberg.gitlogn;

import com.github.loefberg.gitlogn.jira.JiraClient;
import com.github.loefberg.gitlogn.jira.JiraIssue;
import com.github.loefberg.gitlogn.jira.JiraIssueKeyParser;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.awt.Color;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

//      ┌(all-branches-and-tags)
// [→] hash date user comment
//                    optional-link-to-jira-issue
// TODO: parse gitconfig
// TODO: respect mailmap
// TODO: Revert will also mention the JIRA key
public class Main {
    public static void main(String[] args) throws Exception {

        Options options = parseCommandLineArguments(args);


        FileRepositoryBuilder builder = new FileRepositoryBuilder();

        Repository repository = builder.setGitDir(options.getWorkingDirectory().toFile())
                    .readEnvironment() // scan environment GIT_* variables
                    .build();

        Map<ObjectId, Set<String>> allRefs = new HashMap<>();
        for(Ref ref: repository.getRefDatabase().getRefs()) {
            allRefs.computeIfAbsent(ref.getObjectId(), key -> new HashSet<>()).add(ref.getName());
        }

        JiraIssueKeyParser parser = new JiraIssueKeyParser(Set.of("MT", "PAG"));

        try(Git git = new Git(repository)) {
            JiraClient jira = createJiraClient(git);

            Iterable<RevCommit> commits = git.log().setMaxCount(10).call();
            for(RevCommit commit: commits) {
                Set<String> refs = allRefs.get(commit.getId());
                if(refs != null) {
                    System.out.println("   ┌" + refs);
                }
                if(commit.getParentCount() > 1) {
                    System.out.print("→ ");
                } else {
                    System.out.print("  ");
                }

                // hash
                setColor(Color.YELLOW);
                System.out.print(commit.getId().abbreviate(7).name());
                System.out.print(" ");

                // date
                setColor(Color.GREEN);
                System.out.print(parseDate(commit));
                System.out.print(" ");

                // author
                setColor(Color.BLUE);
                System.out.print(pad(parseAuthor(commit), 10));
                System.out.print(" ");

                // message
                setColor(null);
                List<JiraIssue> issuesInCommit = jira.findIssueInShortCommitMessage(commit.getShortMessage());
                if(issuesInCommit.isEmpty()) {
                    System.out.print(commit.getShortMessage());
                    reset();
                    System.out.println();
                } else {
                    for(int i = 0; i < issuesInCommit.size(); i++) {
                        JiraIssue issue = issuesInCommit.get(i);
                        if(i > 0) {
                            System.out.print("                                ");
                        }
                        System.out.println(issue.title + " " + issue.url);
                    }


                    reset();
                }

            }
        }
    }

    private static JiraClient createJiraClient(Git git) {
        StoredConfig cfg = git.getRepository().getConfig();
        String basePath = cfg.getString("logn", null, "jiraBasePath"),
            username = cfg.getString("logn", null, "jiraUsername"),
            password = cfg.getString("logn", null, "jiraPassword");
        if(basePath == null) {
            throw new RuntimeException(".gitconfig is missing logn.jiraBasePath");
        }
        if(username == null) {
            throw new RuntimeException(".gitconfig is missing logn.jiraUsername");
        }
        if(password == null) {
            throw new RuntimeException(".gitconfig is missing logn.jiraPassword");
        }
        return new JiraClient(basePath, username, password);
    }

    private static void setColor(Color color) {

        if(color == Color.BLACK) {
            System.out.print((char)27 + "[30m");
        } else if(color == Color.RED) {
            System.out.print((char)27 + "[31m");
        } else if(color == Color.GREEN) {
            System.out.print((char)27 + "[32m");
        } else if(color == Color.YELLOW) {
            System.out.print((char)27 + "[33m");
        } else if(color == Color.BLUE) {
            System.out.print((char)27 + "[34m");
        } else if(color == Color.MAGENTA) {
            System.out.print((char)27 + "[35m");
        } else if(color == Color.CYAN) {
            System.out.print((char)27 + "[36m");
        } else if(color == Color.WHITE) {
            System.out.print((char)27 + "[37m");
        } else if(color == null) {
            System.out.print((char)27 + "[39m");
        } else {
            throw new RuntimeException("Unknown color: " + color);
        }
    }

    private static void reset() {
        System.out.print((char)27 + "[0m");
    }

    private static LocalDate parseDate(RevCommit commit) {
        Instant instant = Instant.ofEpochSecond(commit.getCommitTime());
        return instant.atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private static String pad(String str, int length) {
        return String.format("%-10s", str);
    }

    private static String parseAuthor(RevCommit commit) {
        String name = commit.getAuthorIdent().getName();
        int idx = name.indexOf(" ");
        if(idx > 0) {
            return name.substring(0, idx);
        }
        return name;
    }


    private static Options parseCommandLineArguments(String[] args) {
        CommandLineParser parser = new CommandLineParser(args);
        return parser.parse();
    }


}
