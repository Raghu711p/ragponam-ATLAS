package com.studentevaluator.cli;

import com.studentevaluator.dto.AssignmentResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.util.concurrent.Callable;

/**
 * CLI command for uploading test files and associating them with assignments.
 */
@Component
@Command(
    name = "upload-tests",
    description = "Upload JUnit test files and associate them with an assignment",
    mixinStandardHelpOptions = true
)
public class UploadTestsCommand implements Callable<Integer> {
    
    @Parameters(index = "0", description = "Path to the JUnit test file")
    private File testFile;
    
    @Option(names = {"-a", "--assignment"}, required = true,
            description = "Assignment ID to associate the test file with")
    private String assignmentId;
    
    @Autowired
    private CLIApiService apiService;
    
    @Override
    public Integer call() throws Exception {
        try {
            // Validate file exists and is a Java file
            if (!testFile.exists()) {
                System.err.println("Error: Test file does not exist: " + testFile.getPath());
                return 1;
            }
            
            if (!testFile.getName().toLowerCase().endsWith(".java")) {
                System.err.println("Error: Test file must be a Java file (.java extension)");
                return 1;
            }
            
            // Basic validation for JUnit test file
            if (!testFile.getName().toLowerCase().contains("test")) {
                System.out.println("Warning: Test file name should typically contain 'test' or 'Test'");
            }
            
            System.out.println("Uploading test file: " + testFile.getName());
            System.out.println("Assignment ID: " + assignmentId);
            
            // Upload test file via API
            AssignmentResponse response = apiService.uploadTestFile(testFile, assignmentId);
            
            System.out.println("\n✓ Test file uploaded successfully!");
            System.out.println("Assignment ID: " + response.getAssignmentId());
            System.out.println("Title: " + response.getTitle());
            System.out.println("Test file: " + (response.isHasTestFile() ? "Associated" : "Not associated"));
            System.out.println("Updated: " + response.getCreatedAt());
            
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