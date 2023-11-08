package com.github.loefberg.gitlogn;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class CommandLineParser {
    private Options options = new Options();
    private List<String> rest;

    public CommandLineParser(String[] args) {
        this.rest = Arrays.asList(args);
    }

    public Options parse() {
        parseOptions();
        return options;
    }

    private void parseOptions() {
        if(rest.isEmpty()) {
            return;
        }

        switch(rest.get(0)) {
            case "-C":
                parseWorkingDirectory();
                break;
        }
    }

    private void parseWorkingDirectory() {
        consume("-C");
        Path path = parseDirectory();
        options.setCustomWorkingDirectory(path);
    }

    private Path parseDirectory() {
        String path = lookahead();
        if(path == null) {
            throw new RuntimeException("Failed to parse command line argument, expected directory got end of string");
        }
        consume(path);
        return Paths.get(path);
    }

    private String lookahead() {
        return rest.isEmpty() ? null : rest.get(0);
    }

    private void consume(String expected) {
        if(rest.isEmpty()) {
            throw new RuntimeException("Unexpected command line argument, expected '" + expected + "' but got end of string");
        }
        String found = rest.get(0);
        if(!found.equals(expected)) {
            throw new RuntimeException("Unexpected command line argument, expected '" + expected + "' but got '" + expected + "'");
        }
        rest = rest.subList(1, rest.size());
    }
}
