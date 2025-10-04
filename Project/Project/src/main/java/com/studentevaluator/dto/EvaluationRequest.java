package com.studentevaluator.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for evaluation requests.
 */
public class EvaluationRequest {
    
    @NotBlank(message = "Student ID is required")
    @Size(max = 50, message = "Student ID cannot exceed 50 characters")
    private String studentId;
    
    @NotBlank(message = "Assignment ID is required")
    @Size(max = 50, message = "Assignment ID cannot exceed 50 characters")
    private String assignmentId;
    
    public EvaluationRequest() {}
    
    public EvaluationRequest(String studentId, String assignmentId) {
        this.studentId = studentId;
        this.assignmentId = assignmentId;
    }
    
    public String getStudentId() {
        return studentId;
    }
    
    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }
    
    public String getAssignmentId() {
        return assignmentId;
    }
    
    public void setAssignmentId(String assignmentId) {
        this.assignmentId = assignmentId;
    }
}