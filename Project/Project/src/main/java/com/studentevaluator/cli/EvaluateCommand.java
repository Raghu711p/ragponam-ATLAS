package com.studentevaluator.cli;

import com.studentevaluator.dto.EvaluationResponse;
import com.studentevaluator.model.EvaluationStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.util.concurrent.Callable;

/**
 * CLI command for triggering evaluation processes.
 */
@Component
@Command(
    name = "evaluate",
    description = "Trigger evaluation of a student submission against an assignment",
    mixinStandardHelpOptions = true
)
public class EvaluateCommand implements Callable<Integer> {
    
    @Parameters(index = "0", description = "Path to the student submission Java file")
    private File submissionFile;
    
    @Option(names = {"-s", "--student"}, required = true,
            description = "Student ID")
    private String studentId;
    
    @Option(names = {"-a", "--assignment"}, required = true,
            description = "Assignment ID")
    private String assignmentId;
    
    @Option(names = {"-v", "--verbose"},
            description = "Show detailed evaluation results")
    private boolean verbose;
    
    @Autowired
    private CLIApiService apiService;
    
    @Override
    public Integer call() throws Exception {
        try {
            // Validate file exists and is a Java file
            if (!submissionFile.exists()) {
                System.err.println("Error: Submission file does not exist: " + submissionFile.getPath());
                return 1;
            }
            
            if (!submissionFile.getName().toLowerCase().endsWith(".java")) {
                System.err.println("Error: Submission file must be a Java file (.java extension)");
                return 1;
            }
            
            System.out.println("Evaluating submission: " + submissionFile.getName());
            System.out.println("Student ID: " + studentId);
            System.out.println("Assignment ID: " + assignmentId);
            System.out.println("Processing...");
            
            // Trigger evaluation via API
            EvaluationResponse response = apiService.triggerEvaluation(
                submissionFile, studentId, assignmentId);
            
            System.out.println("\n✓ Evaluation completed!");
            System.out.println("Evaluation ID: " + response.getEvaluationId());
            System.out.println("Status: " + response.getStatus());
            
            if (response.getScore() != null) {
                System.out.println("Score: " + response.getScore());
            }
            
            if (response.getMaxScore() != null) {
                System.out.println("Max Score: " + response.getMaxScore());
            }
            
            if (response.getEvaluatedAt() != null) {
                System.out.println("Evaluated At: " + response.getEvaluatedAt());
            }
            
            // Show detailed results if verbose mode is enabled
            if (verbose) {
                System.out.println("\n--- Detailed Results ---");
                if (response.getErrorMessage() != null) {
                    System.out.println("Error Message: " + response.getErrorMessage());
                }
                System.out.println("Note: Detailed compilation and test results are available through the web API");
            }
            
            // Determine exit code based on evaluation status
            if (response.getStatus() == EvaluationStatus.COMPLETED) {
                return 0;
            } else if (response.getStatus() == EvaluationStatus.FAILED) {
                System.err.println("Evaluation failed. Use --verbose for more details.");
                return 2;
            } else {
                System.out.println("Evaluation is in progress. Check results later.");
                return 0;
            }
            
        } catch (CLIException e) {
            System.err.println("Error: " + e.getMessage());
            return 1;
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            return 1;
        }
    }
}