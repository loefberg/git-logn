package com.github.loefberg.gitlogn;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

//      ┌(all-branches-and-tags)
// [→] hash date user comment
//                    optional-link-to-jira-issue
// TODO: parse gitconfig
// TODO: respect mailmap
public class Main {
    public static void main(String[] args) throws Exception {

        Options options = parseCommandLineArguments(args);


        FileRepositoryBuilder builder = new FileRepositoryBuilder();

        Repository repository = builder.setGitDir(options.getWorkingDirectory().toFile())
                    .readEnvironment() // scan environment GIT_* variables
                    .build();

        try(Git git = new Git(repository)) {
            Iterable<RevCommit> commits = git.log().setMaxCount(10).call();
            for(RevCommit commit: commits) {
                System.out.println(commit.getId().abbreviate(7).name() + " " + commit.getShortMessage());
            }
        }
    }

    private static Options parseCommandLineArguments(String[] args) {
        CommandLineParser parser = new CommandLineParser(args);
        return parser.parse();
    }


}
