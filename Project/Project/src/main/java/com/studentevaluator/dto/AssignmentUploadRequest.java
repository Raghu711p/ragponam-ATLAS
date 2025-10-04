package com.studentevaluator.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for assignment upload requests.
 */
public class AssignmentUploadRequest {
    
    @NotBlank(message = "Assignment ID is required")
    @Size(max = 50, message = "Assignment ID cannot exceed 50 characters")
    private String assignmentId;
    
    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title cannot exceed 200 characters")
    private String title;
    
    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;
    
    public AssignmentUploadRequest() {}
    
    public AssignmentUploadRequest(String assignmentId, String title, String description) {
        this.assignmentId = assignmentId;
        this.title = title;
        this.description = description;
    }
    
    public String getAssignmentId() {
        return assignmentId;
    }
    
    public void setAssignmentId(String assignmentId) {
        this.assignmentId = assignmentId;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
}