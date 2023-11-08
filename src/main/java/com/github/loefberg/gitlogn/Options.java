package com.github.loefberg.gitlogn;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Options {
    private Path customWorkingDirectory;
    private Path cachedWorkingDirectory;

    public Path getWorkingDirectory() {
        return findGitDirectory();
    }

    public void setCustomWorkingDirectory(Path customWorkingDirectory) {
        this.customWorkingDirectory = customWorkingDirectory;
    }

    private Path findGitDirectory() {
        if(cachedWorkingDirectory != null) {
            return cachedWorkingDirectory;
        }

        Path current;
        if(customWorkingDirectory != null) {
            current = customWorkingDirectory;
        } else {
            String userDir = System.getProperty("user.dir");
            if (userDir == null) {
                throw new RuntimeException("No user.dir system property found");
            }
            current = Paths.get(userDir);
        }

        while(current != null) {
            Path expected = current.resolve(".git");
            if(Files.isDirectory(expected)) {
                return (cachedWorkingDirectory = expected);
            }
            current = current.getParent();
        }
        throw new RuntimeException("Could not find .git directory");
    }
}
