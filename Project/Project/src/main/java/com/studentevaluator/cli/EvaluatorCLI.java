package com.studentevaluator.cli;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.stereotype.Component;
import picocli.CommandLine;
import picocli.CommandLine.Command;

/**
 * Main CLI application for the Student Evaluator System.
 * Uses Picocli framework for command-line interface operations.
 */
@Component
@Command(
    name = "evaluator",
    description = "Student Evaluator System CLI - Automated Java assignment evaluation",
    version = "1.0.0",
    mixinStandardHelpOptions = true,
    subcommands = {
        SubmitCommand.class,
        UploadTestsCommand.class,
        EvaluateCommand.class,
        ResultsCommand.class
    }
)
public class EvaluatorCLI implements CommandLineRunner, ExitCodeGenerator {
    
    private final CommandLine.IFactory factory;
    private int exitCode;
    
    @Autowired
    public EvaluatorCLI(CommandLine.IFactory factory) {
        this.factory = factory;
    }
    
    @Override
    public void run(String... args) throws Exception {
        CommandLine cmd = new CommandLine(this, factory);
        exitCode = cmd.execute(args);
    }
    
    @Override
    public int getExitCode() {
        return exitCode;
    }
}