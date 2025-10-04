package com.studentevaluator.cli;

import com.studentevaluator.dto.EvaluationResponse;
import com.studentevaluator.dto.StudentScoreResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * CLI command for retrieving and displaying evaluation results.
 */
@Component
@Command(
    name = "results",
    description = "Retrieve and display evaluation results for students",
    mixinStandardHelpOptions = true
)
public class ResultsCommand implements Callable<Integer> {
    
    @Option(names = {"-s", "--student"},
            description = "Student ID to get results for")
    private String studentId;
    
    @Option(names = {"-e", "--evaluation"},
            description = "Evaluation ID to get specific result")
    private String evaluationId;
    
    @Option(names = {"-h", "--history"},
            description = "Show evaluation history for student")
    private boolean showHistory;
    
    @Option(names = {"-v", "--verbose"},
            description = "Show detailed results")
    private boolean verbose;
    
    @Autowired
    private CLIApiService apiService;
    
    @Override
    public Integer call() throws Exception {
        try {
            // If evaluation ID is provided, get specific evaluation result
            if (evaluationId != null) {
                return getEvaluationResult();
            }
            
            // If student ID is provided, get student results
            if (studentId != null) {
                if (showHistory) {
                    return getStudentHistory();
                } else {
                    return getStudentScores();
                }
            }
            
            // If no specific options provided, show usage
            System.err.println("Error: Must specify either --student or --evaluation option");
            System.err.println("Use --help for usage information");
            return 1;
            
        } catch (CLIException e) {
            System.err.println("Error: " + e.getMessage());
            return 1;
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            return 1;
        }
    }
    
    private Integer getEvaluationResult() {
        System.out.println("Retrieving evaluation result: " + evaluationId);
        
        EvaluationResponse response = apiService.getEvaluationResult(evaluationId);
        
        System.out.println("\n--- Evaluation Result ---");
        System.out.println("Evaluation ID: " + response.getEvaluationId());
        System.out.println("Student ID: " + response.getStudentId());
        System.out.println("Assignment ID: " + response.getAssignmentId());
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
        
        if (verbose) {
            showDetailedResults(response);
        }
        
        return 0;
    }
    
    private Integer getStudentScores() {
        System.out.println("Retrieving scores for student: " + studentId);
        
        StudentScoreResponse response = apiService.getStudentScores(studentId);
        
        System.out.println("\n--- Student Scores ---");
        System.out.println("Student ID: " + response.getStudentId());
        
        if (response.getCurrentScore() != null) {
            System.out.println("Current Score: " + response.getCurrentScore());
        }
        
        if (verbose && response.getTotalEvaluations() > 0) {
            System.out.println("Total Evaluations: " + response.getTotalEvaluations());
        }
        
        if (verbose && response.getAverageScore() != null) {
            System.out.println("Average Score: " + response.getAverageScore());
        }
        
        if (verbose && response.getMaxScore() != null) {
            System.out.println("Highest Score: " + response.getMaxScore());
        }
        
        return 0;
    }
    
    private Integer getStudentHistory() {
        System.out.println("Retrieving evaluation history for student: " + studentId);
        
        List<EvaluationResponse> evaluations = apiService.getStudentEvaluationHistory(studentId);
        
        if (evaluations.isEmpty()) {
            System.out.println("No evaluation history found for student: " + studentId);
            return 0;
        }
        
        System.out.println("\n--- Evaluation History ---");
        System.out.println("Student ID: " + studentId);
        System.out.println("Total Evaluations: " + evaluations.size());
        System.out.println();
        
        for (int i = 0; i < evaluations.size(); i++) {
            EvaluationResponse eval = evaluations.get(i);
            System.out.println((i + 1) + ". Evaluation ID: " + eval.getEvaluationId());
            System.out.println("   Assignment: " + eval.getAssignmentId());
            System.out.println("   Status: " + eval.getStatus());
            
            if (eval.getScore() != null) {
                System.out.println("   Score: " + eval.getScore());
            }
            
            if (eval.getEvaluatedAt() != null) {
                System.out.println("   Date: " + eval.getEvaluatedAt());
            }
            
            if (verbose) {
                showDetailedResults(eval);
            }
            
            System.out.println();
        }
        
        return 0;
    }
    
    private void showDetailedResults(EvaluationResponse response) {
        if (response.getErrorMessage() != null) {
            System.out.println("   Error: " + response.getErrorMessage());
        }
        System.out.println("   Note: Detailed compilation and test results are available through the web API");
    }
}