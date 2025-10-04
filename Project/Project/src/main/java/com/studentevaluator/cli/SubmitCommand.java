package com.studentevaluator.cli;

import java.io.File;
import java.util.concurrent.Callable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.studentevaluator.dto.AssignmentResponse;
import com.studentevaluator.security.InputValidator;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * CLI command for submitting assignment files.
 */
@Component
@Command(
    name = "submit",
    description = "Submit an assignment file for evaluation setup",
    mixinStandardHelpOptions = true
)
public class SubmitCommand implements Callable<Integer> {
    
    @Parameters(index = "0", description = "Path to the assignment Java file")
    private File assignmentFile;
    
    @Option(names = {"-i", "--assignment-id"}, required = true, 
            description = "Assignment ID")
    private String assignmentId;
    
    @Option(names = {"-t", "--title"}, required = true,
            description = "Assignment title")
    private String title;
    
    @Option(names = {"-d", "--description"},
            description = "Assignment description")
    private String description;
    
    @Autowired
    private CLIApiService apiService;
    
    @Autowired
    private InputValidator inputValidator;
    
    @Override
    public Integer call() throws Exception {
        try {
            // Validate file exists and is a Java file
            if (!assignmentFile.exists()) {
                System.err.println("Error: Assignment file does not exist: " + assignmentFile.getPath());
                return 1;
            }
            
            if (!assignmentFile.getName().toLowerCase().endsWith(".java")) {
                System.err.println("Error: Assignment file must be a Java file (.java extension)");
                return 1;
            }
            
            // Security validation
            inputValidator.validateIdentifier(assignmentId, "assignment ID", 50);
            inputValidator.validateSafeText(title, "title", 200);
            inputValidator.validateSafeText(description, "description", 1000);
            inputValidator.validateFilePath(assignmentFile.getPath(), "assignment file path");
            
            System.out.println("Submitting assignment file: " + assignmentFile.getName());
            System.out.println("Assignment ID: " + assignmentId);
            System.out.println("Title: " + title);
            if (description != null) {
                System.out.println("Description: " + description);
            }
            
            // Upload assignment via API
            AssignmentResponse response = apiService.uploadAssignment(
                assignmentFile, assignmentId, title, description);
            
            System.out.println("\n✓ Assignment submitted successfully!");
            System.out.println("Assignment ID: " + response.getAssignmentId());
            System.out.println("Title: " + response.getTitle());
            System.out.println("Created: " + response.getCreatedAt());
            
            if (response.isHasTestFile()) {
                System.out.println("Test file: Associated");
            } else {
                System.out.println("Note: No test file associated yet. Use 'upload-tests' command to add tests.");
            }
            
            return 0;
            
        } catch (CLIException e) {
            System.err.println("Error: " + e.getMessage());
            return 1;
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            return 1;
        }
    }
}